package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates

class WorkTimeRepository(private val db: PayDatabase) {
    suspend fun updateWorkDate(workDates: WorkDates) =
        db.getWorkTimeDao().updateWorkDate(workDates)

    fun getTimesWorkedByDate(workDateId: Long) =
        db.getWorkTimeDao().getTimesWorkedByDate(workDateId)

    fun getWorkOrderNumbers(employerId: Long) =
        db.getWorkTimeDao().getWorkOrderNumbers(employerId)
}