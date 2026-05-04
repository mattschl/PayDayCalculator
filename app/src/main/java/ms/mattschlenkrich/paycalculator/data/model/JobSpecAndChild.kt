package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.entity.JobSpecMerged

@Parcelize
data class JobSpecAndChild(
    @Embedded
    val jobSpecMerged: JobSpecMerged,
    @Relation(
        parentColumn = "jsmChildId",
        entityColumn = "jobSpecId"
    )
    val jobSpecChild: JobSpec
) : Parcelable