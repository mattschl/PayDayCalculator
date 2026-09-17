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
        val pending = repository.getPendingUploadsSync()
        if (pending.isEmpty()) return Result.success()

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

        var allSuccess = true
        for (pic in pending) {
            try {
                pic.localCachePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val driveId = driveServiceHelper.uploadFile(
                            localFile = file,
                            mimeType = "image/webp",
                            driveFileName = "pic_${pic.pictureId}.webp"
                        )
                        repository.updatePicture(
                            pic.copy(
                                driveFileId = driveId,
                                isUploaded = true
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("PictureUploadWorker", "Failed to upload picture ${pic.pictureId}", e)
                allSuccess = false
            }
        }

        return if (allSuccess) Result.success() else Result.retry()
    }
}