package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.WorkDates

@Parcelize
data class WorkOrderHistoryTimeWorkedCombined(
    @Embedded
    var timeWorked: WorkOrderHistoryTimeWorked,
    @Relation(
        entity = WorkOrderHistory::class,
        parentColumn = "wohtHistoryId",
        entityColumn = "woHistoryId"
    )
    var workOrderHistory: WorkOrderHistoryCombined,
    @Relation(
        entity = WorkDates::class,
        parentColumn = "wohtDateId",
        entityColumn = "workDateId",
    )
    var workDate: WorkDates
) : Parcelable
