package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.Areas
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed

@Parcelize
data class WorkOrderHistoryWorkPerformedCombined(
    @Embedded
    val workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed,
    @Relation(
        entity = WorkPerformed::class,
        parentColumn = "wowpWorkPerformedId",
        entityColumn = "workPerformedId"
    )
    val workPerformed: WorkPerformed,
    @Relation(
        entity = Areas::class,
        parentColumn = "wowpAreaId",
        entityColumn = "areaId"
    )
    val area: Areas?,
) : Parcelable