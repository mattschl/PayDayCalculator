package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformedMerged


@Parcelize
data class WorkPerformedAndChild(
    @Embedded
    val workPerformedMerged: WorkPerformedMerged,
    @Relation(
        entity = WorkPerformed::class,
        parentColumn = "wpmMasterId",
        entityColumn = "workPerformedId"
    )
    val workPerformedParent: WorkPerformed,
    @Relation(
        entity = WorkPerformed::class,
        parentColumn = "wpmChildId",
        entityColumn = "workPerformedId"
    )
    val workPerformedChild: WorkPerformed
) : Parcelable