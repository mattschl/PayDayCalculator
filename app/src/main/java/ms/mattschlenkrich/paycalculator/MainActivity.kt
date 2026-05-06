package ms.mattschlenkrich.paycalculator

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ms.mattschlenkrich.paycalculator.common.compose.PayCalculatorTheme
import ms.mattschlenkrich.paycalculator.common.compose.StandardNavigationBar
import ms.mattschlenkrich.paycalculator.common.compose.StandardTopAppBar
import ms.mattschlenkrich.paycalculator.data.MainViewModelFactory
import ms.mattschlenkrich.paycalculator.data.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.PayCalculationsViewModelFactory
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.PayDetailViewModelFactory
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModelFactory
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModelFactory
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModelFactory
import ms.mattschlenkrich.paycalculator.data.WorkTimeViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTimeViewModelFactory
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
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModelFactory
import ms.mattschlenkrich.paycalculator.ui.areas.AreaUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.areas.AreaViewRoute
import ms.mattschlenkrich.paycalculator.ui.employer.EmployerAddRoute
import ms.mattschlenkrich.paycalculator.ui.employer.EmployerListScreen
import ms.mattschlenkrich.paycalculator.ui.employer.EmployerUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.extras.EmployerExtraDefinitionUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.extras.EmployerExtraDefinitionsAddRoute
import ms.mattschlenkrich.paycalculator.ui.extras.ExtraRoute
import ms.mattschlenkrich.paycalculator.ui.extras.PayPeriodExtraAddRoute
import ms.mattschlenkrich.paycalculator.ui.extras.PayPeriodExtraUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.extras.WorkExtraTypeAddRoute
import ms.mattschlenkrich.paycalculator.ui.extras.WorkExtraTypeUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.jobspec.JobSpecMergeRoute
import ms.mattschlenkrich.paycalculator.ui.jobspec.JobSpecUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.jobspec.JobSpecViewRoute
import ms.mattschlenkrich.paycalculator.ui.material.MaterialMergeRoute
import ms.mattschlenkrich.paycalculator.ui.material.MaterialUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.material.MaterialViewRoute
import ms.mattschlenkrich.paycalculator.ui.paydetail.PayDetailRoute
import ms.mattschlenkrich.paycalculator.ui.payrate.EmployerPayRateAddRoute
import ms.mattschlenkrich.paycalculator.ui.payrate.EmployerPayRateUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.payrate.EmployerPayRatesRoute
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsRoute
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.ui.sync.SyncActivity
import ms.mattschlenkrich.paycalculator.ui.tax.TaxRoute
import ms.mattschlenkrich.paycalculator.ui.tax.TaxRuleAddRoute
import ms.mattschlenkrich.paycalculator.ui.tax.TaxRuleUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.tax.TaxTypeAddRoute
import ms.mattschlenkrich.paycalculator.ui.tax.TaxTypeUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.timesheet.TimeSheetRoute
import ms.mattschlenkrich.paycalculator.ui.workdate.WorkDateAddRoute
import ms.mattschlenkrich.paycalculator.ui.workdate.WorkDateExtraAddRoute
import ms.mattschlenkrich.paycalculator.ui.workdate.WorkDateExtraUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.workdate.WorkDateTimesRoute
import ms.mattschlenkrich.paycalculator.ui.workdate.WorkDateUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.workorder.WorkOrderAddRoute
import ms.mattschlenkrich.paycalculator.ui.workorder.WorkOrderLookupRoute
import ms.mattschlenkrich.paycalculator.ui.workorder.WorkOrderUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.workorder.WorkOrderViewRoute
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.WorkOrderHistoryAddRoute
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.WorkOrderHistoryMaterialUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.WorkOrderHistoryTimeRoute
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.WorkOrderHistoryTimeUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.WorkOrderHistoryUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.WorkOrderHistoryWorkPerformedUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.workperformed.WorkPerformedMergeRoute
import ms.mattschlenkrich.paycalculator.ui.workperformed.WorkPerformedUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.workperformed.WorkPerformedViewRoute

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

            PayCalculatorTheme(
                fontSize = settings?.fontSize ?: 16f
            ) {
                MainScreen(
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

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workTaxViewModel: WorkTaxViewModel,
    workExtraViewModel: WorkExtraViewModel,
    payDayViewModel: PayDayViewModel,
    workOrderViewModel: WorkOrderViewModel,
    payDetailViewModel: PayDetailViewModel,
    payCalculationsViewModel: PayCalculationsViewModel,
    workTimeViewModel: WorkTimeViewModel,
    settingsViewModel: SettingsViewModel,
    onSyncRequested: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val workOrderListLabel = stringResource(R.string.view_work_order_list)
    val jobSpecListLabel = stringResource(R.string.view_job_spec_list)
    val areasListLabel = stringResource(R.string.view_areas_list)
    val workPerformedListLabel = stringResource(R.string.view_work_performed_list)
    val materialListLabel = stringResource(R.string.view_material_list)

    val currentScreen = (bottomNavItems + listOf(
        Screen.EmployerAdd,
        Screen.EmployerUpdate,
        Screen.TaxTypeAdd,
        Screen.TaxTypeUpdate,
        Screen.TaxRuleAdd,
        Screen.TaxRuleUpdate,
        Screen.EmployerPayRates,
        Screen.EmployerPayRateAdd,
        Screen.EmployerPayRateUpdate,
        Screen.EmployerExtraDefinitionsAdd,
        Screen.EmployerExtraDefinitionUpdate,
        Screen.WorkExtraTypeAdd,
        Screen.WorkExtraTypeUpdate,
        Screen.PayPeriodExtraAdd,
        Screen.PayPeriodExtraUpdate,
        Screen.WorkDateAdd,
        Screen.WorkDateUpdate,
        Screen.WorkDateTimes,
        Screen.WorkDateExtraAdd,
        Screen.WorkDateExtraUpdate,
        Screen.WorkOrderHistoryAdd,
        Screen.WorkOrderHistoryUpdate,
        Screen.WorkOrders,
        Screen.JobSpecs,
        Screen.Areas,
        Screen.WorkPerformed,
        Screen.Materials,
        Screen.Settings,
        Screen.AreaUpdate,
        Screen.MaterialUpdate,
        Screen.MaterialMerge,
        Screen.JobSpecMerge,
        Screen.WorkPerformedUpdate,
        Screen.WorkPerformedMerge,
        Screen.WorkOrderAdd,
        Screen.WorkOrderLookup,
        Screen.WorkOrderHistoryWorkPerformedUpdate,
        Screen.WorkOrderHistoryMaterialUpdate,
        Screen.WorkOrderHistoryTimeUpdate,
        Screen.WorkOrderHistoryTime,
        Screen.WorkOrderJobSpecUpdate
    )).find { it.route == currentDestination?.route }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            StandardTopAppBar(
                title = stringResource(currentScreen?.resourceId ?: R.string.app_name),
                onBackClicked = if (navController.previousBackStackEntry != null) {
                    { navController.popBackStack() }
                } else null,
                onSettingsClicked = { navController.navigate(Screen.Settings.route) },
                onMenuAction = { action ->
                    when (action) {
                        "Sync Data" -> onSyncRequested()
                        workOrderListLabel -> navController.navigate(
                            Screen.WorkOrders.route
                        )

                        jobSpecListLabel -> navController.navigate(
                            Screen.JobSpecs.route
                        )

                        areasListLabel -> navController.navigate(Screen.Areas.route)
                        workPerformedListLabel -> navController.navigate(
                            Screen.WorkPerformed.route
                        )

                        materialListLabel -> navController.navigate(
                            Screen.Materials.route
                        )
                    }
                }
            )
        },
        bottomBar = {
            StandardNavigationBar(
                navController = navController,
                currentDestination = currentDestination
            )
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.TimeSheet.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.TimeSheet.route) {
                TimeSheetRoute(
                    mainViewModel,
                    employerViewModel,
                    payDayViewModel,
                    payCalculationsViewModel,
                    payDetailViewModel,
                    settingsViewModel,
                    navController = navController
                )
            }
            composable(Screen.PayDetails.route) {
                PayDetailRoute(
                    mainViewModel,
                    employerViewModel,
                    payDayViewModel,
                    payCalculationsViewModel,
                    payDetailViewModel,
                    settingsViewModel,
                    navController = navController
                )
            }
            composable(Screen.Employers.route) {
                EmployerListScreen(
                    employerViewModel = employerViewModel,
                    onEmployerClick = { employer ->
                        mainViewModel.setEmployer(employer)
                        navController.navigate(Screen.EmployerUpdate.route)
                    },
                    onAddClick = {
                        navController.navigate(Screen.EmployerAdd.route)
                    }
                )
            }
            composable(Screen.EmployerAdd.route) {
                EmployerAddRoute(
                    mainViewModel,
                    employerViewModel,
                    workTaxViewModel,
                    navController
                )
            }
            composable(Screen.EmployerUpdate.route) {
                EmployerUpdateRoute(
                    mainViewModel,
                    employerViewModel,
                    workTaxViewModel,
                    workExtraViewModel,
                    navController
                )
            }
            composable(Screen.EmployerPayRates.route) {
                EmployerPayRatesRoute(
                    mainViewModel,
                    employerViewModel,
                    navController
                )
            }
            composable(Screen.EmployerPayRateAdd.route) {
                EmployerPayRateAddRoute(
                    mainViewModel,
                    employerViewModel,
                    navController
                )
            }
            composable(Screen.EmployerPayRateUpdate.route) {
                EmployerPayRateUpdateRoute(
                    mainViewModel,
                    employerViewModel,
                    navController
                )
            }
            composable(Screen.Taxes.route) {
                TaxRoute(
                    mainViewModel,
                    workTaxViewModel,
                    navController
                )
            }
            composable(Screen.TaxTypeAdd.route) {
                TaxTypeAddRoute(
                    mainViewModel,
                    employerViewModel,
                    workTaxViewModel,
                    navController
                )
            }
            composable(Screen.TaxTypeUpdate.route) {
                TaxTypeUpdateRoute(
                    mainViewModel,
                    workTaxViewModel,
                    navController
                )
            }
            composable(Screen.TaxRuleAdd.route) {
                TaxRuleAddRoute(
                    mainViewModel,
                    workTaxViewModel,
                    navController
                )
            }
            composable(Screen.TaxRuleUpdate.route) {
                TaxRuleUpdateRoute(
                    mainViewModel,
                    workTaxViewModel,
                    navController
                )
            }
            composable(Screen.Extras.route) {
                ExtraRoute(
                    mainViewModel,
                    employerViewModel,
                    workExtraViewModel,
                    navController
                )
            }
            composable(Screen.EmployerExtraDefinitionsAdd.route) {
                EmployerExtraDefinitionsAddRoute(
                    mainViewModel,
                    workExtraViewModel,
                    navController
                )
            }
            composable(Screen.EmployerExtraDefinitionUpdate.route) {
                EmployerExtraDefinitionUpdateRoute(
                    mainViewModel,
                    workExtraViewModel,
                    navController
                )
            }
            composable(Screen.WorkExtraTypeAdd.route) {
                WorkExtraTypeAddRoute(
                    mainViewModel,
                    workExtraViewModel,
                    navController
                )
            }
            composable(Screen.WorkExtraTypeUpdate.route) {
                WorkExtraTypeUpdateRoute(
                    mainViewModel,
                    workExtraViewModel,
                    navController
                )
            }
            composable(Screen.WorkDateAdd.route) {
                WorkDateAddRoute(
                    mainViewModel,
                    payDayViewModel,
                    workExtraViewModel,
                    navController
                )
            }
            composable(Screen.WorkDateUpdate.route) {
                WorkDateUpdateRoute(
                    mainViewModel,
                    payDayViewModel,
                    workExtraViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkDateTimes.route) {
                WorkDateTimesRoute(
                    mainViewModel,
                    workTimeViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkDateExtraAdd.route) {
                WorkDateExtraAddRoute(
                    mainViewModel,
                    payDayViewModel,
                    workExtraViewModel,
                    navController
                )
            }
            composable(Screen.WorkDateExtraUpdate.route) {
                WorkDateExtraUpdateRoute(
                    mainViewModel,
                    payDayViewModel,
                    workExtraViewModel,
                    navController
                )
            }
            composable(Screen.WorkOrderHistoryAdd.route) {
                WorkOrderHistoryAddRoute(
                    mainViewModel,
                    employerViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkOrderHistoryUpdate.route) {
                WorkOrderHistoryUpdateRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkOrderHistoryWorkPerformedUpdate.route) {
                WorkOrderHistoryWorkPerformedUpdateRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkOrders.route) {
                WorkOrderViewRoute(
                    mainViewModel,
                    employerViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkOrderUpdate.route) {
                WorkOrderUpdateRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkOrderLookup.route) {
                WorkOrderLookupRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkOrderHistoryTime.route) {
                WorkOrderHistoryTimeRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkOrderHistoryTimeUpdate.route) {
                WorkOrderHistoryTimeUpdateRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkOrderHistoryMaterialUpdate.route) {
                WorkOrderHistoryMaterialUpdateRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.JobSpecs.route) {
                JobSpecViewRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.JobSpecUpdate.route) {
                JobSpecUpdateRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.JobSpecMerge.route) {
                JobSpecMergeRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.Areas.route) {
                AreaViewRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.AreaUpdate.route) {
                AreaUpdateRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkPerformed.route) {
                WorkPerformedViewRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.Materials.route) {
                MaterialViewRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.MaterialUpdate.route) {
                MaterialUpdateRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.MaterialMerge.route) {
                MaterialMergeRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkPerformedUpdate.route) {
                WorkPerformedUpdateRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkPerformedMerge.route) {
                WorkPerformedMergeRoute(
                    mainViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.WorkOrderAdd.route) {
                WorkOrderAddRoute(
                    mainViewModel,
                    employerViewModel,
                    workOrderViewModel,
                    navController
                )
            }
            composable(Screen.PayPeriodExtraAdd.route) {
                PayPeriodExtraAddRoute(
                    mainViewModel,
                    payDayViewModel,
                    workExtraViewModel,
                    navController
                )
            }
            composable(Screen.PayPeriodExtraUpdate.route) {
                PayPeriodExtraUpdateRoute(
                    mainViewModel,
                    payDayViewModel,
                    workExtraViewModel,
                    navController
                )
            }
            composable(Screen.Settings.route) {
                SettingsRoute(
                    mainViewModel,
                    navController,
                    settingsViewModel
                )
            }
        }
    }
}