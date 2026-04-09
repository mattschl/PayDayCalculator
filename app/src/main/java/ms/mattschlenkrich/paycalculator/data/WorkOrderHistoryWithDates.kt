package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.WorkDates

@Parcelize
data class WorkOrderHistoryWithDates(
    @Embedded
    var history: WorkOrderHistory,
    @Relation(
        entity = WorkDates::class,
        parentColumn = "woHistoryWorkDateId",
        entityColumn = "workDateId"
    )
    var workDate: WorkDates,
    @Relation(
        entity = WorkOrder::class,
        parentColumn = "woHistoryWorkOrderId",
        entityColumn = "workOrderId"
    )
    var workOrder: WorkOrder
) : Parcelable
