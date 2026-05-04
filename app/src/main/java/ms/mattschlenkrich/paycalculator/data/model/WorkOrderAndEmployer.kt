package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder

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