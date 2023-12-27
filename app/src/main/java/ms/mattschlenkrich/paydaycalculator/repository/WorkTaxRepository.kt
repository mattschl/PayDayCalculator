package ms.mattschlenkrich.paydaycalculator.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.EmployerTaxTypes
import ms.mattschlenkrich.paydaycalculator.model.TaxEffectiveDates
import ms.mattschlenkrich.paydaycalculator.model.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules

class WorkTaxRepository(private val db: PayDatabase) {
    suspend fun insertTaxType(workTaxType: TaxTypes) =
        db.getWorkTaxDao().insertTaxType(workTaxType)

    suspend fun updateWorkTaxType(workTaxType: TaxTypes) =
        db.getWorkTaxDao().updateWorkTaxType(workTaxType)

    fun getTaxTypes() =
        db.getWorkTaxDao().getTaxTypes()

    suspend fun insertTaxRule(taxRule: WorkTaxRules) =
        db.getWorkTaxDao().insertTaxRule(taxRule)

    suspend fun updateTaxRule(taxRule: WorkTaxRules) =
        db.getWorkTaxDao().updateTaxRule(taxRule)

    fun getTaxRules(taxType: String, effectiveDate: String) =
        db.getWorkTaxDao().getTaxRules(taxType, effectiveDate)

    suspend fun insertEffectiveDate(effectiveDate: TaxEffectiveDates) =
        db.getWorkTaxDao().insertEffectiveDate(effectiveDate)

    fun getTaxEffectiveDates() =
        db.getWorkTaxDao().getTaxEffectiveDates()

    suspend fun insertEmployerTaxType(employerTaxTypes: EmployerTaxTypes) =
        db.getWorkTaxDao().insertEmployerTaxType(employerTaxTypes)

    suspend fun updateEmployerTaxIncluded(
        employerId: Long, taxType: String, include: Boolean
    ) =
        db.getWorkTaxDao().updateEmployerTaxIncluded(
            employerId, taxType, include
        )

    fun getEmployerTaxTypes(employerId: Long) =
        db.getWorkTaxDao().getEmployerTaxTypes(employerId)
}