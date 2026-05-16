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
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDetailViewModel
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
    val otherHoursLabel = stringResource(R.string.other)
    val pipeLabel = stringResource(R.string.pipe)
    val hrsLabel = stringResource(R.string.hrs)
    val otHrsLabel = stringResource(R.string.ot)
    val dblOtHrsLabel = stringResource(R.string.dbl_ot)
    val otherHrsLabel = stringResource(R.string.other)

    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    val selectedEmployer = mainViewModel.selectedEmployer.value
    val cutOffDates by if (selectedEmployer != null) {
        payDayViewModel.getCutOffDates(selectedEmployer.employerId, payPeriodsLimit)
            .observeAsState(null)
    } else {
        remember { mutableStateOf(emptyList()) }
    }
    val selectedCutOffDate = mainViewModel.selectedCutOffDate.value
    var paySummary by remember { mutableStateOf(TimeSheetPaySummary()) }
    var week1SummaryString by remember { mutableStateOf("") }
    var week2SummaryString by remember { mutableStateOf("") }
    val workDates by if (selectedEmployer != null && selectedCutOffDate.isNotEmpty()) {
        payDayViewModel.getWorkDateList(
            selectedEmployer.employerId,
            selectedCutOffDate
        ).observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val extrasList by if (selectedEmployer != null && selectedCutOffDate.isNotEmpty()) {
        payDayViewModel.getWorkDateExtrasPerPay(
            selectedEmployer.employerId,
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
            val savedEmployer = employers.find { it.employerId == mainViewModel.selectedEmployerId }
            mainViewModel.setEmployer(savedEmployer ?: employers.first())
        }
    }

    LaunchedEffect(cutOffDates, selectedEmployer) {
        val dates = cutOffDates ?: return@LaunchedEffect
        if (selectedEmployer != null) {
            val today = LocalDate.now().toString()
            if (dates.isEmpty() || dates.first().ppCutoffDate < today) {
                coroutineScope.launch {
                    val nextCutOff = projections.generateNextCutOff(
                        selectedEmployer,
                        dates.firstOrNull()?.ppCutoffDate ?: ""
                    )
                    if (nextCutOff.isNotEmpty()) {
                        mainViewModel.setPayPeriod(null)
                        payDayViewModel.insertPayPeriodSync(
                            PayPeriods(
                                nf.generateRandomIdAsLong(),
                                nextCutOff,
                                selectedEmployer.employerId,
                                false,
                                df.getCurrentUTCTimeAsString()
                            )
                        )
                        if (mainViewModel.selectedCutOffDate.value.isEmpty() ||
                            !dates.any { it.ppCutoffDate == mainViewModel.selectedCutOffDate.value }
                        ) {
                            mainViewModel.setCutOffDate(nextCutOff)
                        }
                    }
                }
            } else {
                if (mainViewModel.selectedCutOffDate.value.isEmpty() ||
                    !dates.any { it.ppCutoffDate == mainViewModel.selectedCutOffDate.value }
                ) {
                    val currentCutOff =
                        dates.lastOrNull { it.ppCutoffDate >= today }?.ppCutoffDate
                            ?: dates.first().ppCutoffDate
                    mainViewModel.setCutOffDate(currentCutOff)
                }
            }
        }
    }

    // Update summary and set global state
    LaunchedEffect(selectedEmployer, selectedCutOffDate, workDates) {
        if (selectedEmployer != null && selectedCutOffDate.isNotEmpty() &&
            cutOffDates?.any { it.ppCutoffDate == selectedCutOffDate } == true
        ) {
            val payPeriod = payDayViewModel.getPayPeriodSync(
                selectedCutOffDate,
                selectedEmployer.employerId
            )
            mainViewModel.setPayPeriod(payPeriod)
            if (payPeriod != null) {
                val payCalculations =
                    PayCalculationsAsync(
                        payCalculationsViewModel,
                        payDetailViewModel,
                        selectedEmployer,
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
                week1SummaryString = wk1Summary.ifBlank { zeroHrLabel }
                week2SummaryString = wk2Summary.ifBlank { zeroHrLabel }
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

    val week1EndDate = remember(selectedCutOffDate) {
        if (selectedCutOffDate.isNotEmpty()) {
            LocalDate.parse(selectedCutOffDate).minusDays(7).toString()
        } else ""
    }

    TimeSheetScreen(
        employers = employers,
        selectedEmployer = selectedEmployer,
        onEmployerSelected = {
            if (selectedEmployer?.employerId != it.employerId) {
                mainViewModel.setEmployer(it)
                mainViewModel.setCutOffDate("")
                mainViewModel.setPayPeriod(null)
            }
        },
        onAddNewEmployer = {
            navController.navigate(Screen.EmployerAdd.route)
        },
        cutOffDates = cutOffDates?.map { it.ppCutoffDate } ?: emptyList(),
        selectedCutOffDate = selectedCutOffDate,
        onCutOffDateSelected = { mainViewModel.setCutOffDate(it) },
        paySummary = paySummary,
        week1Summary = week1SummaryString,
        week2Summary = week2SummaryString,
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
        week1EndDate = week1EndDate,
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