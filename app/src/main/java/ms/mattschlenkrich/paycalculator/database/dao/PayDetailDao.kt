package ms.mattschlenkrich.paycalculator.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtras
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
                "AND eprEffectiveDate < :cutoffDate " +
                "AND eprIsDeleted = 0  " +
                "ORDER BY eprEffectiveDate DESC " +
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

    @Query(
        "SELECT * FROM workDateExtras " +
                "WHERE wdeWorkDateId = :workDateId " +
                "ORDER BY wdeName"
    )
    fun getCustomWorkDateExtras(workDateId: Long): List<WorkDateExtras>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT *, MAX(weEffectiveDate) FROM workExtraTypes " +
                "JOIN ( " +
                "SELECT * FROM workExtrasDefinitions " +
                "WHERE weEmployerId = :employerId " +
                "AND weIsDeleted = 0 " +
                "AND weEffectiveDate <= :cutoffDate " +
                ") ON workExtraTypeId = weExtraTypeId " +
                "WHERE wetEmployerId = :employerId " +
                "AND wetAttachTo = :attachTo " +
                "GROUP BY wetName " +
                "ORDER BY wetAppliesTo, wetName"
    )
    fun getExtraTypeAndDefBy(employerId: Long, cutoffDate: String, attachTo: Int):
            List<ExtraDefinitionAndType>
}