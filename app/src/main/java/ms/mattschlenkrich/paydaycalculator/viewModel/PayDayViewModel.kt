package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
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

    fun getWorkDateList(employerId: Long, cutOff: String) =
        payDayRepository.getWorkDateList(employerId, cutOff)

    fun insertWorkDate(workDate: WorkDates) =
        viewModelScope.launch {
            payDayRepository.insertWorkDate(workDate)
        }

    fun getWorkDatesAndExtras(employerId: Long, cutOffDate: String) =
        payDayRepository.getWorkDatesAndExtras(employerId, cutOffDate)
}