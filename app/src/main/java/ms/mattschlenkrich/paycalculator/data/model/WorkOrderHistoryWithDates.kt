package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory

@Parcelize
data class WorkOrderHistoryWithDates(
    @Embedded
    val history: WorkOrderHistory,
    @Relation(
        entity = WorkDates::class,
        parentColumn = "woHistoryWorkDateId",
        entityColumn = "workDateId"
    )
    val workDate: WorkDates,
    @Relation(
        entity = WorkOrder::class,
        parentColumn = "woHistoryWorkOrderId",
        entityColumn = "workOrderId"
    )
    val workOrder: WorkOrder
) : Parcelable