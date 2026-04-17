package ms.mattschlenkrich.paycalculator.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WorkOrderViewModel(
    app: Application,
    private val workOrderRepository: WorkOrderRepository
) : AndroidViewModel(app) {

    fun insertWorkOrder(workOrder: WorkOrder) =
        viewModelScope.launch {
            workOrderRepository.insertWorkOrder(workOrder)
        }

    fun updateWorkOrder(
        workOrderId: Long,
        workOrderNumber: String,
        employerId: Long,
        address: String,
        description: String,
        isDeleted: Boolean,
        updateTime: String,
    ) =
        viewModelScope.launch {
            workOrderRepository.updateWorkOrder(
                workOrderId, workOrderNumber, employerId, address,
                description, isDeleted, updateTime
            )
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

    fun getWorkOrder(workOrderId: Long) = workOrderRepository.getWorkOrder(workOrderId)

    fun getWorkOrder(workOrderNum: String) = workOrderRepository.getWorkOrder(workOrderNum)

    suspend fun findWorkOrder(workOrderNum: String, employerId: Long) =
        workOrderRepository.findWorkOrder(workOrderNum, employerId)


    fun getWorkOrdersByEmployerId(employerId: Long) =
        workOrderRepository.getWorkOrdersByEmployerId(employerId)

    fun searchWorkOrders(employerId: Long, query: String) =
        workOrderRepository.searchWorkOrders(employerId, query)

    fun insertWorkOrderHistory(history: WorkOrderHistory) = viewModelScope.launch {
        workOrderRepository.insertWorkOrderHistory(history)
    }

    fun updateWorkOrderHistory(history: WorkOrderHistory) = viewModelScope.launch {
        workOrderRepository.updateWorkOrderHistory(history)
    }

    suspend fun getWorkOrderHistory(workOrderId: Long, workDateId: Long) =
        workOrderRepository.getWorkOrderHistory(workOrderId, workDateId)


    fun updateWorkOrderHistory(
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
        viewModelScope.launch {
            workOrderRepository.updateWorkOrderHistory(
                historyID, workOrderId, workDateId, regHours,
                otHours, dblOtHours, note, isDeleted, updateTime
            )
        }

    fun deleteWorkOrderHistory(historyId: Long, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkOrderHistory(historyId, updateTime)
        }

    fun deleteWorkOrderHistory(historyId: Long) = viewModelScope.launch {
        workOrderRepository.deleteWorkOrderHistory(
            historyId,
            ms.mattschlenkrich.paycalculator.common.DateFunctions().getCurrentTimeAsString()
        )
    }

    fun getWorkOrderHistoriesByDate(workDateId: Long) =
        workOrderRepository.getWorkOrderHistoriesByDate(workDateId)

    fun getWorkOrderHistoriesById(historyId: Long) =
        workOrderRepository.getWorkOrderHistoriesById(historyId)

    suspend fun getWorkOrderHistoryWithDateById(historyID: Long) =
        workOrderRepository.getWorkOrderHistoryWithDatedById(historyID)

    fun getWorkOrderHistoryCombined(historyId: Long) =
        workOrderRepository.getWorkOrderHistoryCombined(historyId)

    fun insertWorkOrderHistoryTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        viewModelScope.launch { workOrderRepository.insertTimeWorked(timeWorked) }

    fun updateWorkOrderHistoryTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        viewModelScope.launch { workOrderRepository.updateTimeWorked(timeWorked) }

    fun getWorkOrderHistoryTimesByHistory(historyId: Long) =
        workOrderRepository.getTimeWorkedForWorkOrderHistory(historyId)

    fun insertTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        viewModelScope.launch { workOrderRepository.insertTimeWorked(timeWorked) }

    fun updateTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        viewModelScope.launch { workOrderRepository.updateTimeWorked(timeWorked) }

    fun deleteTimeWorked(timeWorkedId: Long, updateTime: String) =
        viewModelScope.launch { workOrderRepository.deleteTimeWorked(timeWorkedId, updateTime) }

    fun deleteTimeWorked(timeWorkedId: Long) =
        viewModelScope.launch {
            workOrderRepository.deleteTimeWorked(
                timeWorkedId,
                ms.mattschlenkrich.paycalculator.common.DateFunctions().getCurrentTimeAsString()
            )
        }

    fun getTimeWorkedPerDay(workDateId: Long) =
        workOrderRepository.getTimeWorkedPerDay(workDateId)

    fun getTimeWorkedForWorkOrderHistory(historyId: Long) =
        workOrderRepository.getTimeWorkedForWorkOrderHistory(historyId)

    fun getWorkPerformedHistoryById(historyWorkPerformedId: Long) =
        workOrderRepository.getWorkPerformedHistoryById(historyWorkPerformedId)

    suspend fun getWorkPerformedHistoryByIdSync(historyWorkPerformedId: Long) =
        workOrderRepository.getWorkPerformedHistoryByIdSync(historyWorkPerformedId)

    fun getWorkOrderHistoriesByWorkOrder(workOrderId: Long) =
        workOrderRepository.getWorkOrderHistoriesByWorkOrder(workOrderId)

    fun getWorkOrderHistory(historyID: Long) = workOrderRepository.getWorkOrderHistory(historyID)

    fun deleteWorkOrderHistoryByWorkDateId(workDateId: Long, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkOrderHistoryByWorkDateId(workDateId, updateTime)
        }

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

    fun deleteJobSpecMerged(jobSpecMergedId: Long) =
        viewModelScope.launch {
            workOrderRepository.deleteJobSpecMerged(
                jobSpecMergedId,
                ms.mattschlenkrich.paycalculator.common.DateFunctions().getCurrentTimeAsString()
            )
        }

    suspend fun getJobSpecSync(jsName: String) = workOrderRepository.getJobSpecSync(jsName)

    fun insertJobSpec(jobSpec: JobSpec) = viewModelScope.launch {
        workOrderRepository.insertJobSpec(jobSpec)
    }

    fun updateJobSpec(jobSpec: JobSpec) = viewModelScope.launch {
        workOrderRepository.updateJobSpec(jobSpec)
    }

    fun getJobSpecsAll() = workOrderRepository.getJobSpecs()

    suspend fun getJobSpecsAllSync() = workOrderRepository.getJobSpecsAllSync()

    fun searchJobSpecs(query: String) = workOrderRepository.searchJobSpecs(query)

    fun insertWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) = viewModelScope.launch {
        workOrderRepository.insertWorkOrderJobSpec(workOrderJobSpec)
    }

    fun updateWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) = viewModelScope.launch {
        workOrderRepository.updateWorkOrderJobSpec(workOrderJobSpec)
    }

    fun deleteWorkOrderJobSpec(
        workOrderJobSpecId: Long, updateTime: String
    ) = viewModelScope.launch {
        workOrderRepository.deleteWorkOrderJobSpec(workOrderJobSpecId, updateTime)
    }

    fun deleteWorkOrderJobSpec(workOrderJobSpecId: Long) = viewModelScope.launch {
        workOrderRepository.deleteWorkOrderJobSpec(
            workOrderJobSpecId,
            ms.mattschlenkrich.paycalculator.common.DateFunctions().getCurrentTimeAsString()
        )
    }

    fun getWorkOrderJobSpecs(workOrderId: Long) =
        workOrderRepository.getWorkOrderJobSpecs(workOrderId)

    fun getWorkOrderJobSpec(workOrderJobSpecId: Long) =
        workOrderRepository.getWorkOrderJobSpec(workOrderJobSpecId)

    suspend fun getWorkOrderJobSpecSync(workOrderJobSpecId: Long) =
        workOrderRepository.getWorkOrderJobSpecSync(workOrderJobSpecId)

    suspend fun getOrCreateJobSpec(name: String): JobSpec {
        val existing = getJobSpecsAllSync().find {
            it.jsName.trim().equals(name.trim(), ignoreCase = true)
        }
        if (existing != null) return existing

        val nf = ms.mattschlenkrich.paycalculator.common.NumberFunctions()
        val df = ms.mattschlenkrich.paycalculator.common.DateFunctions()
        val newJobSpec = JobSpec(
            nf.generateRandomIdAsLong(),
            name.trim(),
            false,
            df.getCurrentTimeAsString()
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

        val nf = ms.mattschlenkrich.paycalculator.common.NumberFunctions()
        val df = ms.mattschlenkrich.paycalculator.common.DateFunctions()
        val newArea = Areas(
            nf.generateRandomIdAsLong(),
            name.trim(),
            false,
            df.getCurrentTimeAsString()
        )
        workOrderRepository.insertArea(newArea)
        return newArea
    }

    fun updateWorkOrderJobSpec(
        originalId: Long,
        jobSpecId: Long,
        areaId: Long?,
        note: String?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val original = getWorkOrderJobSpecSync(originalId)
            if (original != null) {
                val df = ms.mattschlenkrich.paycalculator.common.DateFunctions()
                val updated = WorkOrderJobSpec(
                    original.workOrderJobSpec.workOrderJobSpecId,
                    original.workOrderJobSpec.wojsWorkOrderId,
                    jobSpecId,
                    areaId,
                    note,
                    original.workOrderJobSpec.wojsSequence,
                    false,
                    df.getCurrentTimeAsString()
                )
                workOrderRepository.updateWorkOrderJobSpec(updated)
                onComplete()
            }
        }
    }

    fun insertWorkPerformed(workPerformed: WorkPerformed) = viewModelScope.launch {
        workOrderRepository.insertWorkPerformed(workPerformed)
    }

    fun deleteWorkPerformed(workPerformedId: Long, updateTime: String) = viewModelScope.launch {
        workOrderRepository.deleteWorkPerformed(workPerformedId, updateTime)
    }
//
//    fun deleteWorkPerformed(workPerformed: WorkPerformed) = viewModelScope.launch {
//        workOrderRepository.deleteWorkPerformed(
//            workPerformed.workPerformedId,
//            ms.mattschlenkrich.paycalculator.common.DateFunctions().getCurrentTimeAsString()
//        )
//    }


    fun getWorkPerformedAll() = workOrderRepository.getWorkPerformedAll()

    suspend fun getWorkPerformedAllSync() = workOrderRepository.getWorkPerformedAllSync()

    fun getWorkPerformedChildren(workPerformedId: Long) =
        workOrderRepository.getWorkPerformedChildren(workPerformedId)

    fun getWorkPerformedAndChildList(workPerformedId: Long) =
        workOrderRepository.getWorkPerformedAndChildList(workPerformedId)

    fun insertWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged) =
        viewModelScope.launch {
            workOrderRepository.insertWorkPerformedMerged(workPerformedMerged)
        }

    fun updateWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged) =
        viewModelScope.launch {
            workOrderRepository.updateWorkPerformedMerged(workPerformedMerged)
        }

    fun deleteWorkPerformedMerged(workPerformedMergedId: Long, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkPerformedMerged(
                workPerformedMergedId,
                updateTime
            )
        }

    fun deleteWorkPerformedMerged(workPerformedMergedId: Long) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkPerformedMerged(
                workPerformedMergedId,
                ms.mattschlenkrich.paycalculator.common.DateFunctions().getCurrentTimeAsString()
            )
        }

    fun updateWorkPerformedMerged(oldWorkPerformedId: Long, newWorkPerformedId: Long) =
        viewModelScope.launch {
            workOrderRepository.updateWorkPerformedMerged(oldWorkPerformedId, newWorkPerformedId)
        }

    fun searchFromWorkPerformed(query: String) = workOrderRepository.searchFromWorkPerformed(query)

    fun getWorkPerformed(description: String) = workOrderRepository.getWorkPerformed(description)

    suspend fun getWorkPerformedSync(description: String) =
        workOrderRepository.getWorkPerformedSync(description)

    fun getWorkPerformed(workPerformedId: Long) =
        workOrderRepository.getWorkPerformed(workPerformedId)

    suspend fun getWorkPerformedSync(workPerformedId: Long) =
        workOrderRepository.getWorkPerformedSync(workPerformedId)

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
                ms.mattschlenkrich.paycalculator.common.DateFunctions().getCurrentTimeAsString()
            )
        }

    fun getWorkPerformedCombinedByWorkOrderHistory(historyId: Long) =
        workOrderRepository.getWorkPerformedCombinedByWorkOrderHistory(historyId)

    suspend fun getWorkPerformedByWorkOrderHistorySync(historyId: Long) =
        workOrderRepository.getWorkPerformedByWorkOrderHistorySync(historyId)

    fun insertMaterial(material: Material) = viewModelScope.launch {
        workOrderRepository.insertMaterial(material)
    }

    fun updateMaterial(material: Material) = viewModelScope.launch {
        workOrderRepository.updateMaterial(material)
    }

    fun getMaterialsList() = workOrderRepository.getMaterialsList()

    fun getMaterialsChildren(materialId: Long) =
        workOrderRepository.getMaterialsChildren(materialId)

    fun getMaterialAndChildList(materialId: Long) =
        workOrderRepository.getMaterialAndChildList(materialId)


    fun searchMaterials(query: String) = workOrderRepository.searchMaterials(query)

    fun getMaterial(materialId: Long) = workOrderRepository.getMaterial(materialId)

    suspend fun getMaterialSync(materialId: Long) =
        workOrderRepository.getMaterialSync(materialId)

    fun getMaterial(mName: String) = workOrderRepository.getMaterial(mName)

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

    fun deleteMaterialMerged(childId: Long) = viewModelScope.launch {
        workOrderRepository.deleteMaterialMerged(
            childId,
            ms.mattschlenkrich.paycalculator.common.DateFunctions().getCurrentTimeAsString()
        )
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

    fun removeWorkOrderHistoryMaterial(workOrderHistoryMaterialId: Long, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.removeWorkOrderHistoryMaterial(
                workOrderHistoryMaterialId,
                updateTime
            )
        }

    fun removeWorkOrderHistoryMaterial(workOrderHistoryMaterialId: Long) =
        viewModelScope.launch {
            workOrderRepository.removeWorkOrderHistoryMaterial(
                workOrderHistoryMaterialId,
                ms.mattschlenkrich.paycalculator.common.DateFunctions().getCurrentTimeAsString()
            )
        }

    fun removeAllMaterialsFromWorkOrderHistory(historyId: Long) =
        viewModelScope.launch {
            workOrderRepository.removeAllMaterialsFromWorkOrderHistory(
                historyId,
                ms.mattschlenkrich.paycalculator.common.DateFunctions().getCurrentTimeAsString()
            )
        }

    fun deleteWorkOrderHistoryWorkPerformed(
        historyWorkPerformedId: Long,
        updateTime: String = ms.mattschlenkrich.paycalculator.common.DateFunctions()
            .getCurrentTimeAsString()
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

    suspend fun getMaterialsFromHistoryId(historyId: Long) =
        workOrderRepository.getMaterialsFromHistoryId(historyId)

    fun getMaterialsHistoryByWorkOrderId(workOrderId: Long) =
        workOrderRepository.getMaterialsHistoryByWorkOrderId(workOrderId)

    suspend fun getWorkOrderHistoryMaterialCombined(woHistoryMaterialId: Long) =
        workOrderRepository.getWorkOrderHistoryMaterialCombined(woHistoryMaterialId)

    fun insertArea(area: Areas) = viewModelScope.launch { workOrderRepository.insertArea(area) }

    fun updateArea(area: Areas) = viewModelScope.launch { workOrderRepository.updateArea(area) }

    /**
     * @return LiveData(List(Areas))
     */
    fun getAreasList() = workOrderRepository.getAreasList()

    suspend fun getAreasListSync() = workOrderRepository.getAreasListSync()

    /**
     * @return LiveData(Areas)
     */
    fun getArea(areaId: Long) = workOrderRepository.getArea(areaId)

    suspend fun getAreaSync(areaId: Long) = workOrderRepository.getAreaSync(areaId)

    /**
     * @return LiveData(Areas)
     */
    fun getArea(areaName: String) = workOrderRepository.getArea(areaName)

    suspend fun getAreaSync(areaName: String) = workOrderRepository.getAreaSync(areaName)

    /**
     * @return LiveData(List(Areas))
     */
    fun searchAreas(query: String) = workOrderRepository.searchAreas(query)
}