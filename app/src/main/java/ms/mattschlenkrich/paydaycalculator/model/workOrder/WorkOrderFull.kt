package ms.mattschlenkrich.paydaycalculator.model.workOrder

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers

@Parcelize
data class WorkOrderFull(
    @Embedded
    val workOrder: WorkOrder,
    @Relation(
        entity = Employers::class,
        parentColumn = "employerId",
        entityColumn = "woEmployerId"
    )
    val employer: Employers
) : Parcelable
