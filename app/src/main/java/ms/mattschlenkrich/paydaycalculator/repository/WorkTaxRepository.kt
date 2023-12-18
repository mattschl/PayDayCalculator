package ms.mattschlenkrich.paydaycalculator.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxTypes

class WorkTaxRepository(private val db: PayDatabase) {
    suspend fun insertTaxType(workTaxType: WorkTaxTypes) =
        db.getWorkTaxDao().insertTaxType(workTaxType)

    suspend fun updateWorkTaxType(workTaxType: WorkTaxTypes) =
        db.getWorkTaxDao().updateWorkTaxType(workTaxType)

    fun getTaxTypes() =
        db.getWorkTaxDao().getTaxTypes()

    suspend fun insertTaxRule(taxRule: WorkTaxRules) =
        db.getWorkTaxDao().insertTaxRule(taxRule)

    suspend fun updateTaxRule(taxRule: WorkTaxRules) =
        db.getWorkTaxDao().updateTaxRule(taxRule)

    fun getTaxRules() =
        db.getWorkTaxDao().getTaxRules()
}