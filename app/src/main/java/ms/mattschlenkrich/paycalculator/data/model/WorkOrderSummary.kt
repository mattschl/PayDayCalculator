package ms.mattschlenkrich.paycalculator.data.model

data class WorkOrderSummary(
    val totalRegHours: Double = 0.0,
    val totalOtHours: Double = 0.0,
    val totalDblOtHours: Double = 0.0
)