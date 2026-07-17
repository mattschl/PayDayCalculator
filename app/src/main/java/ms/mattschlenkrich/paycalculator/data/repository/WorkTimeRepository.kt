package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked

class WorkTimeRepository(private val db: PayDatabase) {
    suspend fun updateWorkDate(workDate: WorkDates) = db.getWorkTimeDao().updateWorkDate(workDate)
    fun getTimesWorkedByDate(workDateId: Long) =
        db.getWorkTimeDao().getTimesWorkedByDate(workDateId)

    fun getWorkOrderNumbers(workDateId: Long) = db.getWorkTimeDao().getWorkOrderNumbers(workDateId)

    suspend fun insertTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        db.getWorkOrderTimeDao().insertTimeWorked(timeWorked)

    suspend fun updateTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        db.getWorkOrderTimeDao().updateTimeWorked(timeWorked)

    suspend fun deleteTimeWorked(timeWorkedId: Long, updateTime: String) =
        db.getWorkOrderTimeDao().deleteTimeWorked(timeWorkedId, updateTime)

    fun getTimeWorkedPerDay(workDateId: Long) =
        db.getWorkOrderTimeDao().getTimeWorkedPerDay(workDateId)

    fun getTimeWorkedForWorkOrderHistory(historyId: Long) =
        db.getWorkOrderTimeDao().getTimeWorkedForWorkOrderHistory(historyId)
}