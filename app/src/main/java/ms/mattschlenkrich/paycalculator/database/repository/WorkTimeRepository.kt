package ms.mattschlenkrich.paycalculator.database.repository

import ms.mattschlenkrich.paycalculator.database.PayDatabase
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorked

class WorkTimeRepository(private val db: PayDatabase) {
    suspend fun insertWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) =
        db.getWorkTimeDao().insertWorkTime(workOrderHistoryTimeWorked)

    suspend fun deleteWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) =
        db.getWorkTimeDao().deleteWorkTime(workOrderHistoryTimeWorked)

    suspend fun updateWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) =
        db.getWorkTimeDao().updateWorkTime(workOrderHistoryTimeWorked)

    suspend fun getExistingHistories(workDateId: Long) =
        db.getWorkTimeDao().getExistingHistories(workDateId)

    suspend fun getExistingHistoriesWithTimes(workDateId: Long) =
        db.getWorkTimeDao().getExistingHistoriesWithTimes(workDateId)

    suspend fun getWorkOrders(employerId: Long) =
        db.getWorkTimeDao().getWorkOrders(employerId)

}