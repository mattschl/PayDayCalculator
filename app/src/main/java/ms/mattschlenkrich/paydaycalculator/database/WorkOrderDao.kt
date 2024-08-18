package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
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

    @Delete
    suspend fun deleteWorkOrder(workOrder: WorkOrder)

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId"
    )
    fun getWorkOrdersByEmployerId(employerId: Long): LiveData<List<WorkOrder>>

    @Insert
    suspend fun insertWorkOrderHistory(history: WorkOrderHistory)

    @Update
    suspend fun updateWorkOrderHistory(history: WorkOrderHistory)

    @Delete
    suspend fun deleteWorkOrderHistory(history: WorkOrderHistory)

    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkDateId = :workDateId " +
                "Order by woHistoryWorkOrderId"
    )
    fun getWorkOrderHistory(workDateId: Long): LiveData<List<WorkOrderHistoryFull>>
}