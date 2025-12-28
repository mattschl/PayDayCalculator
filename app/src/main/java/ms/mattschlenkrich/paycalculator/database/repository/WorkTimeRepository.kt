package ms.mattschlenkrich.paycalculator.database.repository

import ms.mattschlenkrich.paycalculator.database.PayDatabase
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorked

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

    suspend fun getExistingHistoriesWithTimes(workDateId: Long) =
        db.getWorkTimeDao().getExistingHistoriesWithTimes(workDateId)

    suspend fun getWorkOrders(employerId: Long) =
        db.getWorkTimeDao().getWorkOrders(employerId)

    suspend fun getWorkDate(workDateId: Long) =
        db.getWorkTimeDao().getWorkDate(workDateId)
}