package ms.mattschlenkrich.paycalculator.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "expensePictures",
    foreignKeys = [
        ForeignKey(
            entity = WorkOrderHistoryExpense::class,
            parentColumns = ["woHistoryExpenseId"],
            childColumns = ["epExpenseId"],
            onDelete = ForeignKey.NO_ACTION,
        )
    ]
)
@Parcelize
data class ExpensePictures(
    @PrimaryKey
    val epPictureId: Long,
    @ColumnInfo(index = true)
    val epExpenseId: Long,
    val epDriveFileId: String?,
    val epIsDeleted: Boolean = false,
    val epUploadTime: String? = null,
    val epUpdateTime: String,
) : Parcelable