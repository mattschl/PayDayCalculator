package ms.mattschlenkrich.paycalculator.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_DATES
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerPayRates
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtraAndTypeAndDef
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates

@Dao
interface PayCalculationsDao {

    @Query(
        "SELECT * FROM employerPayRates " +
                "WHERE eprEmployerId = :employerId " +
                "AND eprEffectiveDate <= :cutoffDate " +
                "AND eprIsDeleted = 0 " +
                "ORDER BY eprEffectiveDate DESC " +
                "LIMIT 1"
    )
    fun getPayRate(employerId: Long, cutoffDate: String): EmployerPayRates

    @Query(
        "SELECT * FROM $TABLE_WORK_DATES " +
                "WHERE wdEmployerId = :employerId " +
                "AND wdCutoffDate = :cutOff " +
                "AND wdIsDeleted = 0 " +
                "ORDER BY wdDate"
    )
    fun getWorkDateList(employerId: Long, cutOff: String): List<WorkDates>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT DISTINCT workDateExtras.*, types.*, defs.* " +
                "FROM workDateExtras " +
                " JOIN " +
                "workDates ON wdeWorkDateId = ( " +
                "SELECT workDateId " +
                "FROM workDates " +
                "WHERE wdCutoffDate = :cutOff AND " +
                "wdEmployerID = :employerId AND " +
                "wdIsDeleted = 0 " +
                ") " +
                " JOIN workExtraTypes as types ON " +
                "workExtraTypeId = wdeExtraTypeId " +
                " JOIN workExtrasDefinitions as defs ON " +
                "wdeExtraTypeId = weExtraTypeId " +
                " WHERE wdeIsDeleted = 0 " +
                "ORDER BY wdeName "
    )
    fun getWorkDateExtrasPerPay(employerId: Long, cutOff: String)
            : List<WorkDateExtraAndTypeAndDef>

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
                "AND wetAppliesTo = :appliesTo " +
                "AND wetIsDeleted = 0 " +
                "GROUP BY wetName " +
                "ORDER BY wetName, weEffectiveDate DESC"
    )
    fun getExtraTypesAndCurrentDef(employerId: Long, cutoffDate: String, appliesTo: Int):
            List<ExtraDefinitionAndType>
}