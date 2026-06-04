package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.Areas
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.entity.JobSpecMerged
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformedMerged
import ms.mattschlenkrich.paycalculator.data.repository.WorkOrderRepository

class WorkOrderViewModel(
    app: Application,
    private val workOrderRepository: WorkOrderRepository
) : AndroidViewModel(app) {

    suspend fun insertWorkOrder(workOrder: WorkOrder) =
        workOrderRepository.insertWorkOrder(workOrder)

    suspend fun updateWorkOrder(workOrder: WorkOrder) =
        workOrderRepository.updateWorkOrder(
            workOrder.workOrderId,
            workOrder.woNumber,
            workOrder.woEmployerId,
            workOrder.woAddress,
            workOrder.woDescription,
            workOrder.woDeleted,
            workOrder.woUpdateTime
        )

    suspend fun findWorkOrder(workOrderNum: String, employerId: Long) =
        workOrderRepository.findWorkOrder(workOrderNum, employerId)

    fun getWorkOrdersByEmployerId(employerId: Long) =
        workOrderRepository.getWorkOrdersByEmployerId(employerId)

    fun searchWorkOrders(employerId: Long, query: String) =
        workOrderRepository.searchWorkOrders(employerId, query)

    fun getUniqueAddresses(employerId: Long) =
        workOrderRepository.getUniqueAddresses(employerId)

    suspend fun insertWorkOrderHistory(history: WorkOrderHistory) =
        workOrderRepository.insertWorkOrderHistory(history)

    suspend fun updateWorkOrderHistory(history: WorkOrderHistory) =
        workOrderRepository.updateWorkOrderHistory(history)

    suspend fun getWorkOrderHistory(workOrderId: Long, workDateId: Long) =
        workOrderRepository.getWorkOrderHistory(workOrderId, workDateId)

    suspend fun deleteWorkOrderHistory(historyId: Long) =
        workOrderRepository.deleteWorkOrderHistory(
            historyId,
            DateFunctions().getCurrentUTCTimeAsString()
        )

    fun getWorkOrderHistoriesByDate(workDateId: Long) =
        workOrderRepository.getWorkOrderHistoriesByDate(workDateId)

    fun getWorkOrderHistory(historyId: Long) =
        workOrderRepository.getWorkOrderHistory(historyId)

    fun getWorkOrderHistoryCombined(historyId: Long) =
        workOrderRepository.getWorkOrderHistoryCombined(historyId)

    fun getWorkOrderSummary(workOrderId: Long) =
        workOrderRepository.getWorkOrderSummary(workOrderId)

    fun getWorkOrderMaterialsSummary(workOrderId: Long) =
        workOrderRepository.getWorkOrderMaterialsSummary(workOrderId)

    fun getWorkOrderWorkPerformedSummary(workOrderId: Long) =
        workOrderRepository.getWorkOrderWorkPerformedSummary(workOrderId)

    val jobSpecsAll = workOrderRepository.getJobSpecs()
    val areasList = workOrderRepository.getAreasList()
    val workPerformedAll = workOrderRepository.getWorkPerformedAll()
    val materialsList = workOrderRepository.getMaterialsList()

    suspend fun insertWorkOrderHistoryTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        workOrderRepository.insertTimeWorked(timeWorked)

    suspend fun updateWorkOrderHistoryTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        workOrderRepository.updateTimeWorked(timeWorked)

    fun getWorkOrderHistoryTimesByHistory(historyId: Long) =
        workOrderRepository.getTimeWorkedForWorkOrderHistory(historyId)

    suspend fun deleteTimeWorked(timeWorkedId: Long, updateTime: String) =
        workOrderRepository.deleteTimeWorked(timeWorkedId, updateTime)

    fun getTimeWorkedPerDay(workDateId: Long) =
        workOrderRepository.getTimeWorkedPerDay(workDateId)

    fun getTimeWorkedForWorkOrderHistory(historyId: Long) =
        workOrderRepository.getTimeWorkedForWorkOrderHistory(historyId)

    fun getWorkPerformedHistoryById(historyWorkPerformedId: Long) =
        workOrderRepository.getWorkPerformedHistoryById(historyWorkPerformedId)

    fun getWorkOrderHistoriesByWorkOrder(workOrderId: Long) =
        workOrderRepository.getWorkOrderHistoriesByWorkOrder(workOrderId)

    fun getJobSpec(jobSpecId: Long) = workOrderRepository.getJobSpec(jobSpecId)

    fun getJobSpecAndChildList(jobSpecId: Long) =
        workOrderRepository.getJobSpecAndChildList(jobSpecId)

    suspend fun insertJobSpecMerged(jobSpecMerged: JobSpecMerged) =
        workOrderRepository.insertJobSpecMerged(jobSpecMerged)

    suspend fun deleteJobSpecMerged(jobSpecMergedId: Long, updateTime: String) =
        workOrderRepository.deleteJobSpecMerged(jobSpecMergedId, updateTime)

    suspend fun updateJobSpecMerged(oldJobSpecId: Long, newJobSpecId: Long) =
        workOrderRepository.updateJobSpecMerged(oldJobSpecId, newJobSpecId)

    suspend fun updateJobSpec(jobSpec: JobSpec) =
        workOrderRepository.updateJobSpec(jobSpec)

    suspend fun deleteJobSpec(jobSpecId: Long, updateTime: String) =
        workOrderRepository.deleteJobSpec(jobSpecId, updateTime)

    suspend fun getJobSpecsAllSync() = workOrderRepository.getJobSpecsAllSync()

    fun searchJobSpecs(query: String) = workOrderRepository.searchJobSpecs(query)

    suspend fun insertWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) =
        workOrderRepository.insertWorkOrderJobSpec(workOrderJobSpec)

    suspend fun updateWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) =
        workOrderRepository.updateWorkOrderJobSpec(workOrderJobSpec)

    fun getWorkOrderJobSpecs(workOrderId: Long) =
        workOrderRepository.getWorkOrderJobSpecs(workOrderId)

    fun getWorkOrderJobSpec(workOrderJobSpecId: Long) =
        workOrderRepository.getWorkOrderJobSpec(workOrderJobSpecId)

    suspend fun getOrCreateJobSpec(name: String): JobSpec {
        val existing = getJobSpecsAllSync().find {
            it.jsName.trim().equals(name.trim(), ignoreCase = true)
        }
        if (existing != null) return existing

        val nf = NumberFunctions()
        val df = DateFunctions()
        val newJobSpec = JobSpec(
            nf.generateRandomIdAsLong(),
            name.trim(),
            false,
            df.getCurrentUTCTimeAsString()
        )
        workOrderRepository.insertJobSpec(newJobSpec)
        return newJobSpec
    }

    suspend fun getOrCreateArea(name: String): Areas? {
        if (name.isBlank()) return null
        val existing = getAreasListSync().find {
            it.areaName.trim().equals(name.trim(), ignoreCase = true)
        }
        if (existing != null) return existing

        val nf = NumberFunctions()
        val df = DateFunctions()
        val newArea = Areas(
            nf.generateRandomIdAsLong(),
            name.trim(),
            false,
            df.getCurrentUTCTimeAsString()
        )
        workOrderRepository.insertArea(newArea)
        return newArea
    }

    suspend fun getOrCreateWorkPerformed(description: String): WorkPerformed? {
        if (description.isBlank()) return null
        val existing = getWorkPerformedAllSync().find {
            it.wpDescription.trim().equals(description.trim(), ignoreCase = true)
        }
        if (existing != null) return existing

        val nf = NumberFunctions()
        val df = DateFunctions()
        val newWorkPerformed = WorkPerformed(
            nf.generateRandomIdAsLong(),
            description.trim(),
            false,
            df.getCurrentUTCTimeAsString()
        )
        workOrderRepository.insertWorkPerformed(newWorkPerformed)
        return newWorkPerformed
    }

    suspend fun getOrCreateMaterial(name: String): Material? {
        if (name.isBlank()) return null
        val existing = workOrderRepository.getMaterialsListSync().find {
            it.mName.trim().equals(name.trim(), ignoreCase = true)
        }
        if (existing != null) return existing

        val nf = NumberFunctions()
        val df = DateFunctions()
        val newMaterial = Material(
            nf.generateRandomIdAsLong(),
            name.trim(),
            0.0,
            0.0,
            false,
            df.getCurrentUTCTimeAsString()
        )
        workOrderRepository.insertMaterial(newMaterial)
        return newMaterial
    }

    suspend fun deleteWorkPerformed(workPerformedId: Long, updateTime: String) =
        workOrderRepository.deleteWorkPerformed(workPerformedId, updateTime)

    suspend fun getWorkPerformedAllSync() = workOrderRepository.getWorkPerformedAllSync()

    fun getWorkPerformedAndChildList(workPerformedId: Long) =
        workOrderRepository.getWorkPerformedAndChildList(workPerformedId)

    suspend fun insertWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged) =
        workOrderRepository.insertWorkPerformedMerged(workPerformedMerged)

    suspend fun deleteWorkPerformedMerged(workPerformedMergedId: Long, updateTime: String) =
        workOrderRepository.deleteWorkPerformedMerged(
            workPerformedMergedId,
            updateTime
        )

    suspend fun updateWorkPerformedMerged(oldWorkPerformedId: Long, newWorkPerformedId: Long) =
        workOrderRepository.updateWorkPerformedMerged(oldWorkPerformedId, newWorkPerformedId)

    fun searchFromWorkPerformed(query: String) = workOrderRepository.searchFromWorkPerformed(query)

    suspend fun getWorkPerformedSync(description: String) =
        workOrderRepository.getWorkPerformedSync(description)

    fun getWorkPerformed(workPerformedId: Long) =
        workOrderRepository.getWorkPerformed(workPerformedId)

    suspend fun updateWorkPerformed(workPerformed: WorkPerformed) =
        workOrderRepository.updateWorkPerformed(workPerformed)

    suspend fun insertWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    ) = workOrderRepository.insertWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed
    )

    suspend fun updateWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    ) = workOrderRepository.updateWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed
    )

    suspend fun removeAllWorkPerformedFromWorkOderHistory(historyId: Long) =
        workOrderRepository.removeAllWorkPerformedFromWorkOrderHistory(
            historyId,
            DateFunctions().getCurrentUTCTimeAsString()
        )

    fun getWorkPerformedCombinedByWorkOrderHistory(historyId: Long) =
        workOrderRepository.getWorkPerformedCombinedByWorkOrderHistory(historyId)

    suspend fun updateMaterial(material: Material) =
        workOrderRepository.updateMaterial(material)

    fun getMaterialAndChildList(materialId: Long) =
        workOrderRepository.getMaterialAndChildList(materialId)

    fun searchMaterials(query: String) = workOrderRepository.searchMaterials(query)

    fun getMaterial(materialId: Long) = workOrderRepository.getMaterial(materialId)

    suspend fun getMaterialSync(materialId: Long) =
        workOrderRepository.getMaterialSync(materialId)

    suspend fun getMaterialSync(mName: String) =
        workOrderRepository.getMaterialSync(mName)

    suspend fun updateMaterialMerged(oldMaterialID: Long, newMaterialID: Long, updateTime: String) =
        workOrderRepository.updateMaterialMerged(
            oldMaterialID,
            newMaterialID,
            updateTime
        )

    suspend fun deleteMaterialMerged(childId: Long, updateTime: String) =
        workOrderRepository.deleteMaterialMerged(childId, updateTime)

    suspend fun insertMaterialMerged(materialMerged: MaterialMerged) =
        workOrderRepository.insertMaterialMerged(materialMerged)

    suspend fun deleteMaterial(materialId: Long, updateTime: String) =
        workOrderRepository.deleteMaterial(materialId, updateTime)

    suspend fun insertWorkOrderHistoryMaterial(workOrderHistoryMaterial: WorkOrderHistoryMaterial) =
        workOrderRepository.insertWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun removeAllMaterialsFromWorkOrderHistory(historyId: Long) =
        workOrderRepository.removeAllMaterialsFromWorkOrderHistory(
            historyId,
            DateFunctions().getCurrentUTCTimeAsString()
        )

    suspend fun deleteWorkOrderHistoryWorkPerformed(
        historyWorkPerformedId: Long,
        updateTime: String = DateFunctions()
            .getCurrentUTCTimeAsString()
    ) = workOrderRepository.deleteWorkOrderHistoryWorkPerformed(historyWorkPerformedId, updateTime)

    suspend fun updateWorkOrderHistoryMaterial(workOrderHistoryMaterial: WorkOrderHistoryMaterial) =
        workOrderRepository.updateWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun deleteWorkOrderHistoryMaterial(
        historyMaterialId: Long, updateTime: String
    ) = workOrderRepository.deleteWorkOrderHistoryMaterial(historyMaterialId, updateTime)

    fun getMaterialsByHistory(historyId: Long) =
        workOrderRepository.getMaterialsByHistory(historyId)

    suspend fun getWorkOrderHistoryMaterialCombined(woHistoryMaterialId: Long) =
        workOrderRepository.getWorkOrderHistoryMaterialCombined(woHistoryMaterialId)

    suspend fun updateArea(area: Areas) =
        workOrderRepository.updateArea(area)

    suspend fun getAreasListSync() = workOrderRepository.getAreasListSync()

    /**
     * @return LiveData(Areas)
     */
    fun getArea(areaId: Long) = workOrderRepository.getArea(areaId)

    /**
     * @return LiveData(List(Areas))
     */
    fun searchAreas(query: String) = workOrderRepository.searchAreas(query)
}