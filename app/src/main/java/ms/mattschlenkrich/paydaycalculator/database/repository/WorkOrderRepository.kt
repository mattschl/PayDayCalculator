package ms.mattschlenkrich.paydaycalculator.database.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.JobSpec
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderJobSpec

class WorkOrderRepository(private val db: PayDatabase) {
    suspend fun insertWorkOrder(workOrder: WorkOrder) =
        db.getWorkOrderDao().insertWorkOrder(workOrder)

    suspend fun updateWorkOrder(
        workOrderId: Long,
        workOrderNumber: String,
        employerId: Long,
        address: String,
        description: String,
        isDeleted: Boolean,
        updateTime: String,
    ) =
        db.getWorkOrderDao().updateWorkOrder(
            workOrderId, workOrderNumber, employerId, address,
            description, isDeleted, updateTime
        )

//    suspend fun deleteWorkOrder(workOrderId: Long, updateTime: String) =
//        db.getWorkOrderDao().deleteWorkOrder(workOrderId, updateTime)
//
//    suspend fun deleteWorkOrder(workOrderNumber: String, updateTime: String) =
//        db.getWorkOrderDao().deleteWorkOrder(workOrderNumber, updateTime)

    fun getWorkOrder(workOrderId: Long) =
        db.getWorkOrderDao().getWorkOrder(workOrderId)

    fun getWorkOrder(workOrderNum: String) =
        db.getWorkOrderDao().getWorkOrder(workOrderNum)

    fun getWorkOrdersByEmployerId(employerId: Long) =
        db.getWorkOrderDao().getWorkOrdersByEmployerId(employerId)

    fun searchWorkOrders(employerId: Long, query: String) =
        db.getWorkOrderDao().searchWorkOrders(employerId, query)

    suspend fun insertWorkOrderHistory(history: WorkOrderHistory) =
        db.getWorkOrderDao().insertWorkOrderHistory(history)

    suspend fun updateWorkOrderHistory(
        historyID: Long,
        workOrderId: Long,
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

    fun getWorkOrderHistoriesByDate(workDateId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoriesByDate(workDateId)

    fun getWorkOrderHistoriesById(workOrderId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoriesById(workOrderId)

    fun getWorkOrderHistory(historyID: Long) =
        db.getWorkOrderDao().getWorkOrderHistory(historyID)

    suspend fun deleteWorkOrderHistoryByWorkDateId(
        workDateId: Long, updateTime: String
    ) = db.getWorkOrderDao().deleteWorkOrderHistoryByWorkDateId(
        workDateId, updateTime
    )

    suspend fun insertJobSpec(jobSpec: JobSpec) =
        db.getWorkOrderDao().insertJobSpec(jobSpec)

    suspend fun updateJobSpec(jobSpec: JobSpec) =
        db.getWorkOrderDao().updateJobSpec(jobSpec)

    fun getJobSpecs() =
        db.getWorkOrderDao().getJobSpecsAll()

    suspend fun insertWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) =
        db.getWorkOrderDao().insertWorkOrderJobSpec(workOrderJobSpec)

    suspend fun deleteWorkOrderJobSpec(
        workOrderJobSpecId: Long, updateTime: String
    ) = db.getWorkOrderDao().deleteWorkOrderJobSpec(
        workOrderJobSpecId, updateTime
    )

    fun getWorkOrderJobSpecs(workOrderId: Long) =
        db.getWorkOrderDao().getWorkOrderJobSpecs(workOrderId)
}