package ms.mattschlenkrich.paycalculator.ui.sync

import android.accounts.Account
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
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
import kotlin.time.Duration.Companion.milliseconds

private const val TAG: String = "SyncActivity"

class SyncActivity : ComponentActivity() {

    private var mCurrentAccount: Account? = null

    private lateinit var credentialManager: CredentialManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var syncViewModel: SyncViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        credentialManager = CredentialManager.create(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
                    onQueryClick = { syncViewModel.query { handleError("Query failed", it) } },
                    onSyncClick = {
                        syncViewModel.performSync {
                            handleError("Sync failed", it)
                        }
                    },
                    onReturnClick = {
                        handleExit()
                    },
                    onClearBackupsClick = {
                        syncViewModel.clearBackups {
                            handleError("Clear backups failed", it)
                        }
                    },
                    onChangeAccountClick = {
                        signOut {
                            signInWithCredentialManager()
                        }
                    },
                    onLegacyConnectClick = {
                        signOut {
                            signInWithLegacyFlow()
                        }
                    }
                )
            }
        }

        val savedEmail = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getString(SYNC_ACCOUNT_EMAIL, null)

        if (savedEmail != null) {
            initializeDriveService(savedEmail)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleExit()
            }
        })
    }

    private fun signOut(onComplete: () -> Unit) {
        googleSignInClient.signOut().addOnCompleteListener {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit {
                    remove(SYNC_ACCOUNT_EMAIL)
                }
            syncViewModel.driveServiceHelper = null
            mCurrentAccount = null
            onComplete()
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
        Log.i(TAG, "Starting Credential Manager sign-in")
        lifecycleScope.launch {
            try {
                val serverClientId = getString(R.string.default_web_client_id)
                syncViewModel.docContent = "Attempting Credential Manager sign-in...\n"
                syncViewModel.isLoading = true

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                Log.i(TAG, "Calling credentialManager.getCredential")
                val result = kotlinx.coroutines.withTimeout(30000.milliseconds) {
                    credentialManager.getCredential(this@SyncActivity, request)
                }
                syncViewModel.isLoading = false
                Log.i(TAG, "Credential received: ${result.credential.type}")
                handleSignInResult(result.credential)
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                syncViewModel.isLoading = false
                Log.e(TAG, "Sign-in timed out. ${e.toString()}")
                val msg = "Sign-in timed out. Try Legacy method if this continues."
                syncViewModel.errorMessage = msg
                Toast.makeText(this@SyncActivity, msg, Toast.LENGTH_LONG).show()
            } catch (e: GetCredentialException) {
                syncViewModel.isLoading = false
                Log.e(TAG, "Credential Manager error", e)
                handleCredentialException(e)
            } catch (e: Exception) {
                syncViewModel.isLoading = false
                Log.e(TAG, "Unexpected sign-in error", e)
            }
        }
    }

    private fun handleCredentialException(e: GetCredentialException) {
        val errorMessage = when (e) {
            is GetCredentialCancellationException -> "Sign-in was canceled."
            is NoCredentialException -> {
                val fingerprint = logSHA1Fingerprint()
                syncViewModel.docContent += "\n--- CONFIG ERROR ---\nSHA-1: $fingerprint\n"
                "No credentials available. Verify SHA-1 in Console."
            }

            else -> "Error: ${e.message}"
        }
        syncViewModel.errorMessage = errorMessage
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private val legacySignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        syncViewModel.isLoading = false
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            handleLegacySignInResult(account)
        } catch (e: ApiException) {
            Log.e(TAG, "Legacy Sign-In failed: ${e.statusCode}", e)
            syncViewModel.errorMessage = "Legacy Sign-In failed: ${e.statusCode}"
            if (e.statusCode == 10 || e.statusCode == 12500) {
                val fingerprint = logSHA1Fingerprint()
                syncViewModel.docContent += "\n--- LEGACY CONFIG ERROR (${e.statusCode}) ---\nSHA-1: $fingerprint\n"
            }
        }
    }

    private fun signInWithLegacyFlow() {
        Log.i(TAG, "Starting Legacy sign-in")
        syncViewModel.isLoading = true
        syncViewModel.docContent = "Attempting Legacy sign-in...\n"
        legacySignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun handleLegacySignInResult(account: GoogleSignInAccount?) {
        if (account != null && account.email != null) {
            val email = account.email!!
            Log.d(TAG, "Legacy Signed in as $email")
            syncViewModel.docContent += "Successfully signed in as: $email\n"
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit {
                putString(SYNC_ACCOUNT_EMAIL, email)
            }
            initializeDriveService(email)
        }
    }

    private fun logSHA1Fingerprint(): String {
        var fingerprint = "Unknown"
        try {
            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val signatures = packageInfo.signingInfo?.apkContentsSigners
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

    private fun handleSignInResult(credential: Credential) {
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleIdTokenCredential.id
                syncViewModel.docContent += "Successfully signed in as: $email\n"
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit {
                    putString(SYNC_ACCOUNT_EMAIL, email)
                }
                initializeDriveService(email)
            } catch (e: Exception) {
                handleError("Parse Error", e)
            }
        }
    }

    private fun handleExit() {
        if (syncViewModel.syncPerformed) {
            Log.d(TAG, "Sync was performed, setting RESULT_OK before exit.")
            setResult(RESULT_OK)
        }
        finish()
    }

    private fun initializeDriveService(email: String) {
        if (email.isBlank()) return
        val account = Account(email, "com.google")
        mCurrentAccount = account
        if (syncViewModel.driveServiceHelper != null && mCurrentAccount == account) return
        
        try {
            val credential = GoogleAccountCredential.usingOAuth2(applicationContext, DRIVE_SCOPES)
            credential.selectedAccount = account
            val googleDriveService = Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(getString(R.string.app_name))
                .build()
            syncViewModel.driveServiceHelper = DriveServiceHelper(googleDriveService)
            mCurrentAccount = account
            Log.d(TAG, "Drive service initialized for $email")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Drive service", e)
            syncViewModel.driveServiceHelper = null
        }
    }

    companion object {
        private val DRIVE_SCOPES = listOf(DriveScopes.DRIVE_APPDATA)
        private val HTTP_TRANSPORT: HttpTransport = NetHttpTransport()
        private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    }
}