package ms.mattschlenkrich.paycalculator.data

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.WorkDates
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorked

class WorkTimeRepository(private val db: PayDatabase) {
    suspend fun insertWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) =
        db.getWorkTimeDao().insertWorkTime(workOrderHistoryTimeWorked)

    suspend fun deleteWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) =
        db.getWorkTimeDao().deleteWorkTime(workOrderHistoryTimeWorked)

    suspend fun updateWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) =
        db.getWorkTimeDao().updateWorkTime(workOrderHistoryTimeWorked)

    suspend fun updateWorkDate(workDates: WorkDates) =
        db.getWorkTimeDao().updateWorkDate(workDates)

    suspend fun updateWorkOrderHistory(workOrderHistory: WorkOrderHistory) =
        db.getWorkTimeDao().updateWorkOrderHistory(workOrderHistory)

    suspend fun getExistingHistories(workDateId: Long) =
        db.getWorkTimeDao().getExistingHistories(workDateId)

    fun getExistingHistoriesWithTimes(workDateId: Long) =
        db.getWorkTimeDao().getExistingHistoriesWithTimes(workDateId)

    fun getTimesWorkedByDate(workDateId: Long) =
        db.getWorkTimeDao().getTimesWorkedByDate(workDateId)

    suspend fun getWorkOrders(employerId: Long) =
        db.getWorkTimeDao().getWorkOrders(employerId)

    fun getWorkOrderNumbers(employerId: Long) =
        db.getWorkTimeDao().getWorkOrderNumbers(employerId)


    suspend fun getWorkDate(workDateId: Long) =
        db.getWorkTimeDao().getWorkDate(workDateId)
}