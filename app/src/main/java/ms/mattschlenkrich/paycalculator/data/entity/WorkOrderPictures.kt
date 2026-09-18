package ms.mattschlenkrich.paycalculator.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "work_order_pictures",
    foreignKeys = [
        ForeignKey(
            entity = WorkOrder::class,
            parentColumns = ["workOrderId"],
            childColumns = ["wpWorkOrderId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorkOrderHistory::class,
            parentColumns = ["woHistoryId"],
            childColumns = ["wpHistoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorkOrderHistoryExpense::class,
            parentColumns = ["woHistoryExpenseId"],
            childColumns = ["wpExpenseId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
@Parcelize
data class WorkOrderPictures(
    @PrimaryKey
    val pictureId: Long,
    @ColumnInfo(index = true)
    val wpWorkOrderId: Long?,
    @ColumnInfo(index = true)
    val wpHistoryId: Long?,
    @ColumnInfo(index = true)
    val wpExpenseId: Long?,
    val driveFileId: String?,
    val localCachePath: String?,
    val isUploaded: Boolean,
    val wpUpdateTime: String,
) : Parcelable