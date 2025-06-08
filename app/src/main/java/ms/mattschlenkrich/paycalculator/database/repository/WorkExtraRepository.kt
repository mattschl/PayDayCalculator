package ms.mattschlenkrich.paycalculator.database.repository

import ms.mattschlenkrich.paycalculator.database.PayDatabase
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtras

class WorkExtraRepository(private val db: PayDatabase) {
    suspend fun insertWorkExtraDefinition(definition: WorkExtrasDefinitions) =
        db.getWorkExtraDao().insertWorkExtraDefinition(definition)

    suspend fun updateWorkExtraDefinition(definition: WorkExtrasDefinitions) =
        db.getWorkExtraDao().updateWorkExtraDefinition(definition)

    suspend fun deleteWorkExtraDefinition(id: Long, updateTime: String) =
        db.getWorkExtraDao().deleteWorkExtraDefinition(id, updateTime)

    fun getActiveExtraDefinitionsFull(employerId: Long, extraTypeId: Long) =
        db.getWorkExtraDao().getActiveExtraDefinitionsFull(employerId, extraTypeId)

    fun getExtraDefTypes(employerId: Long) = db.getWorkExtraDao().getExtraDefTypes(employerId)

    suspend fun insertWorkExtraType(workExtraType: WorkExtraTypes) =
        db.getWorkExtraDao().insertWorkExtraType(workExtraType)

    suspend fun updateWorkExtraType(extraType: WorkExtraTypes) =
        db.getWorkExtraDao().updateWorkExtraType(extraType)

    fun getWorkExtraTypeList(employerId: Long) =
        db.getWorkExtraDao().getWorkExtraTypeList(employerId)

    fun getExtraTypesAndDefByDaily(employerId: Long, cutoffDate: String) =
        db.getWorkExtraDao().getExtraTypesAndDefByDaily(employerId, cutoffDate)

    fun getExtraTypesByDaily(employerId: Long) =
        db.getWorkExtraDao().getExtraTypesByDaily(employerId)

    fun getExtraTypeAndDefByTypeId(typeId: Long, cutoffDate: String) =
        db.getWorkExtraDao().getExtraTypeAndDefByTypeId(typeId, cutoffDate)

    fun getExtraTypesAndDef(employerId: Long, cutoffDate: String, appliesTo: Int) =
        db.getWorkExtraDao().getExtraTypesAndDef(employerId, cutoffDate, appliesTo)

    suspend fun insertWorkDateExtra(extra: WorkDateExtras) =
        db.getWorkExtraDao().insertWorkDateExtra(extra)

    suspend fun updateWorkDateExtra(extra: WorkDateExtras) =
        db.getWorkExtraDao().updateWorkDateExtra(extra)

    fun getDefaultExtraTypesAndCurrentDef(employerId: Long, cutoffDate: String) =
        db.getWorkExtraDao().getDefaultExtraTypesAndCurrentDef(employerId, cutoffDate)
}