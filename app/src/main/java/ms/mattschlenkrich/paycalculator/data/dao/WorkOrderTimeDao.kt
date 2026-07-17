package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryTimeWorkedCombined

@Dao
interface WorkOrderTimeDao {
    @Insert
    suspend fun insertTimeWorked(timeWorked: WorkOrderHistoryTimeWorked)

    @Update
    suspend fun updateTimeWorked(timeWorked: WorkOrderHistoryTimeWorked)

    @Query(
        "UPDATE workOrderHistoryTimeWorked " +
                "SET wohtIsDeleted = 1, " +
                "wohtUpdateTime = :updateTime " +
                "WHERE woHistoryTimeWorkedId = :timeWorkedId"
    )
    suspend fun deleteTimeWorked(timeWorkedId: Long, updateTime: String)

    @Query(
        "SELECT * FROM workOrderHistoryTimeWorked " +
                "WHERE wohtHistoryId = :historyId " +
                "AND wohtIsDeleted = 0"
    )
    suspend fun getTimeWorkedForWorkOrderHistorySync(historyId: Long): List<WorkOrderHistoryTimeWorked>

    @Query(
        "SELECT * FROM workOrderHistoryTimeWorked " +
                "WHERE woHistoryTimeWorkedId = :timeWorkedId"
    )
    suspend fun getTimeWorkedSync(timeWorkedId: Long): WorkOrderHistoryTimeWorked?

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryTimeWorked " +
                "WHERE wohtDateId = :workDateId  " +
                "AND wohtIsDeleted = 0 " +
                "order BY wohtStartTime"
    )
    fun getTimeWorkedPerDay(workDateId: Long): LiveData<List<WorkOrderHistoryTimeWorkedCombined>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryTimeWorked " +
                "WHERE wohtHistoryId = :historyId " +
                "AND wohtIsDeleted = 0 " +
                "order BY wohtStartTime"
    )
    fun getTimeWorkedForWorkOrderHistory(historyId: Long): LiveData<List<WorkOrderHistoryTimeWorkedCombined>>
}