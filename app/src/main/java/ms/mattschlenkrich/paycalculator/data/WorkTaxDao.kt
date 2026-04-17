package ms.mattschlenkrich.paycalculator.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.common.TABLE_EMPLOYER_TAX_TYPES
import ms.mattschlenkrich.paycalculator.common.TABLE_TAX_EFFECTIVE_DATES
import ms.mattschlenkrich.paycalculator.common.TABLE_TAX_TYPES
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_TAX_RULES
import ms.mattschlenkrich.paycalculator.common.TAX_EFFECTIVE_DATE
import ms.mattschlenkrich.paycalculator.common.WORK_TAX_RULE_EFFECTIVE_DATE
import ms.mattschlenkrich.paycalculator.common.WORK_TAX_RULE_LEVEL
import ms.mattschlenkrich.paycalculator.common.WORK_TAX_RULE_TYPE
import ms.mattschlenkrich.paycalculator.common.WORK_TAX_TYPE

@Dao
interface WorkTaxDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaxType(workTaxType: TaxTypes)

    @Update
    suspend fun updateWorkTaxType(workTaxType: TaxTypes)

    @Query(
        "UPDATE $TABLE_TAX_TYPES " +
                "SET ttIsDeleted = 1, " +
                "ttUpdateTime = :updateTime " +
                "WHERE taxTypeId = :taxTypeId"
    )
    suspend fun deleteTaxType(taxTypeId: Long, updateTime: String)

    @Query(
        "SELECT * FROM $TABLE_TAX_TYPES " +
                "WHERE ttIsDeleted = 0 " +
                "ORDER BY $WORK_TAX_TYPE COLLATE NOCASE"
    )
    fun getTaxTypes(): LiveData<List<TaxTypes>>

    @Query(
        "SELECT * FROM $TABLE_TAX_TYPES " +
                "WHERE taxType LIKE :query " +
                "AND ttIsDeleted = 0 " +
                "ORDER BY taxType COLLATE NOCASE"
    )
    fun searchTaxTypes(query: String?): LiveData<List<TaxTypes>>

    @Query(
        "SELECT * FROM taxTypes " +
                "WHERE taxType = :taxType " +
                "AND ttIsDeleted = 0"
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
        "UPDATE $TABLE_WORK_TAX_RULES " +
                "SET wtIsDeleted = 1, " +
                "wtUpdateTime = :updateTime " +
                "WHERE workTaxRuleId = :workTaxRuleId"
    )
    suspend fun deleteTaxRule(workTaxRuleId: Long, updateTime: String)

    @Query(
        "SELECT * FROM $TABLE_WORK_TAX_RULES " +
                "WHERE $WORK_TAX_RULE_TYPE = :taxType " +
                "AND $WORK_TAX_RULE_EFFECTIVE_DATE = :effectiveDate " +
                "AND wtIsDeleted = 0 " +
                "ORDER BY $WORK_TAX_RULE_LEVEL"
    )
    fun getTaxRules(taxType: String, effectiveDate: String):
            LiveData<List<WorkTaxRules>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEffectiveDate(effectiveDate: TaxEffectiveDates)

    @Query(
        "UPDATE $TABLE_TAX_EFFECTIVE_DATES " +
                "SET tdIsDeleted = 1, " +
                "tdUpdateTime = :updateTime " +
                "WHERE tdEffectiveDateId = :id"
    )
    suspend fun deleteEffectiveDate(id: Long, updateTime: String)

    @Query(
        "SELECT * FROM $TABLE_TAX_EFFECTIVE_DATES " +
                "WHERE tdIsDeleted = 0 " +
                "ORDER BY $TAX_EFFECTIVE_DATE DESC"
    )
    fun getTaxEffectiveDates(): LiveData<List<TaxEffectiveDates>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEmployerTaxType(employerTaxTypes: EmployerTaxTypes)

    @Update
    suspend fun updateEmployerTaxType(employerTaxTypes: EmployerTaxTypes)

    @Query(
        "UPDATE $TABLE_EMPLOYER_TAX_TYPES " +
                "SET etrIsDeleted = 1, " +
                "etrUpdateTime = :updateTime " +
                "WHERE etrEmployerId = :employerId AND " +
                "etrTaxType = :taxType"
    )
    suspend fun deleteEmployerTaxType(employerId: Long, taxType: String, updateTime: String)

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
                "AND etrIsDeleted = 0 " +
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
    fun getTaxTypeAndDef(effectiveDate: String): LiveData<List<TaxTypeAndRule>>

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