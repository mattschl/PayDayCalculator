package ms.mattschlenkrich.paycalculator.ui.paydetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
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
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.model.ExtraContainer
import ms.mattschlenkrich.paycalculator.data.model.TaxAndAmount
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.logic.PayCalculationsAsync
import ms.mattschlenkrich.paycalculator.ui.paydetail.composable.PayDetailContent
import ms.mattschlenkrich.paycalculator.ui.paydetail.composable.SelectionCard
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

    val settings by settingsViewModel.settings.observeAsState()
    val payPeriodsLimit = settings?.payPeriodsLimit ?: 15

    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    val selectedEmployer = mainViewModel.selectedEmployer.value
    val cutOffDates by if (selectedEmployer != null) {
        payDayViewModel.getCutOffDates(selectedEmployer.employerId, payPeriodsLimit)
            .observeAsState(null)
    } else {
        remember { mutableStateOf(emptyList()) }
    }
    val selectedCutOffDate = mainViewModel.selectedCutOffDate.value

    val pagerState = rememberPagerState(
        initialPage = 0
    ) { cutOffDates?.size ?: 0 }

    // Initial selection from history
    LaunchedEffect(employers) {
        if (selectedEmployer == null && employers.isNotEmpty()) {
            val savedEmployer = employers.find { it.employerId == mainViewModel.selectedEmployerId }
            mainViewModel.setEmployer(savedEmployer ?: employers.first())
        }
    }

    LaunchedEffect(cutOffDates, selectedEmployer) {
        val dates = cutOffDates ?: return@LaunchedEffect
        if (selectedEmployer != null && dates.isNotEmpty()) {
            if (mainViewModel.selectedCutOffDate.value.isBlank() || !dates.any { it.ppCutoffDate == mainViewModel.selectedCutOffDate.value }) {
                val currentCutOff =
                    dates.lastOrNull { it.ppCutoffDate >= LocalDate.now().toString() }?.ppCutoffDate
                        ?: dates.first().ppCutoffDate
                mainViewModel.setCutOffDate(currentCutOff)
            }
        }
    }

    // Sync ViewModel selection to Pager
    LaunchedEffect(selectedCutOffDate, cutOffDates) {
        val dates = cutOffDates ?: return@LaunchedEffect
        val index = dates.indexOfFirst { it.ppCutoffDate == selectedCutOffDate }
        if (index != -1 && pagerState.currentPage != index) {
            if (mainViewModel.selectedTopLevelIndex.intValue == 1) {
                pagerState.animateScrollToPage(index)
            } else {
                pagerState.scrollToPage(index)
            }
        }
    }

    // Sync Pager selection back to ViewModel
    LaunchedEffect(pagerState, cutOffDates) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            val dates = cutOffDates ?: return@collect
            if (page < dates.size) {
                val newDate = dates[page].ppCutoffDate
                if (mainViewModel.selectedCutOffDate.value != newDate) {
                    mainViewModel.setCutOffDate(newDate)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SelectionCard(
            modifier = Modifier.padding(horizontal = SCREEN_PADDING_HORIZONTAL),
            employers = employers,
            selectedEmployer = selectedEmployer,
            onEmployerSelected = {
                if (selectedEmployer?.employerId != it.employerId) {
                    mainViewModel.setEmployer(it)
                    mainViewModel.setCutOffDate("")
                }
            },
            onAddNewEmployer = {
                navController.navigate(Screen.EmployerAdd.route)
            },
            cutOffDates = cutOffDates?.map { it.ppCutoffDate } ?: emptyList(),
            selectedCutOffDate = selectedCutOffDate,
            onCutOffDateSelected = { mainViewModel.setCutOffDate(it) },
            displayDate = { if (it.isBlank()) "" else df.getDisplayDate(it) }
        )

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                userScrollEnabled = true
            ) { page ->
                val date = cutOffDates?.getOrNull(page)?.ppCutoffDate ?: return@VerticalPager
                val employer = selectedEmployer ?: return@VerticalPager
                PayDetailPage(
                    employer = employer,
                    cutoffDate = date,
                    mainViewModel = mainViewModel,
                    payDayViewModel = payDayViewModel,
                    payCalculationsViewModel = payCalculationsViewModel,
                    payDetailViewModel = payDetailViewModel,
                    nf = nf,
                    df = df,
                    navController = navController
                )
            }
        }
    }
}

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

    val payDayIsLabel = stringResource(R.string.pay_day_is_)
    val netLabel = stringResource(R.string.net_)
    val regLabel = stringResource(R.string.reg_hours)
    val otLabel = stringResource(R.string.overtime)
    val dblOtLabel = stringResource(R.string.double_overtime)
    val otherLabel = stringResource(R.string.other_hours)

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(employer, cutoffDate, trigger) {
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
        }
    }

    PayDetailContent(
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
                    delay(WAIT_250)
                    trigger++
                }
            }
        }
    )

    if (showOverrideDialog != null) {
        val extra = showOverrideDialog!!
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.create_override)) },
            text = { Text(stringResource(R.string.this_is_a_default_extra_override)) },
            confirmButton = {
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
            },
            dismissButton = {
                TextButton(onClick = { showOverrideDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}