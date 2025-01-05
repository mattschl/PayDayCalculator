package ms.mattschlenkrich.paycalculator.database.model.workorder

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize


@Parcelize
data class WorkOrderJobSpecCombined(
    @Embedded
    var WorkOrderJobSpec: WorkOrderJobSpec,
    @Relation(
        entity = JobSpec::class,
        parentColumn = "wojsJobSpecId",
        entityColumn = "jobSpecId"
    )
    var jobSpec: JobSpec
) : Parcelable