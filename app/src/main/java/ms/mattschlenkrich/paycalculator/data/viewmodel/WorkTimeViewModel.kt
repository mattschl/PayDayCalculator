package ms.mattschlenkrich.paycalculator.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.data.repository.WorkTimeRepository

class WorkTimeViewModel(
    app: Application,
    private val workTimeRepository: WorkTimeRepository
) : AndroidViewModel(app) {
    suspend fun insertWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) =
        workTimeRepository.insertWorkTime(workOrderHistoryTimeWorked)

    fun deleteWorkTime(id: Long, updateTime: String) = viewModelScope.launch {
        workTimeRepository.deleteWorkTime(id, updateTime)
    }

    fun deleteWorkTime(id: Long) = viewModelScope.launch {
        workTimeRepository.deleteWorkTime(id, DateFunctions().getCurrentUTCTimeAsString())
    }

    suspend fun updateWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) =
        workTimeRepository.updateWorkTime(workOrderHistoryTimeWorked)

    suspend fun updateWorkDate(workDate: WorkDates) =
        workTimeRepository.updateWorkDate(workDate)

    suspend fun updateWorkOrderHistory(workOrderHistory: WorkOrderHistory) =
        workTimeRepository.updateWorkOrderHistory(workOrderHistory)

    fun getExistingHistories(workDateId: Long) =
        workTimeRepository.getExistingHistories(workDateId)


    fun getExistingHistoriesWithTimes(workDateId: Long) =
        workTimeRepository.getExistingHistoriesWithTimes(workDateId)

    fun getTimesWorkedByDate(workDateId: Long): LiveData<List<WorkOrderHistoryTimeWorkedCombined>> =
        workTimeRepository.getTimesWorkedByDate(workDateId)

    fun getWorkOrders(employerId: Long) =
        workTimeRepository.getWorkOrders(employerId)

    fun getWorkOrderNumbers(employerId: Long): LiveData<List<WorkOrder>> =
        workTimeRepository.getWorkOrderNumbers(employerId)

    fun getWorkDate(workDateId: Long) =
        workTimeRepository.getWorkDate(workDateId)
}