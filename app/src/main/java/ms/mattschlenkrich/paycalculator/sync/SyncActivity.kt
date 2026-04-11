package ms.mattschlenkrich.paycalculator.sync

import android.accounts.Account
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import java.io.File
import java.io.FileNotFoundException
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private const val TAG: String = "SyncActivity"

class SyncActivity : AppCompatActivity() {

    private var mDriveServiceHelper: DriveServiceHelper? = null
    private var mCurrentAccount: Account? = null
    private var mOpenFileId: String? = null

    private var mFileTitleEditText: EditText? = null
    private var mDocContentEditText: EditText? = null

    private var mProgressOverlay: View? = null
    private var mProgressText: TextView? = null
    private var mSyncProgressBar: android.widget.ProgressBar? = null
    private var mErrorTextView: TextView? = null
    private var mErrorScrollView: View? = null

    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sync)

        credentialManager = CredentialManager.create(this)

        mFileTitleEditText = findViewById(R.id.file_title_edittext)
        mDocContentEditText = findViewById(R.id.doc_content_edittext)

        mProgressOverlay = findViewById(R.id.progress_overlay)
        mProgressText = findViewById(R.id.progress_text)
        mSyncProgressBar = findViewById(R.id.sync_progressbar)
        mErrorTextView = findViewById(R.id.error_textview)
        mErrorScrollView = findViewById(R.id.error_scrollview)

        findViewById<View>(R.id.query_btn).setOnClickListener { query() }
        findViewById<View>(R.id.up_button).setOnClickListener { testUpload() }
        findViewById<View>(R.id.down_button).setOnClickListener { testDownload() }
        findViewById<View>(R.id.update_button).setOnClickListener { testUpdate() }
        findViewById<View>(R.id.return_button).setOnClickListener { finish() }


        // Initiate sign-in with Credential Manager
        signInWithCredentialManager()
    }

    private fun showProgress(message: String) {
        mErrorScrollView?.visibility = View.GONE
        mProgressText?.text = message
        mProgressOverlay?.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        mProgressOverlay?.visibility = View.GONE
    }

    /**
     * Gets or creates the target folder "Apps/<app_name>" on Google Drive.
     */
    private suspend fun getTargetFolderId(helper: DriveServiceHelper): String {
        val appsFolderId = helper.getOrCreateFolder("Apps")
        val appName = getString(R.string.app_name)
        return helper.getOrCreateFolder(appName, appsFolderId)
    }

    /**
     * Downloads all backup files found on Google Drive in the Apps/<app_name> folder
     * to the app's external files directory.
     * Also ensures the latest database is available in the internal databases directory
     * for active use.
     */
    fun testDownload() {
        lifecycleScope.launch {
            val helper = mDriveServiceHelper ?: return@launch
            val targetFolderId = getTargetFolderId(helper)
            performDownload(helper, targetFolderId)
        }
    }

    private suspend fun performDownload(helper: DriveServiceHelper, targetFolderId: String) {
        showProgress("Searching for backups...")
        try {
            // 1. Query for all files in the target folder
            val fileList: FileList = helper.queryFiles(targetFolderId)
            val driveFiles = fileList.files ?: emptyList()

            if (driveFiles.isEmpty()) {
                Toast.makeText(
                    this@SyncActivity,
                    "No backups found on Drive",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            // 2. Define target directory
            val dbDir = File(applicationInfo.dataDir, "databases")
            if (!dbDir.exists()) dbDir.mkdirs()

            // 3. Find all database files, sorted oldest to newest
            val dbFiles = driveFiles
                .filter { it.name.startsWith("pay_") && it.name.endsWith(".db") }
                .sortedBy { it.name } // Oldest first

            var downloadCount = 0

            // 4. Download all database files and their associates to /databases
            for (dbFile in dbFiles) {
                val prefix = dbFile.name.removeSuffix(".db")
                for (driveFile in driveFiles) {
                    if (driveFile.name.startsWith(prefix)) {
                        val internalFile = File(dbDir, driveFile.name)
                        if (!internalFile.exists()) {
                            showProgress("Downloading ${driveFile.name} to app...")
                            helper.downloadBinaryFile(driveFile.name, internalFile, targetFolderId)
                            downloadCount++
                        }
                    }
                }
            }

            if (downloadCount > 0) {
                Log.d(TAG, "Downloaded $downloadCount new files to ${dbDir.absolutePath}")
                setResult(RESULT_OK)
                PayDatabase.resetInstance()
                Toast.makeText(
                    this@SyncActivity,
                    "Downloaded $downloadCount files to app storage",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this@SyncActivity,
                    "Local backups are already up to date.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            mDocContentEditText?.setText("Files stored in: ${dbDir.absolutePath}")

        } catch (e: Exception) {
            handleError("Failed to download backups", e)
        } finally {
            hideProgress()
        }
    }

    fun testUpload() {
        showProgress("Uploading database...")
        lifecycleScope.launch {
            try {
                val helper = mDriveServiceHelper ?: run {
                    hideProgress()
                    return@launch
                }

                val targetFolderId = getTargetFolderId(helper)

                // Ensure all WAL data is committed to the main DB file
                PayDatabase.checkpoint(this@SyncActivity)

                // The database is located in the app's custom databases directory: /data/data/<package>/databases/
                val dbDir = File(applicationInfo.dataDir, "databases")
                val dbFile = File(dbDir, "pay.db")

                if (!dbFile.exists()) {
                    throw FileNotFoundException("Database file not found at ${dbFile.absolutePath}")
                }

                // Generate a timestamped filename for Google Drive
                val timestamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val driveFileName = "pay_$timestamp.db"

                showProgress("Uploading $driveFileName...")
                val driveFileId = helper.uploadFile(
                    localFile = dbFile,
                    mimeType = "application/x-sqlite3",
                    driveFileName = driveFileName,
                    folderId = targetFolderId
                )

                // Also upload .db-wal and .db-shm files if they exist
                listOf("-wal", "-shm").forEach { suffix ->
                    val walShmFile = File(dbDir, "pay.db$suffix")
                    if (walShmFile.exists()) {
                        val extraDriveName = "$driveFileName$suffix"
                        helper.uploadFile(
                            localFile = walShmFile,
                            mimeType = "application/octet-stream",
                            driveFileName = extraDriveName,
                            folderId = targetFolderId
                        )
                        Log.d(TAG, "Uploaded extra file: $extraDriveName")
                    }
                }

                Log.d(
                    TAG,
                    "Successfully uploaded database file as $driveFileName and associated files to Apps folder."
                )
                setResult(RESULT_OK)
                Toast.makeText(
                    this@SyncActivity,
                    "Database upload successful: $driveFileName",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                handleError("Upload failed", e)
            } finally {
                hideProgress()
            }
        }
    }

    /**
     * Update function (Sync):
     * 1. Downloads all backups from Google Drive.
     * 2. Analyzes the latest backup and provides a summary of new records.
     * 3. Keeps only the 3 most recent backups on Google Drive (deletes others).
     * 4. Keeps only the 3 most recent backups in local storage (deletes others).
     * 5. Uploads a fresh backup of the merged database to Google Drive.
     */
    fun testUpdate() {
        lifecycleScope.launch {
            try {
                val helper = mDriveServiceHelper ?: run {
                    hideProgress()
                    return@launch
                }

                val targetFolderId = getTargetFolderId(helper)

                // 1. Download all backups first
                performDownload(helper, targetFolderId)

                // 2. Analyze and merge each downloaded backup in chronological order
                val dbDir = File(applicationInfo.dataDir, "databases")
                val localBackups = dbDir.listFiles { _, name ->
                    name.startsWith("pay_") && name.endsWith(".db")
                }?.sortedBy { it.name } ?: emptyList()

                if (localBackups.isNotEmpty()) {
                    val summaryBuilder = StringBuilder("Sync Analysis Complete:\n\n")
                    for (localDb in localBackups) {
                        showProgress("Analyzing ${localDb.name}...")
                        val mergeHelper = MergeHelper(this@SyncActivity, localDb.absolutePath)

                        showProgress("Applying changes from ${localDb.name}...")
                        mSyncProgressBar?.visibility = View.VISIBLE
                        val summary = mergeHelper.applySync { progress, total ->
                            runOnUiThread {
                                mSyncProgressBar?.max = total
                                mSyncProgressBar?.progress = progress
                                mProgressText?.text =
                                    "Syncing ${localDb.name}: table ${progress + 1} of $total..."
                            }
                        }
                        summaryBuilder.append("Results for ${localDb.name}:\n$summary\n\n")
                    }

                    mSyncProgressBar?.visibility = View.GONE
                    mDocContentEditText?.setText(summaryBuilder.toString())
                    Log.d(TAG, "Sync Result: ${summaryBuilder.toString()}")

                    // Clean up local storage: delete all backup files used for sync
                    showProgress("Cleaning up local backups...")
                    for (localDb in localBackups) {
                        Log.d(TAG, "Deleting processed local backup: ${localDb.name}")
                        localDb.delete()
                        File(localDb.absolutePath + "-wal").delete()
                        File(localDb.absolutePath + "-shm").delete()
                    }

                    // Notify Room that the database has changed and reset the instance
                    val db = PayDatabase(this@SyncActivity)
                    db.invalidationTracker.refreshVersionsAsync()
                    PayDatabase.resetInstance()
                } else {
                    mDocContentEditText?.setText("No backups found to sync.")
                }

                // 3. Clean up Google Drive: Keep only 3 latest
                showProgress("Cleaning up old backups...")
                val driveFileList = helper.queryFiles(targetFolderId)
                val driveBackups = driveFileList.files
                    ?.filter { it.name.startsWith("pay_") && it.name.endsWith(".db") }
                    ?.sortedByDescending { it.name } ?: emptyList()

                if (driveBackups.size > 3) {
                    val toDelete = driveBackups.drop(3)
                    for (file in toDelete) {
                        Log.d(TAG, "Deleting old backup from Drive: ${file.name}")
                        helper.deleteFile(file.id)
                        // Also try to delete associated wal/shm files
                        listOf("-wal", "-shm").forEach { suffix ->
                            val extraName = "${file.name}$suffix"
                            driveFileList.files?.find { it.name == extraName }?.let {
                                helper.deleteFile(it.id)
                            }
                        }
                    }
                }

                // 4. Local storage was already cleaned up above after sync.

                // 5. Upload a fresh backup of the merged database
                showProgress("Creating fresh backup...")
                // Flush WAL to disk
                PayDatabase.checkpoint(this@SyncActivity)

                val dbFile = File(dbDir, "pay.db")
                if (dbFile.exists()) {
                    val timestamp =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val driveFileName = "pay_${timestamp}_merged.db"

                    showProgress("Uploading $driveFileName...")
                    helper.uploadFile(
                        localFile = dbFile,
                        mimeType = "application/x-sqlite3",
                        driveFileName = driveFileName,
                        folderId = targetFolderId
                    )

                    // Also upload .db-wal and .db-shm files if they exist
                    listOf("-wal", "-shm").forEach { suffix ->
                        val walShmFile = File(dbDir, "pay.db$suffix")
                        if (walShmFile.exists()) {
                            helper.uploadFile(
                                localFile = walShmFile,
                                mimeType = "application/octet-stream",
                                driveFileName = "$driveFileName$suffix",
                                folderId = targetFolderId
                            )
                        }
                    }
                }

                Toast.makeText(
                    this@SyncActivity,
                    "Sync, Cleanup, and Backup complete.",
                    Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_OK)

            } catch (e: Exception) {
                handleError("Update failed", e)
            } finally {
                hideProgress()
            }
        }
    }

    private fun handleError(message: String, e: Exception) {
        Log.e(TAG, message, e)
        val errorDetail = when (e) {
            is com.google.api.client.googleapis.json.GoogleJsonResponseException -> {
                val firstError = e.details?.errors?.firstOrNull()
                "Google API Error: [${firstError?.reason}] ${firstError?.message}"
            }

            is UserRecoverableAuthIOException -> {
                recoverAuthLauncher.launch(e.intent)
                "Authorization required. Please follow the prompt."
            }

            else -> e.message ?: "Unknown error"
        }

        val fullMessage = "$message: $errorDetail"
        mErrorTextView?.text = fullMessage
        mErrorScrollView?.visibility = View.VISIBLE
        Toast.makeText(this@SyncActivity, fullMessage, Toast.LENGTH_LONG).show()
    }

    private val recoverAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val email = mCurrentAccount?.name
            if (email != null) {
                mDriveServiceHelper = null // Force re-initialization
                initializeDriveService(email)
            }
        }
    }

    private fun signInWithCredentialManager() {
        lifecycleScope.launch {
            val serverClientId = getString(R.string.default_web_client_id)
            val nonce = generateNonce()

            Log.d(TAG, "Starting sign-in with serverClientId: $serverClientId")

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .setNonce(nonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(this@SyncActivity, request)
                handleSignInResult(result.credential)
            } catch (e: GetCredentialException) {
                handleCredentialException(e)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected sign-in error", e)
            }
        }
    }

    private fun handleCredentialException(e: GetCredentialException) {
        when (e) {
            is GetCredentialCancellationException -> {
                Log.w(TAG, "Sign-in was canceled by the user.")
            }

            is NoCredentialException -> {
                Log.e(TAG, "No credentials available. Please ensure:")
                Log.e(TAG, "1. A Google account is signed in on the device/emulator.")
                Log.e(
                    TAG,
                    "2. The SHA-1 fingerprint and package name ($packageName) are registered in Google Cloud Console."
                )
                Log.e(TAG, "3. default_web_client_id is a WEB client ID, not an Android client ID.")
                logSHA1Fingerprint()
            }

            else -> {
                Log.e(TAG, "Credential Manager error (${e.javaClass.simpleName}): ${e.message}")
                if (e.message?.contains("16") == true) {
                    Log.e(
                        TAG,
                        "Developer error (16) usually indicates a SHA-1 or Package Name mismatch."
                    )
                    logSHA1Fingerprint()
                }
            }
        }
    }

    private fun logSHA1Fingerprint() {
        try {
            val packageInfo =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageManager.getPackageInfo(
                        packageName,
                        PackageManager.GET_SIGNING_CERTIFICATES
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                }

            val signatures =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.signingInfo?.apkContentsSigners
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.signatures
                }

            signatures?.forEach { signature ->
                val md = MessageDigest.getInstance("SHA-1")
                val digest = md.digest(signature.toByteArray())
                val hexString = digest.joinToString(":") { "%02X".format(it) }
                Log.i(TAG, "Your SHA-1 Fingerprint: $hexString")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not get SHA-1 fingerprint", e)
        }
    }

    private fun generateNonce(): String {
        val rawNonce = ByteArray(16)
        SecureRandom().nextBytes(rawNonce)
        return Base64.encodeToString(
            rawNonce,
            Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE
        )
    }

    private fun handleSignInResult(credential: Credential) {
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            Log.d(TAG, "Signed in as ${googleIdTokenCredential.id}")
            initializeDriveService(googleIdTokenCredential.id)
        } else {
            Log.e(TAG, "Unexpected credential type: ${credential.type}")
        }
    }


    private fun initializeDriveService(email: String) {
        if (email.isBlank()) {
            Log.e(TAG, "Email is blank, cannot initialize Drive service.")
            return
        }

        val account = Account(email, "com.google")

        if (mDriveServiceHelper != null && mCurrentAccount == account) {
            Log.d(TAG, "Drive service already initialized for $email.")
            return
        }

        Log.d(TAG, "Initializing Drive service for $email")

        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                applicationContext,
                DRIVE_SCOPES
            )
            credential.selectedAccount = account

            Log.d(TAG, "Credential set with account: ${credential.selectedAccountName}")

            val googleDriveService = Drive.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                credential
            )
                .setApplicationName(getString(R.string.app_name))
                .build()

            mDriveServiceHelper = DriveServiceHelper(googleDriveService)
            mCurrentAccount = account
            Log.d(TAG, "Drive service successfully initialized.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Drive service", e)
            mDriveServiceHelper = null
            mCurrentAccount = null
        }
    }

    private fun query() {
        val helper = mDriveServiceHelper ?: run {
            Log.e(TAG, "query: Drive service not initialized.")
            return
        }
        showProgress("Querying files...")
        lifecycleScope.launch {
            try {
                val targetFolderId = getTargetFolderId(helper)
                val fileList: FileList = helper.queryFiles(targetFolderId)
                val builder = StringBuilder()
                for (file in fileList.files) {
                    builder.append(file.name).append("\n")
                }
                mFileTitleEditText?.setText(getString(R.string.query))
                mDocContentEditText?.setText(builder.toString())
                setReadOnlyMode()
            } catch (e: Exception) {
                handleError("Unable to query.", e)
            } finally {
                hideProgress()
            }
        }
    }

    private fun setReadOnlyMode() {
        mFileTitleEditText?.isEnabled = false
        mDocContentEditText?.isEnabled = false
        mOpenFileId = null
    }

    companion object {
        private val DRIVE_SCOPES = listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE)
        private val HTTP_TRANSPORT: HttpTransport = NetHttpTransport()
        private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    }
}