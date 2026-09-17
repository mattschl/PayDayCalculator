package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderPictures

class WorkOrderPictureRepository(db: PayDatabase) {
    private val workOrderPictureDao = db.getWorkOrderPictureDao()

    fun getPicturesForWorkOrder(workOrderId: Long) =
        workOrderPictureDao.getPicturesForWorkOrder(workOrderId)

    suspend fun getPicturesForWorkOrderSync(workOrderId: Long) =
        workOrderPictureDao.getPicturesForWorkOrderSync(workOrderId)

    fun getPicturesForHistory(historyId: Long) =
        workOrderPictureDao.getPicturesForHistory(historyId)

    suspend fun getPicturesForHistorySync(historyId: Long) =
        workOrderPictureDao.getPicturesForHistorySync(historyId)

    fun getPicturesForExpense(expenseId: Long) =
        workOrderPictureDao.getPicturesForExpense(expenseId)

    suspend fun getPicturesForExpenseSync(expenseId: Long) =
        workOrderPictureDao.getPicturesForExpenseSync(expenseId)

    suspend fun insertPicture(picture: WorkOrderPictures) =
        workOrderPictureDao.insertPicture(picture)

    suspend fun updatePicture(picture: WorkOrderPictures) =
        workOrderPictureDao.updatePicture(picture)

    suspend fun deletePictureById(pictureId: Long) =
        workOrderPictureDao.deletePictureById(pictureId)

    suspend fun getPendingUploadsSync() =
        workOrderPictureDao.getPendingUploadsSync()

    suspend fun getPictureSync(pictureId: Long) =
        workOrderPictureDao.getPictureSync(pictureId)
}