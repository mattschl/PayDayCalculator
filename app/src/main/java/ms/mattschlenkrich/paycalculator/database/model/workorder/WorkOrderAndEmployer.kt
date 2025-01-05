package ms.mattschlenkrich.paycalculator.database.model.workorder

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers

@Parcelize
data class WorkOrderAndEmployer(
    @Embedded
    var workOrder: WorkOrder,
    @Relation(
        entity = Employers::class,
        parentColumn = "woEmployerId",
        entityColumn = "employerId"
    )
    var employer: Employers
) : Parcelable
