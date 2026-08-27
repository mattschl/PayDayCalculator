package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.repository.MaterialRepository

class MaterialViewModel(
    app: Application,
    private val materialRepository: MaterialRepository
) : AndroidViewModel(app) {
    suspend fun insertMaterial(material: Material) = materialRepository.insertMaterial(material)
    suspend fun updateMaterial(material: Material) = materialRepository.updateMaterial(material)
    fun getMaterialsList() = materialRepository.getMaterialsList()
    suspend fun getMaterialsListSync() = materialRepository.getMaterialsListSync()
    fun getMaterialAndChildList(materialId: Long) =
        materialRepository.getMaterialAndChildList(materialId)

    fun searchMaterials(query: String) = materialRepository.searchMaterials(query)
    fun getMaterial(materialId: Long) = materialRepository.getMaterial(materialId)
    suspend fun getMaterialSync(materialId: Long) = materialRepository.getMaterialSync(materialId)
    suspend fun getMaterialSync(mName: String) = materialRepository.getMaterialSync(mName)
    suspend fun updateMaterialMerged(oldMaterialID: Long, newMaterialID: Long, updateTime: String) =
        materialRepository.updateMaterialMerged(oldMaterialID, newMaterialID, updateTime)

    suspend fun deleteMaterialMerged(childId: Long, updateTime: String) =
        materialRepository.deleteMaterialMerged(childId, updateTime)

    suspend fun insertMaterialMerged(materialMerged: MaterialMerged) =
        materialRepository.insertMaterialMerged(materialMerged)

    suspend fun deleteMaterial(materialId: Long, updateTime: String) =
        materialRepository.deleteMaterial(materialId, updateTime)

    suspend fun insertWorkOrderHistoryMaterial(workOrderHistoryMaterial: WorkOrderHistoryMaterial) =
        materialRepository.insertWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun updateWorkOrderHistoryMaterial(workOrderHistoryMaterial: WorkOrderHistoryMaterial) =
        materialRepository.updateWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun deleteWorkOrderHistoryMaterial(historyMaterialId: Long, updateTime: String) =
        materialRepository.deleteWorkOrderHistoryMaterial(historyMaterialId, updateTime)

    fun getMaterialsByHistory(historyId: Long) = materialRepository.getMaterialsByHistory(historyId)
    suspend fun getWorkOrderHistoryMaterialCombined(woHistoryMaterialId: Long) =
        materialRepository.getWorkOrderHistoryMaterialCombined(woHistoryMaterialId)

    suspend fun removeAllMaterialsFromWorkOrderHistory(historyId: Long, updateTime: String) =
        materialRepository.removeAllMaterialsFromWorkOrderHistory(historyId, updateTime)

    suspend fun updateMaterialPrice(materialId: Long, newPrice: Double) {
        val material = materialRepository.getMaterialSync(materialId)
        if (material != null) {
            materialRepository.updateMaterial(
                material.copy(
                    mPrice = newPrice,
                    mUpdateTime = DateFunctions().getCurrentUTCTimeAsString()
                )
            )
        }
    }

    suspend fun getOrCreateMaterial(name: String): Material? {
        if (name.isBlank()) return null
        val existing = materialRepository.getMaterialsListSync().find {
            it.mName.trim().equals(name.trim(), ignoreCase = true)
        }
        if (existing != null) return existing

        val nf = NumberFunctions()
        val df = DateFunctions()
        val newMaterial = Material(
            nf.generateRandomIdAsLong(),
            name.trim(),
            0.0,
            0.0,
            false,
            df.getCurrentUTCTimeAsString()
        )
        materialRepository.insertMaterial(newMaterial)
        return newMaterial
    }
}