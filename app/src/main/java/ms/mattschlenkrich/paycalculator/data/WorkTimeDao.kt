package ms.mattschlenkrich.paycalculator.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface WorkTimeDao {
    @Insert()
    suspend fun insertWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked)

    @Query(
        "UPDATE workOrderHistoryTimeWorked " +
                "SET wohtIsDeleted = 1, " +
                "wohtUpdateTime = :updateTime " +
                "WHERE woHistoryTimeWorkedId = :id"
    )
    suspend fun deleteWorkTime(id: Long, updateTime: String)

    @Update()
    suspend fun updateWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked)

    @Update()
    suspend fun updateWorkDate(workDates: WorkDates)

    @Update
    suspend fun updateWorkOrderHistory(workOrderHistory: WorkOrderHistory)

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkDateId = :workDateId " +
                "AND woHistoryDeleted = 0"
    )
    fun getExistingHistories(
        workDateId: Long
    ): List<WorkOrderHistoryCombined>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "INNER JOIN(" +
                "SELECT * FROM workOrderHistoryTimeWorked " +
                "WHERE wohtDateId = :workDateId " +
                ") ON woHistoryId = wohtHistoryId " +
                "WHERE woHistoryDeleted = 0 " +
                "ORDER BY wohtStartTime "
    )
    fun getExistingHistoriesWithTimes(workDateId: Long): List<WorkOrderHistoryTimeWorkedCombined>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "INNER JOIN(" +
                "SELECT * FROM workOrderHistoryTimeWorked " +
                "WHERE wohtDateId = :workDateId " +
                "AND wohtIsDeleted = 0 " +
                ") ON woHistoryId = wohtHistoryId " +
                "WHERE woHistoryDeleted = 0 " +
                "ORDER BY wohtStartTime "
    )
    fun getTimesWorkedByDate(workDateId: Long): LiveData<List<WorkOrderHistoryTimeWorkedCombined>>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId " +
                "AND woDeleted = 0 " +
                "ORDER BY woNumber"
    )
    fun getWorkOrders(employerId: Long): List<WorkOrder>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId " +
                "AND woDeleted = 0 " +
                "ORDER BY woNumber"
    )
    fun getWorkOrderNumbers(employerId: Long): LiveData<List<WorkOrder>>

    @Query(
        "SELECT * FROM workDates " +
                "WHERE workDateId = :workDateId " +
                "AND wdIsDeleted = 0"
    )
    fun getWorkDate(workDateId: Long): WorkDates
}