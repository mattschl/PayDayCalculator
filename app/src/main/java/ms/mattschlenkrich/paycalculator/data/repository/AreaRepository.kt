package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.Areas

class AreaRepository(private val db: PayDatabase) {
    suspend fun insertArea(area: Areas) = db.getAreaDao().insertArea(area)
    suspend fun updateArea(area: Areas) = db.getAreaDao().updateArea(area)
    fun getAreasList() = db.getAreaDao().getAreasList()
    suspend fun getAreasListSync() = db.getAreaDao().getAreasListSync()
    fun getArea(areaId: Long) = db.getAreaDao().getArea(areaId)
    fun searchAreas(query: String) = db.getAreaDao().searchAreas(query)
}