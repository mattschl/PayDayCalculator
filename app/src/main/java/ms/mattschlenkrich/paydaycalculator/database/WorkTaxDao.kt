package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_TAX_TYPES
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_TYPE
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxTypes

@Dao
interface WorkTaxDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaxType(workTaxType: WorkTaxTypes)

    @Update
    suspend fun updateWorkTaxType(workTaxType: WorkTaxTypes)

    @Query(
        "SELECT * FROM $TABLE_WORK_TAX_TYPES " +
                "ORDER BY $WORK_TAX_TYPE COLLATE NOCASE"
    )
    fun getTaxTypes(): LiveData<List<WorkTaxTypes>>

}