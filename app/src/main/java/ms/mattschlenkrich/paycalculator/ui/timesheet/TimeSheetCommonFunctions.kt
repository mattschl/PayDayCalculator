package ms.mattschlenkrich.paycalculator.ui.timesheet

import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.WorkDates

fun getWeekSummaryString(
    workDates: List<WorkDates>,
    nf: NumberFunctions,
    hrLabel: String,
    otLabel: String,
    dblOtLabel: String,
    otherHoursLabel: String,
    pipeLabel: String
): String {
    var reg = 0.0
    var ot = 0.0
    var dbl = 0.0
    var stat = 0.0
    workDates.forEach {
        reg += it.wdRegHours; ot += it.wdOtHours; dbl += it.wdDblOtHours; stat += it.wdStatHours
    }
    val parts = mutableListOf<String>()
    if (reg > 0) parts.add("${nf.displayNumberFromDouble(reg)} $hrLabel")
    if (ot > 0) parts.add("${nf.displayNumberFromDouble(ot)} $otLabel")
    if (dbl > 0) parts.add("${nf.displayNumberFromDouble(dbl)} $dblOtLabel")
    if (stat > 0) parts.add("${nf.displayNumberFromDouble(stat)} $otherHoursLabel")
    return parts.joinToString(pipeLabel)
}

fun formatWorkDateHoursString(
    workDate: WorkDates,
    nf: NumberFunctions,
    hrsLabel: String,
    otHrsLabel: String,
    dblOtHrsLabel: String,
    otherHrsLabel: String,
    pipeLabel: String
): String {
    val parts = mutableListOf<String>()
    if (workDate.wdRegHours > 0) parts.add(
        "${nf.displayNumberFromDouble(workDate.wdRegHours)}$hrsLabel"
    )
    if (workDate.wdOtHours > 0) parts.add(
        "${nf.displayNumberFromDouble(workDate.wdOtHours)}$otHrsLabel"
    )
    if (workDate.wdDblOtHours > 0) parts.add(
        "${nf.displayNumberFromDouble(workDate.wdDblOtHours)}$dblOtHrsLabel"
    )
    if (workDate.wdStatHours > 0) parts.add(
        "${nf.displayNumberFromDouble(workDate.wdStatHours)}$otherHrsLabel"
    )

    var display = parts.joinToString(pipeLabel)
    if (!workDate.wdNote.isNullOrBlank()) {
        if (display.isNotBlank()) display += " - "
        display += workDate.wdNote
    }
    return display
}