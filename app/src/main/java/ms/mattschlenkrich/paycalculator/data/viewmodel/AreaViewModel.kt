package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.Areas
import ms.mattschlenkrich.paycalculator.data.repository.AreaRepository

class AreaViewModel(
    app: Application,
    private val areaRepository: AreaRepository
) : AndroidViewModel(app) {
    suspend fun insertArea(area: Areas) = areaRepository.insertArea(area)
    suspend fun updateArea(area: Areas) = areaRepository.updateArea(area)
    fun getAreasList() = areaRepository.getAreasList()
    suspend fun getAreasListSync() = areaRepository.getAreasListSync()
    fun getArea(areaId: Long) = areaRepository.getArea(areaId)
    fun searchAreas(query: String) = areaRepository.searchAreas(query)

    suspend fun getOrCreateArea(name: String): Areas? {
        if (name.isBlank()) return null
        val existing = getAreasListSync().find {
            it.areaName.trim().equals(name.trim(), ignoreCase = true)
        }
        if (existing != null) return existing

        val nf = NumberFunctions()
        val df = DateFunctions()
        val newArea = Areas(
            nf.generateRandomIdAsLong(),
            name.trim(),
            false,
            df.getCurrentUTCTimeAsString()
        )
        areaRepository.insertArea(newArea)
        return newArea
    }
}