package ms.mattschlenkrich.paycalculator

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ms.mattschlenkrich.paycalculator.common.compose.PayCalculatorTheme
import ms.mattschlenkrich.paycalculator.common.compose.StandardTopAppBar
import ms.mattschlenkrich.paycalculator.data.EmployerRepository
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.EmployerViewModelFactory
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.MainViewModelFactory
import ms.mattschlenkrich.paycalculator.data.PayCalculationsRepository
import ms.mattschlenkrich.paycalculator.data.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.PayCalculationsViewModelFactory
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.PayDayRepository
import ms.mattschlenkrich.paycalculator.data.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.PayDayViewModelFactory
import ms.mattschlenkrich.paycalculator.data.PayDetailRepository
import ms.mattschlenkrich.paycalculator.data.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.PayDetailViewModelFactory
import ms.mattschlenkrich.paycalculator.data.WorkExtraRepository
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModelFactory
import ms.mattschlenkrich.paycalculator.data.WorkOrderRepository
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModelFactory
import ms.mattschlenkrich.paycalculator.data.WorkTaxRepository
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModelFactory
import ms.mattschlenkrich.paycalculator.data.WorkTimeRepository
import ms.mattschlenkrich.paycalculator.data.WorkTimeViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTimeViewModelFactory
import ms.mattschlenkrich.paycalculator.employer.EmployerAddRoute
import ms.mattschlenkrich.paycalculator.employer.EmployerListScreen
import ms.mattschlenkrich.paycalculator.employer.EmployerUpdateRoute
import ms.mattschlenkrich.paycalculator.extras.EmployerExtraDefinitionUpdateRoute
import ms.mattschlenkrich.paycalculator.extras.EmployerExtraDefinitionsAddRoute
import ms.mattschlenkrich.paycalculator.extras.ExtraRoute
import ms.mattschlenkrich.paycalculator.extras.PayPeriodExtraAddRoute
import ms.mattschlenkrich.paycalculator.extras.PayPeriodExtraUpdateRoute
import ms.mattschlenkrich.paycalculator.extras.WorkDateExtraAddRoute
import ms.mattschlenkrich.paycalculator.extras.WorkDateExtraUpdateRoute
import ms.mattschlenkrich.paycalculator.extras.WorkExtraTypeAddRoute
import ms.mattschlenkrich.paycalculator.extras.WorkExtraTypeUpdateRoute
import ms.mattschlenkrich.paycalculator.paydetail.PayDetailRoute
import ms.mattschlenkrich.paycalculator.payrate.EmployerPayRateAddRoute
import ms.mattschlenkrich.paycalculator.payrate.EmployerPayRateUpdateRoute
import ms.mattschlenkrich.paycalculator.payrate.EmployerPayRatesRoute
import ms.mattschlenkrich.paycalculator.settings.SettingsRoute
import ms.mattschlenkrich.paycalculator.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.sync.SyncActivity
import ms.mattschlenkrich.paycalculator.tax.TaxRoute
import ms.mattschlenkrich.paycalculator.tax.TaxRuleAddRoute
import ms.mattschlenkrich.paycalculator.tax.TaxRuleUpdateRoute
import ms.mattschlenkrich.paycalculator.tax.TaxTypeAddRoute
import ms.mattschlenkrich.paycalculator.tax.TaxTypeUpdateRoute
import ms.mattschlenkrich.paycalculator.timesheet.TimeSheetRoute
import ms.mattschlenkrich.paycalculator.workdate.WorkDateAddRoute
import ms.mattschlenkrich.paycalculator.workdate.WorkDateTimesRoute
import ms.mattschlenkrich.paycalculator.workdate.WorkDateUpdateRoute
import ms.mattschlenkrich.paycalculator.workorder.AreaUpdateRoute
import ms.mattschlenkrich.paycalculator.workorder.AreaViewRoute
import ms.mattschlenkrich.paycalculator.workorder.JobSpecMergeRoute
import ms.mattschlenkrich.paycalculator.workorder.JobSpecUpdateRoute
import ms.mattschlenkrich.paycalculator.workorder.JobSpecViewRoute
import ms.mattschlenkrich.paycalculator.workorder.MaterialMergeRoute
import ms.mattschlenkrich.paycalculator.workorder.MaterialUpdateRoute
import ms.mattschlenkrich.paycalculator.workorder.MaterialViewRoute
import ms.mattschlenkrich.paycalculator.workorder.WorkOrderAddRoute
import ms.mattschlenkrich.paycalculator.workorder.WorkOrderHistoryAddRoute
import ms.mattschlenkrich.paycalculator.workorder.WorkOrderHistoryMaterialUpdateRoute
import ms.mattschlenkrich.paycalculator.workorder.WorkOrderHistoryTimeRoute
import ms.mattschlenkrich.paycalculator.workorder.WorkOrderHistoryTimeUpdateRoute
import ms.mattschlenkrich.paycalculator.workorder.WorkOrderHistoryUpdateRoute
import ms.mattschlenkrich.paycalculator.workorder.WorkOrderHistoryWorkPerformedUpdateRoute
import ms.mattschlenkrich.paycalculator.workorder.WorkOrderLookupRoute
import ms.mattschlenkrich.paycalculator.workorder.WorkOrderUpdateRoute
import ms.mattschlenkrich.paycalculator.workorder.WorkOrderViewRoute
import ms.mattschlenkrich.paycalculator.workorder.WorkPerformedMergeRoute
import ms.mattschlenkrich.paycalculator.workorder.WorkPerformedUpdateRoute
import ms.mattschlenkrich.paycalculator.workorder.WorkPerformedViewRoute

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
            val settingsViewModel: SettingsViewModel = viewModel()
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
    onSyncRequested: () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
        topBar = {
            StandardTopAppBar(
                title = stringResource(currentScreen?.resourceId ?: R.string.app_name),
                onSettingsClicked = { navController.navigate(Screen.Settings.route) },
                onMenuAction = { action ->
                    when (action) {
                        "Sync Data" -> onSyncRequested()
                        context.getString(R.string.view_work_order_list) -> navController.navigate(
                            Screen.WorkOrders.route
                        )

                        context.getString(R.string.view_job_spec_list) -> navController.navigate(
                            Screen.JobSpecs.route
                        )

                        context.getString(R.string.view_areas_list) -> navController.navigate(Screen.Areas.route)
                        context.getString(R.string.view_work_performed_list) -> navController.navigate(
                            Screen.WorkPerformed.route
                        )

                        context.getString(R.string.view_material_list) -> navController.navigate(
                            Screen.Materials.route
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = NavigationBarDefaults.windowInsets
            ) {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painterResource(id = screen.icon),
                                contentDescription = stringResource(screen.resourceId),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
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
                    navController
                )
            }
            composable(Screen.PayDetails.route) {
                PayDetailRoute(
                    mainViewModel,
                    employerViewModel,
                    payDayViewModel,
                    payCalculationsViewModel,
                    payDetailViewModel,
                    navController
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
                    navController
                )
            }
        }
    }
}