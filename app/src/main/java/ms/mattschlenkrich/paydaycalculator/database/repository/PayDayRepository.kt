package ms.mattschlenkrich.paydaycalculator.database.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkPayPeriodExtras

class PayDayRepository(private val db: PayDatabase) {

    fun getCutOffDates(employerId: Long) =
        db.getPayDayDao().getCutOffDates(employerId)

    suspend fun insertPayPeriod(cutOff: PayPeriods) =
        db.getPayDayDao().insertPayPeriod(cutOff)

    suspend fun updatePayPeriod(payPeriod: PayPeriods) =
        db.getPayDayDao().updatePayPeriod(payPeriod)

    fun getPayPeriod(cutOff: String, employerId: Long) =
        db.getPayDayDao().getPayPeriod(cutOff, employerId)

    fun getWorkDateList(employerId: Long, cutOff: String) =
        db.getPayDayDao().getWorkDateList(employerId, cutOff)

    fun getWorkDateListUsed(employerId: Long, cutOff: String) =
        db.getPayDayDao().getWorkDateListUsed(employerId, cutOff)

    suspend fun insertWorkDate(workDate: WorkDates) =
        db.getPayDayDao().insertWorkDate(workDate)

    suspend fun updateWorkDate(workDate: WorkDates) =
        db.getPayDayDao().updateWorkDate(workDate)

//    suspend fun updateWorkDates(
//        id: Long,
//        payPeriodId: Long,
//        employerId: Long,
//        cutOffDate: String,
//        date: String,
//        regHours: Double,
//        otHours: Double,
//        dblOtHours: Double,
//        statHours: Double,
//        isDeleted: Boolean,
//        updateTime: String
//    ) =
//        db.getPayDayDao().updateWorkDates(
//            id, payPeriodId, employerId, cutOffDate,
//            date, regHours, otHours, dblOtHours,
//            statHours, isDeleted, updateTime
//        )

//    fun getWorkDatesAndExtras(employerId: Long, cutOffDate: String) =
//        db.getPayDayDao().getWorkDatesAndExtras(employerId, cutOffDate)

    suspend fun insertWorkDateExtra(workDateExtra: WorkDateExtras) =
        db.getPayDayDao().insertWorkDateExtra(workDateExtra)

    suspend fun updateWorkDateExtra(workDateExtra: WorkDateExtras) =
        db.getPayDayDao().updateWorkDateExtra(workDateExtra)

    fun getWorkDateExtras(workDateId: Long) =
        db.getPayDayDao().getWorkDateExtras(workDateId)

    fun getWorkDateExtrasActive(workDateId: Long) =
        db.getPayDayDao().getWorkDateExtrasActive(workDateId)

//    suspend fun deleteWorkDateExtra(extraType: WorkExtraTypes) =
//        db.getPayDayDao().deleteWorkDateExtra(extraType)

//    fun getWorkDateAndExtraDefAndWorkDateExtras(workDateId: Long) =
//        db.getPayDayDao().getWorkDateAndExtraDefAndWorkDateExtras(workDateId)

    suspend fun deleteWorkDateExtra(
        extraName: String, workDateId: Long, updateTime: String
    ) = db.getPayDayDao().deleteWorkDateExtra(
        extraName, workDateId, updateTime
    )

    suspend fun deleteWorkDateExtrasByDateId(
        workDateId: Long, updateTime: String
    ) = db.getPayDayDao().deleteWorkDateExtrasByDateId(
        workDateId, updateTime
    )

    fun getWorkDateExtrasPerPay(employerId: Long, cutOff: String) =
        db.getPayDayDao().getWorkDateExtrasPerPay(employerId, cutOff)

    suspend fun insertPayPeriodExtra(payPeriodExtra: WorkPayPeriodExtras) =
        db.getPayDayDao().insertPayPeriodExtra(payPeriodExtra)

    suspend fun updatePayPeriodExtra(payPeriodExtra: WorkPayPeriodExtras) =
        db.getPayDayDao().updatePayPeriodExtra(payPeriodExtra)

    suspend fun deletePayPeriodExtra(extraId: Long, updateTime: String) =
        db.getPayDayDao().deletePayPeriodExtra(extraId, updateTime)

    fun getPayPeriodExtras(payPeriodId: Long) =
        db.getPayDayDao().getPayPeriodExtras(payPeriodId)

//    fun findPayPeriodExtra(workPayPeriodExtraId: Long) =
//        db.getPayDayDao().findPayPeriodExtra(workPayPeriodExtraId)

    fun findPayPeriodExtra(extraName: String) =
        db.getPayDayDao().findPayPeriodExtra(extraName)

    fun getWorkDateExtrasAndDates(cutOffDate: String) =
        db.getPayDayDao().getWorkDateExtrasAndDates(cutOffDate)

}