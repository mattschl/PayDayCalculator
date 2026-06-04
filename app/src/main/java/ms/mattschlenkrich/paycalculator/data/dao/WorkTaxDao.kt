package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
import ms.mattschlenkrich.paycalculator.data.entity.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.TaxEffectiveDates
import ms.mattschlenkrich.paycalculator.data.entity.TaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkTaxRules

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
                "WHERE ttIsDeleted = 0 " +
                "ORDER BY $WORK_TAX_TYPE COLLATE NOCASE"
    )
    suspend fun getTaxTypesSync(): List<TaxTypes>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaxRule(taxRule: WorkTaxRules)

    @Update
    suspend fun updateTaxRule(taxRule: WorkTaxRules)

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
        "SELECT * FROM $TABLE_EMPLOYER_TAX_TYPES " +
                "WHERE etrEmployerId = :employerId " +
                "AND etrIsDeleted = 0 " +
                "ORDER BY etrTaxType"
    )
    fun getEmployerTaxTypes(employerId: Long): LiveData<List<EmployerTaxTypes>>
}