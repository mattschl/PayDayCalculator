package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryTimeWorkedCombined

@Dao
interface WorkTimeDao {

    @Update
    suspend fun updateWorkDate(workDates: WorkDates)

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
    fun getWorkOrderNumbers(employerId: Long): LiveData<List<WorkOrder>>
}