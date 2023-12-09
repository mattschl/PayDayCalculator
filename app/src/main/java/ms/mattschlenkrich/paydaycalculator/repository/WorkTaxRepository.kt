package ms.mattschlenkrich.paydaycalculator.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxTypes

class WorkTaxRepository(private val db: PayDatabase) {
    suspend fun insertTaxType(workTaxType: WorkTaxTypes) =
        db.getTaxTypeDao().insertTaxType(workTaxType)

    suspend fun updateWorkTaxType(workTaxType: WorkTaxTypes) =
        db.getTaxTypeDao().updateWorkTaxType(workTaxType)

    fun getWorkTypes() =
        db.getTaxTypeDao().getWorkTypes()
}