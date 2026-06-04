package ms.mattschlenkrich.paycalculator.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.common.TABLE_SYNC_HISTORY
import ms.mattschlenkrich.paycalculator.data.entity.SyncHistory

@Dao
interface SyncHistoryDao {
    @Insert
    suspend fun insertSyncHistory(syncHistory: SyncHistory)

    @Update
    suspend fun updateSyncHistory(syncHistory: SyncHistory)

    @Query("SELECT * FROM $TABLE_SYNC_HISTORY WHERE syncId = :syncId")
    suspend fun getSyncHistory(syncId: Long): SyncHistory?

    @Query("SELECT syncTime FROM $TABLE_SYNC_HISTORY WHERE syncDeviceId = :syncId AND syncStatus = 'Success' ORDER BY syncTime DESC LIMIT 1")
    suspend fun getLastSyncTime(syncId: Long): String?

    @Query("SELECT MIN(lastSync) FROM (SELECT MAX(syncTime) as lastSync FROM $TABLE_SYNC_HISTORY WHERE syncStatus = 'Success' GROUP BY syncDeviceId)")
    suspend fun getEarliestLastSuccessSyncTime(): String?

    @Query("SELECT * FROM $TABLE_SYNC_HISTORY ORDER BY syncTime DESC LIMIT 1")
    suspend fun getLastSyncHistory(): SyncHistory?

    @Query("SELECT * FROM $TABLE_SYNC_HISTORY ORDER BY syncTime DESC")
    suspend fun getAllSyncHistory(): List<SyncHistory>

    @Query("DELETE FROM $TABLE_SYNC_HISTORY WHERE syncId NOT IN (SELECT syncId FROM $TABLE_SYNC_HISTORY ORDER BY syncTime DESC LIMIT :limit)")
    suspend fun purgeOldSyncHistory(limit: Int)
}