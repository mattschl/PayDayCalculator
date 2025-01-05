package ms.mattschlenkrich.paycalculator.database.model.workorder

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkOrderHistoryWorkPerformedCombined(
    @Embedded
    val workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed,
    @Relation(
        entity = WorkPerformed::class,
        parentColumn = "wowpWorkPerformedId",
        entityColumn = "workPerformedId"
    )
    val workPerformed: WorkPerformed
) : Parcelable
