package ms.mattschlenkrich.paydaycalculator.database.model.workOrder

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates

@Entity(
    tableName = "workOrderHistory",
    foreignKeys = [ForeignKey(
        entity = WorkOrder::class,
        parentColumns = ["workOrderId"],
        childColumns = ["woHistoryWorkOrderId"]
    ), ForeignKey(
        entity = WorkDates::class,
        parentColumns = ["workDateId"],
        childColumns = ["woHistoryWorkDateId"]
    )],
    indices = [Index(
        value = ["woHistoryWorkOrderId", "woHistoryWorkDateId"],
        unique = true
    )]
)
@Parcelize
data class WorkOrderHistory(
    @PrimaryKey
    val woHistoryId: Long,
    @ColumnInfo(index = true)
    val woHistoryWorkOrderId: Long,
    @ColumnInfo(index = true)
    val woHistoryWorkDateId: Long,
    val woHistoryRegHours: Double,
    val woHistoryOtHours: Double,
    val woHistoryDblOtHours: Double,
    val woHistoryNote: String?,
    val woHistoryDeleted: Boolean,
    val woHistoryUpdateTime: String,
) : Parcelable
