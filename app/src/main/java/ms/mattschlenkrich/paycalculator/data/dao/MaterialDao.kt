package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.model.MaterialAndChild
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryMaterialCombined

@Dao
interface MaterialDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMaterial(material: Material)

    @Update
    suspend fun updateMaterial(material: Material)

    @Query(
        "SELECT * FROM materials " +
                "WHERE mIsDeleted = 0 " +
                "ORDER BY mName"
    )
    fun getMaterialsList(): LiveData<List<Material>>

    @Query(
        "SELECT * FROM materials " +
                "WHERE mIsDeleted = 0 " +
                "ORDER BY mName"
    )
    suspend fun getMaterialsListSync(): List<Material>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM materialMerged " +
                "WHERE mmMasterId = :materialId " +
                "AND mmIsDeleted = 0"
    )
    fun getMaterialAndChildList(materialId: Long): LiveData<List<MaterialAndChild>>

    @Query(
        "SELECT * FROM materials " +
                "WHERE mName LIKE :query " +
                "AND mIsDeleted = 0 " +
                "ORDER BY mName"
    )
    fun searchMaterials(query: String): LiveData<List<Material>>

    @Query(
        "SELECT * FROM materials " +
                "WHERE materialId = :materialId " +
                "AND mIsDeleted = 0"
    )
    fun getMaterial(materialId: Long): LiveData<Material>

    @Query(
        "SELECT * FROM materials " +
                "WHERE materialId = :materialId " +
                "AND mIsDeleted = 0"
    )
    suspend fun getMaterialSync(materialId: Long): Material?

    @Query(
        "SELECT * FROM materials " +
                "WHERE mName = :mName " +
                "AND mIsDeleted = 0"
    )
    suspend fun getMaterialSync(mName: String): Material?

    @Query(
        "SELECT * FROM materials " +
                "WHERE mName = :mName"
    )
    suspend fun getMaterialAnySync(mName: String): Material?

    @Query(
        "UPDATE workOrderHistoryMaterials " +
                "SET wohmMaterialId = :newMaterialID, " +
                "wohmUpdateTime = :updateTime " +
                "WHERE wohmMaterialId = :oldMaterialID "
    )
    suspend fun updateMaterialMerged(oldMaterialID: Long, newMaterialID: Long, updateTime: String)

    @Query(
        "UPDATE materialMerged " +
                "SET mmIsDeleted = 1, " +
                "mmUpdateTime = :updateTime " +
                "WHERE materialMergeId = :materialMergedId"
    )
    suspend fun deleteMaterialMerged(materialMergedId: Long, updateTime: String)

    @Query(
        "UPDATE materials " +
                "SET mIsDeleted = 1, " +
                "mUpdateTime = :updateTime " +
                "WHERE materialId = :materialId"
    )
    suspend fun deleteMaterial(materialId: Long, updateTime: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMaterialMerged(materialMerged: MaterialMerged)


    @Query(
        "UPDATE workOrderHistoryMaterials " +
                "SET wohmIsDeleted = 1, " +
                "wohmUpdateTime = :updateTime " +
                "WHERE wohmHistoryId = :historyId"
    )
    suspend fun removeAllMaterialsFromWorkOrderHistory(historyId: Long, updateTime: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrderHistoryMaterial(
        workOrderHistoryMaterial: WorkOrderHistoryMaterial
    )

    @Update
    suspend fun updateWorkOrderHistoryMaterial(
        workOrderHistoryMaterial: WorkOrderHistoryMaterial
    )

    @Query(
        "UPDATE workOrderHistoryMaterials " +
                "SET wohmIsDeleted = 1," +
                "wohmUpdateTime = :updateTime " +
                "WHERE workOrderHistoryMaterialId = :historyMaterialId"
    )
    suspend fun deleteWorkOrderHistoryMaterial(historyMaterialId: Long, updateTime: String)

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT COALESCE(mmMasterId, wohmMaterialId) as wohmMaterialId, " +
                "wohmHistoryId, " +
                "null as workOrderHistoryMaterialId, " +
                "SUM(wohmQuantity) as wohmQuantity, " +
                "MIN(wohmSequence) as wohmSequence, " +
                "0 as wohmIsDeleted, " +
                "MAX(wohmUpdateTime) as wohmUpdateTime " +
                "FROM workOrderHistoryMaterials " +
                "LEFT JOIN materialMerged ON wohmMaterialId = mmChildId AND mmIsDeleted = 0 " +
                "WHERE wohmHistoryId = :historyId " +
                "AND wohmIsDeleted = 0 " +
                "GROUP BY COALESCE(mmMasterId, wohmMaterialId) " +
                "ORDER BY wohmUpdateTime"
    )
    fun getMaterialsByHistoryCombined(historyId: Long): LiveData<List<WorkOrderHistoryMaterialCombined>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryMaterials " +
                "WHERE wohmHistoryId = :historyId " +
                "AND wohmIsDeleted = 0 " +
                "ORDER BY wohmUpdateTime"
    )
    fun getMaterialsByHistory(historyId: Long): LiveData<List<WorkOrderHistoryMaterialCombined>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "INNER JOIN " +
                "(SELECT * FROM workOrderHistoryMaterials " +
                "WHERE workOrderHistoryMaterialId = :woHistoryMaterialId " +
                "AND wohmIsDeleted = 0) " +
                "ON woHistoryId = wohmHistoryId " +
                "WHERE woHistoryDeleted = 0"
    )
    suspend fun getWorkOrderHistoryMaterialCombined(woHistoryMaterialId: Long):
            WorkOrderHistoryMaterialCombined?
}