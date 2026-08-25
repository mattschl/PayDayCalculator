package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryExpense

@Parcelize
data class WorkOrderHistoryCombined(
    @Embedded
    val workOrderHistory: WorkOrderHistory,
    @Relation(
        entity = WorkOrder::class,
        parentColumn = "woHistoryWorkOrderId",
        entityColumn = "workOrderId"
    )
    val workOrder: WorkOrder,
    @Relation(
        entity = WorkDates::class,
        parentColumn = "woHistoryWorkDateId",
        entityColumn = "workDateId"
    )
    var workDate: WorkDates,
    @Relation(
        entity = WorkOrderHistoryExpense::class,
        parentColumn = "woHistoryId",
        entityColumn = "woheHistoryId"
    )
    val expenses: List<WorkOrderHistoryExpense>

) : Parcelable