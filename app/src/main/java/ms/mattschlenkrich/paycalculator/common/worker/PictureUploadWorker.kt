package ms.mattschlenkrich.paycalculator.common.worker

import android.accounts.Account
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.settings.SettingsManager
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.repository.WorkOrderPictureRepository
import ms.mattschlenkrich.paycalculator.ui.sync.DriveServiceHelper
import java.io.File

class PictureUploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val settings = SettingsManager(applicationContext).loadSettings()
        val email = settings.driveAccount ?: return Result.success()

        val db = PayDatabase(applicationContext)
        val repository = WorkOrderPictureRepository(db)

        val pendingWo = repository.getPendingWorkOrderUploadsSync()
        val pendingHist = repository.getPendingHistoryUploadsSync()
        val pendingExp = repository.getPendingExpenseUploadsSync()

        if (pendingWo.isEmpty() && pendingHist.isEmpty() && pendingExp.isEmpty()) {
            return Result.success()
        }

        val driveServiceHelper = try {
            val credential = GoogleAccountCredential.usingOAuth2(
                applicationContext,
                listOf(DriveScopes.DRIVE_APPDATA)
            )
            credential.selectedAccount = Account(email, "com.google")
            val googleDriveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName(applicationContext.getString(R.string.app_name))
                .build()
            DriveServiceHelper(googleDriveService)
        } catch (e: Exception) {
            Log.e("PictureUploadWorker", "Failed to initialize Drive service", e)
            return Result.retry()
        }

        val df = DateFunctions()
        var allSuccess = true
        val storageDir = File(applicationContext.cacheDir, "pictures")

        for (pic in pendingWo) {
            try {
                val tempFile = File(storageDir, "pic_${pic.wopPictureId}.webp")
                if (tempFile.exists()) {
                    val driveId = driveServiceHelper.uploadFile(
                        localFile = tempFile,
                        mimeType = "image/webp",
                        driveFileName = "pic_${pic.wopPictureId}.webp"
                    )
                    val now = df.getCurrentUTCTimeAsString()
                    repository.updateWorkOrderPicture(
                        pic.copy(
                            wopDriveFileId = driveId,
                            wopUploadTime = now,
                            wopUpdateTime = now
                        )
                    )
                    tempFile.delete()
                }
            } catch (e: Exception) {
                Log.e(
                    "PictureUploadWorker",
                    "Failed to upload WorkOrder picture ${pic.wopPictureId}",
                    e
                )
                allSuccess = false
            }
        }

        for (pic in pendingHist) {
            try {
                val tempFile = File(storageDir, "pic_${pic.wohpPictureId}.webp")
                if (tempFile.exists()) {
                    val driveId = driveServiceHelper.uploadFile(
                        localFile = tempFile,
                        mimeType = "image/webp",
                        driveFileName = "pic_${pic.wohpPictureId}.webp"
                    )
                    val now = df.getCurrentUTCTimeAsString()
                    repository.updateHistoryPicture(
                        pic.copy(
                            wohpDriveFileId = driveId,
                            wohpUploadTime = now,
                            wohpUpdateTime = now
                        )
                    )
                    tempFile.delete()
                }
            } catch (e: Exception) {
                Log.e(
                    "PictureUploadWorker",
                    "Failed to upload History picture ${pic.wohpPictureId}",
                    e
                )
                allSuccess = false
            }
        }

        for (pic in pendingExp) {
            try {
                val tempFile = File(storageDir, "pic_${pic.epPictureId}.webp")
                if (tempFile.exists()) {
                    val driveId = driveServiceHelper.uploadFile(
                        localFile = tempFile,
                        mimeType = "image/webp",
                        driveFileName = "pic_${pic.epPictureId}.webp"
                    )
                    val now = df.getCurrentUTCTimeAsString()
                    repository.updateExpensePicture(
                        pic.copy(
                            epDriveFileId = driveId,
                            epUploadTime = now,
                            epUpdateTime = now
                        )
                    )
                    tempFile.delete()
                }
            } catch (e: Exception) {
                Log.e(
                    "PictureUploadWorker",
                    "Failed to upload Expense picture ${pic.epPictureId}",
                    e
                )
                allSuccess = false
            }
        }

        return if (allSuccess) Result.success() else Result.retry()
    }
}