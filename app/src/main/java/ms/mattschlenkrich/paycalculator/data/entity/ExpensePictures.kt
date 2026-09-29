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
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
@Parcelize
data class ExpensePictures(
    @PrimaryKey
    val pictureId: Long,
    @ColumnInfo(index = true)
    val epExpenseId: Long,
    val driveFileId: String?,
    val epIsDeleted: Boolean = false,
    val epUploadTime: String? = null,
    val epUpdateTime: String,
) : Parcelable