package ms.mattschlenkrich.paycalculator.ui.workorderhistory

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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

    DisposableEffect(Unit) {
        onDispose {
            workOrderViewModel.clearPictureCache(context.cacheDir)
        }
    }

    LaunchedEffect(expensePictures) {
        val helper = mainViewModel.getOrInitializeDriveService(context) ?: return@LaunchedEffect
        expensePictures.forEach { pic ->
            val tempFile = File(context.cacheDir, "pictures/pic_${pic.pictureId}.webp")
            if ((!tempFile.exists()) && (pic.driveFileId != null)) {
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
        onCancel = { navController.popBackStack() },
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
                workOrderViewModel.insertExpensePicture(
                    ExpensePictures(
                        pictureId = pictureId,
                        epExpenseId = activeExpenseId,
                        driveFileId = null,
                        epIsDeleted = false,
                        epUploadTime = null,
                        epUpdateTime = now
                    )
                )

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
                                pictureId = pictureId,
                                epExpenseId = activeExpenseId,
                                driveFileId = driveId,
                                epIsDeleted = false,
                                epUploadTime = uploadTime,
                                epUpdateTime = uploadTime
                            )
                        )
                    } catch (_: Exception) {
                        workOrderViewModel.schedulePictureUpload()
                    }
                } else {
                    workOrderViewModel.schedulePictureUpload()
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
                coroutineScope.launch {
                    workOrderViewModel.downloadPicture(
                        helper,
                        picItem.pictureId,
                        picItem.driveFileId,
                        context.cacheDir
                    )
                }
            } else {
                Toast.makeText(context, R.string.msg_drive_not_connected, Toast.LENGTH_SHORT).show()
            }
        }
    )
}