package ms.mattschlenkrich.paydaycalculator.model.workOrder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workOrderHistory",
    foreignKeys = [ForeignKey(
        entity = WorkOrder::class,
        parentColumns = ["workOrderId"],
        childColumns = ["woHistoryWorkOrderId"]
    )],
    indices = [Index(
        value = ["woHistoryWorkOrderId", "woHistoryWorkDate"],
        unique = true
    )]
)
data class WorkOrderHistory(
    @PrimaryKey
    val woHistoryId: Long,
    @ColumnInfo(index = true)
    val woHistoryWorkOrderId: Long,
    @ColumnInfo(index = true)
    val woHistoryWorkDate: String,
    val woHistoryRegHours: Double,
    val woHistoryOtHours: Double,
    val woHistoryDblOtHours: Double,
)
