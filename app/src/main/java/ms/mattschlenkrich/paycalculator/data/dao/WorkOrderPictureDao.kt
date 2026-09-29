package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.data.entity.ExpensePictures
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryPictures
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderPictures
import ms.mattschlenkrich.paycalculator.data.model.PictureItem

@Dao
interface WorkOrderPictureDao {
    // --- WORK ORDER PICTURES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrderPicture(picture: WorkOrderPictures)

    @Update
    suspend fun updateWorkOrderPicture(picture: WorkOrderPictures)

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE wopWorkOrderId = :workOrderId " +
                "AND wopIsDeleted = 0 " +
                "ORDER BY wopUpdateTime DESC",
    )
    fun getPicturesForWorkOrder(workOrderId: Long): LiveData<List<WorkOrderPictures>>

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE wopWorkOrderId = :workOrderId " +
                "AND wopIsDeleted = 0 " +
                "ORDER BY wopUpdateTime DESC",
    )
    suspend fun getPicturesForWorkOrderSync(workOrderId: Long): List<WorkOrderPictures>

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE (driveFileId IS NULL OR driveFileId = '') " +
                "AND wopIsDeleted = 0",
    )
    suspend fun getPendingWorkOrderUploadsSync(): List<WorkOrderPictures>

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE pictureId = :pictureId",
    )
    suspend fun getWorkOrderPictureSync(pictureId: Long): WorkOrderPictures?

    @Query(
        "UPDATE workOrderPictures " +
                "SET wopIsDeleted = 1, " +
                "wopUpdateTime = :updateTime " +
                "WHERE pictureId = :pictureId",
    )
    suspend fun deleteWorkOrderPictureById(pictureId: Long, updateTime: String)

    @Query("SELECT * FROM workOrderPictures WHERE wopIsDeleted = 0")
    suspend fun getAllWorkOrderPicturesSync(): List<WorkOrderPictures>


    // --- WORK ORDER HISTORY PICTURES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryPicture(picture: WorkOrderHistoryPictures)

    @Update
    suspend fun updateHistoryPicture(picture: WorkOrderHistoryPictures)

    @Query(
        "SELECT * FROM workOrderHistoryPictures " +
                "WHERE wohpHistoryId = :historyId " +
                "AND wohpIsDeleted = 0 " +
                "ORDER BY wohpUpdateTime DESC",
    )
    fun getPicturesForHistory(historyId: Long): LiveData<List<WorkOrderHistoryPictures>>

    @Query(
        "SELECT * FROM workOrderHistoryPictures " +
                "WHERE wohpHistoryId = :historyId " +
                "AND wohpIsDeleted = 0 " +
                "ORDER BY wohpUpdateTime DESC",
    )
    suspend fun getPicturesForHistorySync(historyId: Long): List<WorkOrderHistoryPictures>

    @Query(
        "SELECT * FROM workOrderHistoryPictures " +
                "WHERE (driveFileId IS NULL OR driveFileId = '') " +
                "AND wohpIsDeleted = 0",
    )
    suspend fun getPendingHistoryUploadsSync(): List<WorkOrderHistoryPictures>

    @Query(
        "SELECT * FROM workOrderHistoryPictures " +
                "WHERE pictureId = :pictureId",
    )
    suspend fun getHistoryPictureSync(pictureId: Long): WorkOrderHistoryPictures?

    @Query(
        "UPDATE workOrderHistoryPictures " +
                "SET wohpIsDeleted = 1, " +
                "wohpUpdateTime = :updateTime " +
                "WHERE pictureId = :pictureId",
    )
    suspend fun deleteHistoryPictureById(pictureId: Long, updateTime: String)

    @Query("SELECT * FROM workOrderHistoryPictures WHERE wohpIsDeleted = 0")
    suspend fun getAllHistoryPicturesSync(): List<WorkOrderHistoryPictures>


    // --- EXPENSE PICTURES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpensePicture(picture: ExpensePictures)

    @Update
    suspend fun updateExpensePicture(picture: ExpensePictures)

    @Query(
        "SELECT * FROM expensePictures " +
                "WHERE epExpenseId = :expenseId " +
                "AND epIsDeleted = 0 " +
                "ORDER BY epUpdateTime DESC",
    )
    fun getPicturesForExpense(expenseId: Long): LiveData<List<ExpensePictures>>

    @Query(
        "SELECT * FROM expensePictures " +
                "WHERE epExpenseId = :expenseId " +
                "AND epIsDeleted = 0 " +
                "ORDER BY epUpdateTime DESC",
    )
    suspend fun getPicturesForExpenseSync(expenseId: Long): List<ExpensePictures>

    @Query(
        "SELECT * FROM expensePictures " +
                "WHERE (driveFileId IS NULL OR driveFileId = '') " +
                "AND epIsDeleted = 0",
    )
    suspend fun getPendingExpenseUploadsSync(): List<ExpensePictures>

    @Query(
        "SELECT * FROM expensePictures " +
                "WHERE pictureId = :pictureId",
    )
    suspend fun getExpensePictureSync(pictureId: Long): ExpensePictures?

    @Query(
        "UPDATE expensePictures " +
                "SET epIsDeleted = 1, " +
                "epUpdateTime = :updateTime " +
                "WHERE pictureId = :pictureId",
    )
    suspend fun deleteExpensePictureById(pictureId: Long, updateTime: String)

    @Query("SELECT * FROM expensePictures WHERE epIsDeleted = 0")
    suspend fun getAllExpensePicturesSync(): List<ExpensePictures>

    // --- COMBINED PICTURE QUERIES ---
    @Query(
        "SELECT pictureId, driveFileId FROM workOrderPictures " +
                "WHERE wopWorkOrderId = :workOrderId AND wopIsDeleted = 0 " +
                "UNION ALL " +
                "SELECT pictureId, driveFileId FROM workOrderHistoryPictures " +
                "WHERE wohpHistoryId IN (SELECT woHistoryId FROM workOrderHistory WHERE woHistoryWorkOrderId = :workOrderId AND woHistoryDeleted = 0) AND wohpIsDeleted = 0 " +
                "UNION ALL " +
                "SELECT pictureId, driveFileId FROM expensePictures " +
                "WHERE epExpenseId IN (SELECT woHistoryExpenseId FROM workOrderHistoryExpenses WHERE woheHistoryId IN (SELECT woHistoryId FROM workOrderHistory WHERE woHistoryWorkOrderId = :workOrderId AND woHistoryDeleted = 0) AND woheIsDeleted = 0) AND epIsDeleted = 0"
    )
    fun getPicturesByWorkOrderId(workOrderId: Long): LiveData<List<PictureItem>>

    @Query(
        "SELECT pictureId, driveFileId FROM workOrderHistoryPictures " +
                "WHERE wohpHistoryId = :historyId AND wohpIsDeleted = 0 " +
                "UNION ALL " +
                "SELECT pictureId, driveFileId FROM expensePictures " +
                "WHERE epExpenseId IN (SELECT woHistoryExpenseId FROM workOrderHistoryExpenses WHERE woheHistoryId = :historyId AND woheIsDeleted = 0) AND epIsDeleted = 0"
    )
    fun getPicturesByHistoryId(historyId: Long): LiveData<List<PictureItem>>

    @Query(
        "SELECT pictureId, driveFileId FROM expensePictures " +
                "WHERE epExpenseId = :expenseId AND epIsDeleted = 0"
    )
    fun getPicturesByExpenseId(expenseId: Long): LiveData<List<PictureItem>>
}