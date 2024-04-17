package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYER_TAX_TYPES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_TAX_EFFECTIVE_DATES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_TAX_TYPES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_TAX_RULES
import ms.mattschlenkrich.paydaycalculator.common.TAX_EFFECTIVE_DATE
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_EFFECTIVE_DATE
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_LEVEL
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_TYPE
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_TYPE
import ms.mattschlenkrich.paydaycalculator.model.employer.EmployerTaxTypes
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxComplete
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxEffectiveDates
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.tax.WorkTaxRules

@Dao
interface WorkTaxDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaxType(workTaxType: TaxTypes)

    @Update
    suspend fun updateWorkTaxType(workTaxType: TaxTypes)

    @Query(
        "SELECT * FROM $TABLE_TAX_TYPES " +
                "WHERE ttIsDeleted = 0 " +
                "ORDER BY $WORK_TAX_TYPE COLLATE NOCASE"
    )
    fun getTaxTypes(): LiveData<List<TaxTypes>>

    @Query(
        "SELECT * FROM $TABLE_TAX_TYPES " +
                "WHERE taxType LIKE :query " +
                "ORDER BY taxType COLLATE NOCASE"
    )
    fun searchTaxTypes(query: String?): LiveData<List<TaxTypes>>

    @Query(
        "SELECT * FROM taxTypes " +
                "WHERE taxType = :taxType"
    )
    fun findTaxType(taxType: String): LiveData<TaxTypes>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaxRule(taxRule: WorkTaxRules)

    @Update
    suspend fun updateTaxRule(taxRule: WorkTaxRules)

//    @Query(
//        "SELECT * FROM $TABLE_WORK_TAX_RULES " +
//                "ORDER BY wtType COLLATE NOCASE"
//    )
//    fun getTaxRules(): LiveData<List<WorkTaxRules>>

    @Query(
        "SELECT * FROM $TABLE_WORK_TAX_RULES " +
                "WHERE $WORK_TAX_RULE_TYPE = :taxType " +
                "AND $WORK_TAX_RULE_EFFECTIVE_DATE = :effectiveDate " +
                "ORDER BY $WORK_TAX_RULE_LEVEL"
    )
    fun getTaxRules(taxType: String, effectiveDate: String):
            LiveData<List<WorkTaxRules>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEffectiveDate(effectiveDate: TaxEffectiveDates)

    @Query(
        "SELECT * FROM $TABLE_TAX_EFFECTIVE_DATES " +
                "ORDER BY $TAX_EFFECTIVE_DATE DESC"
    )
    fun getTaxEffectiveDates(): LiveData<List<TaxEffectiveDates>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEmployerTaxType(employerTaxTypes: EmployerTaxTypes)

    @Query(
        "UPDATE $TABLE_EMPLOYER_TAX_TYPES " +
                "SET etrInclude = :include " +
                "WHERE etrEmployerId = :employerId AND " +
                "etrTaxType = :taxType"
    )
    suspend fun updateEmployerTaxIncluded(employerId: Long, taxType: String, include: Boolean)

    @Query(
        "SELECT * FROM $TABLE_EMPLOYER_TAX_TYPES " +
                "WHERE etrEmployerId = :employerId " +
                "ORDER BY etrTaxType"
    )
    fun getEmployerTaxTypes(employerId: Long): LiveData<List<EmployerTaxTypes>>

    //    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT taxTypes.*, taxDef.* FROM taxTypes " +
                "JOIN ( " +
                "SELECT * FROM workTaxRules " +
                "WHERE wtEffectiveDate == :effectiveDate " +
                "AND wtIsDeleted == 0 " +
                ") as taxDef " +
                "ON taxTypes.taxType = taxDef.wtType " +
                "WHERE taxTypes.ttIsDeleted == 0 " +
                "ORDER BY taxDef.wtType, taxDef.wtLevel"
    )
    fun getTaxTypeAndDef(effectiveDate: String): LiveData<List<TaxComplete>>

    @Query(
        "SELECT * FROM workTaxRules " +
                "WHERE wtEffectiveDate = :effectiveDate " +
                "AND wtIsDeleted == 0 " +
                "ORDER BY wtType, wtLevel"
    )
    fun getTaxDefByDate(effectiveDate: String): LiveData<List<WorkTaxRules>>

    @Query(
        "SELECT tdEffectiveDate FROM taxEffectiveDates " +
                "WHERE tdEffectiveDate <= :cutoffDate " +
                "ORDER BY tdEffectiveDate DESC " +
                "LIMIT 1"
    )
    fun getCurrentEffectiveDate(cutoffDate: String): LiveData<String>

    @Query(
        " SELECT taxTypes.* FROM taxTypes " +
                "INNER JOIN " +
                "(SELECT * FROM employerTaxTypes " +
                "WHERE etrEmployerId = :employerId " +
                "AND etrIsDeleted = 0  " +
                "AND etrInclude = 1) " +
                "ON etrTaxType = taxType " +
                "WHERE ttIsDeleted = 0 " +
                "ORDER BY taxType"
    )
    fun getTaxTypesByEmployer(employerId: Long): LiveData<List<TaxTypes>>
}