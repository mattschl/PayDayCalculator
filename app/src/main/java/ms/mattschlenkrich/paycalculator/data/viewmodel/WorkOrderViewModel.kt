package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.worker.PictureUploadWorker
import ms.mattschlenkrich.paycalculator.data.entity.ExpensePictures
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryExpense
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryPictures
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderPictures
import ms.mattschlenkrich.paycalculator.data.repository.WorkOrderRepository
import ms.mattschlenkrich.paycalculator.ui.sync.DriveServiceHelper
import java.io.File
import java.io.FileOutputStream

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

    suspend fun getWorkOrderHistoryExpenseSync(id: Long) =
        workOrderRepository.getWorkOrderHistoryExpenseSync(id)

    fun getExpensesByHistory(historyId: Long) =
        workOrderRepository.getExpensesByHistory(historyId)

    fun getPicturesByWorkOrderId(workOrderId: Long) =
        workOrderRepository.getPicturesByWorkOrderId(workOrderId)

    fun getPicturesByHistoryId(historyId: Long) =
        workOrderRepository.getPicturesByHistoryId(historyId)

    fun getPicturesByExpenseId(expenseId: Long) =
        workOrderRepository.getPicturesByExpenseId(expenseId)

    suspend fun insertWorkOrderPicture(picture: WorkOrderPictures) =
        workOrderRepository.insertWorkOrderPicture(picture)

    suspend fun deleteWorkOrderPictureById(pictureId: Long, updateTime: String) =
        workOrderRepository.deleteWorkOrderPictureById(pictureId, updateTime)

    suspend fun insertHistoryPicture(picture: WorkOrderHistoryPictures) =
        workOrderRepository.insertHistoryPicture(picture)

    suspend fun deleteHistoryPictureById(pictureId: Long, updateTime: String) =
        workOrderRepository.deleteHistoryPictureById(pictureId, updateTime)

    suspend fun insertExpensePicture(picture: ExpensePictures) =
        workOrderRepository.insertExpensePicture(picture)

    suspend fun deleteExpensePictureById(pictureId: Long, updateTime: String) =
        workOrderRepository.deleteExpensePictureById(pictureId, updateTime)

    suspend fun downloadPicture(
        driveServiceHelper: DriveServiceHelper,
        pictureId: Long,
        driveFileId: String?,
        cacheDir: File
    ): File? {
        val fileId = driveFileId ?: return null
        val storageDir = File(cacheDir, "pictures").apply { if (!exists()) mkdirs() }
        val targetFile = File(storageDir, "pic_$pictureId.webp")
        if (!targetFile.exists() || targetFile.length() == 0L) {
            try {
                driveServiceHelper.downloadFileById(fileId, targetFile)
            } catch (e: Exception) {
                Log.e("WorkOrderViewModel", "Failed to download picture", e)
                return null
            }
        }

        generateThumbnailBackground(cacheDir, pictureId)
        return targetFile
    }

    suspend fun generateThumbnailBackground(cacheDir: File, pictureId: Long) =
        withContext(Dispatchers.IO) {
            try {
                val storageDir = File(cacheDir, "pictures")
                val fullFile = File(storageDir, "pic_$pictureId.webp")
                if (!fullFile.exists() || fullFile.length() == 0L) return@withContext

                val thumbFile = File(storageDir, "thumb_$pictureId.webp")
                if (thumbFile.exists() && thumbFile.length() > 0L) return@withContext

                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(fullFile.absolutePath, options)

                var sampleSize = 1
                while (options.outWidth / sampleSize > 200 || options.outHeight / sampleSize > 200) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val bitmap = BitmapFactory.decodeFile(fullFile.absolutePath, decodeOptions)
                    ?: return@withContext
                FileOutputStream(thumbFile).use { out ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 75, out)
                    } else {
                        @Suppress("DEPRECATION")
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 75, out)
                    }
                }
                bitmap.recycle()
            } catch (e: Exception) {
                Log.e("WorkOrderViewModel", "Failed to create thumbnail", e)
        }
    }

    fun clearPictureCache(cacheDir: File) {
        try {
            val storageDir = File(cacheDir, "pictures")
            if (storageDir.exists()) {
                storageDir.listFiles()?.forEach { file ->
                    if (file.isFile) file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e("WorkOrderViewModel", "Failed to clear picture cache", e)
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