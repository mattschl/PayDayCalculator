package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.data.repository.WorkTimeRepository

class WorkTimeViewModel(
    app: Application,
    private val workTimeRepository: WorkTimeRepository
) : AndroidViewModel(app) {

    suspend fun updateWorkDate(workDate: WorkDates) =
        workTimeRepository.updateWorkDate(workDate)

    fun getTimesWorkedByDate(workDateId: Long): LiveData<List<WorkOrderHistoryTimeWorkedCombined>> =
        workTimeRepository.getTimesWorkedByDate(workDateId)

    fun getWorkOrderNumbers(employerId: Long): LiveData<List<WorkOrder>> =
        workTimeRepository.getWorkOrderNumbers(employerId)
}