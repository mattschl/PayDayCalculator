package ms.mattschlenkrich.paycalculator.ui.sync

import android.util.Log
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * A utility for performing read/write operations on Drive files via the REST API.
 */
class DriveServiceHelper(private val mDriveService: Drive) {

    /**
     * Uploads a local file to Google Drive in a specific folder.
     */
    suspend fun uploadFile(
        localFile: File,
        mimeType: String,
        driveFileName: String,
        folderId: String? = "appDataFolder"
    ): String = withContext(Dispatchers.IO) {
        val parents = listOf(folderId ?: "appDataFolder")

        val metadata = com.google.api.services.drive.model.File()
            .setName(driveFileName)
            .setMimeType(mimeType)
            .setParents(parents)

        val mediaContent = FileContent(mimeType, localFile)

        val googleFile = mDriveService.files().create(metadata, mediaContent)
            .setFields("id, name, parents")
            .execute() ?: throw IOException("Null result when uploading file.")

        googleFile.id
    }

    /**
     * Finds or creates a folder with the given name under the specified parent.
     */
    suspend fun getOrCreateFolder(folderName: String, parentId: String? = "appDataFolder"): String =
        withContext(Dispatchers.IO) {
            val parent = parentId ?: "appDataFolder"
            val query =
                "name = '$folderName' and mimeType = 'application/vnd.google-apps.folder' and '$parent' in parents and trashed = false"

            Log.d("DriveServiceHelper", "Searching for folder: $folderName under parent: $parent")

            val result = mDriveService.files().list()
                .setQ(query)
                .setSpaces("appDataFolder")
                .setFields("files(id, name)")
                .execute()

            val files = result.files
            if (!files.isNullOrEmpty()) {
                Log.d(
                    "DriveServiceHelper",
                    "Found existing folder: ${files[0].name} with ID: ${files[0].id}"
                )
                return@withContext files[0].id
            }

            // Not found, create it
            Log.d("DriveServiceHelper", "Creating new folder: $folderName under parent: $parent")
            val metadata = com.google.api.services.drive.model.File()
                .setName(folderName)
                .setMimeType("application/vnd.google-apps.folder")
                .setParents(listOf(parent))

            val folder = mDriveService.files().create(metadata)
                .setFields("id")
                .execute() ?: throw IOException("Null result when creating folder.")
            folder.id
        }

    /**
     * Finds a file ID on Google Drive by its name and folder.
     */
    suspend fun findFileIdByName(fileName: String, folderId: String? = "appDataFolder"): String? =
        withContext(Dispatchers.IO) {
            var query = "name = '$fileName' and trashed = false"
            if (folderId != null) {
                query += " and '$folderId' in parents"
            }

            val result = mDriveService.files().list()
                .setQ(query)
                .setSpaces("appDataFolder")
                .setFields("files(id, name)")
                .execute()

            val files = result.files
            if (files.isNullOrEmpty()) null else files[0].id
        }

    /**
     * Downloads a file from Google Drive to a local file.
     */
    suspend fun downloadBinaryFile(fileName: String, targetFile: File, folderId: String? = null) =
        withContext(Dispatchers.IO) {
            val fileId = findFileIdByName(fileName, folderId)
                ?: throw IOException("File not found on Drive: $fileName")

            FileOutputStream(targetFile).use { outputStream ->
                mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            }
        }

    /**
     * Deletes a file from Google Drive.
     */
    suspend fun deleteFile(fileId: String) = withContext(Dispatchers.IO) {
        mDriveService.files().delete(fileId).execute()
    }

    /**
     * Returns a [com.google.api.services.drive.model.FileList] containing files in a specific folder.
     * Explicitly requests 'id' and 'name' fields to ensure they are available in the result.
     */
    suspend fun queryFiles(folderId: String? = "appDataFolder"): FileList =
        withContext(Dispatchers.IO) {
            var listRequest = mDriveService.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name, modifiedTime, size)")

            if (folderId != null) {
                listRequest = listRequest.setQ("'$folderId' in parents and trashed = false")
            }

            listRequest.execute()
        }

    /**
     * Utility to fetch file content as a string.
     */
    suspend fun downloadFileByName(fileName: String, targetFile: File): String =
        withContext(Dispatchers.IO) {
            val fileId = findFileIdByName(fileName)
                ?: throw IOException("File not found on Drive: $fileName")
            FileOutputStream(targetFile).use { outputStream ->
                mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            }
            mDriveService.files().get(fileId).executeMediaAsInputStream().use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        }
}