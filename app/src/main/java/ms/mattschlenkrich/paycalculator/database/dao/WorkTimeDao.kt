package ms.mattschlenkrich.paycalculator.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined

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
    suspend fun getExistingHistoriesWithTimes(workDateId: Long): List<WorkOrderHistoryTimeWorkedCombined>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId "
    )
    suspend fun getWorkOrders(employerId: Long): List<WorkOrder>

    @Query(
        "SELECT * FROM workDates " +
                "WHERE workDateId = :workDateId"
    )
    suspend fun getWorkDate(workDateId: Long): WorkDates
}