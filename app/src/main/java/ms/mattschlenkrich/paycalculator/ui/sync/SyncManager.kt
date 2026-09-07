package ms.mattschlenkrich.paycalculator.ui.sync

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PAY_DB_NAME
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.SyncHistory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val TAG = "SyncManager"
private const val DB_IDENTITY_HASH = "73589fbc801269925e003ba706d33924"

class SyncManager(
    private val application: Application,
    private val deviceId: Long,
    private val driveServiceHelper: DriveServiceHelper,
    private val df: DateFunctions,
    private val nf: NumberFunctions,
    private val onProgressUpdate: (String) -> Unit,
    private val onConflict: suspend (ConflictInfo) -> ConflictChoice,
    private val onSyncError: (String) -> Unit,
) {
    private val backupDir = File(application.cacheDir, "backup").apply { mkdirs() }

    suspend fun performSync(): Pair<String, String> {
        var status = "Failed"
        val syncReport = StringBuilder("Sync Report:\n")
        val startTime = df.getCurrentUTCTimeAsString()
        var uploadTimestamp: String? = null

        try {
            val appDb = PayDatabase(application)
            val myLastSync = withContext(Dispatchers.IO) {
                appDb.getSyncHistoryDao().getLastSyncTimeSync(deviceId)
            } ?: "1970-01-01 00:00:00"

            syncReport.append("My last sync: $myLastSync\n")

            val allFiles: FileList = driveServiceHelper.queryFiles()
            val fileList = allFiles.files ?: emptyList()

            // Sync lock handling
            val lockFiles = fileList.filter { it.name == "sync.lock" }
            if (lockFiles.isNotEmpty()) {
                val newestLock = lockFiles.maxByOrNull { it.modifiedTime.value }
                if (newestLock != null) {
                    val modifiedTime = newestLock.modifiedTime.value
                    val diffMinutes = (System.currentTimeMillis() - modifiedTime) / (60 * 1000)
                    if (diffMinutes < 5) {
                        status = "Busy"
                        return status to "Aborted: Sync already in progress on another device."
                    } else {
                        for (lock in lockFiles) driveServiceHelper.deleteFile(lock.id)
                        syncReport.append("\nRemoved stale lock file(s).\n")
                    }
                }
            }

            val tempLockFile = File(backupDir, "sync.lock")
            tempLockFile.writeText("Device: $deviceId\nStarted: $startTime")
            driveServiceHelper.uploadFile(tempLockFile, "text/plain", "sync.lock")
            tempLockFile.delete()

            val driveFiles = fileList.asSequence()
                .filter { it.name.startsWith("pay_") && it.name.endsWith(".db") }
                .mapNotNull { file ->
                    val tsPart = file.name.substringAfter("pay_").substringBefore(".db")
                        .substringBefore("_merged")
                    val date = try {
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.parse(tsPart)
                    } catch (_: Exception) {
                        null
                    }

                    if (date != null) {
                        val sqliteTs = df.getDateTimeStringFromDate(date)
                        file to sqliteTs
                    } else null
                }
                .filter { it.second > myLastSync }
                .sortedBy { it.second }
                .toList()

            if (driveFiles.isEmpty()) {
                syncReport.append("No new backups found on Drive.\n")
            } else {
                syncReport.append("Found ${driveFiles.size} backups to evaluate.\n")
                for (pair in driveFiles) {
                    val file = pair.first
                    onProgressUpdate("Syncing ${file.name}...")
                    try {
                        val result = processBackupFile(file)
                        syncReport.append("- ${file.name}: $result\n")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing backup file ${file.name}", e)
                        val errorMsg = "Failed to sync ${file.name}: ${e.message}"
                        onSyncError(errorMsg)
                        syncReport.append("- ${file.name}: FAILED\n")
                    }
                }
            }

            onProgressUpdate("Purging old history...")
            withContext(Dispatchers.IO) {
                appDb.getSyncHistoryDao().purgeOldSyncHistory(50)
            }

            onProgressUpdate("Uploading merged database...")
            uploadTimestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            withContext(Dispatchers.IO) {
                PayDatabase.checkpoint(application)
            }

            val uploadedFile = performUpload(uploadTimestamp)
            syncReport.append("\nMerged database uploaded: $uploadedFile")

            cleanupOldBackups(fileList)

            status = "Success"
        } catch (e: Exception) {
            status = "Error: ${e.message}"
            syncReport.append("\nError: ${e.message}")
            throw e
        } finally {
            val finalSyncTime = if ((status == "Success") && (uploadTimestamp != null)) {
                val date = try {
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.parse(uploadTimestamp)
                } catch (_: Exception) {
                    null
                }
                if (date != null) df.getDateTimeStringFromDate(date) else startTime
            } else startTime
            logSyncHistory(finalSyncTime, status, syncReport.toString())

            // Release lock
            if (status != "Busy") {
                driveServiceHelper.queryFiles().files?.filter { it.name == "sync.lock" }
                    ?.forEach { driveServiceHelper.deleteFile(it.id) }
            }
        }
        return status to syncReport.toString()
    }

    suspend fun getAvailableBackups(): List<DriveFileMeta> {
        val allFiles = driveServiceHelper.queryFiles()
        return allFiles.files?.asSequence()
            ?.filter { it.name.startsWith("pay_") && it.name.endsWith(".db") }
            ?.sortedByDescending { it.name }
            ?.map { DriveFileMeta(it.id, it.name, it.size.toLong(), it.modifiedTime?.value) }
            ?.toList() ?: emptyList()
    }

    fun getLocalBackups(): List<File> {
        return backupDir.listFiles()?.asSequence()?.filter {
            it.name.startsWith("pay_") && it.name.endsWith(".db")
        }?.sortedByDescending { it.name }?.toList() ?: emptyList()
    }

    suspend fun deleteBackup(file: DriveFileMeta): String {
        onProgressUpdate("Deleting ${file.name}...")
        return try {
            driveServiceHelper.deleteFile(file.id)
            val allFiles = driveServiceHelper.queryFiles()
            allFiles.files?.filter { (it.name == "${file.name}-wal") || (it.name == "${file.name}-shm") }
                ?.forEach { auxFile ->
                    driveServiceHelper.deleteFile(auxFile.id)
                }
            "Successfully deleted ${file.name}."
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete backup", e)
            throw e
        }
    }

    suspend fun restoreSpecific(fileName: String): String {
        onProgressUpdate("Downloading $fileName...")
        val localTempFile = File(backupDir, "restore_temp.db")
        val localTempWal = File(backupDir, "restore_temp.db-wal")
        val localTempShm = File(backupDir, "restore_temp.db-shm")

        try {
            driveServiceHelper.downloadBinaryFile(fileName, localTempFile)
            val allFiles = driveServiceHelper.queryFiles()
            allFiles.files?.find { it.name == "$fileName-wal" }?.let {
                driveServiceHelper.downloadBinaryFile(it.name, localTempWal)
            }
            allFiles.files?.find { it.name == "$fileName-shm" }?.let {
                driveServiceHelper.downloadBinaryFile(it.name, localTempShm)
            }
            return restoreFromFile(localTempFile, fileName)
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            throw e
        } finally {
            if (localTempFile.exists()) localTempFile.delete()
            if (localTempWal.exists()) localTempWal.delete()
            if (localTempShm.exists()) localTempShm.delete()
        }
    }

    suspend fun repairLocalDatabase(): String {
        return withContext(Dispatchers.IO) {
            try {
                onProgressUpdate("Repairing local database...")
                val dbName = PAY_DB_NAME
                val dbPath = application.getDatabasePath(dbName)
                if (!dbPath.exists()) return@withContext "Local database file not found."

                Log.d(TAG, "Closing database for repair...")
                PayDatabase.closeDatabase()

                val db = SQLiteDatabase.openDatabase(
                    dbPath.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READWRITE,
                )

                Log.d(TAG, "Forcing identity hash...")
                db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
                db.execSQL("INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, '$DB_IDENTITY_HASH')")

                Log.d(TAG, "Recreating views...")
                db.execSQL("DROP VIEW IF EXISTS `ExtraDefinitionAndType`")
                db.execSQL("CREATE VIEW `ExtraDefinitionAndType` AS SELECT extraDef.*, extraType.* FROM workExtrasDefinitions as extraDef LEFT JOIN workExtraTypes as extraType ON extraDef.weExtraTypeId = extraType.workExtraTypeId")

                // Final safety: clear WAL
                db.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")

                db.close()
                "Local database metadata repaired successfully. Restarting..."
            } catch (e: Exception) {
                Log.e(TAG, "Repair failed", e)
                "Repair failed: ${e.message}"
            }
        }
    }

    private suspend fun restoreFromFile(
        dbFile: File,
        displayName: String
    ): String {
        onProgressUpdate("Clearing local data...")
        return withContext(Dispatchers.IO) {
            try {
                val appDb = PayDatabase(application)
                appDb.clearAllTables()

                val backupDb = SQLiteDatabase.openDatabase(
                    dbFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY,
                )

                val syncHelper = DatabaseSyncHelper(
                    appDb, deviceId, onConflict, onSyncError, isRestore = true
                )

                onProgressUpdate("Copying records...")
                var totalCount = 0

                totalCount += syncHelper.syncEmployers(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncTaxTypes(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncTaxEffectiveDates(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncWorkTaxRules(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncEmployerTaxTypes(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncEmployerPayRates(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkExtraTypes(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkExtrasDefinitions(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncPayPeriods(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkDates(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkDateExtras(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkPayPeriodExtras(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrders(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderHistory(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkPerformed(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncJobSpecs(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncAreas(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderHistoryWorkPerformed(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderJobSpecs(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncMaterials(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderHistoryMaterials(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncJobSpecMerged(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncMaterialMerged(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkPerformedMerged(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderHistoryTimeWorked(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderHistoryExpense(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncSyncHistory(backupDb).let { it.first + it.second }

                backupDb.close()
                "Successfully restored $totalCount records from $displayName."
            } catch (e: Exception) {
                Log.e(TAG, "Restore failed", e)
                throw e
            }
        }
    }

    private suspend fun processBackupFile(file: com.google.api.services.drive.model.File): String {
        val localBackupFile = File(backupDir, file.name)
        driveServiceHelper.downloadBinaryFile(file.name, localBackupFile)

        val result = withContext(Dispatchers.IO) {
            try {
                val backupDb = SQLiteDatabase.openDatabase(
                    localBackupFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY,
                )
                val appDb = PayDatabase(application)
                val syncHelper = DatabaseSyncHelper(appDb, deviceId, onConflict, onSyncError)

                var totalCount = 0
                totalCount += syncHelper.syncEmployers(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncTaxTypes(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncTaxEffectiveDates(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncWorkTaxRules(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncEmployerTaxTypes(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncEmployerPayRates(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkExtraTypes(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkExtrasDefinitions(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncPayPeriods(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkDates(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkDateExtras(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkPayPeriodExtras(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrders(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderHistory(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkPerformed(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncJobSpecs(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncAreas(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderHistoryWorkPerformed(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderJobSpecs(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncMaterials(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderHistoryMaterials(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncJobSpecMerged(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncMaterialMerged(backupDb).let { it.first + it.second }
                totalCount += syncHelper.syncWorkPerformedMerged(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderHistoryTimeWorked(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderHistoryExpense(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncSyncHistory(backupDb).let { it.first + it.second }

                backupDb.close()
                if (totalCount == 0) "Already up to date."
                else "Synchronized: $totalCount"
            } catch (e: Exception) {
                throw e
            }
        }
        if (localBackupFile.exists()) localBackupFile.delete()
        return result
    }

    private suspend fun performUpload(timestamp: String): String {
        return withContext(Dispatchers.IO) {
            val dbPath = application.getDatabasePath(PAY_DB_NAME)
            val driveBaseName = "pay_$timestamp.db"

            val uploadFile = File(backupDir, "upload_$driveBaseName")
            dbPath.inputStream().use { input ->
                uploadFile.outputStream().use { output -> input.copyTo(output) }
            }
            driveServiceHelper.uploadFile(uploadFile, "application/vnd.sqlite3", driveBaseName)
            uploadFile.delete()

            listOf("-wal", "-shm").forEach { suffix ->
                val localFile = File(dbPath.path + suffix)
                if (localFile.exists() && (localFile.length() > 0)) {
                    val driveName = "$driveBaseName$suffix"
                    val upFile = File(backupDir, "upload_$driveName")
                    localFile.inputStream().use { input ->
                        upFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    driveServiceHelper.uploadFile(upFile, "application/vnd.sqlite3", driveName)
                    upFile.delete()
                }
            }
            driveBaseName
        }
    }

    private suspend fun cleanupOldBackups(fileList: List<com.google.api.services.drive.model.File>) {
        val driveBackups = fileList.asSequence()
            .filter { it.name.startsWith("pay_") && it.name.endsWith(".db") }
            .sortedByDescending { it.name }
            .toList()

        if (driveBackups.size <= 5) return

        for (i in 5 until driveBackups.size) {
            val file = driveBackups[i]
            driveServiceHelper.deleteFile(file.id)
            fileList.find { it.name == "${file.name}-wal" }
                ?.let { driveServiceHelper.deleteFile(it.id) }
            fileList.find { it.name == "${file.name}-shm" }
                ?.let { driveServiceHelper.deleteFile(it.id) }
        }
    }

    private suspend fun logSyncHistory(time: String, status: String, records: String) {
        withContext(Dispatchers.IO) {
            try {
                val syncHistory = SyncHistory(
                    syncId = nf.generateRandomIdAsLong(), syncTime = time,
                    syncSourceName = "Google Drive", syncDeviceId = deviceId,
                    syncStatus = status, syncRecordsProcessed = records,
                )
                PayDatabase(application).getSyncHistoryDao().insertSyncHistory(syncHistory)
            } catch (e: Exception) {
                Log.e(TAG, "History log failed", e)
            }
        }
    }
}