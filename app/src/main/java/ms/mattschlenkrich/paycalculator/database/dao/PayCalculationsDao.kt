package ms.mattschlenkrich.paycalculator.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_DATES
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_EXTRA_TYPES
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerPayRates
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.database.model.tax.TaxTypes
import ms.mattschlenkrich.paycalculator.database.model.tax.WorkTaxRules

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
        "SELECT DISTINCT workDateExtras.* " +
                "FROM workDateExtras " +
                " JOIN  ( " +
                "SELECT workDateId " +
                "FROM workDates " +
                "WHERE wdCutoffDate = :cutOff AND " +
                "wdEmployerID = :employerId AND " +
                "wdIsDeleted = 0 " +
                ") ON wdeWorkDateId = workDateId " +
                " WHERE wdeIsDeleted = 0 " +
                "ORDER BY wdeName "
    )
    fun getWorkDateExtrasPerPay(employerId: Long, cutOff: String):
            List<WorkDateExtras>

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
                "AND wetIsDefault = 1 " +
                "AND wetIsDefault = 1 " +
                "AND wetIsDeleted = 0 " +
                "GROUP BY wetName " +
                "ORDER BY wetName"
    )
    fun getDefaultExtraTypesAndCurrentDef(employerId: Long, cutoffDate: String, appliesTo: Int):
            List<ExtraDefinitionAndType>

    @Query(
        "SELECT * FROM workPayPeriodExtras " +
                "WHERE ppePayPeriodId = :payPeriodId " +
                "AND ppeAttachTo = 3 " +
                "ORDER BY ppeName"
    )
    fun getCustomPayPeriodExtras(payPeriodId: Long): List<WorkPayPeriodExtras>

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRA_TYPES " +
                "WHERE wetEmployerId = :employerId " +
                "ORDER BY wetName COLLATE NOCASE"
    )
    fun getExtraTypes(employerId: Long): List<WorkExtraTypes>

    @Query(
        "SELECT tdEffectiveDate FROM taxEffectiveDates " +
                "WHERE tdEffectiveDate < :cutoffDate " +
                "AND tdIsDeleted = 0 " +
                "ORDER BY tdEffectiveDate DESC " +
                "LIMIT 1"
    )
    fun getCurrentEffectiveDate(cutoffDate: String): String

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM taxTypes " +
                "INNER JOIN ( " +
                "SELECT * FROM employerTaxTypes " +
                "WHERE etrEmployerId = :employerId " +
                "AND etrInclude = 1 " +
                "AND etrIsDeleted = 0 " +
                ") ON taxType = etrTaxType " +
                "WHERE ttIsDeleted = 0 " +
                "ORDER BY taxType"
    )
    fun getTaxTypes(employerId: Long): List<TaxTypes>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        " SELECT * FROM workTaxRules  " +
                "WHERE wtEffectiveDate = :effectiveDate " +
                " AND wtIsDeleted = 0 " +
                " ORDER BY wtType, wtLevel"
    )
    fun getTaxRules(effectiveDate: String): List<WorkTaxRules>
}