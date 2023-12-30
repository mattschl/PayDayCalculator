package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paydaycalculator.model.WorkExtrasDefinitions

@Dao
interface WorkExtraDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkExtraDefinition(definition: WorkExtrasDefinitions)

    @Update
    suspend fun updateWorkExtraDefinition(definition: WorkExtrasDefinitions)

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRAS_DEFINITIONS " +
                "WHERE weIsDeleted = 0 " +
                "ORDER BY weName COLLATE NOCASE"
    )
    fun getActiveWorkExtraDefinitions(): LiveData<List<WorkExtrasDefinitions>>
}