package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.TaxEffectiveDates
import ms.mattschlenkrich.paycalculator.data.entity.TaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkTaxRules

class WorkTaxRepository(private val db: PayDatabase) {
    suspend fun insertTaxType(workTaxType: TaxTypes) {
        val existing = db.getWorkTaxDao().getTaxTypeAnySync(workTaxType.taxType)
        if (existing != null) {
            val updated = workTaxType.copy(
                taxTypeId = existing.taxTypeId,
                ttIsDeleted = false
            )
            db.getWorkTaxDao().updateWorkTaxType(updated)
        } else {
            db.getWorkTaxDao().insertTaxType(workTaxType)
        }
    }

    suspend fun updateWorkTaxType(workTaxType: TaxTypes) =
        db.getWorkTaxDao().updateWorkTaxType(workTaxType)

    fun getTaxTypes() = db.getWorkTaxDao().getTaxTypes()

    suspend fun getTaxTypesSync() = db.getWorkTaxDao().getTaxTypesSync()

    suspend fun insertTaxRule(taxRule: WorkTaxRules) = db.getWorkTaxDao().insertTaxRule(taxRule)

    suspend fun updateTaxRule(taxRule: WorkTaxRules) = db.getWorkTaxDao().updateTaxRule(taxRule)

    fun getTaxRules(taxType: String, effectiveDate: String) =
        db.getWorkTaxDao().getTaxRules(taxType, effectiveDate)

    suspend fun insertEffectiveDate(effectiveDate: TaxEffectiveDates) =
        db.getWorkTaxDao().insertEffectiveDate(effectiveDate)

    fun getTaxEffectiveDates() = db.getWorkTaxDao().getTaxEffectiveDates()

    suspend fun insertEmployerTaxType(employerTaxTypes: EmployerTaxTypes) =
        db.getWorkTaxDao().insertEmployerTaxType(employerTaxTypes)

    suspend fun updateEmployerTaxType(employerTaxTypes: EmployerTaxTypes) =
        db.getWorkTaxDao().updateEmployerTaxType(employerTaxTypes)

    fun getEmployerTaxTypes(employerId: Long) = db.getWorkTaxDao().getEmployerTaxTypes(employerId)
}