package ms.mattschlenkrich.paydaycalculator.model.workOrder

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers

@Entity(
    tableName = "workOrders",
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = ["employerId"],
        childColumns = ["woEmployerId"]
    )],
)
@Parcelize
data class WorkOrder(
    @PrimaryKey
    val workOrderId: String,
    @ColumnInfo(index = true)
    val woEmployerId: Long,
    val woAddress: String,
    val woDescription: String,
    val woDeleted: Boolean,
    val woUpdateTime: String,
) : Parcelable
