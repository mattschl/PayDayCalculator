package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.data.entity.MaterialCostHistory

@Dao
interface MaterialCostHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMaterialCostHistory(materialCostHistory: MaterialCostHistory)

    @Update
    suspend fun updateMaterialCostHistory(materialCostHistory: MaterialCostHistory)

    @Query("SELECT * FROM materialCostHistory WHERE materialExpenseHistoryId = :id")
    suspend fun getMaterialCostHistory(id: Long): MaterialCostHistory?

    @Query("SELECT * FROM materialCostHistory WHERE mehMaterialId = :materialId AND mehIsDeleted = 0 ORDER BY mehPurchaseDate DESC")
    fun getMaterialCostHistoryByMaterial(materialId: Long): LiveData<List<MaterialCostHistory>>

    @Query("SELECT * FROM materialCostHistory WHERE mehMaterialId = :materialId AND mehIsDeleted = 0 ORDER BY mehPurchaseDate DESC")
    suspend fun getMaterialCostHistoryByMaterialSync(materialId: Long): List<MaterialCostHistory>

    @Query("SELECT * FROM materialCostHistory ORDER BY mehUpdateTime DESC")
    suspend fun getAllMaterialCostHistorySync(): List<MaterialCostHistory>
}