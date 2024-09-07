package ms.mattschlenkrich.paydaycalculator.database.model.workOrder

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers

@Entity(
    tableName = "workOrders",
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = ["employerId"],
        childColumns = ["woEmployerId"]
    )],
    indices =
    [Index(value = ["woNumber", "woEmployerId"], unique = true)]
)
@Parcelize
data class WorkOrder(
    @PrimaryKey
    val workOrderId: Long,
    @ColumnInfo(index = true)
    val woNumber: String,
    @ColumnInfo(index = true)
    val woEmployerId: Long,
    val woAddress: String,
    val woDescription: String,
    val woDeleted: Boolean,
    val woUpdateTime: String,
) : Parcelable
