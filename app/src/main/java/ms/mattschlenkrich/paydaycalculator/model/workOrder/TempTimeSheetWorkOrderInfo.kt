package ms.mattschlenkrich.paydaycalculator.model.workOrder

data class TempTimeSheetWorkOrderInfo(
    val tempID: Long,
    val woHistoryWorkDate: String,
    val woHistoryRegHours: Double,
    val woHistoryOtHours: Double,
    val woHistoryDblOtHours: Double,
)
