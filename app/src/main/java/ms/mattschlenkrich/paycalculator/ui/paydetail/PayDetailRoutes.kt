package ms.mattschlenkrich.paycalculator.ui.paydetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.ExtraContainer
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.TaxAndAmount
import ms.mattschlenkrich.paycalculator.logic.PayCalculationsAsync
import java.time.LocalDate

@Composable
fun PayDetailRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    payDayViewModel: PayDayViewModel,
    payCalculationsViewModel: PayCalculationsViewModel,
    payDetailViewModel: PayDetailViewModel,
    navController: androidx.navigation.NavController
) {
    val context = LocalContext.current
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    var selectedEmployer by remember { mutableStateOf<Employers?>(null) }
    val cutOffDates by if (selectedEmployer != null) {
        payDayViewModel.getCutOffDates(selectedEmployer!!.employerId)
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

    var trigger by remember { mutableStateOf(0) }

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
                        payDayMessage = context.getString(R.string.pay_day_is_) + payDay,
                        grossPay = nf.displayDollars(payCalculations.getPayGross()),
                        deductions = nf.displayDollars(-payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()),
                        netPay = context.getString(R.string.net_) + nf.displayDollars(
                            payCalculations.getPayGross() - payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()
                        ),
                        totalCredits = nf.displayDollars(payCalculations.getCreditTotalAll()),
                        totalDeductions = nf.displayDollars(payCalculations.getDebitTotalsByPay() + payCalculations.getAllTaxDeductions())
                    )

                val items = mutableListOf<HourlyItem>()
                if (payCalculations.getPayReg() > 0.0) {
                    items.add(
                        HourlyItem(
                            context.getString(R.string.reg_hours),
                            nf.getNumberFromDouble(payCalculations.getHoursReg()),
                            nf.displayDollars(payCalculations.getPayRate()),
                            nf.displayDollars(payCalculations.getPayReg())
                        )
                    )
                }
                if (payCalculations.getPayOt() > 0.0) {
                    items.add(
                        HourlyItem(
                            context.getString(R.string.overtime),
                            nf.getNumberFromDouble(payCalculations.getHoursOt()),
                            nf.displayDollars(payCalculations.getPayRate() * 1.5),
                            nf.displayDollars(payCalculations.getPayOt())
                        )
                    )
                }
                if (payCalculations.getPayDblOt() > 0.0) {
                    items.add(
                        HourlyItem(
                            context.getString(R.string.double_overtime),
                            nf.getNumberFromDouble(payCalculations.getHoursDblOt()),
                            nf.displayDollars(payCalculations.getPayRate() * 2),
                            nf.displayDollars(payCalculations.getPayDblOt())
                        )
                    )
                }
                if (payCalculations.getPayStat() > 0.0) {
                    items.add(
                        HourlyItem(
                            context.getString(R.string.other_hours),
                            nf.getNumberFromDouble(payCalculations.getHoursStat()),
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
            navController.navigate(ms.mattschlenkrich.paycalculator.Screen.EmployerAdd.route)
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
            navController.navigate(ms.mattschlenkrich.paycalculator.Screen.PayPeriodExtraAdd.route)
        },
        onAddDeductionClick = {
            navController.navigate(ms.mattschlenkrich.paycalculator.Screen.PayPeriodExtraAdd.route)
        },
        onExtraClick = { extra ->
            if (extra.payPeriodExtra != null) {
                mainViewModel.setPayPeriodExtra(extra.payPeriodExtra!!)
                navController.navigate(ms.mattschlenkrich.paycalculator.Screen.PayPeriodExtraUpdate.route)
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

private fun insertOrUpdateExtraOnChange(
    extraContainer: ExtraContainer,
    delete: Boolean,
    payPeriodId: Long,
    payDayViewModel: PayDayViewModel,
    nf: NumberFunctions,
    df: DateFunctions
) {
    if (extraContainer.payPeriodExtra != null) {
        val payPeriodExtra = extraContainer.payPeriodExtra!!
        val newExtra = ms.mattschlenkrich.paycalculator.data.WorkPayPeriodExtras(
            payPeriodExtra.workPayPeriodExtraId,
            payPeriodExtra.ppePayPeriodId,
            payPeriodExtra.ppeExtraTypeId,
            payPeriodExtra.ppeName,
            payPeriodExtra.ppeAppliesTo,
            3,
            payPeriodExtra.ppeValue,
            payPeriodExtra.ppeIsFixed,
            payPeriodExtra.ppeIsCredit,
            delete,
            df.getCurrentUTCTimeAsString()
        )
        extraContainer.payPeriodExtra = newExtra
        payDayViewModel.updatePayPeriodExtra(newExtra)
    } else if (extraContainer.extraDefinitionAndType != null) {
        val extraDefinitionAndType = extraContainer.extraDefinitionAndType!!
        val newExtra = ms.mattschlenkrich.paycalculator.data.WorkPayPeriodExtras(
            nf.generateRandomIdAsLong(),
            payPeriodId,
            extraDefinitionAndType.extraType.workExtraTypeId,
            extraDefinitionAndType.extraType.wetName,
            extraDefinitionAndType.extraType.wetAppliesTo,
            extraDefinitionAndType.extraType.wetAttachTo,
            extraDefinitionAndType.definition.weValue,
            extraDefinitionAndType.definition.weIsFixed,
            extraDefinitionAndType.extraType.wetIsCredit,
            delete,
            df.getCurrentUTCTimeAsString()
        )
        extraContainer.payPeriodExtra = newExtra
        payDayViewModel.insertPayPeriodExtra(newExtra)
    }
}