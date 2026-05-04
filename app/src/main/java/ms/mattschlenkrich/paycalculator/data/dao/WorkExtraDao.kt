package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_DATE_EXTRAS
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_EXTRA_TYPES
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefTypeAndEmployer
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefinitionAndType
import ms.mattschlenkrich.paycalculator.data.model.ExtraTypeAndDefByDay

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

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRAS_DEFINITIONS " +
                "WHERE weEmployerId = :employerId " +
                "AND weIsDeleted = 0"
    )
    fun getWorkExtraDefinitions(employerId: Long):
            LiveData<List<WorkExtrasDefinitions>>

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRAS_DEFINITIONS " +
                "WHERE weEmployerId = :employerId " +
                "AND weExtraTypeId = :extraTypeId " +
                "AND weIsDeleted = 0 " +
                "ORDER BY weEffectiveDate DESC"
    )
    fun getWorkExtraDefinitions(employerId: Long, extraTypeId: Long):
            LiveData<List<WorkExtrasDefinitions>>

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

    //    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM ExtraTypeAndDefByDay " +
                "WHERE wetEmployerId = :employerId "
    )
    fun getExtraDefinitionsPerDay(employerId: Long):
            LiveData<List<ExtraTypeAndDefByDay>>

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

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "UPDATE $TABLE_WORK_EXTRA_TYPES " +
                "SET wetIsDeleted = 1, " +
                "wetUpdateTime = :updateTime " +
                "WHERE workExtraTypeId = :id"
    )
    suspend fun deleteWorkExtraType(id: Long, updateTime: String)

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRA_TYPES " +
                "WHERE wetEmployerId = :employerId " +
                "AND wetIsDeleted = 0 " +
                "ORDER BY wetName COLLATE NOCASE"
    )
    fun getWorkExtraTypeList(employerId: Long): LiveData<List<WorkExtraTypes>>

    //    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
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

    //    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workExtraTypes " +
                "JOIN ( " +
                "SELECT * FROM workExtrasDefinitions " +
                "WHERE weEmployerId = :employerId " +
                "AND weIsDeleted = 0 " +
                "AND weEffectiveDate <= :cutoffDate " +
                "GROUP BY weExtraTypeId " +
                "ORDER BY weEffectiveDate DESC " +
                ") ON workExtraTypeId = weExtraTypeId " +
                "WHERE wetEmployerId = :employerId " +
                "AND wetAttachTo = 3 " +
                "AND wetIsDeleted = 0"
    )
    fun getExtraTypesAndDefByPay(employerId: Long, cutoffDate: String):
            LiveData<List<ExtraDefinitionAndType>>

    //    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
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
                "AND wetAppliesTo = :appliesTo " +
                "AND wetIsDeleted = 0 " +
                "GROUP BY wetName " +
                "ORDER BY wetName, weEffectiveDate DESC"
    )
    fun getExtraTypesAndDef(employerId: Long, cutoffDate: String, appliesTo: Int):
            LiveData<List<ExtraDefinitionAndType>>

    //    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
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
    fun getExtraTypeAndDefByTypeId(typeId: Long, cutoffDate: String):
            LiveData<ExtraDefinitionAndType>

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
        "UPDATE $TABLE_WORK_DATE_EXTRAS " +
                "SET wdeIsDeleted = 1, " +
                "wdeUpdateTime = :updateTime " +
                "WHERE workDateExtraId = :id"
    )
    suspend fun deleteWorkDateExtra(id: Long, updateTime: String)

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