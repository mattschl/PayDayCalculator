package ms.mattschlenkrich.paydaycalculator.model.workOrder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers

@Entity(
    tableName = "workOrders",
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = ["employerId"],
        childColumns = ["woEmployerId"]
    )],
)
data class WorkOrder(
    @PrimaryKey
    val workOrderId: String,
    @ColumnInfo(index = true)
    val woEmployerId: Long,
    val woAddress: String,
    val woDescription: String,
)
