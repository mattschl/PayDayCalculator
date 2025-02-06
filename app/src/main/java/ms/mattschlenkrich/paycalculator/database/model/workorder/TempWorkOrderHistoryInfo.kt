package ms.mattschlenkrich.paycalculator.database.model.workorder

data class TempWorkOrderHistoryInfo(
    val woHistoryWorkOrderNumber: String,
    val woHistoryWorkDate: String,
    val woHistoryRegHours: Double,
    val woHistoryOtHours: Double,
    val woHistoryDblOtHours: Double,
    val woHistoryNote: String,
    val woWorkPerformed: String,
    val woArea: String,
    val woWorkPerformedNote: String,
    val woMaterialQty: Double,
    val woMaterial: String,
)
