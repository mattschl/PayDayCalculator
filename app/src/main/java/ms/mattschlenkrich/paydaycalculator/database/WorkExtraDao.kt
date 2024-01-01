package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paydaycalculator.model.ExtraDefinitionFull
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
                "WHERE workExtraId = :id"
    )
    suspend fun deleteWorkExtraDefinition(id: Long, updateTime: String)

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRAS_DEFINITIONS " +
                "WHERE weEmployerId = :employerId " +
                "ORDER BY weName COLLATE NOCASE"
    )
    fun getWorkExtraDefinitions(employerId: Long): LiveData<List<WorkExtrasDefinitions>>

    @Query(
        "SELECT $TABLE_WORK_EXTRAS_DEFINITIONS.*, " +
                "$TABLE_EMPLOYERS.* " +
                "FROM $TABLE_WORK_EXTRAS_DEFINITIONS " +
                "LEFT JOIN $TABLE_EMPLOYERS ON " +
                "$TABLE_WORK_EXTRAS_DEFINITIONS.weEmployerId = " +
                "$TABLE_EMPLOYERS.employerId " +
                "WHERE $TABLE_WORK_EXTRAS_DEFINITIONS.weEmployerId = :employerId " +
                "ORDER BY $TABLE_WORK_EXTRAS_DEFINITIONS.weName " +
                "COLLATE NOCASE"
    )
    fun getActiveExtraDefinitionsFull(employerId: Long): LiveData<List<ExtraDefinitionFull>>

}