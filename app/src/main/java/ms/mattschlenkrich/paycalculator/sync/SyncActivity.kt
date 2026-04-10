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
     * Finds the latest timestamped backup on Google Drive in the Apps/<app_name> folder
     * and downloads it to the app's custom databases directory without renaming it.
     * Also downloads corresponding .wal and .shm files if they exist.
     * Cancels the download if the main database file already exists locally.
     */
    fun testDownload() {
        showProgress("Searching for latest backup...")
        lifecycleScope.launch {
            try {
                val helper = mDriveServiceHelper ?: run {
                    hideProgress()
                    return@launch
                }

                val targetFolderId = getTargetFolderId(helper)

                // 1. Query for all files in the target folder
                val fileList: FileList = helper.queryFiles(targetFolderId)

                // 2. Find the one with the most recent timestamp in the name (lexicographical order works for yyyyMMdd_HHmmss)
                val latestFile = fileList.files
                    ?.filter { it.name.startsWith("pay_") && it.name.endsWith(".db") }
                    ?.maxByOrNull { it.name }

                if (latestFile == null) {
                    Toast.makeText(
                        this@SyncActivity,
                        "No database backups found on Drive",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val driveFileName = latestFile.name

                // 3. Define the target local path
                val dbDir = File(applicationInfo.dataDir, "databases")
                val localFile = File(dbDir, driveFileName)

                // Check if file already exists in internal storage
                if (localFile.exists()) {
                    Log.d(TAG, "File $driveFileName already exists locally. Download cancelled.")
                    Toast.makeText(
                        this@SyncActivity,
                        "File already exists locally.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // Ensure parent directory exists
                dbDir.mkdirs()

                showProgress("Downloading $driveFileName...")
                helper.downloadBinaryFile(driveFileName, localFile, targetFolderId)

                // Also attempt to download .db-wal and .db-shm files
                listOf("-wal", "-shm").forEach { suffix ->
                    val extraFileName = "$driveFileName$suffix"
                    val extraLocalFile = File(dbDir, extraFileName)
                    try {
                        helper.downloadBinaryFile(extraFileName, extraLocalFile, targetFolderId)
                        Log.d(TAG, "Downloaded extra file: $extraFileName")
                    } catch (e: Exception) {
                        Log.d(
                            TAG,
                            "Optional file $extraFileName not found or failed to download: ${e.message}"
                        )
                    }
                }

                Log.d(
                    TAG,
                    "Latest database $driveFileName and associated files downloaded to ${localFile.absolutePath}"
                )
                Toast.makeText(
                    this@SyncActivity,
                    "Downloaded latest: $driveFileName",
                    Toast.LENGTH_SHORT
                ).show()
                mDocContentEditText?.setText("Downloaded $driveFileName to internal storage")

            } catch (e: Exception) {
                handleError("Failed to download latest database", e)
            } finally {
                hideProgress()
            }
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
     * Update function:
     * 1. Keeps only the 3 most recent backups on Google Drive (deletes others).
     * 2. Keeps only the 3 most recent backups in local storage (deletes others).
     * 3. Syncs the absolute latest from Drive if not present locally.
     */
    fun testUpdate() {
        showProgress("Cleaning up old backups...")
        lifecycleScope.launch {
            try {
                val helper = mDriveServiceHelper ?: run {
                    hideProgress()
                    return@launch
                }

                val targetFolderId = getTargetFolderId(helper)

                // 1. Clean up Google Drive: Keep only 3 latest
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

                // 2. Clean up Local Storage: Keep only 3 latest
                val dbDir = File(applicationInfo.dataDir, "databases")
                val localBackups = dbDir.listFiles { _, name ->
                    name.startsWith("pay_") && name.endsWith(".db")
                }?.sortedByDescending { it.name } ?: emptyList()

                if (localBackups.size > 3) {
                    val toDeleteLocal = localBackups.drop(3)
                    for (file in toDeleteLocal) {
                        Log.d(TAG, "Deleting old local backup: ${file.name}")
                        file.delete()
                        // Delete associated wal/shm if present
                        File(file.absolutePath + "-wal").delete()
                        File(file.absolutePath + "-shm").delete()
                    }
                }

                Toast.makeText(
                    this@SyncActivity,
                    "Cleanup complete. Syncing latest...",
                    Toast.LENGTH_SHORT
                ).show()

                // 3. Proceed to sync the latest file
                hideProgress()
                testDownload()

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