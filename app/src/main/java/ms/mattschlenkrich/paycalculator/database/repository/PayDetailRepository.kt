package ms.mattschlenkrich.paycalculator.database.repository

import ms.mattschlenkrich.paycalculator.database.PayDatabase

class PayDetailRepository(private val db: PayDatabase) {
    fun getHoursReg(employerId: Long, cutoffDate: String) =
        db.getPayDetailDao().getHoursReg(employerId, cutoffDate)

    fun getHoursOt(employerId: Long, cutoffDate: String) =
        db.getPayDetailDao().getHoursOt(employerId, cutoffDate)

    fun getHoursDblOt(employerId: Long, cutoffDate: String) =
        db.getPayDetailDao().getHoursDblOt(employerId, cutoffDate)

    fun getHoursStat(employerId: Long, cutoffDate: String) =
        db.getPayDetailDao().getHoursStat(employerId, cutoffDate)

    fun getDaysWorked(employerId: Long, cutoffDate: String) =
        db.getPayDetailDao().getDaysWorked(employerId, cutoffDate)

    fun getPayRate(employerId: Long, cutoffDate: String) =
        db.getPayDetailDao().getPayRate(employerId, cutoffDate)

    fun getWorkDates(employerId: Long, cutoffDate: String) =
        db.getPayDetailDao().getWorkDates(employerId, cutoffDate)

    fun getCustomWorkDateExtras(workDateId: Long) =
        db.getPayDetailDao().getCustomWorkDateExtras(workDateId)

    fun getExtraTypeAndDefBy(employerId: Long, cutoffDate: String, attachTo: Int) =
        db.getPayDetailDao().getExtraTypeAndDefBy(
            employerId, cutoffDate, attachTo
        )
}