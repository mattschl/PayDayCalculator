package ms.mattschlenkrich.paycalculator.database.repository

import ms.mattschlenkrich.paycalculator.database.PayDatabase
import ms.mattschlenkrich.paycalculator.database.model.workorder.Areas
import ms.mattschlenkrich.paycalculator.database.model.workorder.JobSpec
import ms.mattschlenkrich.paycalculator.database.model.workorder.Material
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryMaterialCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed

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
    ) = db.getWorkOrderDao().updateWorkOrder(
        workOrderId, workOrderNumber, employerId, address, description, isDeleted, updateTime
    )

//    suspend fun deleteWorkOrder(workOrderId: Long, updateTime: String) =
//        db.getWorkOrderDao().deleteWorkOrder(workOrderId, updateTime)
//
//    suspend fun deleteWorkOrder(workOrderNumber: String, updateTime: String) =
//        db.getWorkOrderDao().deleteWorkOrder(workOrderNumber, updateTime)

    fun getWorkOrder(workOrderId: Long) = db.getWorkOrderDao().getWorkOrder(workOrderId)

    fun getWorkOrder(workOrderNum: String) = db.getWorkOrderDao().getWorkOrder(workOrderNum)

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
    ) = db.getWorkOrderDao().updateWorkOrderHistory(
        historyID,
        workOrderId,
        workDateId,
        regHours,
        otHours,
        dblOtHours,
        note,
        isDeleted,
        updateTime
    )

    suspend fun deleteWorkOrderHistory(historyID: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkOrderHistory(historyID, updateTime)

    suspend fun deleteWorkOrderHistory(historyId: Long) =
        db.getWorkOrderDao().deleteWorkOrderHistory(historyId)

    fun getWorkOrderHistoriesByDate(workDateId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoriesByDate(workDateId)

    fun getWorkOrderHistoriesById(historyId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoriesById(historyId)

    fun getWorkOrderHistoryWithDatedById(historyID: Long) =
        db.getWorkOrderDao().getWorkOrderHistoryWithDateById(historyID)

    fun getWorkOrderHistoryCombined(historyId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoryCombined(historyId)

    suspend fun insertTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        db.getWorkOrderDao().insertTimeWorked(timeWorked)

    fun getTimeWorkedPerDay(workDateId: Long) =
        db.getWorkOrderDao().getTimeWorkedPerDay(workDateId)

    fun getTimeWorkedForWorkOrderHistory(historyId: Long) =
        db.getWorkOrderDao().getTimeWorkedForWorkOrderHistory(historyId)

    fun getWorkOrderHistoriesByWorkOrder(workOrderId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoriesByWorkOrder(workOrderId)

    fun getWorkPerformedHistoryById(historyWorkPerformedId: Long) =
        db.getWorkOrderDao().getWorkPerformedHistoryById(historyWorkPerformedId)

    fun getWorkOrderHistory(historyID: Long) = db.getWorkOrderDao().getWorkOrderHistory(historyID)

    suspend fun deleteWorkOrderHistoryByWorkDateId(workDateId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkOrderHistoryByWorkDateId(workDateId, updateTime)

    suspend fun insertJobSpec(jobSpec: JobSpec) = db.getWorkOrderDao().insertJobSpec(jobSpec)

    suspend fun updateJobSpec(jobSpec: JobSpec) = db.getWorkOrderDao().updateJobSpec(jobSpec)

    fun getJobSpecs() = db.getWorkOrderDao().getJobSpecsAll()

    fun searchJobSpecs(query: String) = db.getWorkOrderDao().searchJobSpecs(query)

    suspend fun insertWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) =
        db.getWorkOrderDao().insertWorkOrderJobSpec(workOrderJobSpec)

    suspend fun updateWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) =
        db.getWorkOrderDao().updateWorkOrderJobSpec(workOrderJobSpec)

    suspend fun deleteWorkOrderJobSpec(workOrderJobSpecId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkOrderJobSpec(workOrderJobSpecId, updateTime)

    suspend fun deleteWorkOrderJobSpec(workOrderJobSpecId: Long) =
        db.getWorkOrderDao().deleteWorkOrderJobSpec(workOrderJobSpecId)

    fun getWorkOrderJobSpecs(workOrderId: Long) =
        db.getWorkOrderDao().getWorkOrderJobSpecs(workOrderId)

    fun getWorkOrderJobSpec(workOrderJobSpecId: Long) =
        db.getWorkOrderDao().getWorkOrderJobSpec(workOrderJobSpecId)

    suspend fun insertWorkPerformed(workPerformed: WorkPerformed) =
        db.getWorkOrderDao().insertWorkPerformed(workPerformed)

    suspend fun deleteWorkPerformed(workPerformedId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkPerformed(workPerformedId, updateTime)

    fun getWorkPerformedAll() = db.getWorkOrderDao().getWorkPerformedAll()

    fun searchFromWorkPerformed(query: String) = db.getWorkOrderDao().searchFromWorkPerformed(query)

    fun getWorkPerformed(description: String) = db.getWorkOrderDao().getWorkPerformed(description)

    fun getWorkPerformed(workPerformedId: Long) =
        db.getWorkOrderDao().getWorkPerformed(workPerformedId)

    suspend fun updateWorkPerformed(workPerformed: WorkPerformed) =
        db.getWorkOrderDao().updateWorkPerformed(workPerformed)

    suspend fun insertWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    ) = db.getWorkOrderDao().insertWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed)

    suspend fun updateWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    ) = db.getWorkOrderDao().updateWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed)

    suspend fun removeAllWorkPerformedFromWorkOrderHistory(historyId: Long) =
        db.getWorkOrderDao().removeAllWorkPerformedFromWorkOrderHistory(historyId)

    suspend fun deleteWorkOrderHistoryWorkPerformed(historyWorkPerformedId: Long) =
        db.getWorkOrderDao().deleteWorkOrderHistoryWorkPerformed(historyWorkPerformedId)

    fun getWorkPerformedCombinedByWorkOrderHistory(historyId: Long) =
        db.getWorkOrderDao().getWorkPerformedByWorkOrderHistory(historyId)

    suspend fun insertMaterial(material: Material) = db.getWorkOrderDao().insertMaterial(material)

    suspend fun updateMaterial(material: Material) = db.getWorkOrderDao().updateMaterial(material)

    fun getMaterialsList() = db.getWorkOrderDao().getMaterialsList()

    fun searchMaterials(query: String) = db.getWorkOrderDao().searchMaterials(query)

    fun getMaterial(materialId: Long) = db.getWorkOrderDao().getMaterial(materialId)

    suspend fun deleteMaterial(materialId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteMaterial(materialId, updateTime)

    suspend fun insertWorkOrderHistoryMaterial(
        workOrderHistoryMaterial: WorkOrderHistoryMaterial
    ) = db.getWorkOrderDao().insertWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun removeWorkOrderHistoryMaterial(
        workOrderHistoryMaterialId: Long
    ) = db.getWorkOrderDao().removeWorkOrderHistoryMaterial(workOrderHistoryMaterialId)

    suspend fun updateWorkOrderHistoryMaterial(
        workOrderHistoryMaterial: WorkOrderHistoryMaterial
    ) = db.getWorkOrderDao().updateWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun deleteWorkOrderHistoryMaterial(
        historyMaterialId: Long, updateTime: String
    ) = db.getWorkOrderDao().deleteWorkOrderHistoryMaterial(historyMaterialId, updateTime)

    fun getMaterialsByHistory(historyId: Long) =
        db.getWorkOrderDao().getMaterialsByHistory(historyId)

    fun getMaterialsFromHistoryId(historyId: Long) =
        db.getWorkOrderDao().getMaterialsFromHistoryId(historyId)

    fun getMaterialsHistoryByWorkOrderId(workOrderId: Long) =
        db.getWorkOrderDao().getMaterialsHistoryByWorkOrderId(workOrderId)

    fun getWorkOrderHistoryMaterialCombined(woHistoryMaterialId: Long): WorkOrderHistoryMaterialCombined =
        db.getWorkOrderDao().getWorkOrderHistoryMaterialCombined(woHistoryMaterialId)

    suspend fun removeAllMaterialsFromWorkOrderHistory(historyId: Long) =
        db.getWorkOrderDao().removeAllMaterialsFromWorkOrderHistory(historyId)

    suspend fun insertArea(area: Areas) = db.getWorkOrderDao().insertArea(area)

    suspend fun updateArea(area: Areas) = db.getWorkOrderDao().updateArea(area)

    fun getAreasList() = db.getWorkOrderDao().getAreasList()

    fun getArea(areaId: Long) = db.getWorkOrderDao().getArea(areaId)

    fun getArea(areaName: String) = db.getWorkOrderDao().getArea(areaName)

    fun searchAreas(query: String) = db.getWorkOrderDao().searchAreas(query)
}