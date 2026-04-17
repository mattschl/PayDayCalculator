package ms.mattschlenkrich.paycalculator.data

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

    suspend fun deleteWorkExtraType(id: Long, updateTime: String) =
        db.getWorkExtraDao().deleteWorkExtraType(id, updateTime)

    fun getWorkExtraTypeList(employerId: Long) =
        db.getWorkExtraDao().getWorkExtraTypeList(employerId)

    fun getExtraTypesAndDefByDaily(employerId: Long, cutoffDate: String) =
        db.getWorkExtraDao().getExtraTypesAndDefByDaily(employerId, cutoffDate)

    fun getExtraTypesByDaily(employerId: Long) =
        db.getWorkExtraDao().getExtraTypesByDaily(employerId)

    fun getExtraTypeAndDefByTypeId(typeId: Long, cutoffDate: String) =
        db.getWorkExtraDao().getExtraTypeAndDefByTypeId(typeId, cutoffDate)

    suspend fun getExtraTypeAndDefByTypeIdSync(typeId: Long, cutoffDate: String) =
        db.getWorkExtraDao().getExtraTypeAndDefByTypeIdSync(typeId, cutoffDate)

    fun getExtraTypesAndDef(employerId: Long, cutoffDate: String, appliesTo: Int) =
        db.getWorkExtraDao().getExtraTypesAndDef(employerId, cutoffDate, appliesTo)

    suspend fun insertWorkDateExtra(extra: WorkDateExtras) =
        db.getWorkExtraDao().insertWorkDateExtra(extra)

    suspend fun deleteWorkDateExtra(id: Long, updateTime: String) =
        db.getWorkExtraDao().deleteWorkDateExtra(id, updateTime)

    suspend fun updateWorkDateExtra(extra: WorkDateExtras) =
        db.getWorkExtraDao().updateWorkDateExtra(extra)

    fun getDefaultExtraTypesAndCurrentDef(employerId: Long, cutoffDate: String) =
        db.getWorkExtraDao().getDefaultExtraTypesAndCurrentDef(employerId, cutoffDate)
}