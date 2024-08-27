package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.repository.WorkOrderRepository

class WorkOrderViewModel(
    app: Application,
    private val workOrderRepository: WorkOrderRepository
) : AndroidViewModel(app) {

    fun insertWorkOrder(workOrder: WorkOrder) =
        viewModelScope.launch {
            workOrderRepository.insertWorkOrder(workOrder)
        }

    fun updateWorkOrder(
        workOrderId: String,
        employerId: Long,
        address: String,
        description: String,
        isDeleted: Boolean,
        updateTime: String,
    ) =
        viewModelScope.launch {
            workOrderRepository.updateWorkOrder(
                workOrderId, employerId, address,
                description, isDeleted, updateTime
            )
        }

    fun deleteWorkOrder(workOrderId: Long, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkOrder(workOrderId, updateTime)
        }

    fun getWorkOrder(workOrderNum: String) =
        workOrderRepository.getWorkOrder(workOrderNum)

    fun getWorkOrdersByEmployerId(employerId: Long) =
        workOrderRepository.getWorkOrdersByEmployerId(employerId)

    fun insertWorkOrderHistory(history: WorkOrderHistory) =
        viewModelScope.launch {
            workOrderRepository.insertWorkOrderHistory(history)
        }

    fun updateWorkOrderHistory(
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

    fun getWorkOrderHistoriesByDate(workDateId: Long) =
        workOrderRepository.getWorkOrderHistoriesByDate(workDateId)

    fun getWorkOrderHistoriesById(workOrderId: String) =
        workOrderRepository.getWorkOrderHistoriesById(workOrderId)

    fun getWorkOrderHistory(historyID: Long) =
        workOrderRepository.getWorkOrderHistory(historyID)
}