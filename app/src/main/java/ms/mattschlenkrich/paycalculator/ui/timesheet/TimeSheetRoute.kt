package ms.mattschlenkrich.paycalculator.ui.timesheet

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.logic.PayCalculationsAsync
import ms.mattschlenkrich.paycalculator.logic.PayDateProjections
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.ui.timesheet.composable.TimeSheetScreen
import java.time.LocalDate

@Composable
fun TimeSheetRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    payDayViewModel: PayDayViewModel,
    payCalculationsViewModel: PayCalculationsViewModel,
    payDetailViewModel: PayDetailViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }
    val projections = remember { PayDateProjections() }

    val settings by settingsViewModel.settings.observeAsState()
    val payPeriodsLimit = settings?.payPeriodsLimit ?: 15

    val totalsLabel = stringResource(R.string.totals)
    val week1Label = stringResource(R.string.week_1_)
    val week2Label = stringResource(R.string.week_2_)
    val zeroHrLabel = stringResource(R.string._0_hr)
    val hrLabel = stringResource(R.string.hr)
    val otLabel = stringResource(R.string.ot)
    val dblOtLabel = stringResource(R.string.dbl_ot)
    val otherHoursLabel = stringResource(R.string.other_hours)
    val pipeLabel = stringResource(R.string.pipe)
    val hrsLabel = stringResource(R.string.hrs)
    val otHrsLabel = stringResource(R.string.ot_hrs)
    val dblOtHrsLabel = stringResource(R.string.dbl_ot_hrs)
    val otherHrsLabel = stringResource(R.string.other_hours)

    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    var selectedEmployer by remember { mutableStateOf<Employers?>(null) }
    val cutOffDates by if (selectedEmployer != null) {
        payDayViewModel.getCutOffDates(selectedEmployer!!.employerId, payPeriodsLimit)
            .observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }
    var selectedCutOffDate by remember { mutableStateOf("") }
    var paySummary by remember { mutableStateOf(TimeSheetPaySummary()) }
    val workDates by if (selectedEmployer != null && selectedCutOffDate.isNotEmpty()) {
        payDayViewModel.getWorkDateList(
            selectedEmployer!!.employerId,
            selectedCutOffDate
        ).observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val extrasList by if (selectedEmployer != null && selectedCutOffDate.isNotEmpty()) {
        payDayViewModel.getWorkDateExtrasPerPay(
            selectedEmployer!!.employerId,
            selectedCutOffDate
        ).observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val workDateExtras = remember(extrasList) {
        extrasList.groupBy { it.extra.wdeWorkDateId }
    }

    // Initial selection from history
    LaunchedEffect(employers) {
        if (selectedEmployer == null && employers.isNotEmpty()) {
            val historyEmployer = mainViewModel.getEmployer()
            selectedEmployer = historyEmployer ?: employers.first()
        }
    }

    LaunchedEffect(cutOffDates, selectedEmployer) {
        if (selectedEmployer != null) {
            val today = LocalDate.now().toString()
            if (cutOffDates.isEmpty() || cutOffDates.first().ppCutoffDate < today) {
                coroutineScope.launch {
                    val nextCutOff = projections.generateNextCutOff(
                        selectedEmployer!!,
                        cutOffDates.firstOrNull()?.ppCutoffDate ?: ""
                    )
                    if (nextCutOff.isNotEmpty()) {
                        mainViewModel.setPayPeriod(null)
                        payDayViewModel.insertPayPeriodSync(
                            PayPeriods(
                                nf.generateRandomIdAsLong(),
                                nextCutOff,
                                selectedEmployer!!.employerId,
                                false,
                                df.getCurrentUTCTimeAsString()
                            )
                        )
                        selectedCutOffDate = nextCutOff
                    }
                }
            } else {
                val historyCutOff = mainViewModel.getCutOffDate()
                val historyEmployerId = mainViewModel.getEmployer()?.employerId
                val currentCutOff = cutOffDates.last { it.ppCutoffDate >= today }.ppCutoffDate

                if (historyEmployerId != selectedEmployer!!.employerId) {
                    selectedCutOffDate = currentCutOff
                } else if (historyCutOff == null ||
                    !cutOffDates.any { it.ppCutoffDate == historyCutOff }
                ) {
                    if (selectedCutOffDate.isEmpty() || !cutOffDates.any { it.ppCutoffDate == selectedCutOffDate }) {
                        selectedCutOffDate = currentCutOff
                    }
                } else if (selectedCutOffDate.isEmpty()) {
                    selectedCutOffDate = historyCutOff
                }
            }
        }
    }

    // Update summary and set global state
    LaunchedEffect(selectedEmployer, selectedCutOffDate, workDates) {
        if (selectedEmployer != null && selectedCutOffDate.isNotEmpty()) {
            mainViewModel.setEmployer(selectedEmployer!!)
            mainViewModel.setCutOffDate(selectedCutOffDate)

            val payPeriod = payDayViewModel.getPayPeriodSync(
                selectedCutOffDate,
                selectedEmployer!!.employerId
            )
            mainViewModel.setPayPeriod(payPeriod)
            if (payPeriod != null) {
                val payCalculations =
                    PayCalculationsAsync(
                        payCalculationsViewModel,
                        payDetailViewModel,
                        selectedEmployer!!,
                        payPeriod
                    )
                payCalculations.waitForCalculations()

                val week1EndDate =
                    LocalDate.parse(selectedCutOffDate).minusDays(7).toString()

                val wk1Summary = getWeekSummaryString(
                    workDates.filter { it.wdDate <= week1EndDate },
                    nf, hrLabel, otLabel, dblOtLabel, otherHoursLabel, pipeLabel
                )
                val wk2Summary = getWeekSummaryString(
                    workDates.filter { it.wdDate > week1EndDate },
                    nf, hrLabel, otLabel, dblOtLabel, otherHoursLabel, pipeLabel
                )

                var totalHoursDesc = ""
                if (payCalculations.getHoursReg() > 0) totalHoursDesc += "${
                    nf.displayNumberFromDouble(payCalculations.getHoursReg())
                } hr "
                if (payCalculations.getHoursOt() > 0) totalHoursDesc += "| ${
                    nf.displayNumberFromDouble(payCalculations.getHoursOt())
                } ot "
                if (payCalculations.getHoursDblOt() > 0) totalHoursDesc += "| ${
                    nf.displayNumberFromDouble(payCalculations.getHoursDblOt())
                } dbl ot "
                if (payCalculations.getHoursStat() > 0) totalHoursDesc += "| ${
                    nf.displayNumberFromDouble(payCalculations.getHoursStat())
                } other "

                paySummary = TimeSheetPaySummary(
                    grossPay = nf.displayDollars(payCalculations.getPayGross()),
                    deductions = nf.displayDollars(-payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()),
                    netPay = nf.displayDollars(payCalculations.getPayGross() - payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()),
                    totalHoursDescription = totalsLabel + " " + totalHoursDesc.trim(),
                    week1Total = week1Label + (wk1Summary.ifBlank {
                        zeroHrLabel
                    }),
                    week2Total = week2Label + (wk2Summary.ifBlank {
                        zeroHrLabel
                    })
                )
            }
        }
    }

    var showWorkDateOptionsDialog by remember { mutableStateOf<WorkDates?>(null) }
    var showDeleteWorkDateConfirmDialog by remember { mutableStateOf<WorkDates?>(null) }

    if (showWorkDateOptionsDialog != null) {
        val workDate = showWorkDateOptionsDialog!!
        AlertDialog(
            onDismissRequest = { showWorkDateOptionsDialog = null },
            confirmButton = {
                TextButton(onClick = {
                    mainViewModel.setWorkDateObject(workDate)
                    showWorkDateOptionsDialog = null
                    navController.navigate(Screen.WorkDateUpdate.route)
                }) {
                    Text(stringResource(R.string.open_caps))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteWorkDateConfirmDialog = workDate
                    showWorkDateOptionsDialog = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            title = {
                Text(
                    stringResource(R.string.choose_option_for) + df.getDisplayDate(workDate.wdDate)
                )
            }
        )
    }

    if (showDeleteWorkDateConfirmDialog != null) {
        val workDate = showDeleteWorkDateConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteWorkDateConfirmDialog = null },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        payDayViewModel.updateWorkDate(
                            workDate.copy(
                                wdIsDeleted = true,
                                wdUpdateTime = df.getCurrentUTCTimeAsString()
                            )
                        )
                    }
                    showDeleteWorkDateConfirmDialog = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteWorkDateConfirmDialog = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = {
                Text(
                    stringResource(R.string.are_you_sure_you_want_to_delete) + df.getDisplayDate(
                        workDate.wdDate
                    )
                )
            },
            text = { Text(stringResource(R.string.this_cannot_be_undone)) }
        )
    }

    TimeSheetScreen(
        employers = employers,
        selectedEmployer = selectedEmployer,
        onEmployerSelected = {
            if (selectedEmployer?.employerId != it.employerId) {
                selectedEmployer = it
                selectedCutOffDate = ""
                mainViewModel.setPayPeriod(null)
            }
        },
        onAddNewEmployer = {
            navController.navigate(Screen.EmployerAdd.route)
        },
        cutOffDates = cutOffDates.map { it.ppCutoffDate },
        selectedCutOffDate = selectedCutOffDate,
        onCutOffDateSelected = { selectedCutOffDate = it },
        paySummary = paySummary,
        workDates = workDates,
        workDateExtras = workDateExtras,
        onWorkDateClick = { workDate ->
            mainViewModel.setWorkDateObject(workDate)
            navController.navigate(Screen.WorkDateUpdate.route)
        },
        onWorkDateLongClick = { workDate ->
            showWorkDateOptionsDialog = workDate
        },
        onAddWorkDateClick = {
            navController.navigate(Screen.WorkDateAdd.route)
        },
        onViewPayDetailsClick = {
            navController.navigate(Screen.PayDetails.route)
        },
        onGenerateCutoffClick = {
            if (selectedEmployer != null) {
                coroutineScope.launch {
                    val dates = payDayViewModel.getCutOffDatesSync(
                        selectedEmployer!!.employerId,
                        payPeriodsLimit
                    )
                    val nextCutOff = projections.generateNextCutOff(
                        selectedEmployer!!,
                        dates.firstOrNull()?.ppCutoffDate ?: ""
                    )
                    if (nextCutOff.isNotEmpty()) {
                        payDayViewModel.insertPayPeriodSync(
                            PayPeriods(
                                nf.generateRandomIdAsLong(),
                                nextCutOff,
                                selectedEmployer!!.employerId,
                                false,
                                df.getCurrentUTCTimeAsString()
                            )
                        )
                    }
                }
            }
        },
        displayDate = { df.getDisplayDate(it) },
        formatHours = { workDate ->
            formatWorkDateHoursString(
                workDate,
                nf,
                hrsLabel,
                otHrsLabel,
                dblOtHrsLabel,
                otherHrsLabel,
                pipeLabel
            )
        }
    )
}