package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkPayPeriodExtras

class PayDayRepository(private val db: PayDatabase) {

    fun getCutOffDates(employerId: Long, limit: Int) = db.getPayDayDao().getCutOffDates(employerId, limit)

    suspend fun getCutOffDatesSync(employerId: Long, limit: Int) =
        db.getPayDayDao().getCutOffDatesSync(employerId, limit)

    suspend fun insertPayPeriod(cutOff: PayPeriods) = db.getPayDayDao().insertPayPeriod(cutOff)

    suspend fun updatePayPeriod(payPeriod: PayPeriods) =
        db.getPayDayDao().updatePayPeriod(payPeriod)

    fun getPayPeriod(cutOff: String, employerId: Long) =
        db.getPayDayDao().getPayPeriod(cutOff, employerId)

    suspend fun getPayPeriodSync(cutOff: String, employerId: Long) =
        db.getPayDayDao().getPayPeriodSync(cutOff, employerId)

    suspend fun getPayPeriodAnySync(cutOff: String, employerId: Long) =
        db.getPayDayDao().getPayPeriodAnySync(cutOff, employerId)

    suspend fun getWorkDateSync(employerId: Long, date: String, cutOff: String) =
        db.getPayDayDao().getWorkDateSync(employerId, date, cutOff)

    fun getWorkDateList(employerId: Long, cutOff: String) =
        db.getPayDayDao().getWorkDateList(employerId, cutOff)

    fun getWorkDatesByDateRange(employerId: Long, firstDate: String, lastDate: String) =
        db.getPayDayDao().getWorkDatesByDateRange(employerId, firstDate, lastDate)

    fun getWorkDateListUsed(employerId: Long, cutOff: String) =
        db.getPayDayDao().getWorkDateListUsed(employerId, cutOff)

    suspend fun insertWorkDate(workDate: WorkDates) {
        val existing = db.getPayDayDao().getWorkDateAnySync(
            workDate.wdEmployerId,
            workDate.wdDate,
            workDate.wdCutoffDate
        )
        if (existing != null) {
            val updated = workDate.copy(
                workDateId = existing.workDateId,
                wdIsDeleted = false
            )
            db.getPayDayDao().updateWorkDate(updated)
            val updateTime = ms.mattschlenkrich.paycalculator.common.DateFunctions()
                .getCurrentUTCTimeAsString()
            val histories =
                db.getWorkOrderDao().getWorkOrderHistoriesByDateSync(existing.workDateId)
            histories.forEach { history ->
                db.getWorkOrderDao().deleteWorkOrderHistory(history.woHistoryId, updateTime)
                db.getWorkPerformedDao()
                    .removeAllWorkPerformedFromWorkOrderHistory(history.woHistoryId, updateTime)
                db.getMaterialDao()
                    .removeAllMaterialsFromWorkOrderHistory(history.woHistoryId, updateTime)
                db.getWorkOrderTimeDao()
                    .removeAllTimeWorkedFromWorkOrderHistory(history.woHistoryId, updateTime)
            }
            db.getPayDayDao().removeAllWorkDateExtras(existing.workDateId, updateTime)
        } else {
            db.getPayDayDao().insertWorkDate(workDate)
        }
    }

    suspend fun updateWorkDate(workDate: WorkDates) {
        val existing = db.getPayDayDao().getWorkDateByIdAnySync(workDate.workDateId)
        if (existing != null && existing.wdIsDeleted && !workDate.wdIsDeleted) {
            val updateTime = ms.mattschlenkrich.paycalculator.common.DateFunctions()
                .getCurrentUTCTimeAsString()
            val histories =
                db.getWorkOrderDao().getWorkOrderHistoriesByDateSync(workDate.workDateId)
            histories.forEach { history ->
                db.getWorkOrderDao().deleteWorkOrderHistory(history.woHistoryId, updateTime)
                db.getWorkPerformedDao()
                    .removeAllWorkPerformedFromWorkOrderHistory(history.woHistoryId, updateTime)
                db.getMaterialDao()
                    .removeAllMaterialsFromWorkOrderHistory(history.woHistoryId, updateTime)
                db.getWorkOrderTimeDao()
                    .removeAllTimeWorkedFromWorkOrderHistory(history.woHistoryId, updateTime)
            }
            db.getPayDayDao().removeAllWorkDateExtras(workDate.workDateId, updateTime)
        }
        db.getPayDayDao().updateWorkDate(workDate)
    }

    suspend fun insertWorkDateExtra(workDateExtra: WorkDateExtras) =
        db.getPayDayDao().insertWorkDateExtra(workDateExtra)

    suspend fun updateWorkDateExtra(workDateExtra: WorkDateExtras) =
        db.getPayDayDao().updateWorkDateExtra(workDateExtra)

    fun getWorkDateExtras(workDateId: Long) = db.getPayDayDao().getWorkDateExtras(workDateId)

    suspend fun deleteWorkDateExtra(
        extraName: String, workDateId: Long, updateTime: String
    ) = db.getPayDayDao().deleteWorkDateExtra(
        extraName, workDateId, updateTime
    )

    fun getWorkDateExtrasPerPay(employerId: Long, cutOff: String) =
        db.getPayDayDao().getWorkDateExtrasPerPay(employerId, cutOff)

    suspend fun insertPayPeriodExtra(payPeriodExtra: WorkPayPeriodExtras) =
        db.getPayDayDao().insertPayPeriodExtra(payPeriodExtra)

    suspend fun updatePayPeriodExtra(payPeriodExtra: WorkPayPeriodExtras) =
        db.getPayDayDao().updatePayPeriodExtra(payPeriodExtra)

    fun getPayPeriodExtras(payPeriodId: Long) = db.getPayDayDao().getPayPeriodExtras(payPeriodId)

}