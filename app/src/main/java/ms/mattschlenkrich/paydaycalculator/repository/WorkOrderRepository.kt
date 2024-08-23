package ms.mattschlenkrich.paydaycalculator.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrderHistory

class WorkOrderRepository(private val db: PayDatabase) {
    suspend fun insertWorkOrder(workOrder: WorkOrder) =
        db.getWorkOrderDao().insertWorkOrder(workOrder)

    suspend fun updateWorkOrder(workOrder: WorkOrder) =
        db.getWorkOrderDao().updateWorkOrder(workOrder)

    suspend fun deleteWorkOrder(workOrderId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkOrder(workOrderId, updateTime)

    fun getWorkOrder(workOrderNum: String) =
        db.getWorkOrderDao().getWorkOrder(workOrderNum)

    fun getWorkOrdersByEmployerId(employerId: Long) =
        db.getWorkOrderDao().getWorkOrdersByEmployerId(employerId)

    suspend fun insertWorkOrderHistory(history: WorkOrderHistory) =
        db.getWorkOrderDao().insertWorkOrderHistory(history)

    suspend fun updateWorkOrderHistory(history: WorkOrderHistory) =
        db.getWorkOrderDao().updateWorkOrderHistory(history)

    suspend fun deleteWorkOrderHistory(historyID: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkOrderHistory(historyID, updateTime)

    fun getWorkOrderHistory(workDateId: Long) =
        db.getWorkOrderDao().getWorkOrderHistory(workDateId)
}