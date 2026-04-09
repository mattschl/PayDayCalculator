package ms.mattschlenkrich.paycalculator.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.data.WorkDates
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorkedCombined

@Dao
interface WorkTimeDao {
    @Insert()
    suspend fun insertWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked)

    @Delete()
    suspend fun deleteWorkTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked)

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
    suspend fun getExistingHistories(
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
    suspend fun getWorkOrders(employerId: Long): List<WorkOrder>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId " +
                "AND woDeleted = 0 " +
                "ORDER BY woNumber"
    )
    fun getWorkOrderNumbers(employerId: Long): LiveData<List<WorkOrder>>

    @Query(
        "SELECT * FROM workDates " +
                "WHERE workDateId = :workDateId"
    )
    suspend fun getWorkDate(workDateId: Long): WorkDates
}