package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

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