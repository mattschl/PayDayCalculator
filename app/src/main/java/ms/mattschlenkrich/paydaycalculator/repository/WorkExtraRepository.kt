package ms.mattschlenkrich.paydaycalculator.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtrasDefinitions
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDateExtras

class WorkExtraRepository(private val db: PayDatabase) {
    suspend fun insertWorkExtraDefinition(definition: WorkExtrasDefinitions) =
        db.getWorkExtraDao().insertWorkExtraDefinition(definition)

    suspend fun updateWorkExtraDefinition(definition: WorkExtrasDefinitions) =
        db.getWorkExtraDao().updateWorkExtraDefinition(definition)

    suspend fun deleteWorkExtraDefinition(id: Long, updateTime: String) =
        db.getWorkExtraDao().deleteWorkExtraDefinition(id, updateTime)

//    fun getWorkExtraDefinitions(employerId: Long) =
//        db.getWorkExtraDao().getWorkExtraDefinitions(employerId)
//
//    fun getWorkExtraDefinitions(employerId: Long, extraTypeId: Long) =
//        db.getWorkExtraDao().getWorkExtraDefinitions(employerId, extraTypeId)

    fun getActiveExtraDefinitionsFull(employerId: Long, extraTypeId: Long) =
        db.getWorkExtraDao().getActiveExtraDefinitionsFull(employerId, extraTypeId)

//    fun getExtraDefinitionsPerDay(employerId: Long) =
//        db.getWorkExtraDao().getExtraDefinitionsPerDay(employerId)

    fun getExtraDefTypes(employerId: Long) =
        db.getWorkExtraDao().getExtraDefTypes(employerId)

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

    fun getExtraTypesAndDef(employerId: Long, cutoffDate: String, attachTo: Int) =
        db.getWorkExtraDao().getExtraTypesAndDef(employerId, cutoffDate, attachTo)

    suspend fun insertWorkDateExtra(extra: WorkDateExtras) =
        db.getWorkExtraDao().insertWorkDateExtra(extra)

    suspend fun updateWorkDateExtra(extra: WorkDateExtras) =
        db.getWorkExtraDao().updateWorkDateExtra(extra)

//    fun getWorkDateExtras(workDateId: Long) =
//        db.getWorkExtraDao().getWorkDateExtras(workDateId)
}