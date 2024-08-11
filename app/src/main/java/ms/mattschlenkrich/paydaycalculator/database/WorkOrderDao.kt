package ms.mattschlenkrich.paydaycalculator.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrder

@Dao
interface WorkOrderDao {
    @Insert
    suspend fun insertWorkOrder(workOrder: WorkOrder)

    @Update
    suspend fun updateWorkOrder(workOrder: WorkOrder)

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId"
    )
    fun getWorkOrdersByEmployerId(employerId: Long): LiveData<WorkOrder>
}