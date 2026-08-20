package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtrasDefinitions

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

    suspend fun insertWorkExtraType(workExtraType: WorkExtraTypes) {
        val existing = db.getWorkExtraDao().findWorkExtraTypeByNameAnySync(
            workExtraType.wetEmployerId,
            workExtraType.wetName
        )
        if (existing != null) {
            val updated = workExtraType.copy(
                workExtraTypeId = existing.workExtraTypeId,
                wetIsDeleted = false
            )
            db.getWorkExtraDao().updateWorkExtraType(updated)
        } else {
            db.getWorkExtraDao().insertWorkExtraType(workExtraType)
        }
    }

    suspend fun updateWorkExtraType(extraType: WorkExtraTypes) =
        db.getWorkExtraDao().updateWorkExtraType(extraType)

    fun getWorkExtraTypeList(employerId: Long) =
        db.getWorkExtraDao().getWorkExtraTypeList(employerId)

    fun getExtraTypesAndDefByDaily(employerId: Long, cutoffDate: String) =
        db.getWorkExtraDao().getExtraTypesAndDefByDaily(employerId, cutoffDate)

    fun getExtraTypesByDaily(employerId: Long) =
        db.getWorkExtraDao().getExtraTypesByDaily(employerId)

    suspend fun getExtraTypeAndDefByTypeIdSync(typeId: Long, cutoffDate: String) =
        db.getWorkExtraDao().getExtraTypeAndDefByTypeIdSync(typeId, cutoffDate)

    suspend fun insertWorkDateExtra(extra: WorkDateExtras) =
        db.getWorkExtraDao().insertWorkDateExtra(extra)

    suspend fun updateWorkDateExtra(extra: WorkDateExtras) =
        db.getWorkExtraDao().updateWorkDateExtra(extra)
}