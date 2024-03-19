package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.WorkPayPeriodExtras
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

    fun updatePayPeriod(payPeriod: PayPeriods) =
        viewModelScope.launch {
            payDayRepository.updatePayPeriod(payPeriod)
        }

    fun getPayPeriod(cutOff: String, employerId: Long) =
        payDayRepository.getPayPeriod(cutOff, employerId)

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

//    fun deleteWorkDateExtra(extraType: WorkExtraTypes) =
//        viewModelScope.launch {
//            payDayRepository.deleteWorkDateExtra(extraType)
//        }
//    fun getWorkDatesAndExtras(employerId: Long, cutOffDate: String) =
//        payDayRepository.getWorkDatesAndExtras(employerId, cutOffDate)

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

    fun getWorkDateExtrasActive(workDateId: Long) =
        payDayRepository.getWorkDateExtrasActive(workDateId)

//    fun getWorkDateAndExtraDefAndWorkDateExtras(workDateId: Long) =
//        payDayRepository.getWorkDateAndExtraDefAndWorkDateExtras(workDateId)

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

//    fun findPayPeriodExtra(workPayPeriodExtraId: Long) =
//        payDayRepository.findPayPeriodExtra(workPayPeriodExtraId)

    fun findPayPeriodExtra(extraName: String) =
        payDayRepository.findPayPeriodExtra(extraName)

    fun getWorkDateExtrasAndDates(cutOffDate: String) =
        payDayRepository.getWorkDateExtrasAndDates(cutOffDate)
}