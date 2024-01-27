package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.common.PER_DAY
import ms.mattschlenkrich.paydaycalculator.common.PER_HOUR
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRA_TYPES
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

    @Transaction
    @Query(
        "SELECT $TABLE_WORK_EXTRAS_DEFINITIONS.*, " +
                "$TABLE_EMPLOYERS.* " +
                "FROM $TABLE_WORK_EXTRAS_DEFINITIONS " +
                "LEFT JOIN $TABLE_EMPLOYERS ON " +
                "$TABLE_WORK_EXTRAS_DEFINITIONS.weEmployerId = " +
                "$TABLE_EMPLOYERS.employerId " +
                "LEFT JOIN $TABLE_WORK_EXTRA_TYPES ON " +
                "$TABLE_WORK_EXTRAS_DEFINITIONS.weExtraTypeId =" +
                "$TABLE_WORK_EXTRA_TYPES.workExtraTypeId " +
                "WHERE $TABLE_WORK_EXTRAS_DEFINITIONS.weEmployerId = :employerId " +
                "ORDER BY $TABLE_WORK_EXTRA_TYPES.wetName " +
                "COLLATE NOCASE"
    )
    fun getActiveExtraDefinitionsFull(employerId: Long): LiveData<List<ExtraDefinitionFull>>

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRAS_DEFINITIONS " +
                "WHERE (weAttachTo = '$PER_HOUR'OR " +
                "weAttachTo = '$PER_DAY') " +
                "AND workExtraDefId = :employerId " +
                "AND weIsDeleted  = 0"
    )
    fun getExtraDefinitionsPerDay(employerId: Long):
            LiveData<List<WorkExtrasDefinitions>>

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRA_TYPES " +
                "WHERE wetEmployerId = :employerId " +
                "ORDER BY wetName COLLATE NOCASE"
    )
    fun getExtraDefTypes(employerId: Long): LiveData<List<WorkExtraTypes>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkExtraType(workExtraType: WorkExtraTypes)
}