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

    fun updateWorkOrder(workOrder: WorkOrder) =
        viewModelScope.launch {
            workOrderRepository.updateWorkOrder(workOrder)
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

    fun updateWorkOrderHistory(history: WorkOrderHistory) =
        viewModelScope.launch {
            workOrderRepository.updateWorkOrderHistory(history)
        }

    fun deleteWorkOrderHistory(historyId: Long, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkOrderHistory(historyId, updateTime)
        }

    fun getWorkOrderHistories(workDateId: Long) =
        workOrderRepository.getWorkOrderHistories(workDateId)

    fun getWorkOrderHistory(historyID: Long) =
        workOrderRepository.getWorkOrderHistory(historyID)
}