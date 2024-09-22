package ms.mattschlenkrich.paydaycalculator.database.model.workOrder

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "workOrderJobSpecs",
    foreignKeys = [
        ForeignKey(
            entity = WorkOrder::class,
            parentColumns = ["workOrderId"],
            childColumns = ["wojsWWorkOrderId"]
        ),
        ForeignKey(
            entity = JobSpec::class,
            parentColumns = ["jobSpecId"],
            childColumns = ["wojsJobSpecId"]
        )
    ],
    indices =
    [Index(
        value = ["wojsWWorkOrderId", "wojsJobSpecId"],
        unique = true
    )
    ]
)
@Parcelize
data class WorkOrderJobSpec(
    @PrimaryKey
    val workOrderJobSpecId: Long,
    @ColumnInfo(index = true)
    val wojsWWorkOrderId: Long,
    @ColumnInfo(index = true)
    val wojsJobSpecId: Long,
    val wojsIsDeleted: Boolean,
    val wojsUpdateTime: String,
) : Parcelable
