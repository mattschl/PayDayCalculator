package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.Areas
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderJobSpec


@Parcelize
data class WorkOrderJobSpecCombined(
    @Embedded
    var workOrderJobSpec: WorkOrderJobSpec,
    @Relation(
        entity = JobSpec::class,
        parentColumn = "wojsJobSpecId",
        entityColumn = "jobSpecId"
    )
    var jobSpec: JobSpec,
    @Relation(
        entity = Areas::class,
        parentColumn = "wojsAreaId",
        entityColumn = "areaId"
    )
    var area: Areas?
) : Parcelable