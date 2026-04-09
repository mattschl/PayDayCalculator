package ms.mattschlenkrich.paycalculator.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.data.Areas
import ms.mattschlenkrich.paycalculator.data.JobSpec
import ms.mattschlenkrich.paycalculator.data.Material
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.data.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.WorkPerformedMerged
import ms.mattschlenkrich.paycalculator.data.WorkOrderRepository

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

    fun getWorkOrder(workOrderId: Long) = workOrderRepository.getWorkOrder(workOrderId)

    fun getWorkOrder(workOrderNum: String) = workOrderRepository.getWorkOrder(workOrderNum)

    fun findWorkOrder(workOrderNum: String, employerId: Long) =
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

    fun getWorkOrderHistory(workOrderId: Long, workDateId: Long) =
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
        workOrderRepository.deleteWorkOrderHistory(historyId)
    }

    fun getWorkOrderHistoriesByDate(workDateId: Long) =
        workOrderRepository.getWorkOrderHistoriesByDate(workDateId)

    fun getWorkOrderHistoriesById(historyId: Long) =
        workOrderRepository.getWorkOrderHistoriesById(historyId)

    fun getWorkOrderHistoryWithDateById(historyID: Long) =
        workOrderRepository.getWorkOrderHistoryWithDatedById(historyID)

    fun getWorkOrderHistoryCombined(historyId: Long) =
        workOrderRepository.getWorkOrderHistoryCombined(historyId)

    fun insertTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        viewModelScope.launch { workOrderRepository.insertTimeWorked(timeWorked) }

    fun updateTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        viewModelScope.launch { workOrderRepository.updateTimeWorked(timeWorked) }

    fun deleteTimeWorked(timeWorkedId: Long, updateTime: String) =
        viewModelScope.launch { workOrderRepository.deleteTimeWorked(timeWorkedId, updateTime) }

    fun deleteTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        viewModelScope.launch { workOrderRepository.deleteTimeWorked(timeWorked) }

    fun getTimeWorkedPerDay(workDateId: Long) =
        workOrderRepository.getTimeWorkedPerDay(workDateId)

    fun getTimeWorkedForWorkOrderHistory(historyId: Long) =
        workOrderRepository.getTimeWorkedForWorkOrderHistory(historyId)

    fun getWorkPerformedHistoryById(historyWorkPerformedId: Long) =
        workOrderRepository.getWorkPerformedHistoryById(historyWorkPerformedId)

    fun getWorkOrderHistoriesByWorkOrder(workOrderId: Long) =
        workOrderRepository.getWorkOrderHistoriesByWorkOrder(workOrderId)

    fun getWorkOrderHistory(historyID: Long) = workOrderRepository.getWorkOrderHistory(historyID)

    fun deleteWorkOrderHistoryByWorkDateId(workDateId: Long, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkOrderHistoryByWorkDateId(workDateId, updateTime)
        }

    fun insertJobSpec(jobSpec: JobSpec) = viewModelScope.launch {
        workOrderRepository.insertJobSpec(jobSpec)
    }

    fun updateJobSpec(jobSpec: JobSpec) = viewModelScope.launch {
        workOrderRepository.updateJobSpec(jobSpec)
    }

    fun getJobSpecsAll() = workOrderRepository.getJobSpecs()

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
        workOrderRepository.deleteWorkOrderJobSpec(workOrderJobSpecId)
    }

    fun getWorkOrderJobSpecs(workOrderId: Long) =
        workOrderRepository.getWorkOrderJobSpecs(workOrderId)

    fun getWorkOrderJobSpec(workOrderJobSpecId: Long) =
        workOrderRepository.getWorkOrderJobSpec(workOrderJobSpecId)

    fun insertWorkPerformed(workPerformed: WorkPerformed) = viewModelScope.launch {
        workOrderRepository.insertWorkPerformed(workPerformed)
    }

    fun deleteWorkPerformed(workPerformedId: Long, updateTime: String) = viewModelScope.launch {
        workOrderRepository.deleteWorkPerformed(workPerformedId, updateTime)
    }

    fun deleteWorkPerformed(workPerformed: WorkPerformed) = viewModelScope.launch {
        workOrderRepository.deleteWorkPerformed(workPerformed)
    }


    fun getWorkPerformedAll() = workOrderRepository.getWorkPerformedAll()

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

    fun deleteWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkPerformedMerged(workPerformedMerged)
        }

    fun deleteWorkPerformedMerged(workPerformedMergedId: Long) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkPerformedMerged(workPerformedMergedId)
        }

    fun updateWorkPerformedMerged(oldWorkPerformedId: Long, newWorkPerformedId: Long) =
        viewModelScope.launch {
            workOrderRepository.updateWorkPerformedMerged(oldWorkPerformedId, newWorkPerformedId)
        }

    fun searchFromWorkPerformed(query: String) = workOrderRepository.searchFromWorkPerformed(query)

    fun getWorkPerformed(description: String) = workOrderRepository.getWorkPerformed(description)

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
            workOrderRepository.removeAllWorkPerformedFromWorkOrderHistory(historyId)
        }

    fun getWorkPerformedCombinedByWorkOrderHistory(historyId: Long) =
        workOrderRepository.getWorkPerformedCombinedByWorkOrderHistory(historyId)

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

    fun getMaterial(mName: String) = workOrderRepository.getMaterial(mName)

    fun updateMaterialMerged(oldMaterialID: Long, newMaterialID: Long, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.updateMaterialMerged(
                oldMaterialID,
                newMaterialID,
                updateTime
            )
        }

    fun deleteMaterialMerged(childId: Long) = viewModelScope.launch {
        workOrderRepository.deleteMaterialMerged(childId)
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

    fun removeWorkOrderHistoryMaterial(workOrderHistoryMaterialId: Long) =
        viewModelScope.launch {
            workOrderRepository.removeWorkOrderHistoryMaterial(workOrderHistoryMaterialId)
        }

    fun removeAllMaterialsFromWorkOrderHistory(historyId: Long) =
        viewModelScope.launch {
            workOrderRepository.removeAllMaterialsFromWorkOrderHistory(historyId)
        }

    fun deleteWorkOrderHistoryWorkPerformed(historyWorkPerformedId: Long) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkOrderHistoryWorkPerformed(historyWorkPerformedId)
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

    fun getMaterialsFromHistoryId(historyId: Long) =
        workOrderRepository.getMaterialsFromHistoryId(historyId)

    fun getMaterialsHistoryByWorkOrderId(workOrderId: Long) =
        workOrderRepository.getMaterialsHistoryByWorkOrderId(workOrderId)

    fun getWorkOrderHistoryMaterialCombined(woHistoryMaterialId: Long) =
        workOrderRepository.getWorkOrderHistoryMaterialCombined(woHistoryMaterialId)

    fun insertArea(area: Areas) = viewModelScope.launch { workOrderRepository.insertArea(area) }

    fun updateArea(area: Areas) = viewModelScope.launch { workOrderRepository.updateArea(area) }

    /**
     * @return LiveData(List(Areas))
     */
    fun getAreasList() = workOrderRepository.getAreasList()

    /**
     * @return LiveData(Areas)
     */
    fun getArea(areaId: Long) = workOrderRepository.getArea(areaId)

    /**
     * @return LiveData(Areas)
     */
    fun getArea(areaName: String) = workOrderRepository.getArea(areaName)

    /**
     * @return LiveData(List(Areas))
     */
    fun searchAreas(query: String) = workOrderRepository.searchAreas(query)
}