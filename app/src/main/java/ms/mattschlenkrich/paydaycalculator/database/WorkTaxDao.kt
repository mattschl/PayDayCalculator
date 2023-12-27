package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
import ms.mattschlenkrich.paydaycalculator.model.EmployerTaxTypes
import ms.mattschlenkrich.paydaycalculator.model.TaxEffectiveDates
import ms.mattschlenkrich.paydaycalculator.model.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules

@Dao
interface WorkTaxDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaxType(workTaxType: TaxTypes)

    @Update
    suspend fun updateWorkTaxType(workTaxType: TaxTypes)

    @Query(
        "SELECT * FROM $TABLE_TAX_TYPES " +
                "ORDER BY $WORK_TAX_TYPE COLLATE NOCASE"
    )
    fun getTaxTypes(): LiveData<List<TaxTypes>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaxRule(taxRule: WorkTaxRules)

    @Update
    suspend fun updateTaxRule(taxRule: WorkTaxRules)

    @Query(
        "SELECT * FROM $TABLE_WORK_TAX_RULES " +
                "ORDER BY wtType COLLATE NOCASE"
    )
    fun getTaxRules(): LiveData<List<WorkTaxRules>>

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

    @Insert()
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
}