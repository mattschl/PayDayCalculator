package ms.mattschlenkrich.paycalculator.database.model.workorder.merged

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed


@Parcelize
data class WorkPerformedAndChildren(
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
    val workPerformedChildren: List<WorkPerformed>
) : Parcelable