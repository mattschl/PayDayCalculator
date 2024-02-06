package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import ms.mattschlenkrich.paydaycalculator.repository.PayDayRepository

class PayDayViewModel(
    app: Application,
    private val payDayRepository: PayDayRepository,
) : AndroidViewModel(app) {
    fun getCutOffDates(employerId: Long) =
        payDayRepository.getCutOffDates(employerId)

    fun insertPayPeriod(cutOff: PayPeriods) =
        viewModelScope.launch {
            payDayRepository.insertPayPeriod(cutOff)
        }

    fun updateWorkDate(workDate: WorkDates) =
        viewModelScope.launch {
            payDayRepository.updateWorkDate(workDate)
        }

    fun getWorkDateList(employerId: Long, cutOff: String) =
        payDayRepository.getWorkDateList(employerId, cutOff)

    fun insertWorkDate(workDate: WorkDates) =
        viewModelScope.launch {
            payDayRepository.insertWorkDate(workDate)
        }

    fun getWorkDatesAndExtras(employerId: Long, cutOffDate: String) =
        payDayRepository.getWorkDatesAndExtras(employerId, cutOffDate)

    fun insertWorkDateExtra(workDateExtra: WorkDateExtras) =
        viewModelScope.launch {
            payDayRepository.insertWorkDateExtra(workDateExtra)
        }

    fun updateWorkDateExtra(workDateExtra: WorkDateExtras) =
        viewModelScope.launch {
            payDayRepository.updateWorkDateExtra(workDateExtra)
        }

    fun getWorkDateExtras(workDateId: Long) =
        payDayRepository.getWorkDateExtras(workDateId)

    fun getWorkDateAndExtraDefAndWorkDateExtras(workDateId: Long) =
        payDayRepository.getWorkDateAndExtraDefAndWorkDateExtras(workDateId)

    fun deleteWorkDateExtra(
        extraName: String, workDateId: Long, updateTime: String
    ) = viewModelScope.launch {
        payDayRepository.deleteWorkDateExtra(extraName, workDateId, updateTime)
    }
}