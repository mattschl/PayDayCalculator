package ms.mattschlenkrich.paydaycalculator.model.workOrder

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDateFull

@Parcelize
data class WorkOrderHistoryFull(
    @Embedded
    val history: WorkOrderHistory,
    @Relation(
        entity = WorkDateFull::class,
        parentColumn = "workDateId",
        entityColumn = "woHistoryWorkDateId"
    )
    val workDateFull: WorkDateFull,
    @Relation(
        entity = WorkOrder::class,
        parentColumn = "workOrderId",
        entityColumn = "woHistoryWorkOrderId"
    )
    val workOrder: WorkOrder
) : Parcelable
