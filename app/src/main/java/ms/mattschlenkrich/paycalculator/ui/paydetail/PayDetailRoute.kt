package ms.mattschlenkrich.paycalculator.ui.paydetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.model.ExtraContainer
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.TaxAndAmount
import ms.mattschlenkrich.paycalculator.logic.PayCalculationsAsync
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import java.time.LocalDate

@Composable
fun PayDetailRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    payDayViewModel: PayDayViewModel,
    payCalculationsViewModel: PayCalculationsViewModel,
    payDetailViewModel: PayDetailViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    navController: NavController
) {
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val settings by settingsViewModel.settings.observeAsState()
    val payPeriodsLimit = settings?.payPeriodsLimit ?: 15

    val payDayIsLabel = stringResource(R.string.pay_day_is_)
    val netLabel = stringResource(R.string.net_)
    val regLabel = stringResource(R.string.reg_hours)
    val otLabel = stringResource(R.string.overtime)
    val dblOtLabel = stringResource(R.string.double_overtime)
    val otherLabel = stringResource(R.string.other_hours)

    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    var selectedEmployer by remember { mutableStateOf<Employers?>(null) }
    val cutOffDates by if (selectedEmployer != null) {
        payDayViewModel.getCutOffDates(selectedEmployer!!.employerId, payPeriodsLimit)
            .observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }
    var selectedCutOffDate by remember { mutableStateOf("") }

    var paySummary by remember { mutableStateOf(PaySummaryData()) }
    var hourlyBreakdown by remember { mutableStateOf(HourlyBreakdownData()) }
    var credits by remember { mutableStateOf<List<ExtraContainer>>(emptyList()) }
    var deductions by remember { mutableStateOf<List<ExtraContainer>>(emptyList()) }
    var taxes by remember { mutableStateOf<List<TaxAndAmount>>(emptyList()) }

    var trigger by remember { mutableIntStateOf(0) }

    // Initial selection from history
    androidx.compose.runtime.LaunchedEffect(employers) {
        if (selectedEmployer == null && employers.isNotEmpty()) {
            val historyEmployer = mainViewModel.getEmployer()
            selectedEmployer = historyEmployer ?: employers.first()
        }
    }

    androidx.compose.runtime.LaunchedEffect(cutOffDates, selectedEmployer) {
        if (selectedEmployer != null && cutOffDates.isNotEmpty()) {
            val historyCutOff = mainViewModel.getCutOffDate()
            val historyEmployerId = mainViewModel.getEmployer()?.employerId

            if (historyEmployerId != selectedEmployer!!.employerId ||
                historyCutOff == null ||
                !cutOffDates.any { it.ppCutoffDate == historyCutOff }
            ) {
                if (selectedCutOffDate.isEmpty() || !cutOffDates.any { it.ppCutoffDate == selectedCutOffDate }) {
                    selectedCutOffDate = cutOffDates.first().ppCutoffDate
                }
            } else if (selectedCutOffDate.isEmpty()) {
                selectedCutOffDate = historyCutOff
            }
        }
    }

    // Recalculate when selection changes
    androidx.compose.runtime.LaunchedEffect(selectedEmployer, selectedCutOffDate, trigger) {
        if (selectedEmployer != null && selectedCutOffDate.isNotEmpty()) {
            mainViewModel.setEmployer(selectedEmployer!!)
            mainViewModel.setCutOffDate(selectedCutOffDate)

            val payPeriod = payDayViewModel.getPayPeriodSync(
                selectedCutOffDate,
                selectedEmployer!!.employerId
            )
            if (payPeriod != null) {
                mainViewModel.setPayPeriod(payPeriod)
                val payCalculations =
                    PayCalculationsAsync(
                        payCalculationsViewModel,
                        payDetailViewModel,
                        selectedEmployer!!,
                        payPeriod
                    )
                payCalculations.waitForCalculations()

                credits = payCalculations.getCredits()
                deductions = payCalculations.getDebits()
                taxes = payCalculations.getTaxList()

                val payDay = df.getDisplayDate(
                    LocalDate.parse(selectedCutOffDate)
                        .plusDays(selectedEmployer!!.cutoffDaysBefore.toLong())
                        .toString()
                )

                paySummary =
                    PaySummaryData(
                        payDayMessage = payDayIsLabel + payDay,
                        grossPay = nf.displayDollars(payCalculations.getPayGross()),
                        deductions = nf.displayDollars(-payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()),
                        netPay = netLabel + nf.displayDollars(
                            payCalculations.getPayGross() - payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()
                        ),
                        totalCredits = nf.displayDollars(payCalculations.getCreditTotalAll()),
                        totalDeductions = nf.displayDollars(payCalculations.getDebitTotalsByPay() + payCalculations.getAllTaxDeductions())
                    )

                val items = mutableListOf<HourlyItem>()
                if (payCalculations.getPayReg() > 0.0) {
                    items.add(
                        HourlyItem(
                            regLabel,
                            nf.displayNumberFromDouble(payCalculations.getHoursReg()),
                            nf.displayDollars(payCalculations.getPayRate()),
                            nf.displayDollars(payCalculations.getPayReg())
                        )
                    )
                }
                if (payCalculations.getPayOt() > 0.0) {
                    items.add(
                        HourlyItem(
                            otLabel,
                            nf.displayNumberFromDouble(payCalculations.getHoursOt()),
                            nf.displayDollars(payCalculations.getPayRate() * 1.5),
                            nf.displayDollars(payCalculations.getPayOt())
                        )
                    )
                }
                if (payCalculations.getPayDblOt() > 0.0) {
                    items.add(
                        HourlyItem(
                            dblOtLabel,
                            nf.displayNumberFromDouble(payCalculations.getHoursDblOt()),
                            nf.displayDollars(payCalculations.getPayRate() * 2),
                            nf.displayDollars(payCalculations.getPayDblOt())
                        )
                    )
                }
                if (payCalculations.getPayStat() > 0.0) {
                    items.add(
                        HourlyItem(
                            otherLabel,
                            nf.displayNumberFromDouble(payCalculations.getHoursStat()),
                            nf.displayDollars(payCalculations.getPayRate()),
                            nf.displayDollars(payCalculations.getPayStat())
                        )
                    )
                }
                hourlyBreakdown =
                    HourlyBreakdownData(
                        items,
                        nf.displayDollars(payCalculations.getPayAllHourly())
                    )
            }
        }
    }

    PayDetailScreen(
        employers = employers,
        selectedEmployer = selectedEmployer,
        onEmployerSelected = {
            if (selectedEmployer?.employerId != it.employerId) {
                selectedEmployer = it
                selectedCutOffDate = ""
            }
        },
        onAddNewEmployer = {
            navController.navigate(Screen.EmployerAdd.route)
        },
        cutOffDates = cutOffDates.map { it.ppCutoffDate },
        selectedCutOffDate = selectedCutOffDate,
        onCutOffDateSelected = { selectedCutOffDate = it },
        paySummary = paySummary,
        hourlyBreakdown = hourlyBreakdown,
        credits = credits,
        deductions = deductions,
        taxes = taxes,
        onAddCreditClick = {
            navController.navigate(Screen.PayPeriodExtraAdd.route)
        },
        onAddDeductionClick = {
            navController.navigate(Screen.PayPeriodExtraAdd.route)
        },
        onExtraClick = { extra ->
            if (extra.payPeriodExtra != null) {
                mainViewModel.setPayPeriodExtra(extra.payPeriodExtra!!)
                navController.navigate(Screen.PayPeriodExtraUpdate.route)
            }
        },
        onExtraActiveChange = { extra, active ->
            coroutineScope.launch {
                val payPeriod = payDayViewModel.getPayPeriodSync(
                    selectedCutOffDate,
                    selectedEmployer!!.employerId
                )
                if (payPeriod != null) {
                    insertOrUpdateExtraOnChange(
                        extra, !active, payPeriod.payPeriodId,
                        payDayViewModel, nf, df
                    )
                    delay(WAIT_250)
                    trigger++
                }
            }
        },
        onDeleteCutOffClick = {
            // TODO: Confirm and Delete
        },
        onBackClick = {
            navController.popBackStack()
        }
    )
}