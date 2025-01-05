package ms.mattschlenkrich.paycalculator.database.model.workorder

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
            childColumns = ["wojsWorkOrderId"]
        ),
        ForeignKey(
            entity = JobSpec::class,
            parentColumns = ["jobSpecId"],
            childColumns = ["wojsJobSpecId"]
        )
    ],
    indices =
    [Index(
        value = ["wojsWorkOrderId", "wojsJobSpecId"],
        unique = true
    )
    ]
)
@Parcelize
data class WorkOrderJobSpec(
    @PrimaryKey
    val workOrderJobSpecId: Long,
    @ColumnInfo(index = true)
    val wojsWorkOrderId: Long,
    @ColumnInfo(index = true)
    val wojsJobSpecId: Long,
    val wojsSequence: Int,
    val wojsIsDeleted: Boolean,
    val wojsUpdateTime: String,
) : Parcelable


