package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRA_TYPES
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraDefinitionFull
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraTypeAndDefByDay
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtrasDefinitions
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDateExtras

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
                "WHERE weEmployerId = :employerId "
    )
    fun getWorkExtraDefinitions(employerId: Long): LiveData<List<WorkExtrasDefinitions>>

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRAS_DEFINITIONS " +
                "WHERE weEmployerId = :employerId " +
                "AND weExtraTypeId = :extraTypeId " +
                "ORDER BY weEffectiveDate DESC"
    )
    fun getWorkExtraDefinitions(employerId: Long, extraTypeId: Long):
            LiveData<List<WorkExtrasDefinitions>>

    //    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT $TABLE_WORK_EXTRAS_DEFINITIONS.*, " +
                "$TABLE_EMPLOYERS.*, " +
                "$TABLE_WORK_EXTRA_TYPES.* " +
                "FROM $TABLE_WORK_EXTRAS_DEFINITIONS " +
                "LEFT JOIN $TABLE_EMPLOYERS ON " +
                "$TABLE_WORK_EXTRAS_DEFINITIONS.weEmployerId = " +
                "$TABLE_EMPLOYERS.employerId " +
                "LEFT JOIN $TABLE_WORK_EXTRA_TYPES ON " +
                "$TABLE_WORK_EXTRAS_DEFINITIONS.weExtraTypeId =" +
                "$TABLE_WORK_EXTRA_TYPES.workExtraTypeId " +
                "WHERE $TABLE_WORK_EXTRAS_DEFINITIONS.weEmployerId = :employerId " +
                "AND $TABLE_WORK_EXTRAS_DEFINITIONS.weExtraTypeId = :extraTypeId " +
                "ORDER BY $TABLE_WORK_EXTRAS_DEFINITIONS.weEffectiveDate DESC "
    )
    fun getActiveExtraDefinitionsFull(
        employerId: Long,
        extraTypeId: Long
    ): LiveData<List<ExtraDefinitionFull>>

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
        "SELECT * FROM $TABLE_WORK_EXTRA_TYPES " +
                "WHERE wetEmployerId = :employerId " +
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
                "AND wetAttachTo = :attachTo " +
                "AND wetIsDeleted = 0 " +
                "ORDER BY wetName"
    )
    fun getExtraTypesAndDef(employerId: Long, cutoffDate: String, attachTo: Int):
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

    @Insert
    suspend fun insertWorkDateExtra(extra: WorkDateExtras)

    @Update
    suspend fun updateWorkDateExtra(extra: WorkDateExtras)

    @Query(
        "SELECT * FROM workDateExtras " +
                "WHERE wdeWorkDateId = :workDateId "
    )
    fun getWorkDateExtras(workDateId: Long): LiveData<List<WorkDateExtras>>
}