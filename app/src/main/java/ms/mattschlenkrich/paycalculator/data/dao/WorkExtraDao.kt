package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_EXTRA_TYPES
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefTypeAndEmployer
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefinitionAndType

@Dao
interface WorkExtraDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkExtraDefinition(definition: WorkExtrasDefinitions)

    @Update
    suspend fun updateWorkExtraDefinition(definition: WorkExtrasDefinitions)

    @Query(
        "UPDATE $TABLE_WORK_EXTRAS_DEFINITIONS " +
                "SET weIsDeleted = 1, " +
                "weUpdateTime = :updateTime " +
                "WHERE workExtraDefId = :id"
    )
    suspend fun deleteWorkExtraDefinition(id: Long, updateTime: String)

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRAS_DEFINITIONS " +
                "WHERE weEmployerId = :employerId " +
                "AND weExtraTypeId = :extraTypeId " +
                "AND weIsDeleted = 0 " +
                "ORDER BY weEffectiveDate DESC "
    )
    fun getActiveExtraDefinitionsFull(
        employerId: Long,
        extraTypeId: Long
    ): LiveData<List<ExtraDefTypeAndEmployer>>

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRA_TYPES " +
                "WHERE wetEmployerId = :employerId " +
                "AND wetIsDeleted = 0 " +
                "ORDER BY wetName COLLATE NOCASE"
    )
    fun getExtraDefTypes(employerId: Long): LiveData<List<WorkExtraTypes>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkExtraType(workExtraType: WorkExtraTypes)

    @Update
    suspend fun updateWorkExtraType(extraType: WorkExtraTypes)

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRA_TYPES " +
                "WHERE wetEmployerId = :employerId " +
                "AND wetIsDeleted = 0 " +
                "ORDER BY wetName COLLATE NOCASE"
    )
    fun getWorkExtraTypeList(employerId: Long): LiveData<List<WorkExtraTypes>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workExtraTypes " +
                "JOIN ( " +
                "SELECT  * FROM workExtrasDefinitions " +
                "WHERE weEffectiveDate <= :cutoffDate " +
                "AND weIsDeleted = 0 " +
                "GROUP BY weExtraTypeId " +
                "ORDER BY weEffectiveDate DESC " +
                ") ON workExtraTypeId = weExtraTypeId " +
                "WHERE wetEmployerId = :employerId " +
                "AND wetAttachTo = 1 " +
                "AND wetIsDeleted = 0 " +
                "ORDER BY wetName"
    )
    fun getExtraTypesAndDefByDaily(employerId: Long, cutoffDate: String):
            LiveData<List<ExtraDefinitionAndType>>

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRA_TYPES " +
                "WHERE wetEmployerId = :employerId " +
                "AND wetAttachTo = 1 " +
                "AND wetIsDeleted = 0 " +
                "ORDER BY wetName COLLATE NOCASE"
    )
    fun getExtraTypesByDaily(employerId: Long): LiveData<List<WorkExtraTypes>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workExtraTypes " +
                "JOIN ( " +
                "SELECT * FROM workExtrasDefinitions " +
                "WHERE weExtraTypeId = :typeId " +
                "AND weEffectiveDate <= :cutoffDate " +
                "ORDER BY weEffectiveDate DESC " +
                "LIMIT 1 " +
                ") on " +
                "workExtraTypeId = weExtraTypeId " +
                "WHERE workExtraTypeId = :typeId " +
                "AND wetIsDeleted = 0"
    )
    suspend fun getExtraTypeAndDefByTypeIdSync(typeId: Long, cutoffDate: String):
            ExtraDefinitionAndType?

    @Insert
    suspend fun insertWorkDateExtra(extra: WorkDateExtras)

    @Update
    suspend fun updateWorkDateExtra(extra: WorkDateExtras)

    @Query(
        "SELECT * FROM workDateExtras " +
                "WHERE wdeWorkDateId = :workDateId " +
                "AND wdeIsDeleted = 0"
    )
    fun getWorkDateExtras(workDateId: Long): LiveData<List<WorkDateExtras>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT *, MAX(weEffectiveDate) FROM workExtraTypes " +
                "JOIN ( " +
                "SELECT * FROM workExtrasDefinitions " +
                "WHERE weEmployerId = :employerId " +
                "AND weIsDeleted = 0 " +
                "AND weEffectiveDate <= :cutoffDate " +
                ") ON workExtraTypeId = weExtraTypeId " +
                "WHERE wetEmployerId = :employerId " +
                "AND wetIsDefault = 1 " +
                "GROUP BY wetName " +
                "ORDER BY wetAppliesTo, wetName"
    )
    fun getDefaultExtraTypesAndCurrentDef(employerId: Long, cutoffDate: String):
            LiveData<List<ExtraDefinitionAndType>>
}