package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderPictures

@Dao
interface WorkOrderPictureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPicture(picture: WorkOrderPictures)

    @Update
    suspend fun updatePicture(picture: WorkOrderPictures)

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE wopWorkOrderId = :workOrderId " +
                "AND wopIsDeleted = 0 " +
                "ORDER BY wopUpdateTime DESC",
    )
    fun getPicturesForWorkOrder(workOrderId: Long): LiveData<List<WorkOrderPictures>>

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE (wopWorkOrderId = :workOrderId " +
                "OR wopHistoryId IN (SELECT woHistoryId FROM workOrderHistory WHERE woHistoryWorkOrderId = :workOrderId) " +
                "OR wopExpenseId IN (SELECT woHistoryExpenseId FROM workOrderHistoryExpenses WHERE woheHistoryId IN (SELECT woHistoryId FROM workOrderHistory WHERE woHistoryWorkOrderId = :workOrderId))) " +
                "AND wopIsDeleted = 0 " +
                "ORDER BY wopUpdateTime DESC",
    )
    fun getPicturesByWorkOrderId(workOrderId: Long): LiveData<List<WorkOrderPictures>>

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE wopWorkOrderId = :workOrderId " +
                "AND wopIsDeleted = 0 " +
                "ORDER BY wopUpdateTime DESC",
    )
    suspend fun getPicturesForWorkOrderSync(workOrderId: Long): List<WorkOrderPictures>

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE wopHistoryId = :historyId " +
                "AND wopIsDeleted = 0 " +
                "ORDER BY wopUpdateTime DESC",
    )
    fun getPicturesForHistory(historyId: Long): LiveData<List<WorkOrderPictures>>

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE wopHistoryId = :historyId " +
                "AND wopIsDeleted = 0 " +
                "ORDER BY wopUpdateTime DESC",
    )
    suspend fun getPicturesForHistorySync(historyId: Long): List<WorkOrderPictures>

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE wopExpenseId = :expenseId " +
                "AND wopIsDeleted = 0 " +
                "ORDER BY wopUpdateTime DESC",
    )
    fun getPicturesForExpense(expenseId: Long): LiveData<List<WorkOrderPictures>>

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE wopExpenseId = :expenseId " +
                "AND wopIsDeleted = 0 " +
                "ORDER BY wopUpdateTime DESC",
    )
    suspend fun getPicturesForExpenseSync(expenseId: Long): List<WorkOrderPictures>

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE (driveFileId IS NULL OR driveFileId = '') " +
                "AND wopIsDeleted = 0",
    )
    suspend fun getPendingUploadsSync(): List<WorkOrderPictures>

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE pictureId = :pictureId",
    )
    suspend fun getPictureSync(pictureId: Long): WorkOrderPictures?

    @Query(
        "UPDATE workOrderPictures " +
                "SET wopIsDeleted = 1, " +
                "wopUpdateTime = :updateTime " +
                "WHERE pictureId = :pictureId",
    )
    suspend fun deletePictureById(pictureId: Long, updateTime: String)

    @Query(
        "SELECT * FROM workOrderPictures " +
                "WHERE wopIsDeleted = 0",
    )
    suspend fun getAllPicturesSync(): List<WorkOrderPictures>
}