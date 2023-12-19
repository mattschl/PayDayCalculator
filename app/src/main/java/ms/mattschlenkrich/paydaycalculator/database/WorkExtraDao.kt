package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRA_FREQUENCIES
import ms.mattschlenkrich.paydaycalculator.common.WORK_EXTRA_FREQUENCY
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraFrequencies

@Dao
interface WorkExtraDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExtraFrequency(extraFrequency: WorkExtraFrequencies)

    @Update
    suspend fun updateExtraFrequency(extraFrequency: WorkExtraFrequencies)

    @Query(
        "SELECT * FROM $TABLE_WORK_EXTRA_FREQUENCIES " +
                "ORDER BY $WORK_EXTRA_FREQUENCY COLLATE NOCASE"
    )
    fun getWorkExtraFrequency(): LiveData<List<WorkExtraFrequencies>>
}