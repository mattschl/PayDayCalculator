package ms.mattschlenkrich.paycalculator.ui.sync

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import java.io.File

private const val TAG = "SyncViewModel"

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    var driveServiceHelper by mutableStateOf<DriveServiceHelper?>(null)
    var deviceId by mutableLongStateOf(0L)
    var progressMessage by mutableStateOf<String?>(null)
    var availableBackups by mutableStateOf<List<DriveFileMeta>>(emptyList())
    var driveFilesList by mutableStateOf<List<DriveFileItem>>(emptyList())
    var localBackups by mutableStateOf<List<File>>(emptyList())
    var docContent by mutableStateOf(application.getString(R.string.app_name) + " Sync System")
    var isLoading by mutableStateOf(value = false)
    var errorMessage by mutableStateOf<String?>(null)
    var syncPerformed by mutableStateOf(value = false)
    var lastSyncTimeDisplay by mutableStateOf<String?>(null)

    private val df = DateFunctions()
    private val nf = NumberFunctions()

    fun loadLastSyncTime() {
        viewModelScope.launch(Dispatchers.IO) {
            val rawTime =
                PayDatabase(getApplication()).getSyncHistoryDao().getLastSyncTime(deviceId)
            val formatted = df.convertUtcToLocalDisplay(rawTime)
            withContext(Dispatchers.Main) {
                lastSyncTimeDisplay = formatted
            }
        }
    }

    private var applyToAllChoice: ConflictChoice? = null
    var showConflictDialog by mutableStateOf<ConflictInfo?>(null)
    private var conflictDeferred: CompletableDeferred<ConflictChoice>? = null

    fun onConflictChoice(choice: ConflictChoice, applyToAll: Boolean) {
        if (applyToAll) {
            applyToAllChoice = choice
        }
        conflictDeferred?.complete(choice)
        showConflictDialog = null
    }

    fun disconnect() {
        driveServiceHelper = null
        errorMessage = null
    }

    fun queryDriveFiles() {
        if (isLoading) return
        val helper = driveServiceHelper ?: return
        isLoading = true
        progressMessage = "Querying Drive..."
        viewModelScope.launch {
            try {
                val manager = SyncManager(
                    application = getApplication(),
                    deviceId = deviceId,
                    driveServiceHelper = helper,
                    df = df,
                    nf = nf,
                    onProgressUpdate = { progressMessage = it },
                    onConflict = { ConflictChoice.KEEP_DRIVE },
                ) { error -> Log.e(TAG, "Query error: $error") }
                availableBackups = manager.getAvailableBackups()
                localBackups = manager.getLocalBackups()

                val report = StringBuilder("Files on Google Drive:\n\n")
                if (availableBackups.isEmpty()) {
                    report.append("No backups found.")
                } else {
                    availableBackups.forEach { file ->
                        report.append("- ${file.name}\n")
                    }
                }
                docContent = report.toString()
            } catch (e: Exception) {
                Log.e(TAG, "Query failed", e)
                errorMessage = "Query failed: ${e.message}"
            } finally {
                isLoading = false
                progressMessage = null
            }
        }
    }

    fun performSync(onAuthError: (Exception) -> Unit) {
        if (isLoading) return
        val helper = driveServiceHelper ?: return
        isLoading = true
        progressMessage = "Synchronizing..."
        applyToAllChoice = null

        val manager = SyncManager(
            application = getApplication(),
            deviceId = deviceId,
            driveServiceHelper = helper,
            df = df,
            nf = nf,
            onProgressUpdate = { progressMessage = it },
            onConflict = { info -> showConflictDialogWrapper(info) },
        ) { error -> Log.e(TAG, "Sync error: $error") }

        viewModelScope.launch {
            try {
                val result = manager.performSync()
                docContent = result.second
                if (result.first == "Success") {
                    syncPerformed = true
                    loadLastSyncTime()
                } else {
                    errorMessage = if (result.first == "Busy") {
                        "Sync already in progress on another device."
                    } else {
                        "Sync failed. See report for details."
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed", e)
                onAuthError(e)
            } finally {
                isLoading = false
                progressMessage = null
            }
        }
    }

    fun restore(fileName: String, onSuccess: () -> Unit) {
        if (isLoading) return
        val helper = driveServiceHelper ?: return
        isLoading = true
        progressMessage = "Restoring from Drive..."
        val manager = SyncManager(
            application = getApplication(),
            deviceId = deviceId,
            driveServiceHelper = helper,
            df = df,
            nf = nf,
            onProgressUpdate = { progressMessage = it },
            onConflict = { ConflictChoice.KEEP_DRIVE },
        ) { error -> Log.e(TAG, "Restore error: $error") }

        viewModelScope.launch {
            try {
                val result = manager.restoreSpecific(fileName)
                docContent = result
                if (result.startsWith("Successfully")) {
                    loadLastSyncTime()
                    onSuccess()
                } else {
                    errorMessage = "Restore failed. See report for details."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Restore failed", e)
                errorMessage = "Restore failed: ${e.message}"
            } finally {
                isLoading = false
                progressMessage = null
            }
        }
    }

    fun manualUpload(onSuccess: () -> Unit) {
        if (isLoading) return
        val helper = driveServiceHelper ?: return
        isLoading = true
        progressMessage = "Uploading current state..."
        viewModelScope.launch {
            try {
                val manager = SyncManager(
                    application = getApplication(),
                    deviceId = deviceId,
                    driveServiceHelper = helper,
                    df = df,
                    nf = nf,
                    onProgressUpdate = { progressMessage = it },
                    onConflict = { ConflictChoice.KEEP_DRIVE },
                ) { error -> Log.e(TAG, "Upload error: $error") }
                val result = manager.manualUpload()
                docContent = result
                if (result.startsWith("Successfully")) {
                    loadLastSyncTime()
                    onSuccess()
                } else {
                    errorMessage = "Upload failed. See report for details."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Manual upload failed", e)
                errorMessage = "Upload failed: ${e.message}"
            } finally {
                isLoading = false
                progressMessage = null
            }
        }
    }

    fun repairDatabase(onSuccess: () -> Unit) {
        if (isLoading) return
        val helper = driveServiceHelper ?: return
        isLoading = true
        progressMessage = "Repairing..."
        viewModelScope.launch {
            try {
                val manager = SyncManager(
                    application = getApplication(),
                    deviceId = deviceId,
                    driveServiceHelper = helper,
                    df = df,
                    nf = nf,
                    onProgressUpdate = { progressMessage = it },
                    onConflict = { ConflictChoice.KEEP_DRIVE },
                ) { error -> Log.e(TAG, "Repair error: $error") }
                val result = manager.repairLocalDatabase()
                docContent = result
                if (result.contains("successfully")) {
                    loadLastSyncTime()
                    onSuccess()
                } else {
                    errorMessage = "Repair failed. See report for details."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Repair error", e)
                errorMessage = "Repair failed: ${e.message}"
            } finally {
                isLoading = false
                progressMessage = null
            }
        }
    }

    fun deleteBackup(meta: DriveFileMeta, onAuthError: (Exception) -> Unit) {
        if (isLoading) return
        val helper = driveServiceHelper ?: return
        isLoading = true
        progressMessage = "Deleting backup..."
        val manager = SyncManager(
            application = getApplication(),
            deviceId = deviceId,
            driveServiceHelper = helper,
            df = df,
            nf = nf,
            onProgressUpdate = { progressMessage = it },
            onConflict = { ConflictChoice.KEEP_DRIVE },
            onSyncError = { error -> Log.e(TAG, "Delete error: $error") }
        )

        viewModelScope.launch {
            try {
                val result = manager.deleteBackup(meta)
                docContent = result
                availableBackups = availableBackups.filter { it.id != meta.id }
            } catch (e: Exception) {
                Log.e(TAG, "Delete failed", e)
                onAuthError(e)
            } finally {
                isLoading = false
                progressMessage = null
            }
        }
    }

    fun clearBackups(onAuthError: (Exception) -> Unit) {
        if (isLoading) return
        val helper = driveServiceHelper ?: return
        isLoading = true
        progressMessage = "Deleting backups..."
        val manager = SyncManager(
            application = getApplication(),
            deviceId = deviceId,
            driveServiceHelper = helper,
            df = df,
            nf = nf,
            onProgressUpdate = { progressMessage = it },
            onConflict = { ConflictChoice.KEEP_DRIVE },
            onSyncError = { error -> Log.e(TAG, "Clear backups error: $error") }
        )

        viewModelScope.launch {
            try {
                availableBackups.forEach { manager.deleteBackup(it) }
                docContent = "All backups deleted from Google Drive."
                availableBackups = emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Clear backups failed", e)
                onAuthError(e)
            } finally {
                isLoading = false
                progressMessage = null
            }
        }
    }

    fun purgeOrphanPictures(onAuthError: (Exception) -> Unit) {
        if (isLoading) return
        val helper = driveServiceHelper ?: return
        isLoading = true
        progressMessage = "Scanning for orphan pictures on Google Drive..."
        val manager = SyncManager(
            application = getApplication(),
            deviceId = deviceId,
            driveServiceHelper = helper,
            df = df,
            nf = nf,
            onProgressUpdate = { progressMessage = it },
            onConflict = { ConflictChoice.KEEP_DRIVE },
            onSyncError = { error -> Log.e(TAG, "Purge orphan pictures error: $error") }
        )

        viewModelScope.launch {
            try {
                val result = manager.purgeOrphanPicturesOnDrive()
                docContent = result
            } catch (e: Exception) {
                Log.e(TAG, "Purge orphan pictures failed", e)
                onAuthError(e)
            } finally {
                isLoading = false
                progressMessage = null
            }
        }
    }

    fun loadDriveFilesDetails() {
        val helper = driveServiceHelper ?: return
        if (isLoading) return
        isLoading = true
        progressMessage = "Scanning files on Google Drive..."
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val appDb = PayDatabase(getApplication())
                val pictureDao = appDb.getWorkOrderPictureDao()
                val workOrderDao = appDb.getWorkOrderDao()
                val payDayDao = appDb.getPayDayDao()

                val files = helper.queryFiles().files ?: emptyList()
                val items = files.map { file ->
                    val fileId = file.id ?: ""
                    val fileName = file.name ?: "Unnamed"
                    val sizeBytes: Long? = file.getSize()
                    val sizeFormatted = formatFileSize(sizeBytes)
                    val modifiedFormatted = file.modifiedTime?.let {
                        df.convertUtcToLocalDisplay(it.toStringRfc3339())
                    } ?: "Unknown date"

                    val isPhoto = fileName.startsWith("pic_") ||
                            fileName.endsWith(".webp") ||
                            fileName.endsWith(".jpg") ||
                            fileName.endsWith(".jpeg") ||
                            fileName.endsWith(".png")

                    val isBackup = fileName.endsWith(".db") ||
                            fileName.endsWith(".sqlite") ||
                            fileName.endsWith(".json") ||
                            fileName.contains("backup")

                    var woRef: String? = null
                    var isOrphan = false

                    if (isPhoto) {
                        val picId = try {
                            fileName.removePrefix("pic_")
                                .removeSuffix(".webp")
                                .removeSuffix(".jpg")
                                .removeSuffix(".jpeg")
                                .removeSuffix(".png")
                                .toLong()
                        } catch (_: Exception) {
                            null
                        }

                        if (picId != null) {
                            // Check WorkOrderPictures
                            val wop = pictureDao.getWorkOrderPictureSync(picId)
                            if (wop != null && !wop.wopIsDeleted) {
                                val wo = workOrderDao.getWorkOrderByIdAnySync(wop.wopWorkOrderId)
                                woRef = if (wo != null) {
                                    "Work Order #${wo.woNumber} (${wo.woAddress.ifBlank { wo.woDescription }})"
                                } else {
                                    "Work Order ID #${wop.wopWorkOrderId}"
                                }
                            } else {
                                // Check HistoryPictures
                                val wohp = pictureDao.getHistoryPictureSync(picId)
                                if (wohp != null && !wohp.wohpIsDeleted) {
                                    val hist =
                                        workOrderDao.getWorkOrderHistoryByIdAnySync(wohp.wohpHistoryId)
                                    val wo =
                                        hist?.let { workOrderDao.getWorkOrderByIdAnySync(it.woHistoryWorkOrderId) }
                                    val wd =
                                        hist?.let { payDayDao.getWorkDateSync(it.woHistoryWorkDateId) }
                                    woRef = if (wo != null) {
                                        "WO #${wo.woNumber} History (${wd?.wdDate ?: "Date #${hist.woHistoryWorkDateId}"})"
                                    } else {
                                        "History ID #${wohp.wohpHistoryId}"
                                    }
                                } else {
                                    // Check ExpensePictures
                                    val ep = pictureDao.getExpensePictureSync(picId)
                                    if (ep != null && !ep.epIsDeleted) {
                                        val exp =
                                            workOrderDao.getWorkOrderHistoryExpenseSync(ep.epExpenseId)
                                        val hist = exp?.let {
                                            workOrderDao.getWorkOrderHistoryByIdAnySync(it.woheHistoryId)
                                        }
                                        val wo =
                                            hist?.let { workOrderDao.getWorkOrderByIdAnySync(it.woHistoryWorkOrderId) }
                                        woRef = if (wo != null) {
                                            "WO #${wo.woNumber} Expense (${exp.woheSupplier.ifBlank { "Expense" }})"
                                        } else {
                                            "Expense ID #${ep.epExpenseId}"
                                        }
                                    } else {
                                        woRef = "Orphan (No database reference)"
                                        isOrphan = true
                                    }
                                }
                            }
                        } else {
                            woRef = "Orphan (No picture ID)"
                            isOrphan = true
                        }
                    }

                    DriveFileItem(
                        id = fileId,
                        name = fileName,
                        size = sizeBytes,
                        sizeFormatted = sizeFormatted,
                        modifiedTimeFormatted = modifiedFormatted,
                        isPicture = isPhoto,
                        isBackup = isBackup,
                        workOrderReference = woRef,
                        isOrphan = isOrphan
                    )
                }

                withContext(Dispatchers.Main) {
                    driveFilesList = items
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load Drive file details", e)
                withContext(Dispatchers.Main) {
                    errorMessage = "Failed to load Drive files: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    progressMessage = null
                }
            }
        }
    }

    fun deleteDriveFileItem(item: DriveFileItem, onAuthError: (Exception) -> Unit) {
        if (isLoading) return
        val helper = driveServiceHelper ?: return
        isLoading = true
        progressMessage = "Deleting ${item.name}..."
        viewModelScope.launch {
            try {
                helper.deleteFile(item.id)
                driveFilesList = driveFilesList.filter { it.id != item.id }
                docContent = "Deleted ${item.name} from Google Drive."
            } catch (e: Exception) {
                Log.e(TAG, "Delete file failed", e)
                onAuthError(e)
            } finally {
                isLoading = false
                progressMessage = null
            }
        }
    }

    fun autoCleanupDrivePhotos(onAuthError: (Exception) -> Unit) {
        if (isLoading) return
        val helper = driveServiceHelper ?: return
        isLoading = true
        progressMessage = "Cleaning up Drive photos & checking integrity..."
        val manager = SyncManager(
            application = getApplication(),
            deviceId = deviceId,
            driveServiceHelper = helper,
            df = df,
            nf = nf,
            onProgressUpdate = { progressMessage = it },
            onConflict = { ConflictChoice.KEEP_DRIVE },
            onSyncError = { error -> Log.e(TAG, "Auto cleanup photos error: $error") }
        )

        viewModelScope.launch {
            try {
                val result = manager.purgeOrphanPicturesOnDrive()
                docContent = result
                loadDriveFilesDetails()
            } catch (e: Exception) {
                Log.e(TAG, "Auto cleanup photos failed", e)
                onAuthError(e)
            } finally {
                isLoading = false
                progressMessage = null
            }
        }
    }

    private fun formatFileSize(bytes: Long?): String {
        if (bytes == null || bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "${kb.toInt()} KB"
        val mb = kb / 1024.0
        return String.format(java.util.Locale.getDefault(), "%.2f MB", mb)
    }

    private suspend fun showConflictDialogWrapper(info: ConflictInfo): ConflictChoice {
        applyToAllChoice?.let { return it }
        val deferred = CompletableDeferred<ConflictChoice>()
        conflictDeferred = deferred
        showConflictDialog = info
        return deferred.await()
    }
}