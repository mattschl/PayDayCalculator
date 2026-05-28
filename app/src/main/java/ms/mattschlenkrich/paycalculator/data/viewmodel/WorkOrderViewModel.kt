package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
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

    fun insertWorkOrder(workOrder: WorkOrder) =
        viewModelScope.launch {
            workOrderRepository.insertWorkOrder(workOrder)
        }

    fun updateWorkOrder(workOrder: WorkOrder) = viewModelScope.launch {
        workOrderRepository.updateWorkOrder(
            workOrder.workOrderId,
            workOrder.woNumber,
            workOrder.woEmployerId,
            workOrder.woAddress,
            workOrder.woDescription,
            workOrder.woDeleted,
            workOrder.woUpdateTime
        )
    }

    suspend fun findWorkOrder(workOrderNum: String, employerId: Long) =
        workOrderRepository.findWorkOrder(workOrderNum, employerId)

    fun getWorkOrdersByEmployerId(employerId: Long) =
        workOrderRepository.getWorkOrdersByEmployerId(employerId)

    fun searchWorkOrders(employerId: Long, query: String) =
        workOrderRepository.searchWorkOrders(employerId, query)

    fun getUniqueAddresses(employerId: Long) =
        workOrderRepository.getUniqueAddresses(employerId)

    fun insertWorkOrderHistory(history: WorkOrderHistory) = viewModelScope.launch {
        workOrderRepository.insertWorkOrderHistory(history)
    }

    fun updateWorkOrderHistory(history: WorkOrderHistory) = viewModelScope.launch {
        workOrderRepository.updateWorkOrderHistory(history)
    }

    suspend fun getWorkOrderHistory(workOrderId: Long, workDateId: Long) =
        workOrderRepository.getWorkOrderHistory(workOrderId, workDateId)

    fun deleteWorkOrderHistory(historyId: Long) = viewModelScope.launch {
        workOrderRepository.deleteWorkOrderHistory(
            historyId,
            DateFunctions().getCurrentUTCTimeAsString()
        )
    }

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

    fun insertWorkOrderHistoryTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        viewModelScope.launch { workOrderRepository.insertTimeWorked(timeWorked) }

    fun updateWorkOrderHistoryTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        viewModelScope.launch { workOrderRepository.updateTimeWorked(timeWorked) }

    fun getWorkOrderHistoryTimesByHistory(historyId: Long) =
        workOrderRepository.getTimeWorkedForWorkOrderHistory(historyId)

    fun deleteTimeWorked(timeWorkedId: Long, updateTime: String) =
        viewModelScope.launch { workOrderRepository.deleteTimeWorked(timeWorkedId, updateTime) }

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

    fun insertJobSpecMerged(jobSpecMerged: JobSpecMerged) =
        viewModelScope.launch {
            workOrderRepository.insertJobSpecMerged(jobSpecMerged)
        }

    fun deleteJobSpecMerged(jobSpecMergedId: Long, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.deleteJobSpecMerged(jobSpecMergedId, updateTime)
        }

    fun updateJobSpecMerged(oldJobSpecId: Long, newJobSpecId: Long) =
        viewModelScope.launch {
            workOrderRepository.updateJobSpecMerged(oldJobSpecId, newJobSpecId)
        }

    fun updateJobSpec(jobSpec: JobSpec) = viewModelScope.launch {
        workOrderRepository.updateJobSpec(jobSpec)
    }

    fun deleteJobSpec(jobSpecId: Long, updateTime: String) = viewModelScope.launch {
        workOrderRepository.deleteJobSpec(jobSpecId, updateTime)
    }

    suspend fun getJobSpecsAllSync() = workOrderRepository.getJobSpecsAllSync()

    fun searchJobSpecs(query: String) = workOrderRepository.searchJobSpecs(query)

    fun insertWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) = viewModelScope.launch {
        workOrderRepository.insertWorkOrderJobSpec(workOrderJobSpec)
    }

    fun updateWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) = viewModelScope.launch {
        workOrderRepository.updateWorkOrderJobSpec(workOrderJobSpec)
    }

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

    fun deleteWorkPerformed(workPerformedId: Long, updateTime: String) = viewModelScope.launch {
        workOrderRepository.deleteWorkPerformed(workPerformedId, updateTime)
    }

    suspend fun getWorkPerformedAllSync() = workOrderRepository.getWorkPerformedAllSync()

    fun getWorkPerformedAndChildList(workPerformedId: Long) =
        workOrderRepository.getWorkPerformedAndChildList(workPerformedId)

    fun insertWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged) =
        viewModelScope.launch {
            workOrderRepository.insertWorkPerformedMerged(workPerformedMerged)
        }

    fun deleteWorkPerformedMerged(workPerformedMergedId: Long, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkPerformedMerged(
                workPerformedMergedId,
                updateTime
            )
        }

    fun updateWorkPerformedMerged(oldWorkPerformedId: Long, newWorkPerformedId: Long) =
        viewModelScope.launch {
            workOrderRepository.updateWorkPerformedMerged(oldWorkPerformedId, newWorkPerformedId)
        }

    fun searchFromWorkPerformed(query: String) = workOrderRepository.searchFromWorkPerformed(query)

    suspend fun getWorkPerformedSync(description: String) =
        workOrderRepository.getWorkPerformedSync(description)

    fun getWorkPerformed(workPerformedId: Long) =
        workOrderRepository.getWorkPerformed(workPerformedId)

    fun updateWorkPerformed(workPerformed: WorkPerformed) = viewModelScope.launch {
        workOrderRepository.updateWorkPerformed(workPerformed)
    }

    fun insertWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    ) = viewModelScope.launch {
        workOrderRepository.insertWorkOrderHistoryWorkPerformed(
            workOrderHistoryWorkPerformed
        )
    }

    fun updateWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    ) = viewModelScope.launch {
        workOrderRepository.updateWorkOrderHistoryWorkPerformed(
            workOrderHistoryWorkPerformed
        )
    }

    fun removeAllWorkPerformedFromWorkOderHistory(historyId: Long) =
        viewModelScope.launch {
            workOrderRepository.removeAllWorkPerformedFromWorkOrderHistory(
                historyId,
                DateFunctions().getCurrentUTCTimeAsString()
            )
        }

    fun getWorkPerformedCombinedByWorkOrderHistory(historyId: Long) =
        workOrderRepository.getWorkPerformedCombinedByWorkOrderHistory(historyId)

    fun updateMaterial(material: Material) = viewModelScope.launch {
        workOrderRepository.updateMaterial(material)
    }

    fun getMaterialAndChildList(materialId: Long) =
        workOrderRepository.getMaterialAndChildList(materialId)

    fun searchMaterials(query: String) = workOrderRepository.searchMaterials(query)

    fun getMaterial(materialId: Long) = workOrderRepository.getMaterial(materialId)

    suspend fun getMaterialSync(materialId: Long) =
        workOrderRepository.getMaterialSync(materialId)

    suspend fun getMaterialSync(mName: String) =
        workOrderRepository.getMaterialSync(mName)

    fun updateMaterialMerged(oldMaterialID: Long, newMaterialID: Long, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.updateMaterialMerged(
                oldMaterialID,
                newMaterialID,
                updateTime
            )
        }

    fun deleteMaterialMerged(childId: Long, updateTime: String) = viewModelScope.launch {
        workOrderRepository.deleteMaterialMerged(childId, updateTime)
    }

    fun insertMaterialMerged(materialMerged: MaterialMerged) =
        viewModelScope.launch { workOrderRepository.insertMaterialMerged(materialMerged) }

    fun deleteMaterial(materialId: Long, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.deleteMaterial(materialId, updateTime)
        }

    fun insertWorkOrderHistoryMaterial(workOrderHistoryMaterial: WorkOrderHistoryMaterial) =
        viewModelScope.launch {
            workOrderRepository.insertWorkOrderHistoryMaterial(workOrderHistoryMaterial)
        }

    fun removeAllMaterialsFromWorkOrderHistory(historyId: Long) =
        viewModelScope.launch {
            workOrderRepository.removeAllMaterialsFromWorkOrderHistory(
                historyId,
                DateFunctions().getCurrentUTCTimeAsString()
            )
        }

    fun deleteWorkOrderHistoryWorkPerformed(
        historyWorkPerformedId: Long,
        updateTime: String = DateFunctions()
            .getCurrentUTCTimeAsString()
    ) = viewModelScope.launch {
        workOrderRepository.deleteWorkOrderHistoryWorkPerformed(historyWorkPerformedId, updateTime)
    }

    fun updateWorkOrderHistoryMaterial(workOrderHistoryMaterial: WorkOrderHistoryMaterial) =
        viewModelScope.launch {
            workOrderRepository.updateWorkOrderHistoryMaterial(workOrderHistoryMaterial)
        }

    fun deleteWorkOrderHistoryMaterial(
        historyMaterialId: Long, updateTime: String
    ) = viewModelScope.launch {
        workOrderRepository.deleteWorkOrderHistoryMaterial(historyMaterialId, updateTime)
    }

    fun getMaterialsByHistory(historyId: Long) =
        workOrderRepository.getMaterialsByHistory(historyId)

    suspend fun getWorkOrderHistoryMaterialCombined(woHistoryMaterialId: Long) =
        workOrderRepository.getWorkOrderHistoryMaterialCombined(woHistoryMaterialId)

    fun updateArea(area: Areas) = viewModelScope.launch { workOrderRepository.updateArea(area) }

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