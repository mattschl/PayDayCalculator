package ms.mattschlenkrich.paycalculator.ui.sync

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModelProvider
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
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DEVICE_ID
import ms.mattschlenkrich.paycalculator.common.PREFS_NAME
import ms.mattschlenkrich.paycalculator.common.compose.PayCalculatorTheme
import ms.mattschlenkrich.paycalculator.common.settings.SettingsManager
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModelFactory
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.ui.sync.composable.SyncScreen

private const val TAG: String = "SyncActivity"

class SyncActivity : ComponentActivity() {

    private lateinit var credentialManager: CredentialManager
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var syncViewModel: SyncViewModel
    private lateinit var mainViewModel: MainViewModel
    private var mCurrentAccount: Account? = null
    private var pendingAction: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        credentialManager = CredentialManager.create(this)
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        syncViewModel = ViewModelProvider(this)[SyncViewModel::class.java]
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(application)
        )[MainViewModel::class.java]

        val settingsManager = SettingsManager(this)
        val settings = settingsManager.loadSettings()

        val deviceId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getLong(DEVICE_ID, 0L)
        syncViewModel.deviceId = deviceId

        settings.driveAccount?.let {
            initializeDriveService(it)
        }

        setContent {
            val appSettings by settingsViewModel.settings.observeAsState()

            PayCalculatorTheme(
                fontSize = appSettings?.fontSize ?: 16f,
                minColumnWidth = appSettings?.minColumnWidth ?: 360,
            ) {
                SyncScreen(
                    viewModel = syncViewModel,
                    onBack = { handleExit() },
                    onConnect = { signInWithCredentialManager() },
                    onConnectLegacy = { signInWithAccountPicker() },
                    onDisconnect = { disconnectAccount() },
                    onSync = {
                        syncViewModel.performSync { e ->
                            handleError("Sync failed", e) {
                                syncViewModel.performSync { }
                            }
                        }
                    },
                    onRestore = { fileName ->
                        syncViewModel.restore(fileName) {
                            Toast.makeText(
                                this,
                                getString(R.string.msg_restore_successful),
                                Toast.LENGTH_LONG,
                            ).show()
                            restartApp()
                        }
                    },
                    onRepairLocal = {
                        syncViewModel.repairDatabase {
                            Toast.makeText(
                                this,
                                getString(R.string.msg_database_repaired),
                                Toast.LENGTH_LONG,
                            ).show()
                            restartApp()
                        }
                    },
                    onManualUpload = {
                        syncViewModel.manualUpload {
                            Toast.makeText(
                                this,
                                getString(R.string.msg_upload_successful),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onDeleteBackup = { meta ->
                        syncViewModel.deleteBackup(meta) { e ->
                            handleError("Delete failed", e) { }
                        }
                    },
                ) {
                    syncViewModel.clearBackups { e ->
                        handleError("Clear backups failed", e) { }
                    }
                }
            }
        }
    }

    private fun restartApp() {
        PayDatabase.resetInstance()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun disconnectAccount() {
        val settingsManager = SettingsManager(this)
        val settings = settingsManager.loadSettings()
        settingsManager.saveSettings(settings.copy(driveAccount = null))
        syncViewModel.disconnect()
        mCurrentAccount = null
        Toast.makeText(this, getString(R.string.msg_disconnected_from_google), Toast.LENGTH_SHORT)
            .show()
    }

    private fun handleError(message: String, e: Exception, action: (() -> Unit)) {
        Log.e(TAG, message, e)
        if (e is UserRecoverableAuthIOException) {
            pendingAction = action
            recoverAuthLauncher.launch(e.intent)
        } else {
            val errMsg = "$message: ${e.message}"
            Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
        }
    }

    private val recoverAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val settings = SettingsManager(this).loadSettings()
            settings.driveAccount?.let {
                syncViewModel.driveServiceHelper = null
                initializeDriveService(it)
                pendingAction?.invoke()
            }
        }
    }

    private val legacySignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            accountName?.let { initializeDriveService(it) }
        }
    }

    private fun signInWithCredentialManager() {
        lifecycleScope.launch {
            val serverClientId = getString(R.string.default_web_client_id)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts = false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(autoSelectEnabled = false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(this@SyncActivity, request)
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(result.credential.data)
                initializeDriveService(googleIdTokenCredential.id)
            } catch (e: Exception) {
                Log.e(TAG, "Sign-in error", e)
            } catch (noCred: NoCredentialException) {
                Log.e(TAG, "No credentials ", noCred)
            }
        }
    }

    private fun signInWithAccountPicker() {
        val intent = AccountManager.newChooseAccountIntent(
            null, null, arrayOf("com.google"), null, null, null, null
        )
        legacySignInLauncher.launch(intent)
    }

    private fun initializeDriveService(email: String) {
        val account = Account(email, "com.google")
        try {
            val credential = GoogleAccountCredential.usingOAuth2(this, DRIVE_SCOPES)
            credential.selectedAccount = account
            val googleDriveService = Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(getString(R.string.app_name))
                .build()
            val helper = DriveServiceHelper(googleDriveService)
            syncViewModel.driveServiceHelper = helper
            mainViewModel.driveServiceHelper.value = helper
            mCurrentAccount = account

            val settingsManager = SettingsManager(this)
            val settings = settingsManager.loadSettings()
            if (settings.driveAccount != email) {
                settingsManager.saveSettings(settings.copy(driveAccount = email))
            }
            Log.d(TAG, "Drive service initialized for $email")
        } catch (e: Exception) {
            Log.e(TAG, "Drive init failed", e)
        }
    }

    private fun handleExit() {
        if (syncViewModel.syncPerformed) {
            setResult(RESULT_OK)
        }
        finish()
    }

    companion object {
        private val DRIVE_SCOPES = listOf(DriveScopes.DRIVE_APPDATA)
        private val HTTP_TRANSPORT: HttpTransport = NetHttpTransport()
        private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    }
}