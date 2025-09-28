package ms.mattschlenkrich.paycalculator.database.model.workorder

data class TempWorkOrderHistoryInfo(
    val woHistoryId: Long,
    val woHistoryWorkOrderNumber: String,
    val woHistoryWorkDate: String,
    var woHistoryRegHours: Double,
    var woHistoryOtHours: Double,
    var woHistoryDblOtHours: Double,
    var woHistoryNote: String,
    var woWorkPerformed: String,
    var woArea: String,
    var woWorkPerformedNote: String,
    var woMaterialQty: Double,
    var woMaterial: String,
)
