package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.data.entity.Areas

@Dao
interface AreaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArea(area: Areas)

    @Update
    suspend fun updateArea(area: Areas)

    @Query(
        "SELECT * FROM areas " +
                "WHERE areaIsDeleted = 0 " +
                "ORDER BY areaName"
    )
    fun getAreasList(): LiveData<List<Areas>>

    @Query(
        "SELECT * FROM areas " +
                "WHERE areaIsDeleted = 0 " +
                "ORDER BY areaName"
    )
    suspend fun getAreasListSync(): List<Areas>

    @Query(
        "SELECT * FROM areas " +
                "WHERE areaId = :areaId " +
                "AND areaIsDeleted = 0"
    )
    fun getArea(areaId: Long): LiveData<Areas>

    @Query(
        "SELECT * FROM areas " +
                "WHERE areaName LIKE :query " +
                "AND areaIsDeleted = 0"
    )
    fun searchAreas(query: String): LiveData<List<Areas>>
}