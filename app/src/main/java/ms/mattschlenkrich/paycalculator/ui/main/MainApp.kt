package ms.mattschlenkrich.paycalculator.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.bottomNavItems
import ms.mattschlenkrich.paycalculator.common.compose.StandardNavigationBar
import ms.mattschlenkrich.paycalculator.common.compose.StandardTopAppBar
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTimeViewModel
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel

@Composable
fun MainApp(
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
    onSyncRequested: () -> Unit,
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
        Screen.MainPager,
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

    val displayScreen = if (currentDestination?.route == Screen.MainPager.route) {
        bottomNavItems[mainViewModel.selectedTopLevelIndex.intValue]
    } else {
        currentScreen
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            StandardTopAppBar(
                title = stringResource(displayScreen?.resourceId ?: R.string.app_name),
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
                mainViewModel = mainViewModel,
                navController = navController,
                currentDestination = currentDestination
            )
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            innerPadding = innerPadding,
            mainViewModel = mainViewModel,
            employerViewModel = employerViewModel,
            workTaxViewModel = workTaxViewModel,
            workExtraViewModel = workExtraViewModel,
            payDayViewModel = payDayViewModel,
            workOrderViewModel = workOrderViewModel,
            payDetailViewModel = payDetailViewModel,
            payCalculationsViewModel = payCalculationsViewModel,
            workTimeViewModel = workTimeViewModel,
            settingsViewModel = settingsViewModel
        )
    }
}