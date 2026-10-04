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
                    val date = df.parseFileTimestamp(tsPart)

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

            downloadMissingPictures()
            uploadPendingPictures()

            onProgressUpdate("Uploading merged database...")
            uploadTimestamp = df.getCurrentFileTimestamp()

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
                val date = df.parseFileTimestamp(uploadTimestamp)
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

    suspend fun manualUpload(): String {
        Log.d(TAG, "Starting manual upload of current database.")
        onProgressUpdate("Preparing database...")
        return withContext(Dispatchers.IO) {
            try {
                uploadPendingPictures()

                onProgressUpdate("Uploading...")
                val timestamp = df.getCurrentFileTimestamp()
                val uploadedFile = performUpload(timestamp)

                "Successfully uploaded current state as $uploadedFile"
            } catch (e: Exception) {
                Log.e(TAG, "Manual upload failed", e)
                throw e
            }
        }
    }

    private suspend fun uploadPendingPictures() {
        try {
            val appDb = PayDatabase(application)
            val pictureDao = appDb.getWorkOrderPictureDao()
            val now = df.getCurrentUTCTimeAsString()
            val storageDir = File(application.cacheDir, "pictures")

            val pendingWo = pictureDao.getPendingWorkOrderUploadsSync()
            for (pic in pendingWo) {
                val tempFile = File(storageDir, "pic_${pic.wopPictureId}.webp")
                if (tempFile.exists()) {
                    val driveId = driveServiceHelper.uploadFile(
                        localFile = tempFile,
                        mimeType = "image/webp",
                        driveFileName = "pic_${pic.wopPictureId}.webp",
                    )
                    pictureDao.updateWorkOrderPicture(
                        pic.copy(
                            wopDriveFileId = driveId,
                            wopUploadTime = now,
                            wopUpdateTime = now
                        )
                    )
                    tempFile.delete()
                }
            }

            val pendingHist = pictureDao.getPendingHistoryUploadsSync()
            for (pic in pendingHist) {
                val tempFile = File(storageDir, "pic_${pic.wohpPictureId}.webp")
                if (tempFile.exists()) {
                    val driveId = driveServiceHelper.uploadFile(
                        localFile = tempFile,
                        mimeType = "image/webp",
                        driveFileName = "pic_${pic.wohpPictureId}.webp",
                    )
                    pictureDao.updateHistoryPicture(
                        pic.copy(
                            wohpDriveFileId = driveId,
                            wohpUploadTime = now,
                            wohpUpdateTime = now
                        )
                    )
                    tempFile.delete()
                }
            }

            val pendingExp = pictureDao.getPendingExpenseUploadsSync()
            for (pic in pendingExp) {
                val tempFile = File(storageDir, "pic_${pic.epPictureId}.webp")
                if (tempFile.exists()) {
                    val driveId = driveServiceHelper.uploadFile(
                        localFile = tempFile,
                        mimeType = "image/webp",
                        driveFileName = "pic_${pic.epPictureId}.webp",
                    )
                    pictureDao.updateExpensePicture(
                        pic.copy(
                            epDriveFileId = driveId,
                            epUploadTime = now,
                            epUpdateTime = now
                        )
                    )
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload pending pictures before sync", e)
        }
    }

    private suspend fun downloadMissingPictures() {
        try {
            val appDb = PayDatabase(application)
            val pictureDao = appDb.getWorkOrderPictureDao()
            val storageDir = File(application.cacheDir, "pictures").apply { mkdirs() }

            for (pic in pictureDao.getAllWorkOrderPicturesSync()) {
                val driveId = pic.wopDriveFileId ?: continue
                val targetFile = File(storageDir, "pic_${pic.wopPictureId}.webp")
                if (!targetFile.exists()) {
                    try {
                        driveServiceHelper.downloadFileById(driveId, targetFile)
                    } catch (_: Exception) {
                    }
                }
            }
            for (pic in pictureDao.getAllHistoryPicturesSync()) {
                val driveId = pic.wohpDriveFileId ?: continue
                val targetFile = File(storageDir, "pic_${pic.wohpPictureId}.webp")
                if (!targetFile.exists()) {
                    try {
                        driveServiceHelper.downloadFileById(driveId, targetFile)
                    } catch (_: Exception) {
                    }
                }
            }
            for (pic in pictureDao.getAllExpensePicturesSync()) {
                val driveId = pic.epDriveFileId ?: continue
                val targetFile = File(storageDir, "pic_${pic.epPictureId}.webp")
                if (!targetFile.exists()) {
                    try {
                        driveServiceHelper.downloadFileById(driveId, targetFile)
                    } catch (_: Exception) {
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading missing pictures during sync", e)
        }
    }

    suspend fun purgeOrphanPicturesOnDrive(): String {
        return withContext(Dispatchers.IO) {
            try {
                onProgressUpdate("Examining picture references on Google Drive...")
                val driveFiles = driveServiceHelper.queryFiles().files ?: emptyList()
                val pictureFilesOnDrive = driveFiles.filter {
                    (it.name != null) && (it.name.startsWith("pic_") || it.name.endsWith(".webp") || it.name.endsWith(
                        ".jpg"
                    ) || it.name.endsWith(".jpeg"))
                }

                val appDb = PayDatabase(application)
                val pictureDao = appDb.getWorkOrderPictureDao()
                val now = df.getCurrentUTCTimeAsString()

                val drivePictureMap = mutableMapOf<Long, com.google.api.services.drive.model.File>()
                for (file in pictureFilesOnDrive) {
                    val name = file.name ?: continue
                    val picId = try {
                        name.removePrefix("pic_")
                            .removeSuffix(".webp")
                            .removeSuffix(".jpg")
                            .removeSuffix(".jpeg")
                            .toLong()
                    } catch (_: Exception) {
                        null
                    }
                    if (picId != null && file.id != null) {
                        drivePictureMap[picId] = file
                    }
                }

                var repairedCount = 0

                // 1. Resolve Work Order Picture references
                val woPics = pictureDao.getAllWorkOrderPicturesSync()
                for (pic in woPics) {
                    val driveFile = drivePictureMap[pic.wopPictureId]
                    if (driveFile != null && pic.wopDriveFileId != driveFile.id) {
                        val updated = pic.copy(
                            wopDriveFileId = driveFile.id,
                            wopUploadTime = pic.wopUploadTime ?: now,
                            wopUpdateTime = now
                        )
                        pictureDao.updateWorkOrderPicture(updated)
                        repairedCount++
                    }
                }

                // 2. Resolve History Picture references
                val histPics = pictureDao.getAllHistoryPicturesSync()
                for (pic in histPics) {
                    val driveFile = drivePictureMap[pic.wohpPictureId]
                    if (driveFile != null && pic.wohpDriveFileId != driveFile.id) {
                        val updated = pic.copy(
                            wohpDriveFileId = driveFile.id,
                            wohpUploadTime = pic.wohpUploadTime ?: now,
                            wohpUpdateTime = now
                        )
                        pictureDao.updateHistoryPicture(updated)
                        repairedCount++
                    }
                }

                // 3. Resolve Expense Picture references
                val expPics = pictureDao.getAllExpensePicturesSync()
                for (pic in expPics) {
                    val driveFile = drivePictureMap[pic.epPictureId]
                    if (driveFile != null && pic.epDriveFileId != driveFile.id) {
                        val updated = pic.copy(
                            epDriveFileId = driveFile.id,
                            epUploadTime = pic.epUploadTime ?: now,
                            epUpdateTime = now
                        )
                        pictureDao.updateExpensePicture(updated)
                        repairedCount++
                    }
                }

                val activeWorkOrderPics = pictureDao.getAllWorkOrderPicturesSync()
                val activeHistoryPics = pictureDao.getAllHistoryPicturesSync()
                val activeExpensePics = pictureDao.getAllExpensePicturesSync()

                val activePictureIds = (
                        activeWorkOrderPics.map { it.wopPictureId } +
                                activeHistoryPics.map { it.wohpPictureId } +
                                activeExpensePics.map { it.epPictureId }
                        ).toSet()

                onProgressUpdate("Purging orphan picture files...")
                var purgedCount = 0
                for (driveFile in pictureFilesOnDrive) {
                    val fileId = driveFile.id ?: continue
                    val name = driveFile.name ?: continue
                    val picId = try {
                        name.removePrefix("pic_")
                            .removeSuffix(".webp")
                            .removeSuffix(".jpg")
                            .removeSuffix(".jpeg")
                            .toLong()
                    } catch (_: Exception) {
                        null
                    }

                    if (picId != null && !activePictureIds.contains(picId)) {
                        try {
                            driveServiceHelper.deleteFile(fileId)
                            purgedCount++
                            Log.d(TAG, "Deleted orphan Drive picture file: $name ($fileId)")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to delete orphan Drive file $fileId", e)
                        }
                    }
                }

                val report = StringBuilder()
                if (repairedCount > 0) {
                    report.append("Repaired $repairedCount broken picture reference(s). ")
                }
                if (purgedCount > 0) {
                    report.append("Purged $purgedCount orphan picture file(s) from Google Drive.")
                } else if (repairedCount == 0) {
                    report.append("All picture references are healthy. No orphan files found on Google Drive.")
                }
                report.toString()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to examine and purge picture references on Drive", e)
                "Error examining picture references: ${e.message}"
            }
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
        displayName: String,
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
                    appDb, df, deviceId, onConflict, onSyncError, isRestore = true,
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
                totalCount += syncHelper.syncWorkOrderHistoryJobSpecs(backupDb)
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
                totalCount += syncHelper.syncWorkOrderPictures(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderHistoryPictures(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncExpensePictures(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncSyncHistory(backupDb).let { it.first + it.second }

                downloadMissingPictures()

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
                val syncHelper = DatabaseSyncHelper(appDb, df, deviceId, onConflict, onSyncError)

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
                totalCount += syncHelper.syncWorkOrderHistoryJobSpecs(backupDb)
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
                totalCount += syncHelper.syncWorkOrderPictures(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncWorkOrderHistoryPictures(backupDb)
                    .let { it.first + it.second }
                totalCount += syncHelper.syncExpensePictures(backupDb)
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
            val driveBaseName = "pay_$timestamp.db"
            val uploadFile = File(backupDir, "upload_$driveBaseName")

            val exported = PayDatabase.exportDatabase(application, uploadFile)
            if ((!exported) || (!uploadFile.exists()) || (uploadFile.length() == 0L)) {
                throw IllegalStateException("Failed to export database for upload")
            }

            driveServiceHelper.uploadFile(uploadFile, "application/vnd.sqlite3", driveBaseName)
            uploadFile.delete()

            driveBaseName
        }
    }

    private suspend fun cleanupOldBackups(fileList: List<com.google.api.services.drive.model.File>) {
        val driveBackups = fileList.asSequence()
            .filter { (it.name != null) && it.name.startsWith("pay_") && it.name.endsWith(".db") }
            .sortedByDescending { it.name }
            .toList()

        if (driveBackups.size <= 1) return

        val twoWeeksAgoMillis = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000L)

        for (i in 1 until driveBackups.size) {
            val file = driveBackups[i]
            val timestampStr = file.name.removePrefix("pay_").removeSuffix(".db")
            val backupDate = df.parseFileTimestamp(timestampStr)
            val fileTime = backupDate?.time ?: file.modifiedTime?.value ?: 0L

            if ((fileTime > 0L) && (fileTime < twoWeeksAgoMillis)) {
                try {
                    driveServiceHelper.deleteFile(file.id)
                    fileList.find { it.name == "${file.name}-wal" }
                        ?.let { driveServiceHelper.deleteFile(it.id) }
                    fileList.find { it.name == "${file.name}-shm" }
                        ?.let { driveServiceHelper.deleteFile(it.id) }
                    Log.d(TAG, "Purged backup older than 2 weeks: ${file.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to purge old backup ${file.name}", e)
                }
            }
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