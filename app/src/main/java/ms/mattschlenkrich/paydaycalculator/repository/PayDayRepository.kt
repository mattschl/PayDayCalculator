package ms.mattschlenkrich.paydaycalculator.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.WorkDates

class PayDayRepository(private val db: PayDatabase) {

    fun getCutOffDates(employerId: Long) =
        db.getPayDayDao().getCutOffDates(employerId)

    suspend fun insertPayPeriod(cutOff: PayPeriods) =
        db.getPayDayDao().insertPayPeriod(cutOff)

    fun getWorkDateList(employerId: Long, cutOff: String) =
        db.getPayDayDao().getWorkDateList(employerId, cutOff)

    suspend fun insertWorkDate(workDate: WorkDates) =
        db.getPayDayDao().insertWorkDate(workDate)

    fun getWorkDatesAndExtras(employerId: Long, cutOffDate: String) =
        db.getPayDayDao().getWorkDatesAndExtras(employerId, cutOffDate)
}