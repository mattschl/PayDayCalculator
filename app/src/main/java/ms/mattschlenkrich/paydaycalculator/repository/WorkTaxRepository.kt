package ms.mattschlenkrich.paydaycalculator.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.employer.EmployerTaxTypes
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxEffectiveDates
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.tax.WorkTaxRules

class WorkTaxRepository(private val db: PayDatabase) {
    suspend fun insertTaxType(workTaxType: TaxTypes) =
        db.getWorkTaxDao().insertTaxType(workTaxType)

    suspend fun updateWorkTaxType(workTaxType: TaxTypes) =
        db.getWorkTaxDao().updateWorkTaxType(workTaxType)

    fun getTaxTypes() =
        db.getWorkTaxDao().getTaxTypes()

    fun searchTaxTypes(query: String?) =
        db.getWorkTaxDao().searchTaxTypes(query)

    fun findTaxType(taxType: String) =
        db.getWorkTaxDao().findTaxType(taxType)

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

    fun getTaxTypeAndDef(effectiveDate: String) =
        db.getWorkTaxDao().getTaxTypeAndDef(effectiveDate)

    fun getCurrentEffectiveDate(cutoffDate: String) =
        db.getWorkTaxDao().getCurrentEffectiveDate(cutoffDate)

    fun getTaxTypesByEmployer(employerId: Long) =
        db.getWorkTaxDao().getTaxTypesByEmployer(employerId)

    fun getTaxDefByDate(effectiveDate: String) =
        db.getWorkTaxDao().getTaxDefByDate(effectiveDate)
}