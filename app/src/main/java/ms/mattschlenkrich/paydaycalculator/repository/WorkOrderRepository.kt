package ms.mattschlenkrich.paydaycalculator.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrderHistory

class WorkOrderRepository(private val db: PayDatabase) {
    suspend fun insertWorkOrder(workOrder: WorkOrder) =
        db.getWorkOrderDao().insertWorkOrder(workOrder)

    suspend fun updateWorkOrder(
        workOrderId: String,
        employerId: Long,
        address: String,
        description: String,
        isDeleted: Boolean,
        updateTime: String,
    ) =
        db.getWorkOrderDao().updateWorkOrder(
            workOrderId, employerId, address,
            description, isDeleted, updateTime
        )

    suspend fun deleteWorkOrder(workOrderId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkOrder(workOrderId, updateTime)

    fun getWorkOrder(workOrderNum: String) =
        db.getWorkOrderDao().getWorkOrder(workOrderNum)

    fun getWorkOrdersByEmployerId(employerId: Long) =
        db.getWorkOrderDao().getWorkOrdersByEmployerId(employerId)

    suspend fun insertWorkOrderHistory(history: WorkOrderHistory) =
        db.getWorkOrderDao().insertWorkOrderHistory(history)

    suspend fun updateWorkOrderHistory(
        historyID: Long,
        workOrderId: String,
        workDateId: Long,
        regHours: Double,
        otHours: Double,
        dblOtHours: Double,
        note: String?,
        isDeleted: Boolean,
        updateTime: String
    ) =
        db.getWorkOrderDao().updateWorkOrderHistory(
            historyID, workOrderId, workDateId, regHours,
            otHours, dblOtHours, note, isDeleted, updateTime
        )

    suspend fun deleteWorkOrderHistory(historyID: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkOrderHistory(historyID, updateTime)

    fun getWorkOrderHistories(workDateId: Long) =
        db.getWorkOrderDao().getWorkOrderHistories(workDateId)

    fun getWorkOrderHistory(historyID: Long) =
        db.getWorkOrderDao().getWorkOrderHistory(historyID)
}