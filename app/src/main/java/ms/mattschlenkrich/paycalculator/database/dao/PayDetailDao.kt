package ms.mattschlenkrich.paycalculator.database.dao

import androidx.room.Dao
import androidx.room.Query
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates

@Dao
interface PayDetailDao {
    @Query(
        "SELECT SUM(wdRegHours) FROM workDates " +
                "WHERE wdEmployerId = :employerId " +
                "AND wdCutoffDate = :cutoffDate " +
                "AND wdIsDeleted = 0"
    )
    fun getHoursReg(employerId: Long, cutoffDate: String): Double

    @Query(
        "SELECT SUM(wdOtHours) FROM workDates " +
                "WHERE wdEmployerId = :employerId " +
                "AND wdCutoffDate = :cutoffDate " +
                "AND wdIsDeleted = 0"
    )
    fun getHoursOt(employerId: Long, cutoffDate: String): Double

    @Query(
        "SELECT SUM(wdDblOtHours) FROM workDates " +
                "WHERE wdEmployerId = :employerId " +
                "AND wdCutoffDate = :cutoffDate  " +
                "AND wdIsDeleted = 0"
    )
    fun getHoursDblOt(employerId: Long, cutoffDate: String): Double

    @Query(
        "SELECT SUM(wdStatHours) FROM workDates " +
                "WHERE wdEmployerId = :employerId " +
                "AND wdCutoffDate = :cutoffDate  " +
                "AND wdIsDeleted = 0"
    )
    fun getHoursStat(employerId: Long, cutoffDate: String): Double

    @Query(
        "SELECT COUNT(workDateId) FROM workDates " +
                "WHERE wdEmployerId = :employerId " +
                "AND wdCutoffDate = :cutoffDate  " +
                "AND wdIsDeleted = 0 " +
                "AND (wdRegHours > 0 " +
                "OR wdOtHours > 0 " +
                "OR wdDblOtHours > 0)"
    )
    fun getDaysWorked(employerId: Long, cutoffDate: String): Int

    @Query(
        "SELECT eprPayRate FROM employerPayRates " +
                "WHERE eprEmployerId = :employerId " +
                "AND eprEffectiveDate > :cutoffDate " +
                "ORDER BY eprEffectiveDate " +
                "LIMIT 1"
    )
    fun getPayRate(employerId: Long, cutoffDate: String): Double

    @Query(
        "SELECT * FROM workDates " +
                "WHERE wdEmployerId = :employerId " +
                "AND wdCutoffDate = :cutoffDate " +
                "AND wdIsDeleted = 0 " +
                "ORDER BY wdDate"
    )
    fun getWorkDates(employerId: Long, cutoffDate: String): List<WorkDates>
}