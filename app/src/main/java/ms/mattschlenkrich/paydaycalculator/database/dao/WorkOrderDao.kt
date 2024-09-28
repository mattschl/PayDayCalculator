package ms.mattschlenkrich.paydaycalculator.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.JobSpec
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderJobSpec
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkPerformed

@Dao
interface WorkOrderDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkOrder(workOrder: WorkOrder)

    @Query(
        "Update workOrders " +
                "SET woNumber = :workOrderNumber, " +
                "woEmployerId = :employerId, " +
                "woAddress = :address, " +
                "woDescription = :description," +
                "woDeleted = :isDeleted," +
                "woUpdateTime = :updateTime " +
                "WHERE workOrderId = :workOrderId"
    )
    suspend fun updateWorkOrder(
        workOrderId: Long,
        workOrderNumber: String,
        employerId: Long,
        address: String,
        description: String,
        isDeleted: Boolean,
        updateTime: String,
    )

    @Query(
        "UPDATE workOrders " +
                "SET woDeleted = 1, " +
                "woUpdateTime = :updateTime " +
                "WHERE workOrderId = :workOrderId"
    )
    suspend fun deleteWorkOrder(workOrderId: Long, updateTime: String)


    @Query(
        "UPDATE workOrders " +
                "SET woDeleted = 1, " +
                "woUpdateTime = :updateTime " +
                "WHERE workOrderId = :workOrderNumber"
    )
    suspend fun deleteWorkOrder(workOrderNumber: String, updateTime: String)

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE workOrderId = :workOrderId " +
                "AND woDeleted = 0"
    )
    fun getWorkOrder(workOrderId: Long): LiveData<WorkOrder>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woNumber = :workOrderNum " +
                "AND woDeleted = 0"
    )
    fun getWorkOrder(workOrderNum: String): LiveData<WorkOrder>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId " +
                "AND woDeleted = 0 " +
                "ORDER BY woNumber"
    )
    fun getWorkOrdersByEmployerId(employerId: Long): LiveData<List<WorkOrder>>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId " +
                "AND (woNumber LIKE :query " +
                "OR woAddress LIKE :query) " +
                "ORDER BY woNumber"
    )
    fun searchWorkOrders(employerId: Long, query: String):
            LiveData<List<WorkOrder>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkOrderHistory(history: WorkOrderHistory)

    @Query(
        "Update workOrderHistory " +
                "SET woHistoryWorkOrderId = :workOrderId, " +
                "woHistoryWorkDateId = :workDateId, " +
                "woHistoryRegHours = :regHours, " +
                "woHistoryOtHours = :otHours, " +
                "woHistoryDblOtHours = :dblOtHours, " +
                "woHistoryNote = :note, " +
                "woHistoryDeleted = :isDeleted, " +
                "woHistoryUpdateTime = :updateTime " +
                "WHERE woHistoryId = :historyID"
    )
    suspend fun updateWorkOrderHistory(
        historyID: Long,
        workOrderId: Long,
        workDateId: Long,
        regHours: Double,
        otHours: Double,
        dblOtHours: Double,
        note: String?,
        isDeleted: Boolean,
        updateTime: String
    )

    @Query(
        "UPDATE workOrderHistory " +
                "SET woHistoryDeleted = 1, " +
                "woHistoryUpdateTime = :updateTime " +
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
    fun getWorkOrderHistoriesByDate(workDateId: Long): LiveData<List<WorkOrderHistoryWithDates>>

    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkOrderId = :workOrderId " +
                "AND woHistoryDeleted = 0 " +
                "Order BY woHistoryUpdateTime"
    )
    fun getWorkOrderHistoriesById(workOrderId: Long): LiveData<List<WorkOrderHistoryWithDates>>


    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkOrderId = :workOrderNumber " +
                "AND woHistoryDeleted = 0 " +
                "Order BY woHistoryUpdateTime"
    )
    fun getWorkOrderHistoriesByNumber(workOrderNumber: String): LiveData<List<WorkOrderHistoryWithDates>>

    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryId = :historyID"
    )
    fun getWorkOrderHistory(historyID: Long): LiveData<WorkOrderHistoryWithDates>

    @Query(
        "UPDATE workOrderHistory " +
                "SET woHistoryDeleted = 1, " +
                "woHistoryUpdateTime = :updateTime " +
                "WHERE woHistoryWorkDateId = :workDateId"
    )
    suspend fun deleteWorkOrderHistoryByWorkDateId(
        workDateId: Long, updateTime: String
    )

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertJobSpec(jobSpec: JobSpec)

    @Update
    suspend fun updateJobSpec(jobSpec: JobSpec)

    @Query(
        "SELECT * FROM jobSpecs " +
                "WHERE jsIsDeleted = 0 " +
                "ORDER BY jsName"
    )
    fun getJobSpecsAll(): LiveData<List<JobSpec>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec)

    @Query(
        "UPDATE workOrderJobSpecs " +
                "SET wojsIsDeleted = 1, " +
                "wojsUpdateTime = :updateTime " +
                "WHERE workOrderJobSpecId = :workOrderJobSpecId"
    )
    suspend fun deleteWorkOrderJobSpec(
        workOrderJobSpecId: Long, updateTime: String
    )

    @Transaction
    @Query(
        "SELECT * FROM workOrderJobSpecs " +
                "WHERE wojsIsDeleted = 0 " +
                "AND wojsWorkOrderId = :workOrderId " +
                "ORDER BY wojsSequence, " +
                "wojsUpdateTime"
    )
    fun getWorkOrderJobSpecs(workOrderId: Long):
            LiveData<List<WorkOrderJobSpecCombined>>

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
}