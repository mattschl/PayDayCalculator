package ms.mattschlenkrich.paycalculator.ui.paydetail.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.model.ExtraContainer
import ms.mattschlenkrich.paycalculator.data.model.TaxAndAmount
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.logic.PayCalculationsAsync
import ms.mattschlenkrich.paycalculator.ui.paydetail.HourlyBreakdownData
import ms.mattschlenkrich.paycalculator.ui.paydetail.HourlyItem
import ms.mattschlenkrich.paycalculator.ui.paydetail.PaySummaryData
import ms.mattschlenkrich.paycalculator.ui.paydetail.insertOrUpdateExtraOnChange
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayDetailPage(
    employer: Employers,
    cutoffDate: String,
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    payCalculationsViewModel: PayCalculationsViewModel,
    payDetailViewModel: PayDetailViewModel,
    nf: NumberFunctions,
    df: DateFunctions,
    navController: NavController
) {
    var paySummary by remember { mutableStateOf(PaySummaryData()) }
    var hourlyBreakdown by remember { mutableStateOf(HourlyBreakdownData()) }
    var credits by remember { mutableStateOf<List<ExtraContainer>>(emptyList()) }
    var deductions by remember { mutableStateOf<List<ExtraContainer>>(emptyList()) }
    var taxes by remember { mutableStateOf<List<TaxAndAmount>>(emptyList()) }
    var trigger by remember { mutableIntStateOf(0) }
    var showOverrideDialog by remember { mutableStateOf<ExtraContainer?>(null) }

    // Cache to prevent redundant calculations
    val calculationCache = remember { mutableMapOf<String, Unit>() }
    val cacheKey = "${employer.employerId}_${cutoffDate}_$trigger"

    val payDayIsLabel = stringResource(R.string.pay_day_is_)
    val netLabel = stringResource(R.string.net_)
    val regLabel = stringResource(R.string.reg_hours)
    val otLabel = stringResource(R.string.overtime)
    val dblOtLabel = stringResource(R.string.double_overtime)
    val otherLabel = stringResource(R.string.other_hours)

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(employer, cutoffDate, trigger) {
        if (calculationCache.containsKey(cacheKey)) return@LaunchedEffect
        
        val payPeriod = payDayViewModel.getPayPeriodSync(
            cutoffDate,
            employer.employerId
        )
        if (payPeriod != null) {
            mainViewModel.setPayPeriod(payPeriod)
            val payCalculations =
                PayCalculationsAsync(
                    payCalculationsViewModel,
                    payDetailViewModel,
                    employer,
                    payPeriod
                )
            payCalculations.waitForCalculations()

            credits = payCalculations.getCredits()
            deductions = payCalculations.getDebits()
            taxes = payCalculations.getTaxList()

            val payDay = try {
                df.getDisplayDate(
                    LocalDate.parse(cutoffDate)
                        .plusDays(employer.cutoffDaysBefore.toLong())
                        .toString()
                )
            } catch (e: Exception) {
                ""
            }

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
            calculationCache[cacheKey] = Unit
        }
    }

    PayDetailContent(
        paySummary = paySummary,
        hourlyBreakdown = hourlyBreakdown,
        credits = credits,
        deductions = deductions,
        taxes = taxes,
        onAddCreditClick = {
            navController.navigate(Screen.PayPeriodExtraAdd.createRoute(true))
        },
        onAddDeductionClick = {
            navController.navigate(Screen.PayPeriodExtraAdd.createRoute(false))
        },
        onExtraClick = { extra ->
            if (extra.payPeriodExtra != null) {
                mainViewModel.setPayPeriodExtra(extra.payPeriodExtra!!)
                navController.navigate(Screen.PayPeriodExtraUpdate.route)
            } else if (extra.extraDefinitionAndType != null) {
                showOverrideDialog = extra
            }
        },
        onExtraActiveChange = { extra, active ->
            coroutineScope.launch {
                val payPeriod = payDayViewModel.getPayPeriodSync(
                    cutoffDate,
                    employer.employerId
                )
                if (payPeriod != null) {
                    insertOrUpdateExtraOnChange(
                        extra, !active, payPeriod.payPeriodId,
                        payDayViewModel, nf, df
                    )
                    trigger++
                }
            }
        }
    )

    if (showOverrideDialog != null) {
        val extra = showOverrideDialog!!
        ModalBottomSheet(
            onDismissRequest = { showOverrideDialog = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.create_override),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(stringResource(R.string.this_is_a_default_extra_override))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showOverrideDialog = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = {
                        coroutineScope.launch {
                            val payPeriod = payDayViewModel.getPayPeriodSync(
                                cutoffDate,
                                employer.employerId
                            )
                            if (payPeriod != null) {
                                insertOrUpdateExtraOnChange(
                                    extra, false, payPeriod.payPeriodId,
                                    payDayViewModel, nf, df
                                )
                                // The helper sets the new extra in extra.payPeriodExtra
                                if (extra.payPeriodExtra != null) {
                                    mainViewModel.setPayPeriodExtra(extra.payPeriodExtra!!)
                                    navController.navigate(Screen.PayPeriodExtraUpdate.route)
                                }
                            }
                        }
                    }) {
                        Text(stringResource(R.string.yes))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}