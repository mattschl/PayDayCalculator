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

    fun deleteWorkOrder(workOrderId: Long, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkOrder(workOrderId, updateTime)
        }

    fun deleteWorkOrder(workOrderNumber: String, updateTime: String) =
        viewModelScope.launch {
            workOrderRepository.deleteWorkOrder(workOrderNumber, updateTime)
        }

    fun getWorkOrder(workOrderId: Long) =
        workOrderRepository.getWorkOrder(workOrderId)

    fun getWorkOrder(workOrderNum: String) =
        workOrderRepository.getWorkOrder(workOrderNum)

    fun getWorkOrdersByEmployerId(employerId: Long) =
        workOrderRepository.getWorkOrdersByEmployerId(employerId)

    fun searchWorkOrders(employerId: Long, query: String) =
        workOrderRepository.searchWorkOrders(employerId, query)

    fun insertWorkOrderHistory(history: WorkOrderHistory) =
        viewModelScope.launch {
            workOrderRepository.insertWorkOrderHistory(history)
        }

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

    fun getWorkOrderHistoriesByDate(workDateId: Long) =
        workOrderRepository.getWorkOrderHistoriesByDate(workDateId)

    fun getWorkOrderHistoriesById(workOrderId: Long) =
        workOrderRepository.getWorkOrderHistoriesById(workOrderId)

    fun getWorkOrderHistory(historyID: Long) =
        workOrderRepository.getWorkOrderHistory(historyID)

    fun deleteWorkOrderHistoryByWorkDateId(
        workDateId: Long, updateTime: String
    ) = viewModelScope.launch {
        workOrderRepository.deleteWorkOrderHistoryByWorkDateId(
            workDateId, updateTime
        )
    }
}