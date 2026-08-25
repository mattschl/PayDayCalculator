package ms.mattschlenkrich.paycalculator.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "workOrderHistoryExpense-*-",
    foreignKeys = [
        ForeignKey(
            entity = WorkOrderHistory::class,
            parentColumns = ["woHistoryId"],
            childColumns = ["woheHistoryId"]
        )
    ],
)
@Parcelize
data class WorkOrderHistoryExpense(
    @PrimaryKey
    val woHistoryExpenseId: Long,
    @ColumnInfo(index = true)
    val woheHistoryId: Long,
    val woheType: String,
    val woheSupplier: String,
    val woheInvoiceNo: String,
    val woheAmount: Double,
    val woheIsDeleted: Boolean,
    val woheUpdateTime: String,
) : Parcelable