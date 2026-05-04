package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked

class WorkTimeRepository(private val db: PayDatabase) {
    suspend fun insertWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) =
        db.getWorkTimeDao().insertWorkTime(workOrderHistoryTimeWorked)

    suspend fun deleteWorkTime(id: Long, updateTime: String) =
        db.getWorkTimeDao().deleteWorkTime(id, updateTime)

    suspend fun updateWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) =
        db.getWorkTimeDao().updateWorkTime(workOrderHistoryTimeWorked)

    suspend fun updateWorkDate(workDates: WorkDates) =
        db.getWorkTimeDao().updateWorkDate(workDates)

    suspend fun updateWorkOrderHistory(workOrderHistory: WorkOrderHistory) =
        db.getWorkTimeDao().updateWorkOrderHistory(workOrderHistory)

    fun getExistingHistories(workDateId: Long) =
        db.getWorkTimeDao().getExistingHistories(workDateId)

    fun getExistingHistoriesWithTimes(workDateId: Long) =
        db.getWorkTimeDao().getExistingHistoriesWithTimes(workDateId)

    fun getTimesWorkedByDate(workDateId: Long) =
        db.getWorkTimeDao().getTimesWorkedByDate(workDateId)

    fun getWorkOrders(employerId: Long) =
        db.getWorkTimeDao().getWorkOrders(employerId)

    fun getWorkOrderNumbers(employerId: Long) =
        db.getWorkTimeDao().getWorkOrderNumbers(employerId)


    fun getWorkDate(workDateId: Long) =
        db.getWorkTimeDao().getWorkDate(workDateId)
}