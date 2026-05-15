package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.data.repository.PayDayRepository

class PayDayViewModel(
    app: Application,
    private val payDayRepository: PayDayRepository,
) : AndroidViewModel(app) {
    fun getCutOffDates(employerId: Long, limit: Int) =
        payDayRepository.getCutOffDates(employerId, limit)

    suspend fun getCutOffDatesSync(employerId: Long, limit: Int) =
        payDayRepository.getCutOffDatesSync(employerId, limit)

    suspend fun insertPayPeriodSync(cutOff: PayPeriods) =
        payDayRepository.insertPayPeriod(cutOff)

    suspend fun getPayPeriodSync(cutOff: String, employerId: Long) =
        payDayRepository.getPayPeriodSync(cutOff, employerId)

    fun updateWorkDate(workDate: WorkDates) =
        viewModelScope.launch {
            payDayRepository.updateWorkDate(workDate)
        }

    fun getWorkDateList(employerId: Long, cutOff: String) =
        payDayRepository.getWorkDateList(employerId, cutOff)

    fun getWorkDatesByDateRange(employerId: Long, firstDate: String, lastDate: String) =
        payDayRepository.getWorkDatesByDateRange(employerId, firstDate, lastDate)

    fun getWorkDateListUsed(employerId: Long, cutOff: String) =
        payDayRepository.getWorkDateListUsed(employerId, cutOff)

    fun insertWorkDate(workDate: WorkDates) =
        viewModelScope.launch {
            payDayRepository.insertWorkDate(workDate)
        }

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

    fun deleteWorkDateExtra(
        extraName: String, workDateId: Long, updateTime: String
    ) = viewModelScope.launch {
        payDayRepository.deleteWorkDateExtra(extraName, workDateId, updateTime)
    }

    fun getWorkDateExtrasPerPay(employerId: Long, cutOff: String) =
        payDayRepository.getWorkDateExtrasPerPay(employerId, cutOff)

    fun insertPayPeriodExtra(payPeriodExtra: WorkPayPeriodExtras) =
        viewModelScope.launch {
            payDayRepository.insertPayPeriodExtra(payPeriodExtra)
        }

    fun updatePayPeriodExtra(payPeriodExtra: WorkPayPeriodExtras) =
        viewModelScope.launch {
            payDayRepository.updatePayPeriodExtra(payPeriodExtra)
        }

    fun getPayPeriodExtras(payPeriodId: Long) =
        payDayRepository.getPayPeriodExtras(payPeriodId)
}