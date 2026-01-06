package ms.mattschlenkrich.paycalculator.database.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.database.repository.WorkTimeRepository

class WorkTimeViewModel(
    app: Application,
    private val workTimeRepository: WorkTimeRepository
) : AndroidViewModel(app) {
    suspend fun insertWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) =
        workTimeRepository.insertWorkTime(workOrderHistoryTimeWorked)

    suspend fun deleteWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) =
        workTimeRepository.deleteWorkTime(workOrderHistoryTimeWorked)

    suspend fun updateWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) =
        workTimeRepository.updateWorkTime(workOrderHistoryTimeWorked)

    suspend fun updateWorkDate(workDates: WorkDates) =
        workTimeRepository.updateWorkDate(workDates)

    suspend fun updateWorkOrderHistory(workOrderHistory: WorkOrderHistory) =
        workTimeRepository.updateWorkOrderHistory(workOrderHistory)

    suspend fun getExistingHistories(workDateId: Long) =
        workTimeRepository.getExistingHistories(workDateId)

    suspend fun getExistingHistoriesWithTimes(workDateId: Long) =
        workTimeRepository.getExistingHistoriesWithTimes(workDateId)

    fun getTimesWorkedByDate(workDateId: Long) =
        viewModelScope.launch {
            workTimeRepository.getTimesWorkedByDate(workDateId)
        }

    suspend fun getWorkOrders(employerId: Long) =
        workTimeRepository.getWorkOrders(employerId)

    suspend fun getWorkDate(workDateId: Long) =
        workTimeRepository.getWorkDate(workDateId)

}