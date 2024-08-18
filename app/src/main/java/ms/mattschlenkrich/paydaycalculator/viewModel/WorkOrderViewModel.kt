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

    fun deleteWorkOrder(workOrder: WorkOrder) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkOrder(workOrder)
        }

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

    fun deleteWorkOrderHistory(history: WorkOrderHistory) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkOrderHistory(history)
        }

    fun getWorkOrderHistory(workDateId: Long) =
        workOrderRepository.getWorkOrderHistory(workDateId)
}