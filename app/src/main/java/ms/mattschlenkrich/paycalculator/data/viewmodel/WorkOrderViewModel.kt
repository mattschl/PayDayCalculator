package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryWorkPerformed
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

    fun getWorkOrder(workOrderId: Long) = workOrderRepository.getWorkOrder(workOrderId)

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

    suspend fun updateMaterialMerged(oldMaterialID: Long, newMaterialID: Long, updateTime: String) =
        workOrderRepository.updateMaterialMerged(
            oldMaterialID,
            newMaterialID,
            updateTime
        )

    suspend fun insertWorkOrderHistoryMaterial(workOrderHistoryMaterial: WorkOrderHistoryMaterial) =
        workOrderRepository.insertWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun removeAllMaterialsFromWorkOrderHistory(historyId: Long) =
        workOrderRepository.removeAllMaterialsFromWorkOrderHistory(
            historyId,
            DateFunctions().getCurrentUTCTimeAsString()
        )

    suspend fun removeAllTimeWorkedFromWorkOrderHistory(historyId: Long) =
        workOrderRepository.removeAllTimeWorkedFromWorkOrderHistory(
            historyId,
            DateFunctions().getCurrentUTCTimeAsString()
        )

    suspend fun deleteWorkDate(workDateId: Long) =
        workOrderRepository.deleteWorkDate(
            workDateId,
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
}