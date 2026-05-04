package ms.mattschlenkrich.paycalculator.data.entity

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
        ),
        ForeignKey(
            entity = Areas::class,
            parentColumns = ["areaId"],
            childColumns = ["wojsAreaId"]
        )
    ],
    indices =
        [Index(
            value = ["wojsWorkOrderId", "wojsJobSpecId", "wojsAreaId"],
            unique = true
        )]
)
@Parcelize
data class WorkOrderJobSpec(
    @PrimaryKey
    val workOrderJobSpecId: Long,
    @ColumnInfo(index = true)
    val wojsWorkOrderId: Long,
    @ColumnInfo(index = true)
    val wojsJobSpecId: Long,
    @ColumnInfo(index = true)
    val wojsAreaId: Long?,
    val wojsNote: String?,
    val wojsSequence: Int,
    val wojsIsDeleted: Boolean,
    val wojsUpdateTime: String,
) : Parcelable