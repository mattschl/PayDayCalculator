package ms.mattschlenkrich.paydaycalculator.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.WorkExtrasDefinitions

class WorkExtraRepository(private val db: PayDatabase) {
    suspend fun insertWorkExtraDefinition(definition: WorkExtrasDefinitions) =
        db.getWorkExtraDao().insertWorkExtraDefinition(definition)

    suspend fun updateWorkExtraDefinition(definition: WorkExtrasDefinitions) =
        db.getWorkExtraDao().updateWorkExtraDefinition(definition)

    suspend fun deleteWorkExtraDefinition(id: Long, updateTime: String) =
        db.getWorkExtraDao().deleteWorkExtraDefinition(id, updateTime)

    fun getWorkExtraDefinitions(employerId: Long) =
        db.getWorkExtraDao().getWorkExtraDefinitions(employerId)

    fun getActiveExtraDefinitionsFull(employerId: Long) =
        db.getWorkExtraDao().getActiveExtraDefinitionsFull(employerId)

    fun getExtraDefinitionsPerDay(employerId: Long) =
        db.getWorkExtraDao().getExtraDefinitionsPerDay(employerId)
}