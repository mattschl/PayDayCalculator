package ms.mattschlenkrich.paydaycalculator.model.workOrder

data class TempWorkOrderInfo(
    val tempID: String,
    val woHistoryWorkDate: String,
    val woHistoryRegHours: Double,
    val woHistoryOtHours: Double,
    val woHistoryDblOtHours: Double,
)
