package ms.mattschlenkrich.paycalculator.database.repository

import ms.mattschlenkrich.paycalculator.database.PayDatabase

class PayCalculationsRepository(private val db: PayDatabase) {

    fun getPayRate(employerId: Long, cutoffDate: String) =
        db.getPayCalculationsDao().getPayRate(employerId, cutoffDate)

    fun getWorkDateList(employerId: Long, cutOff: String) =
        db.getPayCalculationsDao().getWorkDateList(employerId, cutOff)

    fun getWorkDateExtrasPerPay(employerId: Long, cutOff: String) =
        db.getPayCalculationsDao().getWorkDateExtrasPerPay(employerId, cutOff)

    fun getExtraTypesAndCurrentDef(employerId: Long, cutoffDate: String, appliesTo: Int) =
        db.getPayCalculationsDao().getExtraTypesAndCurrentDef(
            employerId, cutoffDate, appliesTo
        )
}