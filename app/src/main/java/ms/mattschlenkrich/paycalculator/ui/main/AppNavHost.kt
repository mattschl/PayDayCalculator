package ms.mattschlenkrich.paycalculator.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.data.viewmodel.AreaViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.JobSpecViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MaterialViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkPerformedViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTimeViewModel
import ms.mattschlenkrich.paycalculator.ui.areas.AreaUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.areas.AreaViewRoute
import ms.mattschlenkrich.paycalculator.ui.employer.EmployerAddRoute
import ms.mattschlenkrich.paycalculator.ui.employer.EmployerUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.extras.EmployerExtraDefinitionUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.extras.EmployerExtraDefinitionsAddRoute
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
import ms.mattschlenkrich.paycalculator.ui.payrate.EmployerPayRateAddRoute
import ms.mattschlenkrich.paycalculator.ui.payrate.EmployerPayRateUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.payrate.composable.EmployerPayRatesRoute
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsRoute
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.ui.tax.TaxRuleAddRoute
import ms.mattschlenkrich.paycalculator.ui.tax.TaxRuleUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.tax.TaxTypeAddRoute
import ms.mattschlenkrich.paycalculator.ui.tax.TaxTypeUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.workdate.WorkDateAddRoute
import ms.mattschlenkrich.paycalculator.ui.workdate.WorkDateExtraAddRoute
import ms.mattschlenkrich.paycalculator.ui.workdate.WorkDateExtraUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.workdate.WorkDateTimesRoute
import ms.mattschlenkrich.paycalculator.ui.workdate.WorkDateUpdateRoute
import ms.mattschlenkrich.paycalculator.ui.workorder.WorkOrderAddRoute
import ms.mattschlenkrich.paycalculator.ui.workorder.WorkOrderJobSpecUpdateRoute
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

@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workTaxViewModel: WorkTaxViewModel,
    workExtraViewModel: WorkExtraViewModel,
    payDayViewModel: PayDayViewModel,
    workOrderViewModel: WorkOrderViewModel,
    jobSpecViewModel: JobSpecViewModel,
    materialViewModel: MaterialViewModel,
    workPerformedViewModel: WorkPerformedViewModel,
    areaViewModel: AreaViewModel,
    payDetailViewModel: PayDetailViewModel,
    payCalculationsViewModel: PayCalculationsViewModel,
    workTimeViewModel: WorkTimeViewModel,
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        navController,
        startDestination = Screen.MainPager.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.MainPager.route) {
            TopLevelPager(
                mainViewModel = mainViewModel,
                employerViewModel = employerViewModel,
                workTaxViewModel = workTaxViewModel,
                workExtraViewModel = workExtraViewModel,
                payDayViewModel = payDayViewModel,
                payDetailViewModel = payDetailViewModel,
                payCalculationsViewModel = payCalculationsViewModel,
                settingsViewModel = settingsViewModel,
                navController = navController
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
                navController
            )
        }
        composable(Screen.WorkDateExtraUpdate.route) {
            WorkDateExtraUpdateRoute(
                mainViewModel,
                payDayViewModel,
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
                workPerformedViewModel,
                materialViewModel,
                areaViewModel,
                navController
            )
        }
        composable(Screen.WorkOrderHistoryWorkPerformedUpdate.route) {
            WorkOrderHistoryWorkPerformedUpdateRoute(
                mainViewModel,
                workOrderViewModel,
                workPerformedViewModel,
                areaViewModel,
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
                jobSpecViewModel,
                areaViewModel,
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
                materialViewModel,
                navController
            )
        }
        composable(Screen.JobSpecs.route) {
            JobSpecViewRoute(
                mainViewModel,
                jobSpecViewModel,
                navController
            )
        }
        composable(Screen.JobSpecUpdate.route) {
            JobSpecUpdateRoute(
                mainViewModel,
                jobSpecViewModel,
                navController
            )
        }
        composable(Screen.JobSpecMerge.route) {
            JobSpecMergeRoute(
                mainViewModel,
                jobSpecViewModel,
                navController
            )
        }
        composable(Screen.Areas.route) {
            AreaViewRoute(
                mainViewModel,
                areaViewModel,
                navController
            )
        }
        composable(Screen.AreaUpdate.route) {
            AreaUpdateRoute(
                mainViewModel,
                areaViewModel,
                navController
            )
        }
        composable(Screen.WorkPerformed.route) {
            WorkPerformedViewRoute(
                mainViewModel,
                workPerformedViewModel,
                navController
            )
        }
        composable(Screen.Materials.route) {
            MaterialViewRoute(
                mainViewModel,
                materialViewModel,
                navController
            )
        }
        composable(Screen.MaterialUpdate.route) {
            MaterialUpdateRoute(
                mainViewModel,
                materialViewModel,
                navController
            )
        }
        composable(Screen.MaterialMerge.route) {
            MaterialMergeRoute(
                mainViewModel,
                materialViewModel,
                navController
            )
        }
        composable(Screen.WorkPerformedUpdate.route) {
            WorkPerformedUpdateRoute(
                mainViewModel,
                workPerformedViewModel,
                navController
            )
        }
        composable(Screen.WorkPerformedMerge.route) {
            WorkPerformedMergeRoute(
                mainViewModel,
                workPerformedViewModel,
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
        composable(
            route = Screen.PayPeriodExtraAdd.route,
            arguments = listOf(
                androidx.navigation.navArgument("isCredit") {
                    type = androidx.navigation.NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val isCredit = backStackEntry.arguments?.getBoolean("isCredit") ?: false
            PayPeriodExtraAddRoute(
                mainViewModel,
                payDayViewModel,
                workExtraViewModel,
                navController,
                isCredit
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
        composable(Screen.WorkOrderJobSpecUpdate.route) {
            WorkOrderJobSpecUpdateRoute(
                mainViewModel,
                jobSpecViewModel,
                areaViewModel,
                navController
            )
        }
        composable(Screen.Settings.route) {
            SettingsRoute(
                settingsViewModel,
                employerViewModel
            )
        }
    }
}