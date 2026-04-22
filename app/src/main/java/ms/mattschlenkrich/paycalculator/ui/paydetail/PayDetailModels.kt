package ms.mattschlenkrich.paycalculator.ui.paydetail

data class PaySummaryData(
    val payDayMessage: String = "",
    val grossPay: String = "$0.00",
    val deductions: String = "$0.00",
    val netPay: String = "$0.00",
    val totalCredits: String = "$0.00",
    val totalDeductions: String = "$0.00"
)

data class HourlyBreakdownData(
    val items: List<HourlyItem> = emptyList(),
    val totalHourly: String = "$0.00"
)

data class HourlyItem(
    val description: String,
    val qty: String,
    val rate: String,
    val total: String
)