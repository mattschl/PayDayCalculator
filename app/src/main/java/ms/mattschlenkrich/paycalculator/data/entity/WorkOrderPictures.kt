package ms.mattschlenkrich.paycalculator.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "workOrderPictures",
    foreignKeys = [
        ForeignKey(
            entity = WorkOrder::class,
            parentColumns = ["workOrderId"],
            childColumns = ["wopWorkOrderId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorkOrderHistory::class,
            parentColumns = ["woHistoryId"],
            childColumns = ["wopHistoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorkOrderHistoryExpense::class,
            parentColumns = ["woHistoryExpenseId"],
            childColumns = ["wopExpenseId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
@Parcelize
data class WorkOrderPictures(
    @PrimaryKey
    val pictureId: Long,
    @ColumnInfo(index = true)
    val wopWorkOrderId: Long?,
    @ColumnInfo(index = true)
    val wopHistoryId: Long?,
    @ColumnInfo(index = true)
    val wopExpenseId: Long?,
    val driveFileId: String?,
    val wopIsDeleted: Boolean = false,
    val wopUploadTime: String? = null,
    val wopUpdateTime: String,
) : Parcelable