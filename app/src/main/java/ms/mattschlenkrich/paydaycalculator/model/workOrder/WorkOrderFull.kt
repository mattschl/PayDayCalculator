package ms.mattschlenkrich.paydaycalculator.model.workOrder

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers

@Parcelize
data class WorkOrderFull(
    @Embedded
    var workOrder: WorkOrder,
    @Relation(
        entity = Employers::class,
        parentColumn = "woEmployerId",
        entityColumn = "employerId"
    )
    var employer: Employers
) : Parcelable
