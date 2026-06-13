package ms.mattschlenkrich.paycalculator.ui.sync

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.common.DEVICE_ID
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PREFS_NAME
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.SyncHistory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val TAG = "SyncViewModel"

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    var driveServiceHelper by mutableStateOf<DriveServiceHelper?>(null)
    var docContent by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var progressMessage by mutableStateOf("")
    var syncProgress by mutableIntStateOf(0)
    var syncMax by mutableIntStateOf(0)
    var errorMessage by mutableStateOf<String?>(null)

    private fun showProgress(message: String) {
        Log.d(TAG, "Progress: $message")
        errorMessage = null
        progressMessage = message
        isLoading = true
    }

    private fun hideProgress() {
        isLoading = false
    }

    private fun getTargetFolderId(): String {
        return "appDataFolder"
    }

    private suspend fun isFirstSync(): Boolean {
        return withContext(Dispatchers.IO) {
            val db = PayDatabase(getApplication())
            val history = db.getSyncHistoryDao().getLastSyncHistory()
            history == null
        }
    }

    private suspend fun performDownload(helper: DriveServiceHelper, targetFolderId: String) {
        showProgress("Searching for backups...")
        val fileList: FileList = helper.queryFiles(targetFolderId)
        val driveFiles = fileList.files ?: emptyList()

        if (driveFiles.isNotEmpty()) {
            val dbDir = File(getApplication<Application>().applicationInfo.dataDir, "databases")
            if (!dbDir.exists()) dbDir.mkdirs()

            // Clear any old drive backups to ensure consistency
            dbDir.listFiles { _, name -> name.startsWith("pay_from_drive") }
                ?.forEach { it.delete() }

            val fourWeeksAgoMs = System.currentTimeMillis() - (28 * 24 * 60 * 60 * 1000L)
            val isFirstSync = isFirstSync()

            val allDbFiles = driveFiles
                .filter { (it.name.startsWith("pay_") || it.name == "pay.db") && it.name.endsWith(".db") }
                .sortedBy { it.name }

            val dbFilesToDownload = if (isFirstSync && allDbFiles.isNotEmpty()) {
                val latest = allDbFiles.last()
                allDbFiles.filter {
                    it == latest || (it.modifiedTime?.value ?: 0L) > fourWeeksAgoMs
                }.distinct()
            } else {
                allDbFiles.filter { (it.modifiedTime?.value ?: 0L) > fourWeeksAgoMs }
            }

            var downloadCount = 0
            if (dbFilesToDownload.isEmpty()) {
                Log.d(TAG, "No backups found to download.")
            }

            for (dbFile in dbFilesToDownload) {
                val relatedSuffixes = listOf("", "-wal", "-shm")
                for (suffix in relatedSuffixes) {
                    val remoteName = dbFile.name + suffix
                    val driveFile = driveFiles.find { it.name == remoteName }
                    if (driveFile != null) {
                        val localName = if (remoteName.startsWith("pay.db")) {
                            remoteName.replace("pay.db", "pay_from_drive.db")
                        } else {
                            remoteName
                        }
                        val internalFile = File(dbDir, localName)
                        if (!internalFile.exists() || localName.startsWith("pay_from_drive")) {
                            showProgress("Downloading $remoteName to app...")
                            helper.downloadBinaryFile(remoteName, internalFile, targetFolderId)
                            downloadCount++
                        }
                    }
                }
            }

            if (downloadCount > 0) {
                Log.d(TAG, "Downloaded $downloadCount new files to ${dbDir.absolutePath}")
                PayDatabase.resetInstance()
            }
            docContent = "Files stored in: ${dbDir.absolutePath}"
        }
    }

    private suspend fun logSyncAttempt(status: String, summary: String) {
        withContext(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val prefs = context.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE)
                val deviceId = prefs.getLong(DEVICE_ID, 0L)
                val db = PayDatabase(context)
                val syncHistory = SyncHistory(
                    syncId = NumberFunctions().generateRandomIdAsLong(),
                    syncTime = DateFunctions().getCurrentUTCTimeAsString(),
                    syncSourceName = "Google Drive",
                    syncDeviceId = deviceId,
                    syncStatus = status,
                    syncRecordsProcessed = summary
                )
                db.getSyncHistoryDao().insertSyncHistory(syncHistory)
                db.getSyncHistoryDao().purgeOldSyncHistory(50)
                Log.d(TAG, "Sync attempt logged and purged: $status")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log sync attempt", e)
            }
        }
    }

    fun performSync(onAuthError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val helper = driveServiceHelper ?: run {
                    hideProgress()
                    return@launch
                }

                val targetFolderId = getTargetFolderId()
                try {
                    performDownload(helper, targetFolderId)
                } catch (e: Exception) {
                    Log.e(TAG, "Download phase failed, proceeding to backup", e)
                }

                val context = getApplication<Application>()
                val dbDir = File(context.applicationInfo.dataDir, "databases")
                val localBackups = dbDir.listFiles { _, name ->
                    name.startsWith("pay_") && name.endsWith(".db")
                }?.sortedBy { it.name } ?: emptyList()

                if (localBackups.isNotEmpty()) {
                    val summaryBuilder = StringBuilder("Sync Analysis and Results:\n\n")
                    for (localDb in localBackups) {
                        showProgress("Analyzing ${localDb.name}...")
                        val mergeHelper = MergeHelper(context, localDb.absolutePath)

                        val analysis = mergeHelper.getSyncSummary()
                        summaryBuilder.append("--- ANALYSIS: ${localDb.name} ---\n")
                        summaryBuilder.append(analysis).append("\n\n")
                        docContent = summaryBuilder.toString()

                        showProgress("Applying changes from ${localDb.name}...")
                        val summary = mergeHelper.applySync { progress, total ->
                            syncMax = total
                            syncProgress = progress
                            progressMessage =
                                "Syncing ${localDb.name}: table ${progress + 1} of $total..."
                        }
                        summaryBuilder.append("--- SYNC RESULTS: ${localDb.name} ---\n")
                        summaryBuilder.append(summary).append("\n\n")
                        docContent = summaryBuilder.toString()
                    }

                    syncMax = 0
                    Log.d(TAG, "Sync Result: $docContent")

                    showProgress("Cleaning up local backups...")
                    for (localDb in localBackups) {
                        Log.d(TAG, "Deleting processed local backup: ${localDb.name}")
                        localDb.delete()
                        File(localDb.absolutePath + "-wal").delete()
                        File(localDb.absolutePath + "-shm").delete()
                    }

                    val db = PayDatabase(context)
                    db.invalidationTracker.refreshVersionsAsync()
                    PayDatabase.resetInstance()
                } else {
                    if (docContent.isBlank()) {
                        docContent = "No new backups to merge from the last 4 weeks."
                    }
                }

                showProgress("Creating fresh backup...")
                withContext(Dispatchers.IO) {
                    PayDatabase.checkpoint(context)
                    PayDatabase.closeDatabase()
                }

                val dbFile = File(dbDir, "pay.db")
                if (dbFile.exists()) {
                    val timestamp =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.format(Date())
                    val isMerged = localBackups.isNotEmpty()
                    val driveFileName = if (isMerged) {
                        "pay_${timestamp}_merged.db"
                    } else {
                        "pay_${timestamp}.db"
                    }

                    showProgress("Uploading $driveFileName...")
                    listOf("", "-wal", "-shm").forEach { suffix ->
                        val localFile = if (suffix == "") dbFile else File(dbDir, "pay.db$suffix")
                        if (localFile.exists()) {
                            helper.uploadFile(
                                localFile = localFile,
                                mimeType = "application/vnd-sqlite3",
                                driveFileName = "$driveFileName$suffix",
                                folderId = targetFolderId
                            )
                        }
                    }
                }

                showProgress("Cleaning up local backups...")
                val finalDbDir = File(context.applicationInfo.dataDir, "databases")
                finalDbDir.listFiles { _, name ->
                    name.startsWith("pay_")
                }?.forEach { it.delete() }

                showProgress("Cleaning up old backups...")
                val finalDriveFileList = helper.queryFiles(targetFolderId)
                val finalDriveBackups = finalDriveFileList.files
                    ?.filter { it.name.startsWith("pay_") && it.name.endsWith(".db") }
                    ?.sortedByDescending { it.name } ?: emptyList()

                if (finalDriveBackups.isNotEmpty()) {
                    val db = PayDatabase(context)
                    val formatter = SimpleDateFormat("yyyy-LL-dd HH:mm:ss", Locale.CANADA)
                    formatter.timeZone = TimeZone.getTimeZone("UTC")
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    calendar.add(Calendar.DAY_OF_YEAR, -28)
                    val hardLimit = formatter.format(calendar.time)

                    val globalBaseline =
                        db.getSyncHistoryDao().getEarliestLastSuccessSyncTime(hardLimit)
                            ?: hardLimit

                    val driveDateFormatter =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    driveDateFormatter.timeZone = TimeZone.getTimeZone("UTC")

                    finalDriveBackups.forEachIndexed { index, file ->
                        val isProtected = index < 3
                        val fileTimestamp = try {
                            val parts = file.name.split("_")
                            if (parts.size >= 3) {
                                val tsPart = "${parts[1]}_${parts[2].take(6)}"
                                driveDateFormatter.parse(tsPart)
                            } else null
                        } catch (_: Exception) {
                            null
                        }

                        val fileTimeStr = fileTimestamp?.let { formatter.format(it) } ?: ""

                        val isTooOld = fileTimeStr.isNotEmpty() && fileTimeStr < hardLimit
                        val isRedundant = fileTimeStr.isNotEmpty() && fileTimeStr < globalBaseline

                        if (!isProtected && (isTooOld || isRedundant)) {
                            Log.d(
                                TAG,
                                "Culling redundant backup: ${file.name} (Time: $fileTimeStr, Baseline: $globalBaseline)"
                            )
                            helper.deleteFile(file.id)
                            listOf("-wal", "-shm").forEach { suffix ->
                                val extraName = "${file.name}$suffix"
                                finalDriveFileList.files?.find { it.name == extraName }?.let {
                                    helper.deleteFile(it.id)
                                }
                            }
                        }
                    }
                }

                logSyncAttempt("Success", docContent)
                errorMessage = null
                progressMessage = "Sync, Cleanup, and Backup complete."

            } catch (e: Exception) {
                logSyncAttempt("Failed", e.message ?: "Unknown error")
                onAuthError(e)
            } finally {
                hideProgress()
            }
        }
    }

    fun clearBackups(onAuthError: (Exception) -> Unit) {
        viewModelScope.launch {
            val helper = driveServiceHelper ?: run {
                Log.e(TAG, "clearBackups: Drive service not initialized.")
                return@launch
            }
            showProgress("Deleting backups from Google Drive...")
            try {
                val targetFolderId = getTargetFolderId()
                val fileList: FileList = helper.queryFiles(targetFolderId)
                val relatedFiles = fileList.files
                    ?.filter { it.name.startsWith("pay") } ?: emptyList()

                if (relatedFiles.isEmpty()) {
                    docContent = "No backups found to delete."
                } else {
                    for (file in relatedFiles) {
                        Log.d(TAG, "Deleting file from Drive: ${file.name}")
                        helper.deleteFile(file.id)
                    }
                    docContent = "All backups deleted from Google Drive."
                }
            } catch (e: Exception) {
                onAuthError(e)
            } finally {
                hideProgress()
            }
        }
    }

    fun query(onAuthError: (Exception) -> Unit) {
        val helper = driveServiceHelper ?: run {
            Log.e(TAG, "query: Drive service not initialized.")
            return
        }
        showProgress("Querying files...")
        viewModelScope.launch {
            try {
                val targetFolderId = getTargetFolderId()
                val fileList: FileList = helper.queryFiles(targetFolderId)
                val builder = StringBuilder("Files on Google Drive:\n\n")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                val relatedFiles = fileList.files
                    ?.filter { it.name.startsWith("pay") }
                    ?.sortedByDescending { it.name } ?: emptyList()

                if (relatedFiles.isEmpty()) {
                    builder.append("No related files found.")
                } else {
                    for (file in relatedFiles) {
                        val size = formatFileSize(file.size?.toLong())
                        val date = file.modifiedTime?.let {
                            dateFormat.format(Date(it.value))
                        } ?: "Unknown date"
                        builder.append("${file.name}\n")
                            .append("  Size: $size | Modified: $date\n\n")
                    }
                }
                docContent = builder.toString()
            } catch (e: Exception) {
                onAuthError(e)
            } finally {
                hideProgress()
            }
        }
    }

    private fun formatFileSize(size: Long?): String {
        if (size == null) return "0 B"
        if (size < 1024) return "$size B"
        val kb = size / 1024
        if (kb < 1024) return "$kb KB"
        val mb = kb / 1024
        if (mb < 1024) return "$mb MB"
        val gb = mb / 1024
        return "$gb GB"
    }
}