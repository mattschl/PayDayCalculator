package ms.mattschlenkrich.paycalculator.ui.sync

import android.accounts.Account
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.content.edit
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
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.PREFS_NAME
import ms.mattschlenkrich.paycalculator.common.SYNC_ACCOUNT_EMAIL
import ms.mattschlenkrich.paycalculator.common.compose.PayCalculatorTheme
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.ui.sync.composable.SyncScreen
import java.security.MessageDigest
import java.security.SecureRandom

private const val TAG: String = "SyncActivity"

class SyncActivity : ComponentActivity() {

    private var mCurrentAccount: Account? = null

    private lateinit var credentialManager: CredentialManager
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var syncViewModel: SyncViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Temporarily disable to rule out layout-driven freezes
        // enableEdgeToEdge()

        credentialManager = CredentialManager.create(this)
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        syncViewModel = ViewModelProvider(this)[SyncViewModel::class.java]

        setContent {
            val settings by settingsViewModel.settings.observeAsState()

            PayCalculatorTheme(
                fontSize = settings?.fontSize ?: 16f
            ) {
                SyncScreen(
                    docContent = syncViewModel.docContent,
                    isLoading = syncViewModel.isLoading,
                    isConnected = syncViewModel.driveServiceHelper != null,
                    progressMessage = syncViewModel.progressMessage,
                    syncProgress = syncViewModel.syncProgress,
                    syncMax = syncViewModel.syncMax,
                    errorMessage = syncViewModel.errorMessage,
                    onDocContentChange = { /* read only */ },
                    onQueryClick = { syncViewModel.query { handleError("Query failed", it) } },
                    onSyncClick = { syncViewModel.performSync { handleError("Sync failed", it) } },
                    onReturnClick = { finish() },
                    onClearBackupsClick = {
                        syncViewModel.clearBackups {
                            handleError(
                                "Clear backups failed",
                                it
                            )
                        }
                    },
                    onChangeAccountClick = {
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit {
                                remove(SYNC_ACCOUNT_EMAIL)
                            }
                        syncViewModel.driveServiceHelper = null
                        signInWithCredentialManager()
                    }
                )
            }
        }

        // Initiate sign-in with Credential Manager if no saved email
        val savedEmail = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getString(SYNC_ACCOUNT_EMAIL, null)

        if (savedEmail != null) {
            initializeDriveService(savedEmail)
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
        syncViewModel.errorMessage = fullMessage
        Toast.makeText(this@SyncActivity, fullMessage, Toast.LENGTH_LONG).show()
    }

    private val recoverAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val email = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(SYNC_ACCOUNT_EMAIL, null)
            if (email != null) {
                syncViewModel.driveServiceHelper = null
                initializeDriveService(email)
            }
        }
    }

    private fun signInWithCredentialManager() {
        Log.i(TAG, "signInWithCredentialManager called")
        lifecycleScope.launch {
            try {
                val serverClientId = getString(R.string.default_web_client_id)
                // val nonce = generateNonce() // Removed to rule out system hang

                Log.i(TAG, "Starting sign-in with serverClientId: $serverClientId")
                syncViewModel.docContent = "Attempting sign-in...\n"
                syncViewModel.isLoading = true

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    // .setNonce(nonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                Log.i(TAG, "Calling credentialManager.getCredential")
                val result = kotlinx.coroutines.withTimeout(10000) {
                    credentialManager.getCredential(this@SyncActivity, request)
                }
                syncViewModel.isLoading = false
                Log.i(TAG, "Credential received: ${result.credential.type}")
                handleSignInResult(result.credential)
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                syncViewModel.isLoading = false
                Log.e(TAG, "Sign-in timed out. System dialog did not appear.")
                val msg =
                    "Sign-in timed out. Please check your internet and Google account settings."
                syncViewModel.errorMessage = msg
                Toast.makeText(this@SyncActivity, msg, Toast.LENGTH_LONG).show()
            } catch (e: GetCredentialException) {
                syncViewModel.isLoading = false
                Log.e(TAG, "Credential Manager error", e)
                handleCredentialException(e)
            } catch (e: Exception) {
                syncViewModel.isLoading = false
                Log.e(TAG, "Unexpected sign-in error", e)
                val msg = "Sign-in error: ${e.message}"
                syncViewModel.errorMessage = msg
                Toast.makeText(this@SyncActivity, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleCredentialException(e: GetCredentialException) {
        val errorMessage = when (e) {
            is GetCredentialCancellationException -> {
                Log.w(TAG, "Sign-in was canceled by the user.")
                "Sign-in was canceled."
            }

            is NoCredentialException -> {
                Log.e(TAG, "No credentials available.")
                val fingerprint = logSHA1Fingerprint()
                syncViewModel.docContent += "\n--- CONFIGURATION ERROR ---\n" +
                        "The system reports no accounts found for this request.\n" +
                        "This usually means the App Signature (SHA-1) or Client ID does not match the Google Cloud Console settings.\n\n" +
                        "Your Device SHA-1:\n$fingerprint\n\n" +
                        "Please ensure this fingerprint is registered for Client ID: ${getString(R.string.default_web_client_id)} in the Google Cloud Console."
                "No credentials available. See log for details."
            }

            else -> {
                val msg = "Credential Manager error (${e.javaClass.simpleName}): ${e.message}"
                Log.e(TAG, msg)
                msg
            }
        }
        syncViewModel.errorMessage = errorMessage
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun logSHA1Fingerprint(): String {
        var fingerprint = "Unknown"
        try {
            val packageInfo =
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )

            val signatures =
                packageInfo.signingInfo?.apkContentsSigners

            signatures?.forEach { signature ->
                val md = MessageDigest.getInstance("SHA-1")
                val digest = md.digest(signature.toByteArray())
                fingerprint = digest.joinToString(":") { "%02X".format(it) }
                Log.i(TAG, "Your SHA-1 Fingerprint: $fingerprint")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not get SHA-1 fingerprint", e)
        }
        return fingerprint
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
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleIdTokenCredential.id
                Log.d(TAG, "Signed in as $email")
                syncViewModel.docContent += "Successfully signed in as: $email\n"
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit {
                        putString(SYNC_ACCOUNT_EMAIL, email)
                    }
                initializeDriveService(email)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse Google ID Token", e)
                val msg = "Sign-in failed: ${e.message}"
                syncViewModel.errorMessage = msg
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        } else {
            val msg = "Unexpected credential type: ${credential.type}"
            Log.e(TAG, msg)
            syncViewModel.errorMessage = msg
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeDriveService(email: String) {
        if (email.isBlank()) {
            Log.e(TAG, "Email is blank, cannot initialize Drive service.")
            return
        }

        val account = Account(email, "com.google")
        mCurrentAccount = account

        if (syncViewModel.driveServiceHelper != null && mCurrentAccount == account) {
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

            syncViewModel.driveServiceHelper = DriveServiceHelper(googleDriveService)
            mCurrentAccount = account
            Log.d(TAG, "Drive service successfully initialized.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Drive service", e)
            syncViewModel.driveServiceHelper = null
            mCurrentAccount = null
        }
    }

    companion object {
        private val DRIVE_SCOPES = listOf(DriveScopes.DRIVE_APPDATA)
        private val HTTP_TRANSPORT: HttpTransport = NetHttpTransport()
        private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    }
}