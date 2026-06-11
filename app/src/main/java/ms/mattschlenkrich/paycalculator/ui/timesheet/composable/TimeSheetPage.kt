package ms.mattschlenkrich.paycalculator.ui.timesheet.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.logic.PayCalculationsAsync
import ms.mattschlenkrich.paycalculator.ui.timesheet.TimeSheetPaySummary
import ms.mattschlenkrich.paycalculator.ui.timesheet.formatWorkDateHoursString
import ms.mattschlenkrich.paycalculator.ui.timesheet.getWeekSummaryString
import java.time.LocalDate

@Composable
fun TimeSheetPage(
    employer: Employers,
    cutoffDate: String,
    payDayViewModel: PayDayViewModel,
    payCalculationsViewModel: PayCalculationsViewModel,
    payDetailViewModel: PayDetailViewModel,
    nf: NumberFunctions,
    df: DateFunctions,
    onWorkDateClick: (WorkDates) -> Unit,
    onWorkDateLongClick: (WorkDates) -> Unit,
    onViewPayDetailsClick: () -> Unit,
    hrLabel: String,
    otLabel: String,
    dblOtLabel: String,
    otherHoursLabel: String,
    pipeLabel: String,
    hrsLabel: String,
    otHrsLabel: String,
    dblOtHrsLabel: String,
    otherHrsLabel: String,
    minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH
) {
    val workDates by payDayViewModel.getWorkDateList(
        employer.employerId,
        cutoffDate
    ).observeAsState(emptyList())

    val extrasList by payDayViewModel.getWorkDateExtrasPerPay(
        employer.employerId,
        cutoffDate
    ).observeAsState(emptyList())

    val workDateExtras = remember(extrasList) {
        extrasList.groupBy { it.extra.wdeWorkDateId }
    }

    val totalsLabel = stringResource(R.string.totals)
    val week1Label = stringResource(R.string.week_1_)
    val week2Label = stringResource(R.string.week_2_)
    val zeroHrLabel = stringResource(R.string._0_hr)

    var paySummary by remember { mutableStateOf(TimeSheetPaySummary()) }
    var week1SummaryString by remember { mutableStateOf("") }
    var week2SummaryString by remember { mutableStateOf("") }

    // Cache to prevent redundant calculations on recomposition
    val calculationCache = remember { mutableMapOf<String, TimeSheetPaySummary>() }
    val summaryCache = remember { mutableMapOf<String, Pair<String, String>>() }
    val cacheKey = "${employer.employerId}_$cutoffDate"

    LaunchedEffect(workDates, employer, cutoffDate) {
        if (workDates.isEmpty()) return@LaunchedEffect

        // Check cache first
        if (calculationCache.containsKey(cacheKey) && summaryCache.containsKey(cacheKey)) {
            paySummary = calculationCache[cacheKey]!!
            val summaries = summaryCache[cacheKey]!!
            week1SummaryString = summaries.first
            week2SummaryString = summaries.second
            return@LaunchedEffect
        }

        val payPeriod = payDayViewModel.getPayPeriodSync(
            cutoffDate,
            employer.employerId
        )
        if (payPeriod != null) {
            val payCalculations =
                PayCalculationsAsync(
                    payCalculationsViewModel,
                    payDetailViewModel,
                    employer,
                    payPeriod
                )
            payCalculations.waitForCalculations()

            val week1EndDate = try {
                LocalDate.parse(cutoffDate).minusDays(7).toString()
            } catch (_: Exception) {
                ""
            }

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

            val newSummary = TimeSheetPaySummary(
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
            paySummary = newSummary
            calculationCache[cacheKey] = newSummary
            val w1 = wk1Summary.ifBlank { zeroHrLabel }
            val w2 = wk2Summary.ifBlank { zeroHrLabel }
            week1SummaryString = w1
            week2SummaryString = w2
            summaryCache[cacheKey] = Pair(w1, w2)
        }
    }

    val week1EndDate = remember(cutoffDate) {
        try {
            LocalDate.parse(cutoffDate).minusDays(7).toString()
        } catch (_: Exception) {
            ""
        }
    }

    TimeSheetContent(
        paySummary = paySummary,
        week1Summary = week1SummaryString,
        week2Summary = week2SummaryString,
        workDates = workDates,
        workDateExtras = workDateExtras,
        onWorkDateClick = remember(onWorkDateClick) { onWorkDateClick },
        onWorkDateLongClick = remember(onWorkDateLongClick) { onWorkDateLongClick },
        onViewPayDetailsClick = remember(onViewPayDetailsClick) { onViewPayDetailsClick },
        week1EndDate = week1EndDate,
        displayDate = remember(df) { { if (it.isBlank()) "" else df.getDisplayDate(it) } },
        formatHours = remember(nf, hrsLabel, otHrsLabel, dblOtHrsLabel, otherHrsLabel, pipeLabel) {
            { workDate ->
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
        },
        minColumnWidth = minColumnWidth
    )
}