package ms.mattschlenkrich.paycalculator.ui.timesheet

data class TimeSheetPaySummary(
    val grossPay: String = "$0.00",
    val deductions: String = "$0.00",
    val netPay: String = "$0.00",
    val totalHoursDescription: String = "",
    val week1Total: String = "",
    val week2Total: String = ""
)