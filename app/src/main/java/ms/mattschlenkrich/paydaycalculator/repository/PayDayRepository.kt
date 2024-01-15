package ms.mattschlenkrich.paydaycalculator.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods

class PayDayRepository(private val db: PayDatabase) {

    fun getCutOffDates(employerId: Long) =
        db.getPayDayDao().getCutOffDates(employerId)

    suspend fun insertCutOffDate(cutOff: PayPeriods) =
        db.getPayDayDao().insertCutOffDate(cutOff)

    fun getWorkDateList(employerId: Long, cutOff: String) =
        db.getPayDayDao().getWorkDateList(employerId, cutOff)
}