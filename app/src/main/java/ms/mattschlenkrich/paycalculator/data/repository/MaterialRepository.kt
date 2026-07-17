package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryMaterial

class MaterialRepository(private val db: PayDatabase) {
    suspend fun insertMaterial(material: Material) = db.getMaterialDao().insertMaterial(material)
    suspend fun updateMaterial(material: Material) = db.getMaterialDao().updateMaterial(material)
    fun getMaterialsList() = db.getMaterialDao().getMaterialsList()
    suspend fun getMaterialsListSync() = db.getMaterialDao().getMaterialsListSync()
    fun getMaterialAndChildList(materialId: Long) =
        db.getMaterialDao().getMaterialAndChildList(materialId)

    fun searchMaterials(query: String) = db.getMaterialDao().searchMaterials(query)
    fun getMaterial(materialId: Long) = db.getMaterialDao().getMaterial(materialId)
    suspend fun getMaterialSync(materialId: Long) = db.getMaterialDao().getMaterialSync(materialId)
    suspend fun getMaterialSync(mName: String) = db.getMaterialDao().getMaterialSync(mName)
    suspend fun updateMaterialMerged(oldMaterialID: Long, newMaterialID: Long, updateTime: String) =
        db.getMaterialDao().updateMaterialMerged(oldMaterialID, newMaterialID, updateTime)

    suspend fun deleteMaterialMerged(childId: Long, updateTime: String) =
        db.getMaterialDao().deleteMaterialMerged(childId, updateTime)

    suspend fun insertMaterialMerged(materialMerged: MaterialMerged) =
        db.getMaterialDao().insertMaterialMerged(materialMerged)

    suspend fun deleteMaterial(materialId: Long, updateTime: String) =
        db.getMaterialDao().deleteMaterial(materialId, updateTime)

    suspend fun insertWorkOrderHistoryMaterial(workOrderHistoryMaterial: WorkOrderHistoryMaterial) =
        db.getMaterialDao().insertWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun updateWorkOrderHistoryMaterial(workOrderHistoryMaterial: WorkOrderHistoryMaterial) =
        db.getMaterialDao().updateWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun deleteWorkOrderHistoryMaterial(historyMaterialId: Long, updateTime: String) =
        db.getMaterialDao().deleteWorkOrderHistoryMaterial(historyMaterialId, updateTime)

    fun getMaterialsByHistory(historyId: Long) =
        db.getMaterialDao().getMaterialsByHistory(historyId)

    suspend fun getWorkOrderHistoryMaterialCombined(woHistoryMaterialId: Long) =
        db.getMaterialDao().getWorkOrderHistoryMaterialCombined(woHistoryMaterialId)

    suspend fun removeAllMaterialsFromWorkOrderHistory(historyId: Long, updateTime: String) =
        db.getMaterialDao().removeAllMaterialsFromWorkOrderHistory(historyId, updateTime)
}