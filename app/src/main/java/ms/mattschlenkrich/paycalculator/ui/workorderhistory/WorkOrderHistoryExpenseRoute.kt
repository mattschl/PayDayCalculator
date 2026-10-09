package ms.mattschlenkrich.paycalculator.ui.workorderhistory

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.ExpensePictures
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryExpense
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable.WorkOrderHistoryExpenseScreen
import java.io.File

@Composable
fun WorkOrderHistoryExpenseRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController,
    isUpdate: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }

    val history = mainViewModel.getWorkOrderHistory() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val initialExpense = if (isUpdate) mainViewModel.getWorkOrderHistoryExpense() else null
    val activeExpenseId = remember(initialExpense) {
        initialExpense?.woHistoryExpenseId ?: nf.generateRandomIdAsLong()
    }

    val expensePictures by workOrderViewModel.getPicturesByExpenseId(activeExpenseId)
        .observeAsState(emptyList())

    LaunchedEffect(expensePictures) {
        val helper = mainViewModel.getOrInitializeDriveService(context) ?: return@LaunchedEffect
        expensePictures.forEach { pic ->
            val fullInCache = File(context.cacheDir, "pictures/pic_${pic.pictureId}.webp")
            val fullInFiles = File(context.filesDir, "pictures/pic_${pic.pictureId}.webp")
            val existsLocally = (fullInCache.exists() && fullInCache.length() > 0L) ||
                    (fullInFiles.exists() && fullInFiles.length() > 0L)
            if (!existsLocally && !pic.driveFileId.isNullOrBlank()) {
                workOrderViewModel.downloadPicture(
                    helper,
                    pic.pictureId,
                    pic.driveFileId,
                    context.cacheDir
                )
            }
        }
    }

    WorkOrderHistoryExpenseScreen(
        mainViewModel = mainViewModel,
        navController = navController,
        initialExpense = initialExpense,
        pictures = expensePictures,
        onSaveExpense = { type, supplier, invoiceNo, amount ->
            val now = df.getCurrentUTCTimeAsString()
            val amtValue = try {
                nf.getDoubleFromDollars(amount)
            } catch (_: Exception) {
                0.0
            }
            coroutineScope.launch {
                val expense = WorkOrderHistoryExpense(
                    woHistoryExpenseId = activeExpenseId,
                    woheHistoryId = history.woHistoryId,
                    woheType = type,
                    woheSupplier = supplier,
                    woheInvoiceNo = invoiceNo,
                    woheAmount = amtValue,
                    woheIsDeleted = false,
                    woheUpdateTime = now
                )
                if (isUpdate) {
                    workOrderViewModel.updateWorkOrderHistoryExpense(expense)
                } else {
                    workOrderViewModel.insertWorkOrderHistoryExpense(expense)
                }
                navController.popBackStack()
            }
        },
        onDeleteExpense = {
            val now = df.getCurrentUTCTimeAsString()
            coroutineScope.launch {
                workOrderViewModel.deleteWorkOrderHistoryExpense(activeExpenseId, now)
                navController.popBackStack()
            }
        },
        onCancel = {
            if (!isUpdate) {
                coroutineScope.launch {
                    val existing =
                        workOrderViewModel.getWorkOrderHistoryExpenseSync(activeExpenseId)
                    if (existing != null && existing.woheType == "Expense" && existing.woheAmount == 0.0 && existing.woheSupplier.isEmpty()) {
                        workOrderViewModel.deleteWorkOrderHistoryExpense(
                            activeExpenseId,
                            df.getCurrentUTCTimeAsString()
                        )
                    }
                    navController.popBackStack()
                }
            } else {
                navController.popBackStack()
            }
        },
        onPictureTaken = { file ->
            val pictureId = try {
                file.nameWithoutExtension.removePrefix("pic_").toLong()
            } catch (_: Exception) {
                nf.generateRandomIdAsLong()
            }
            val targetFile = File(context.cacheDir, "pictures/pic_$pictureId.webp")
            if ((file.absolutePath != targetFile.absolutePath) && file.exists()) {
                file.copyTo(targetFile, overwrite = true)
            }
            val now = df.getCurrentUTCTimeAsString()
            coroutineScope.launch {
                val existingExpense =
                    workOrderViewModel.getWorkOrderHistoryExpenseSync(activeExpenseId)
                if (existingExpense == null) {
                    val draftExpense = WorkOrderHistoryExpense(
                        woHistoryExpenseId = activeExpenseId,
                        woheHistoryId = history.woHistoryId,
                        woheType = "Expense",
                        woheSupplier = "",
                        woheInvoiceNo = "",
                        woheAmount = 0.0,
                        woheIsDeleted = false,
                        woheUpdateTime = now
                    )
                    workOrderViewModel.insertWorkOrderHistoryExpense(draftExpense)
                }

                workOrderViewModel.generateThumbnailBackground(context.cacheDir, pictureId)
                workOrderViewModel.insertExpensePicture(
                    ExpensePictures(
                        epPictureId = pictureId,
                        epExpenseId = activeExpenseId,
                        epDriveFileId = null,
                        epIsDeleted = false,
                        epUploadTime = null,
                        epUpdateTime = now
                    )
                )

                Toast.makeText(context, R.string.msg_picture_saved_locally, Toast.LENGTH_LONG)
                    .show()

                val helper = mainViewModel.getOrInitializeDriveService(context)
                if (helper != null && targetFile.exists()) {
                    try {
                        val driveId = helper.uploadFile(
                            localFile = targetFile,
                            mimeType = "image/webp",
                            driveFileName = "pic_$pictureId.webp"
                        )
                        val uploadTime = df.getCurrentUTCTimeAsString()
                        workOrderViewModel.insertExpensePicture(
                            ExpensePictures(
                                epPictureId = pictureId,
                                epExpenseId = activeExpenseId,
                                epDriveFileId = driveId,
                                epIsDeleted = false,
                                epUploadTime = uploadTime,
                                epUpdateTime = uploadTime
                            )
                        )
                        Toast.makeText(
                            context,
                            R.string.msg_picture_uploaded_to_drive,
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (_: Exception) {
                        workOrderViewModel.schedulePictureUpload()
                        Toast.makeText(
                            context,
                            R.string.msg_picture_pending_upload,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    workOrderViewModel.schedulePictureUpload()
                    Toast.makeText(context, R.string.msg_picture_pending_upload, Toast.LENGTH_LONG)
                        .show()
                }
            }
        },
        onDeletePicture = { picItem ->
            val now = df.getCurrentUTCTimeAsString()
            coroutineScope.launch {
                workOrderViewModel.deleteExpensePictureById(picItem.pictureId, now)
                val tempFile = File(context.cacheDir, "pictures/pic_${picItem.pictureId}.webp")
                if (tempFile.exists()) tempFile.delete()
            }
        },
        onDownloadPicture = { picItem ->
            val helper = mainViewModel.getOrInitializeDriveService(context)
            if (helper != null) {
                workOrderViewModel.downloadPicture(
                    helper,
                    picItem.pictureId,
                    picItem.driveFileId,
                    context.cacheDir
                )
            } else {
                Toast.makeText(context, R.string.msg_drive_not_connected, Toast.LENGTH_LONG).show()
            }
        }
    )
}