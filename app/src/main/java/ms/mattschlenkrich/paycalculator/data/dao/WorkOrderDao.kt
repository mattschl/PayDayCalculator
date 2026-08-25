package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryExpense
import ms.mattschlenkrich.paycalculator.data.model.ExpenseSummary
import ms.mattschlenkrich.paycalculator.data.model.MaterialAndQuantity
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderSummary
import ms.mattschlenkrich.paycalculator.data.model.WorkPerformedAndQuantity

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
        "SELECT * FROM workOrders " +
                "WHERE workOrderId = :workOrderId " +
                "AND woDeleted = 0"
    )
    fun getWorkOrder(workOrderId: Long): LiveData<WorkOrder>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woNumber = :workOrderNum " +
                "AND woEmployerId = :employerId " +
                "AND woDeleted = 0"
    )
    suspend fun findWorkOrder(workOrderNum: String, employerId: Long): WorkOrder?

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woNumber = :workOrderNum " +
                "AND woEmployerId = :employerId"
    )
    suspend fun findWorkOrderAnySync(workOrderNum: String, employerId: Long): WorkOrder?

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId " +
                "AND woDeleted = 0 " +
                "ORDER BY woNumber"
    )
    fun getWorkOrdersByEmployerId(employerId: Long): LiveData<List<WorkOrder>>

    @Query(
        "SELECT DISTINCT woAddress FROM workOrders " +
                "WHERE woEmployerId = :employerId " +
                "AND woDeleted = 0 " +
                "ORDER BY woAddress COLLATE NOCASE"
    )
    fun getUniqueAddresses(employerId: Long): LiveData<List<String>>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId " +
                "AND (woNumber LIKE :query " +
                "OR woAddress LIKE :query) " +
                "AND woDeleted = 0 " +
                "ORDER BY woNumber"
    )
    fun searchWorkOrders(employerId: Long, query: String): LiveData<List<WorkOrder>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkOrderHistory(history: WorkOrderHistory)

    @Update
    suspend fun updateWorkOrderHistory(history: WorkOrderHistory)

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
                "WHERE woHistoryId = :historyId"
    )
    suspend fun deleteWorkOrderHistory(historyId: Long, updateTime: String)

    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkDateId = :workDateId " +
                "AND woHistoryDeleted = 0 " +
                "Order by woHistoryUpdateTime"
    )
    fun getWorkOrderHistoriesByDate(workDateId: Long): LiveData<List<WorkOrderHistoryWithDates>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryId = :historyId " +
                "AND woHistoryDeleted = 0 "
    )
    fun getWorkOrderHistory(historyId: Long): LiveData<WorkOrderHistoryWithDates>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkOrderId = :workOrderId " +
                "AND woHistoryDeleted = 0 " +
                "Order BY woHistoryUpdateTime"
    )
    fun getWorkOrderHistoriesByWorkOrder(workOrderId: Long):
            LiveData<List<WorkOrderHistoryWithDates>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryId = :historyId " +
                "AND woHistoryDeleted = 0"
    )
    fun getWorkOrderHistoryCombined(historyId: Long): LiveData<WorkOrderHistoryCombined>

    @Query(
        "SELECT SUM(woHistoryRegHours) as totalRegHours, " +
                "SUM(woHistoryOtHours) as totalOtHours, " +
                "SUM(woHistoryDblOtHours) as totalDblOtHours " +
                "FROM workOrderHistory " +
                "WHERE woHistoryWorkOrderId = :workOrderId " +
                "AND woHistoryDeleted = 0"
    )
    fun getWorkOrderSummary(workOrderId: Long): LiveData<WorkOrderSummary>

    @Query(
        "SELECT m.mName as name, SUM(wohm.wohmQuantity) as quantity, SUM(m.mPrice * wohm.wohmQuantity) as totalAmount " +
                "FROM workOrderHistoryMaterials wohm " +
                "LEFT JOIN materialMerged ON wohm.wohmMaterialId = mmChildId AND mmIsDeleted = 0 " +
                "INNER JOIN materials m ON m.materialId = COALESCE(mmMasterId, wohm.wohmMaterialId) " +
                "WHERE wohm.wohmHistoryId IN (SELECT woHistoryId FROM workOrderHistory WHERE woHistoryWorkOrderId = :workOrderId AND woHistoryDeleted = 0) " +
                "AND wohm.wohmIsDeleted = 0 " +
                "GROUP BY m.mName " +
                "ORDER BY m.mName"
    )
    fun getWorkOrderMaterialsSummary(workOrderId: Long): LiveData<List<MaterialAndQuantity>>

    @Query(
        "SELECT wp.wpDescription as description, null as area, COUNT(*) as quantity " +
                "FROM workOrderHistoryWorkPerformed wowp " +
                "LEFT JOIN workPerformedMerged ON wowp.wowpWorkPerformedId = wpmChildId AND wpmIsDeleted = 0 " +
                "INNER JOIN workPerformed wp ON wp.workPerformedId = COALESCE(wpmMasterId, wowp.wowpWorkPerformedId) " +
                "WHERE wowp.wowpHistoryId IN (SELECT woHistoryId FROM workOrderHistory WHERE woHistoryWorkOrderId = :workOrderId AND woHistoryDeleted = 0) " +
                "AND wowp.wowpIsDeleted = 0 " +
                "GROUP BY wp.wpDescription " +
                "ORDER BY wp.wpDescription"
    )
    fun getWorkOrderWorkPerformedSummary(workOrderId: Long): LiveData<List<WorkPerformedAndQuantity>>

    @Query(
        "SELECT woheSupplier as supplier, woheType as type, SUM(woheAmount) as totalAmount " +
                "FROM `workOrderHistoryExpense-*-` " +
                "WHERE woheHistoryId IN (SELECT woHistoryId FROM workOrderHistory WHERE woHistoryWorkOrderId = :workOrderId AND woHistoryDeleted = 0) " +
                "AND woheIsDeleted = 0 " +
                "GROUP BY woheSupplier, woheType " +
                "ORDER BY woheSupplier, woheType"
    )
    fun getWorkOrderExpensesSummary(workOrderId: Long): LiveData<List<ExpenseSummary>>

    @Query(
        "SELECT * FROM `workOrderHistoryExpense-*-` " +
                "WHERE woheHistoryId IN (SELECT woHistoryId FROM workOrderHistory WHERE woHistoryWorkOrderId = :workOrderId AND woHistoryDeleted = 0) " +
                "AND woheIsDeleted = 0 " +
                "ORDER BY woheUpdateTime DESC"
    )
    fun getWorkOrderExpensesAll(workOrderId: Long): LiveData<List<WorkOrderHistoryExpense>>

    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkDateId = :workDateId " +
                "AND woHistoryDeleted = 0"
    )
    suspend fun getWorkOrderHistoriesByDateSync(workDateId: Long): List<WorkOrderHistory>

    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryId = :historyId " +
                "AND woHistoryDeleted = 0"
    )
    suspend fun getWorkOrderHistorySync(historyId: Long): WorkOrderHistory?

    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkOrderId = :workOrderId " +
                "AND woHistoryWorkDateId = :workDateId " +
                "AND woHistoryDeleted = 0"
    )
    suspend fun getWorkOrderHistorySync(workOrderId: Long, workDateId: Long): WorkOrderHistory?

    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkOrderId = :workOrderId " +
                "AND woHistoryWorkDateId = :workDateId"
    )
    suspend fun getWorkOrderHistoryAnySync(workOrderId: Long, workDateId: Long): WorkOrderHistory?

    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryId = :historyId"
    )
    suspend fun getWorkOrderHistoryByIdAnySync(historyId: Long): WorkOrderHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrderHistoryExpense(expense: WorkOrderHistoryExpense)

    @Update
    suspend fun updateWorkOrderHistoryExpense(expense: WorkOrderHistoryExpense)

    @Query(
        "UPDATE `workOrderHistoryExpense-*-` " +
                "SET woheIsDeleted = 1, " +
                "woheUpdateTime = :updateTime " +
                "WHERE woHistoryExpenseId = :expenseId"
    )
    suspend fun deleteWorkOrderHistoryExpense(expenseId: Long, updateTime: String)

    @Query(
        "SELECT * FROM `workOrderHistoryExpense-*-` " +
                "WHERE woheHistoryId = :historyId " +
                "AND woheIsDeleted = 0 " +
                "ORDER BY woheUpdateTime"
    )
    fun getExpensesByHistory(historyId: Long): LiveData<List<WorkOrderHistoryExpense>>
}