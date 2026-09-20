package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.worker.PictureUploadWorker
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryExpense
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderPictures
import ms.mattschlenkrich.paycalculator.data.repository.WorkOrderRepository
import ms.mattschlenkrich.paycalculator.ui.sync.DriveServiceHelper
import java.io.File

class WorkOrderViewModel(
    app: Application,
    private val workOrderRepository: WorkOrderRepository,
) : AndroidViewModel(app) {

    suspend fun insertWorkOrder(workOrder: WorkOrder) =
        workOrderRepository.insertWorkOrder(workOrder)

    suspend fun updateWorkOrder(workOrder: WorkOrder) =
        workOrderRepository.updateWorkOrder(
            workOrder.workOrderId,
            workOrder.woNumber,
            workOrder.woEmployerId,
            workOrder.woAddress,
            workOrder.woDescription,
            workOrder.woDeleted,
            workOrder.woUpdateTime,
        )

    fun getWorkOrder(workOrderId: Long) = workOrderRepository.getWorkOrder(workOrderId)

    suspend fun findWorkOrder(workOrderNum: String, employerId: Long) =
        workOrderRepository.findWorkOrder(workOrderNum, employerId)

    fun getWorkOrdersByEmployerId(employerId: Long) =
        workOrderRepository.getWorkOrdersByEmployerId(employerId)

    fun searchWorkOrders(employerId: Long, query: String) =
        workOrderRepository.searchWorkOrders(employerId, query)

    fun getUniqueAddresses(employerId: Long) =
        workOrderRepository.getUniqueAddresses(employerId)

    suspend fun insertWorkOrderHistory(history: WorkOrderHistory) =
        workOrderRepository.insertWorkOrderHistory(history)

    suspend fun updateWorkOrderHistory(history: WorkOrderHistory) =
        workOrderRepository.updateWorkOrderHistory(history)

    suspend fun getWorkOrderHistory(workOrderId: Long, workDateId: Long) =
        workOrderRepository.getWorkOrderHistory(workOrderId, workDateId)

    suspend fun deleteWorkOrderHistory(historyId: Long) =
        workOrderRepository.deleteWorkOrderHistory(
            historyId,
            DateFunctions().getCurrentUTCTimeAsString()
        )

    fun getWorkOrderHistoriesByDate(workDateId: Long) =
        workOrderRepository.getWorkOrderHistoriesByDate(workDateId)

    fun getWorkOrderHistory(historyId: Long) =
        workOrderRepository.getWorkOrderHistory(historyId)

    fun getWorkOrderHistoryCombined(historyId: Long) =
        workOrderRepository.getWorkOrderHistoryCombined(historyId)

    fun getWorkOrderSummary(workOrderId: Long) =
        workOrderRepository.getWorkOrderSummary(workOrderId)

    fun getWorkOrderMaterialsSummary(workOrderId: Long) =
        workOrderRepository.getWorkOrderMaterialsSummary(workOrderId)

    fun getWorkOrderWorkPerformedSummary(workOrderId: Long) =
        workOrderRepository.getWorkOrderWorkPerformedSummary(workOrderId)

    fun getWorkOrderJobSpecsSummary(workOrderId: Long) =
        workOrderRepository.getWorkOrderJobSpecsSummary(workOrderId)

    fun getWorkOrderExpensesSummary(workOrderId: Long) =
        workOrderRepository.getWorkOrderExpensesSummary(workOrderId)

    fun getWorkOrderExpensesAll(workOrderId: Long) =
        workOrderRepository.getWorkOrderExpensesAll(workOrderId)

    suspend fun insertWorkOrderHistoryTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        workOrderRepository.insertTimeWorked(timeWorked)

    suspend fun updateWorkOrderHistoryTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
        workOrderRepository.updateTimeWorked(timeWorked)

    fun getWorkOrderHistoryTimesByHistory(historyId: Long) =
        workOrderRepository.getTimeWorkedForWorkOrderHistory(historyId)

    suspend fun deleteTimeWorked(timeWorkedId: Long, updateTime: String) =
        workOrderRepository.deleteTimeWorked(timeWorkedId, updateTime)

    fun getTimeWorkedPerDay(workDateId: Long) =
        workOrderRepository.getTimeWorkedPerDay(workDateId)

    fun getTimeWorkedForWorkOrderHistory(historyId: Long) =
        workOrderRepository.getTimeWorkedForWorkOrderHistory(historyId)

    fun getWorkOrderHistoriesByWorkOrder(workOrderId: Long) =
        workOrderRepository.getWorkOrderHistoriesByWorkOrder(workOrderId)

    suspend fun deleteWorkDate(workDateId: Long) =
        workOrderRepository.deleteWorkDate(
            workDateId,
            DateFunctions().getCurrentUTCTimeAsString()
        )

    suspend fun insertWorkOrderHistoryExpense(expense: WorkOrderHistoryExpense) =
        workOrderRepository.insertWorkOrderHistoryExpense(expense)

    suspend fun updateWorkOrderHistoryExpense(expense: WorkOrderHistoryExpense) =
        workOrderRepository.updateWorkOrderHistoryExpense(expense)

    suspend fun deleteWorkOrderHistoryExpense(expenseId: Long, updateTime: String) =
        workOrderRepository.deleteWorkOrderHistoryExpense(expenseId, updateTime)

    fun getExpensesByHistory(historyId: Long) =
        workOrderRepository.getExpensesByHistory(historyId)

    fun getPicturesByWorkOrderId(workOrderId: Long) =
        workOrderRepository.getPicturesByWorkOrderId(workOrderId)

    fun getPicturesForHistory(historyId: Long) =
        workOrderRepository.getPicturesForHistory(historyId)

    fun getPicturesForExpense(expenseId: Long) =
        workOrderRepository.getPicturesForExpense(expenseId)

    suspend fun insertPicture(picture: WorkOrderPictures) =
        workOrderRepository.insertPicture(picture)

    suspend fun updatePicture(picture: WorkOrderPictures) =
        workOrderRepository.updatePicture(picture)

    suspend fun deletePictureById(pictureId: Long) =
        workOrderRepository.deletePictureById(pictureId)

    suspend fun downloadPicture(
        driveServiceHelper: DriveServiceHelper,
        picture: WorkOrderPictures,
        cacheDir: File
    ): WorkOrderPictures? {
        val fileId = picture.driveFileId ?: return null
        val storageDir = File(cacheDir, "pictures")
        if (!storageDir.exists()) storageDir.mkdirs()
        val targetFile = File(storageDir, "pic_${picture.pictureId}.webp")

        return try {
            driveServiceHelper.downloadFileById(fileId, targetFile)
            val updated = picture.copy(localCachePath = targetFile.absolutePath)
            updatePicture(updated)
            updated
        } catch (e: Exception) {
            Log.e("WorkOrderViewModel", "Failed to download picture", e)
            null
        }
    }

    fun schedulePictureUpload() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadRequest = OneTimeWorkRequestBuilder<PictureUploadWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(getApplication()).enqueue(uploadRequest)
    }
}