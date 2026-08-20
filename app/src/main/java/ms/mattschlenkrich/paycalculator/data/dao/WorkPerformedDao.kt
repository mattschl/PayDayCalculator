package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformedMerged
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryWorkPerformedCombined
import ms.mattschlenkrich.paycalculator.data.model.WorkPerformedAndChild

@Dao
interface WorkPerformedDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkPerformed(workPerformed: WorkPerformed)

    @Query(
        "UPDATE workPerformed " +
                "SET wpIsDeleted = 1," +
                "wpUpdateTime = :updateTime " +
                "WHERE workPerformedId = :workPerformedId"
    )
    suspend fun deleteWorkPerformed(workPerformedId: Long, updateTime: String)


    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE wpIsDeleted = 0 " +
                "ORDER BY wpDescription"
    )
    fun getWorkPerformedAll(): LiveData<List<WorkPerformed>>

    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE wpIsDeleted = 0 " +
                "ORDER BY wpDescription"
    )
    suspend fun getWorkPerformedAllSync(): List<WorkPerformed>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM workPerformedMerged " +
                "WHERE wpmMasterId = :workPerformedId " +
                "AND wpmIsDeleted = 0"
    )
    fun getWorkPerformedAndChildList(workPerformedId: Long): LiveData<List<WorkPerformedAndChild>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged)

    @Query(
        "UPDATE workPerformedMerged " +
                "SET wpmIsDeleted = 1, " +
                "wpmUpdateTime = :updateTime " +
                "WHERE workPerformedMergeId = :workPerformedMergedId"
    )
    suspend fun deleteWorkPerformedMerged(workPerformedMergedId: Long, updateTime: String)


    @Query(
        "Update workOrderHistoryWorkPerformed " +
                "SET wowpWorkPerformedId = :newWorkPerformedId " +
                "WHERE wowpWorkPerformedId = :oldWorkPerformedId"
    )
    suspend fun updateWorkPerformedMerged(oldWorkPerformedId: Long, newWorkPerformedId: Long)

    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE wpDescription LIKE :query " +
                "AND wpIsDeleted = 0 " +
                "ORDER BY wpDescription"
    )
    fun searchFromWorkPerformed(query: String): LiveData<List<WorkPerformed>>

    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE wpDescription = :description " +
                "AND wpIsDeleted = 0"
    )
    suspend fun getWorkPerformedSync(description: String): WorkPerformed?

    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE wpDescription = :description"
    )
    suspend fun getWorkPerformedAnySync(description: String): WorkPerformed?

    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE workPerformedId = :workPerformedId " +
                "AND wpIsDeleted = 0"
    )
    fun getWorkPerformed(workPerformedId: Long): LiveData<WorkPerformed>

    @Update
    suspend fun updateWorkPerformed(workPerformed: WorkPerformed)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    )

    @Update
    suspend fun updateWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    )

    @Query(
        "UPDATE workOrderHistoryWorkPerformed  " +
                "SET wowpIsDeleted = 1, " +
                "wowpUpdateTime = :updateTime " +
                "WHERE wowpHistoryId = :historyId"
    )
    suspend fun removeAllWorkPerformedFromWorkOrderHistory(historyId: Long, updateTime: String)

    @Query(
        "UPDATE workOrderHistoryWorkPerformed " +
                "SET wowpIsDeleted = 1, " +
                "wowpUpdateTime = :updateTime " +
                "WHERE workOrderHistoryWorkPerformedId = :historyWorkPerformedId"
    )
    suspend fun deleteWorkOrderHistoryWorkPerformed(
        historyWorkPerformedId: Long,
        updateTime: String
    )

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryWorkPerformed " +
                "WHERE wowpIsDeleted = 0 " +
                "AND wowpHistoryId = :historyId " +
                "ORDER BY wowpAreaId, wowpSequence, " +
                "wowpUpdateTime"
    )
    fun getWorkPerformedByWorkOrderHistory(historyId: Long):
            LiveData<List<WorkOrderHistoryWorkPerformedCombined>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryWorkPerformed " +
                "WHERE workOrderHistoryWorkPerformedId = :historyWorkPerformedId " +
                "AND wowpIsDeleted = 0"
    )
    fun getWorkPerformedHistoryById(historyWorkPerformedId: Long):
            LiveData<WorkOrderHistoryWorkPerformedCombined>
}