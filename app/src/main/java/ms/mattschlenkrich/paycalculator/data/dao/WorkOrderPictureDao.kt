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
        "SELECT * FROM work_order_pictures " +
                "WHERE wpWorkOrderId = :workOrderId " +
                "ORDER BY wpUpdateTime DESC",
    )
    fun getPicturesForWorkOrder(workOrderId: Long): LiveData<List<WorkOrderPictures>>

    @Query(
        "SELECT * FROM work_order_pictures " +
                "WHERE wpWorkOrderId = :workOrderId " +
                "OR wpHistoryId IN (SELECT woHistoryId FROM workOrderHistory WHERE woHistoryWorkOrderId = :workOrderId) " +
                "OR wpExpenseId IN (SELECT woHistoryExpenseId FROM `workOrderHistoryExpense-*-` WHERE woheHistoryId IN (SELECT woHistoryId FROM workOrderHistory WHERE woHistoryWorkOrderId = :workOrderId)) " +
                "ORDER BY wpUpdateTime DESC",
    )
    fun getPicturesByWorkOrderId(workOrderId: Long): LiveData<List<WorkOrderPictures>>

    @Query(
        "SELECT * FROM work_order_pictures " +
                "WHERE wpWorkOrderId = :workOrderId " +
                "ORDER BY wpUpdateTime DESC",
    )
    suspend fun getPicturesForWorkOrderSync(workOrderId: Long): List<WorkOrderPictures>

    @Query(
        "SELECT * FROM work_order_pictures " +
                "WHERE wpHistoryId = :historyId " +
                "ORDER BY wpUpdateTime DESC",
    )
    fun getPicturesForHistory(historyId: Long): LiveData<List<WorkOrderPictures>>

    @Query(
        "SELECT * FROM work_order_pictures " +
                "WHERE wpHistoryId = :historyId " +
                "ORDER BY wpUpdateTime DESC",
    )
    suspend fun getPicturesForHistorySync(historyId: Long): List<WorkOrderPictures>

    @Query(
        "SELECT * FROM work_order_pictures " +
                "WHERE wpExpenseId = :expenseId " +
                "ORDER BY wpUpdateTime DESC",
    )
    fun getPicturesForExpense(expenseId: Long): LiveData<List<WorkOrderPictures>>

    @Query(
        "SELECT * FROM work_order_pictures " +
                "WHERE wpExpenseId = :expenseId " +
                "ORDER BY wpUpdateTime DESC",
    )
    suspend fun getPicturesForExpenseSync(expenseId: Long): List<WorkOrderPictures>

    @Query(
        "SELECT * FROM work_order_pictures " +
                "WHERE isUploaded = 0",
    )
    suspend fun getPendingUploadsSync(): List<WorkOrderPictures>

    @Query(
        "SELECT * FROM work_order_pictures " +
                "WHERE pictureId = :pictureId",
    )
    suspend fun getPictureSync(pictureId: Long): WorkOrderPictures?

    @Query(
        "DELETE FROM work_order_pictures " +
                "WHERE pictureId = :pictureId",
    )
    suspend fun deletePictureById(pictureId: Long)

    @Query(
        "SELECT * FROM work_order_pictures",
    )
    suspend fun getAllPicturesSync(): List<WorkOrderPictures>
}