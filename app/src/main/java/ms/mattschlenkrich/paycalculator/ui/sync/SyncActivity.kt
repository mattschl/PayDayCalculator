package ms.mattschlenkrich.paycalculator.ui.sync

import android.accounts.Account
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.PREFS_NAME
import ms.mattschlenkrich.paycalculator.common.SYNC_ACCOUNT_EMAIL
import ms.mattschlenkrich.paycalculator.common.compose.PayCalculatorTheme
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.core.content.edit
import ms.mattschlenkrich.paycalculator.ui.sync.composable.SyncScreen

private const val TAG: String = "SyncActivity"

class SyncActivity : ComponentActivity() {

    private var mDriveServiceHelper: DriveServiceHelper? = null
    private var mCurrentAccount: Account? = null

    private var docContent by mutableStateOf("")
    private var isLoading by mutableStateOf(false)
    private var progressMessage by mutableStateOf("")
    private var syncProgress by mutableIntStateOf(0)
    private var syncMax by mutableIntStateOf(0)
    private var errorMessage by mutableStateOf<String?>(null)

    private lateinit var credentialManager: CredentialManager
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        credentialManager = CredentialManager.create(this)
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        setContent {
            val settings by settingsViewModel.settings.observeAsState()

            PayCalculatorTheme(
                fontSize = settings?.fontSize ?: 16f
            ) {
                SyncScreen(
                    docContent = docContent,
                    isLoading = isLoading,
                    progressMessage = progressMessage,
                    syncProgress = syncProgress,
                    syncMax = syncMax,
                    errorMessage = errorMessage,
                    onDocContentChange = { docContent = it },
                    onQueryClick = { query() },
                    onSyncClick = { performSync() },
                    onReturnClick = { finish() },
                    onClearBackupsClick = { clearBackups() },
                    onChangeAccountClick = {
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit {
                                remove(SYNC_ACCOUNT_EMAIL)
                            }
                        mCurrentAccount = null
                        mDriveServiceHelper = null
                        signInWithCredentialManager()
                    }
                )
            }
        }

        // Initiate sign-in with Credential Manager
        val savedEmail = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getString(SYNC_ACCOUNT_EMAIL, null)

        if (savedEmail != null) {
            initializeDriveService(savedEmail)
        } else {
            signInWithCredentialManager()
        }
    }

    private fun showProgress(message: String) {
        errorMessage = null
        progressMessage = message
        isLoading = true
    }

    private fun hideProgress() {
        isLoading = false
    }

    private fun getTargetFolderId(helper: DriveServiceHelper): String {
        return "appDataFolder"
    }


    private suspend fun performDownload(helper: DriveServiceHelper, targetFolderId: String) {
        showProgress("Searching for backups...")
        try {
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

            val dbDir = File(applicationInfo.dataDir, "databases")
            if (!dbDir.exists()) dbDir.mkdirs()

            val dbFiles = driveFiles
                .filter { (it.name.startsWith("pay_") || it.name == "pay.db") && it.name.endsWith(".db") }
                .sortedBy { it.name }

            var downloadCount = 0

            for (dbFile in dbFiles) {
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
                        if (!internalFile.exists()) {
                            showProgress("Downloading $remoteName to app...")
                            helper.downloadBinaryFile(remoteName, internalFile, targetFolderId)
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

            docContent = "Files stored in: ${dbDir.absolutePath}"

        } catch (e: Exception) {
            handleError("Failed to download backups", e)
        } finally {
            hideProgress()
        }
    }


    fun performSync() {
        lifecycleScope.launch {
            try {
                val helper = mDriveServiceHelper ?: run {
                    hideProgress()
                    return@launch
                }

                val targetFolderId = getTargetFolderId(helper)
                performDownload(helper, targetFolderId)

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
                        val summary = mergeHelper.applySync { progress, total ->
                            syncMax = total
                            syncProgress = progress
                            progressMessage =
                                "Syncing ${localDb.name}: table ${progress + 1} of $total..."
                        }
                        summaryBuilder.append("Results for ${localDb.name}:\n$summary\n\n")
                    }

                    syncMax = 0
                    docContent = summaryBuilder.toString()
                    Log.d(TAG, "Sync Result: $docContent")

                    showProgress("Cleaning up local backups...")
                    for (localDb in localBackups) {
                        Log.d(TAG, "Deleting processed local backup: ${localDb.name}")
                        localDb.delete()
                        File(localDb.absolutePath + "-wal").delete()
                        File(localDb.absolutePath + "-shm").delete()
                    }

                    val db = PayDatabase(this@SyncActivity)
                    db.invalidationTracker.refreshVersionsAsync()
                    PayDatabase.resetInstance()
                } else {
                    docContent = "No backups found to sync."
                }

                showProgress("Creating fresh backup...")
                PayDatabase.checkpoint(this@SyncActivity)

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
                val finalDbDir = File(applicationInfo.dataDir, "databases")
                finalDbDir.listFiles { _, name ->
                    name.startsWith("pay_")
                }?.forEach { it.delete() }

                showProgress("Cleaning up old backups...")
                val finalDriveFileList = helper.queryFiles(targetFolderId)
                val finalDriveBackups = finalDriveFileList.files
                    ?.filter { it.name.startsWith("pay") && it.name.endsWith(".db") }
                    ?.sortedByDescending { it.name } ?: emptyList()

                if (finalDriveBackups.size > 3) {
                    val toDelete = finalDriveBackups.drop(3)
                    for (file in toDelete) {
                        Log.d(TAG, "Deleting old backup from Drive: ${file.name}")
                        helper.deleteFile(file.id)
                        listOf("-wal", "-shm").forEach { suffix ->
                            val extraName = "${file.name}$suffix"
                            finalDriveFileList.files?.find { it.name == extraName }?.let {
                                helper.deleteFile(it.id)
                            }
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

    fun clearBackups() {
        lifecycleScope.launch {
            val helper = mDriveServiceHelper ?: run {
                Log.e(TAG, "clearBackups: Drive service not initialized.")
                return@launch
            }
            showProgress("Deleting backups from Google Drive...")
            try {
                val targetFolderId = getTargetFolderId(helper)
                val fileList: FileList = helper.queryFiles(targetFolderId)
                val relatedFiles = fileList.files
                    ?.filter { it.name.startsWith("pay") } ?: emptyList()

                if (relatedFiles.isEmpty()) {
                    Toast.makeText(this@SyncActivity, "No backups found to delete.", Toast.LENGTH_SHORT).show()
                } else {
                    for (file in relatedFiles) {
                        Log.d(TAG, "Deleting file from Drive: ${file.name}")
                        helper.deleteFile(file.id)
                    }
                    Toast.makeText(this@SyncActivity, "All backups deleted from Google Drive.", Toast.LENGTH_SHORT).show()
                    docContent = "All backups deleted from Google Drive."
                }
            } catch (e: Exception) {
                handleError("Failed to clear backups", e)
            } finally {
                hideProgress()
            }
        }
    }

    private fun handleError(message: String, e: Exception) {
        Log.e(TAG, message, e)
        val errorDetail = when (e) {
            is GoogleJsonResponseException -> {
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
        errorMessage = fullMessage
        Toast.makeText(this@SyncActivity, fullMessage, Toast.LENGTH_LONG).show()
    }

    private val recoverAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val email = mCurrentAccount?.name
            if (email != null) {
                mDriveServiceHelper = null
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
                Log.e(TAG, "No credentials available.")
                logSHA1Fingerprint()
            }

            else -> {
                Log.e(TAG, "Credential Manager error (${e.javaClass.simpleName}): ${e.message}")
            }
        }
    }

    private fun logSHA1Fingerprint() {
        try {
            val packageInfo =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageManager.getPackageInfo(
                        packageName,
                        PackageManager.GET_SIGNING_CERTIFICATES
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                }

            val signatures =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
            val email = googleIdTokenCredential.id
            Log.d(TAG, "Signed in as $email")
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit {
                    putString(SYNC_ACCOUNT_EMAIL, email)
                }
            initializeDriveService(email)
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
                val builder = StringBuilder("Files on Google Drive:\n\n")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                val relatedFiles = fileList.files
                    ?.filter { it.name.startsWith("pay") }
                    ?.sortedByDescending { it.name } ?: emptyList()

                if (relatedFiles.isEmpty()) {
                    builder.append("No related files found.")
                } else {
                    for (file in relatedFiles) {
                        val size = formatFileSize(file.size.toLong())
                        val date = file.modifiedTime?.let {
                            dateFormat.format(Date(it.value))
                        } ?: "Unknown date"
                        builder.append("${file.name}\n")
                            .append("  Size: $size | Modified: $date\n\n")
                    }
                }
                docContent = builder.toString()
            } catch (e: Exception) {
                handleError("Unable to query.", e)
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

    companion object {
        private val DRIVE_SCOPES = listOf(DriveScopes.DRIVE_APPDATA)
        private val HTTP_TRANSPORT: HttpTransport = NetHttpTransport()
        private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    }
}