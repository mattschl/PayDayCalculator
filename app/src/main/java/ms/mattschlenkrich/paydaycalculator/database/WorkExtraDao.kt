package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRA_TYPES
import ms.mattschlenkrich.paydaycalculator.model.ExtraDefinitionAndType
import ms.mattschlenkrich.paydaycalculator.model.ExtraDefinitionFull
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.WorkExtrasDefinitions

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

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
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

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query(
        "SELECT $TABLE_WORK_EXTRAS_DEFINITIONS.*," +
                "$TABLE_WORK_EXTRA_TYPES.* " +
                "FROM $TABLE_WORK_EXTRAS_DEFINITIONS " +
                "LEFT JOIN $TABLE_WORK_EXTRA_TYPES ON " +
                "(weExtraTypeId = ( " +
                "SELECT workExtraTypeId FROM $TABLE_WORK_EXTRA_TYPES " +
                "WHERE weEmployerId = :employerId " +
                "AND (wetAttachTo = 0 OR " +
                "wetAttachTo = 1) " +
                "AND weIsDeleted  = 0)) " +
                "WHERE $TABLE_WORK_EXTRAS_DEFINITIONS.weEffectiveDate = " +
                "(SELECT MAX(weEffectiveDate) FROM $TABLE_WORK_EXTRAS_DEFINITIONS)"
    )
    fun getExtraDefinitionsPerDay(employerId: Long):
            LiveData<List<ExtraDefinitionAndType>>

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

    @Transaction
    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRA_TYPES " +
                "WHERE wetEmployerId = :employerId " +
                "ORDER BY wetName COLLATE NOCASE"
    )
    fun getWorkExtraTypeList(employerId: Long): LiveData<List<WorkExtraTypes>>

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRA_TYPES " +
                "WHERE wetEmployerId = :employerId " +
                "AND (wetAttachTo = 0 OR " +
                "wetAttachTo = 1) " +
                "AND wetIsDeleted = 0"
    )
    fun getExtraTypesByDaily(employerId: Long):
            LiveData<List<WorkExtraTypes>>
}