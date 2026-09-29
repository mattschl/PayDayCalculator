package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.ExpensePictures
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryPictures
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderPictures

class WorkOrderPictureRepository(db: PayDatabase) {
    private val dao = db.getWorkOrderPictureDao()

    fun getPicturesForWorkOrder(workOrderId: Long) = dao.getPicturesForWorkOrder(workOrderId)
    suspend fun insertWorkOrderPicture(picture: WorkOrderPictures) =
        dao.insertWorkOrderPicture(picture)

    suspend fun updateWorkOrderPicture(picture: WorkOrderPictures) =
        dao.updateWorkOrderPicture(picture)

    suspend fun deleteWorkOrderPictureById(pictureId: Long, updateTime: String) =
        dao.deleteWorkOrderPictureById(pictureId, updateTime)

    suspend fun getPendingWorkOrderUploadsSync() = dao.getPendingWorkOrderUploadsSync()

    fun getPicturesForHistory(historyId: Long) = dao.getPicturesForHistory(historyId)
    suspend fun insertHistoryPicture(picture: WorkOrderHistoryPictures) =
        dao.insertHistoryPicture(picture)

    suspend fun updateHistoryPicture(picture: WorkOrderHistoryPictures) =
        dao.updateHistoryPicture(picture)

    suspend fun deleteHistoryPictureById(pictureId: Long, updateTime: String) =
        dao.deleteHistoryPictureById(pictureId, updateTime)

    suspend fun getPendingHistoryUploadsSync() = dao.getPendingHistoryUploadsSync()

    fun getPicturesForExpense(expenseId: Long) = dao.getPicturesForExpense(expenseId)
    suspend fun insertExpensePicture(picture: ExpensePictures) = dao.insertExpensePicture(picture)
    suspend fun updateExpensePicture(picture: ExpensePictures) = dao.updateExpensePicture(picture)
    suspend fun deleteExpensePictureById(pictureId: Long, updateTime: String) =
        dao.deleteExpensePictureById(pictureId, updateTime)

    suspend fun getPendingExpenseUploadsSync() = dao.getPendingExpenseUploadsSync()
}