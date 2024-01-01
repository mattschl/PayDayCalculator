package ms.mattschlenkrich.paydaycalculator.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.WorkExtrasDefinitions

class WorkExtraRepository(private val db: PayDatabase) {
    suspend fun insertWorkExtraDefinition(definition: WorkExtrasDefinitions) =
        db.getWorkExtraDao().insertWorkExtraDefinition(definition)

    suspend fun updateWorkExtraDefinition(definition: WorkExtrasDefinitions) =
        db.getWorkExtraDao().updateWorkExtraDefinition(definition)

    fun getActiveWorkExtraDefinitions() =
        db.getWorkExtraDao().getActiveWorkExtraDefinitions()

    fun getActiveExtraDefinitionsFull(employerId: Long) =
        db.getWorkExtraDao().getActiveExtraDefinitionsFull(employerId)

    fun getExtraDefinitionNamesByEmployer(employerId: Long) =
        db.getWorkExtraDao().getExtraDefinitionNamesByEmployer(employerId)
}