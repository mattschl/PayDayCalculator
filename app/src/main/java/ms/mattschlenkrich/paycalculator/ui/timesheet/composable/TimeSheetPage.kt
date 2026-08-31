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

    val payPeriod by payDayViewModel.getPayPeriod(cutoffDate, employer.employerId).observeAsState()

    val payPeriodExtras by payDayViewModel.getPayPeriodExtras(
        payPeriod?.payPeriodId ?: 0L
    ).observeAsState(emptyList())

    val totalsLabel = stringResource(R.string.totals)
    val week1Label = stringResource(R.string.label_week_1_prefix)
    val week2Label = stringResource(R.string.label_week_2_prefix)
    val zeroHrLabel = stringResource(R.string.label_zero_hours)

    var paySummary by remember { mutableStateOf(TimeSheetPaySummary()) }
    var week1SummaryString by remember { mutableStateOf("") }
    var week2SummaryString by remember { mutableStateOf("") }

    LaunchedEffect(workDates, extrasList, payPeriodExtras, employer, cutoffDate) {
        if (workDates.isEmpty()) return@LaunchedEffect

        val curPayPeriod = payDayViewModel.getPayPeriodSync(
            cutoffDate,
            employer.employerId
        )
        if (curPayPeriod != null) {
            val payCalculations =
                PayCalculationsAsync(
                    payCalculationsViewModel,
                    payDetailViewModel,
                    employer,
                    curPayPeriod
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

            paySummary = TimeSheetPaySummary(
                grossPay = nf.displayDollars(payCalculations.getPayGross()),
                deductions = nf.displayDollars(-payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()),
                netPay = nf.displayDollars(payCalculations.getPayGross() - payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()),
                totalHoursDescription = "$totalsLabel ${totalHoursDesc.trim()}",
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