package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

    suspend fun updatePayPeriod(payPeriod: PayPeriods) =
        payDayRepository.updatePayPeriod(payPeriod)

    fun getPayPeriod(cutOff: String, employerId: Long) =
        payDayRepository.getPayPeriod(cutOff, employerId)

    suspend fun getPayPeriodSync(cutOff: String, employerId: Long) =
        payDayRepository.getPayPeriodSync(cutOff, employerId)

    suspend fun getPayPeriodAnySync(cutOff: String, employerId: Long) =
        payDayRepository.getPayPeriodAnySync(cutOff, employerId)

    suspend fun findOrCreatePayPeriod(
        cutoffDate: String,
        employerId: Long,
        updateTime: String,
        generateId: () -> Long
    ): PayPeriods {
        val existing = getPayPeriodAnySync(cutoffDate, employerId)
        return if (existing != null) {
            val updated = existing.copy(
                ppIsDeleted = false,
                ppUpdateTime = updateTime
            )
            updatePayPeriod(updated)
            updated
        } else {
            val newPeriod = PayPeriods(
                generateId(),
                cutoffDate,
                employerId,
                false,
                updateTime
            )
            insertPayPeriodSync(newPeriod)
            newPeriod
        }
    }

    suspend fun getWorkDateSync(employerId: Long, date: String, cutOff: String) =
        payDayRepository.getWorkDateSync(employerId, date, cutOff)

    suspend fun updateWorkDate(workDate: WorkDates) =
        payDayRepository.updateWorkDate(workDate)

    fun getWorkDateList(employerId: Long, cutOff: String) =
        payDayRepository.getWorkDateList(employerId, cutOff)

    fun getWorkDatesByDateRange(employerId: Long, firstDate: String, lastDate: String) =
        payDayRepository.getWorkDatesByDateRange(employerId, firstDate, lastDate)

    fun getWorkDateListUsed(employerId: Long, cutOff: String) =
        payDayRepository.getWorkDateListUsed(employerId, cutOff)

    suspend fun insertWorkDate(workDate: WorkDates) =
        payDayRepository.insertWorkDate(workDate)

    suspend fun insertWorkDateExtra(workDateExtra: WorkDateExtras) =
        payDayRepository.insertWorkDateExtra(workDateExtra)

    suspend fun updateWorkDateExtra(workDateExtra: WorkDateExtras) =
        payDayRepository.updateWorkDateExtra(workDateExtra)

    fun getWorkDateExtras(workDateId: Long) =
        payDayRepository.getWorkDateExtras(workDateId)

    suspend fun deleteWorkDateExtra(
        extraName: String, workDateId: Long, updateTime: String
    ) = payDayRepository.deleteWorkDateExtra(extraName, workDateId, updateTime)

    fun getWorkDateExtrasPerPay(employerId: Long, cutOff: String) =
        payDayRepository.getWorkDateExtrasPerPay(employerId, cutOff)

    suspend fun insertPayPeriodExtra(payPeriodExtra: WorkPayPeriodExtras) =
        payDayRepository.insertPayPeriodExtra(payPeriodExtra)

    suspend fun updatePayPeriodExtra(payPeriodExtra: WorkPayPeriodExtras) =
        payDayRepository.updatePayPeriodExtra(payPeriodExtra)

    fun getPayPeriodExtras(payPeriodId: Long) =
        payDayRepository.getPayPeriodExtras(payPeriodId)
}