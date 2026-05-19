package ms.mattschlenkrich.paycalculator.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.ui.employer.composable.EmployerListScreen
import ms.mattschlenkrich.paycalculator.ui.extras.ExtraRoute
import ms.mattschlenkrich.paycalculator.ui.paydetail.PayDetailRoute
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.ui.tax.TaxRoute
import ms.mattschlenkrich.paycalculator.ui.timesheet.TimeSheetRoute

@Composable
fun TopLevelPager(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workTaxViewModel: WorkTaxViewModel,
    workExtraViewModel: WorkExtraViewModel,
    payDayViewModel: PayDayViewModel,
    payDetailViewModel: PayDetailViewModel,
    payCalculationsViewModel: PayCalculationsViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavController
) {
    val pagerState = rememberPagerState(
        initialPage = mainViewModel.selectedTopLevelIndex.intValue
    ) { 5 }

    // Sync ViewModel index to Pager
    LaunchedEffect(mainViewModel.selectedTopLevelIndex.intValue) {
        if (pagerState.currentPage != mainViewModel.selectedTopLevelIndex.intValue) {
            pagerState.animateScrollToPage(mainViewModel.selectedTopLevelIndex.intValue)
        }
    }

    // Sync Pager index to ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            mainViewModel.setSelectedTopLevelIndex(page)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = true,
        beyondViewportPageCount = 1
    ) { page ->
        when (page) {
            0 -> TimeSheetRoute(
                mainViewModel,
                employerViewModel,
                payDayViewModel,
                payCalculationsViewModel,
                payDetailViewModel,
                settingsViewModel,
                navController = navController
            )

            1 -> PayDetailRoute(
                mainViewModel,
                employerViewModel,
                payDayViewModel,
                payCalculationsViewModel,
                payDetailViewModel,
                settingsViewModel,
                navController = navController
            )

            2 -> EmployerListScreen(
                employerViewModel = employerViewModel,
                onEmployerClick = { employer ->
                    mainViewModel.setEmployer(employer)
                    navController.navigate(Screen.EmployerUpdate.route)
                },
                onAddClick = {
                    navController.navigate(Screen.EmployerAdd.route)
                }
            )

            3 -> TaxRoute(
                mainViewModel,
                workTaxViewModel,
                navController
            )

            4 -> ExtraRoute(
                mainViewModel,
                employerViewModel,
                workExtraViewModel,
                navController
            )
        }
    }
}