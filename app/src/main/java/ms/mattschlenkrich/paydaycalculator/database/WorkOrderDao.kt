package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrderHistoryFull

@Dao
interface WorkOrderDao {
    @Insert
    suspend fun insertWorkOrder(workOrder: WorkOrder)

    @Update
    suspend fun updateWorkOrder(workOrder: WorkOrder)

    @Query(
        "UPDATE workOrders " +
                "SET woDeleted = 1, " +
                "woUpdateTime = ':updateTime' " +
                "WHERE workOrderId = :workOrderId"
    )
    suspend fun deleteWorkOrder(workOrderId: Long, updateTime: String)

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE workOrderId = :workOrderNum " +
                "AND woDeleted = 0"
    )
    fun getWorkOrder(workOrderNum: String): LiveData<WorkOrder>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId"
    )
    fun getWorkOrdersByEmployerId(employerId: Long): LiveData<List<WorkOrder>>

    @Insert
    suspend fun insertWorkOrderHistory(history: WorkOrderHistory)

    @Update
    suspend fun updateWorkOrderHistory(history: WorkOrderHistory)

    @Query(
        "UPDATE workOrderHistory " +
                "SET woHistoryDeleted = 1, " +
                "woHistoryUpdateTime = ':updateTime' " +
                "WHERE woHistoryId = :historyID"
    )
    suspend fun deleteWorkOrderHistory(historyID: Long, updateTime: String)

    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkDateId = :workDateId " +
                "AND woHistoryDeleted = 0 " +
                "Order by woHistoryUpdateTime"
    )
    fun getWorkOrderHistory(workDateId: Long): LiveData<List<WorkOrderHistoryFull>>
}