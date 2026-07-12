package ms.mattschlenkrich.paycalculator

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.paycalculator.common.compose.PayCalculatorTheme
import ms.mattschlenkrich.paycalculator.common.compose.paddingScale
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.repository.EmployerRepository
import ms.mattschlenkrich.paycalculator.data.repository.PayCalculationsRepository
import ms.mattschlenkrich.paycalculator.data.repository.PayDayRepository
import ms.mattschlenkrich.paycalculator.data.repository.PayDetailRepository
import ms.mattschlenkrich.paycalculator.data.repository.WorkExtraRepository
import ms.mattschlenkrich.paycalculator.data.repository.WorkOrderRepository
import ms.mattschlenkrich.paycalculator.data.repository.WorkTaxRepository
import ms.mattschlenkrich.paycalculator.data.repository.WorkTimeRepository
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModelFactory
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModelFactory
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayCalculationsViewModelFactory
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModelFactory
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDetailViewModelFactory
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkExtraViewModelFactory
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModelFactory
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTaxViewModelFactory
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTimeViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTimeViewModelFactory
import ms.mattschlenkrich.paycalculator.ui.auth.AuthenticationScreen
import ms.mattschlenkrich.paycalculator.ui.main.MainApp
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.ui.sync.SyncActivity

class MainActivity : ComponentActivity() {

    internal lateinit var mainViewModel: MainViewModel
    internal lateinit var employerViewModel: EmployerViewModel
    internal lateinit var workTaxViewModel: WorkTaxViewModel
    internal lateinit var workExtraViewModel: WorkExtraViewModel
    internal lateinit var payDayViewModel: PayDayViewModel
    internal lateinit var workOrderViewModel: WorkOrderViewModel
    internal lateinit var payDetailViewModel: PayDetailViewModel
    internal lateinit var payCalculationsViewModel: PayCalculationsViewModel
    internal lateinit var workTimeViewModel: WorkTimeViewModel
    internal lateinit var settingsViewModel: SettingsViewModel

    private val syncLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("MainActivity", "Sync confirmed, restarting activity to reload data.")
            Toast.makeText(this, "Data refreshed from sync.", Toast.LENGTH_SHORT).show()
            PayDatabase.resetInstance()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setupViewModels()

        setContent {
            val settings by settingsViewModel.settings.observeAsState()
            val windowInfo = LocalWindowInfo.current
            val density = LocalDensity.current

            LaunchedEffect(settings?.minColumnWidth, windowInfo.containerSize) {
                val minWidth = settings?.minColumnWidth ?: 360
                val widthDp = with(density) { windowInfo.containerSize.width.toDp() }
                val columns = maxOf(1, (widthDp.value / minWidth).toInt())
                paddingScale.floatValue = 1f / columns
            }

            PayCalculatorTheme(
                isSystemTheme = settings?.isSystemTheme ?: true,
                isDarkTheme = settings?.isDarkTheme ?: false,
                fontSize = settings?.fontSize ?: 16f
            ) {
                val isAuthenticated by mainViewModel.isAuthenticated
                val isPasswordProtected = settings?.isPasswordProtected ?: false

                if (isPasswordProtected && !isAuthenticated) {
                    AuthenticationScreen(
                        onPasswordVerify = { password ->
                            settingsViewModel.verifyPassword(password)
                        },
                        onPasswordSet = { password ->
                            settingsViewModel.savePassword(password)
                        },
                        onAuthenticated = {
                            mainViewModel.setAuthenticated(true)
                        }
                    )
                } else {
                    MainApp(
                        mainViewModel = mainViewModel,
                        employerViewModel = employerViewModel,
                        workTaxViewModel = workTaxViewModel,
                        workExtraViewModel = workExtraViewModel,
                        payDayViewModel = payDayViewModel,
                        workOrderViewModel = workOrderViewModel,
                        payDetailViewModel = payDetailViewModel,
                        payCalculationsViewModel = payCalculationsViewModel,
                        workTimeViewModel = workTimeViewModel,
                        settingsViewModel = settingsViewModel,
                        onSyncRequested = {
                            val intent = Intent(this, SyncActivity::class.java)
                            syncLauncher.launch(intent)
                        }
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            mainViewModel.setAuthenticated(false)
        }
    }

    private fun setupViewModels() {
        val db = PayDatabase(this)
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(application))[MainViewModel::class.java]
        employerViewModel = ViewModelProvider(
            this,
            EmployerViewModelFactory(application, EmployerRepository(db))
        )[EmployerViewModel::class.java]
        workTaxViewModel = ViewModelProvider(
            this,
            WorkTaxViewModelFactory(application, WorkTaxRepository(db))
        )[WorkTaxViewModel::class.java]
        workExtraViewModel = ViewModelProvider(
            this,
            WorkExtraViewModelFactory(application, WorkExtraRepository(db))
        )[WorkExtraViewModel::class.java]
        payDayViewModel = ViewModelProvider(
            this,
            PayDayViewModelFactory(application, PayDayRepository(db))
        )[PayDayViewModel::class.java]
        workOrderViewModel = ViewModelProvider(
            this,
            WorkOrderViewModelFactory(application, WorkOrderRepository(db))
        )[WorkOrderViewModel::class.java]
        payDetailViewModel = ViewModelProvider(
            this,
            PayDetailViewModelFactory(application, PayDetailRepository(db))
        )[PayDetailViewModel::class.java]
        payCalculationsViewModel = ViewModelProvider(
            this,
            PayCalculationsViewModelFactory(application, PayCalculationsRepository(db))
        )[PayCalculationsViewModel::class.java]
        workTimeViewModel = ViewModelProvider(
            this,
            WorkTimeViewModelFactory(application, WorkTimeRepository(db))
        )[WorkTimeViewModel::class.java]
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
    }
}