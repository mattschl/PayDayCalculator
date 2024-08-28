package ms.mattschlenkrich.paydaycalculator.model.workOrder

data class TempWorkOrderHistoryInfo(
    val woHistoryWorkOrderNumber: String,
    val woHistoryWorkDate: String,
    val woHistoryRegHours: Double,
    val woHistoryOtHours: Double,
    val woHistoryDblOtHours: Double,
    val woHistoryNote: String?,
)
