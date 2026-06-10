package ms.mattschlenkrich.paycalculator.ui.paydetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectionCard
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.ui.paydetail.composable.PayDetailPage
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import java.time.LocalDate

@Composable
fun PayDetailRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    payDayViewModel: PayDayViewModel,
    payCalculationsViewModel: PayCalculationsViewModel,
    payDetailViewModel: PayDetailViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    navController: NavController
) {
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }

    val settings by settingsViewModel.settings.observeAsState()
    val payPeriodsLimit = settings?.payPeriodsLimit ?: 15

    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    val selectedEmployer = mainViewModel.selectedEmployer.value
    val cutOffDates by if (selectedEmployer != null) {
        payDayViewModel.getCutOffDates(selectedEmployer.employerId, payPeriodsLimit)
            .observeAsState(null)
    } else {
        remember { mutableStateOf(emptyList()) }
    }
    val selectedCutOffDate = mainViewModel.selectedCutOffDate.value

    val pagerState = rememberPagerState(
        initialPage = 0
    ) { cutOffDates?.size ?: 0 }

    // Initial selection from history
    LaunchedEffect(employers) {
        if (selectedEmployer == null && employers.isNotEmpty()) {
            val savedEmployer = employers.find { it.employerId == mainViewModel.selectedEmployerId }
            mainViewModel.setEmployer(savedEmployer ?: employers.first())
        }
    }

    LaunchedEffect(cutOffDates, selectedEmployer) {
        val dates = cutOffDates ?: return@LaunchedEffect
        if (selectedEmployer != null && dates.isNotEmpty()) {
            val today = LocalDate.now().toString()
            if (mainViewModel.selectedCutOffDate.value.isBlank() ||
                !dates.any { it.ppCutoffDate == mainViewModel.selectedCutOffDate.value } ||
                mainViewModel.selectedCutOffDate.value > today
            ) {
                val currentCutOff =
                    dates.lastOrNull { it.ppCutoffDate >= today }?.ppCutoffDate
                        ?: dates.first().ppCutoffDate
                mainViewModel.setCutOffDate(currentCutOff)
            }
        }
    }

    // Sync ViewModel selection to Pager
    LaunchedEffect(selectedCutOffDate, cutOffDates) {
        val dates = cutOffDates ?: return@LaunchedEffect
        val index = dates.indexOfFirst { it.ppCutoffDate == selectedCutOffDate }
        if (index != -1 && pagerState.currentPage != index) {
            if (mainViewModel.selectedTopLevelIndex.intValue == 1) {
                pagerState.animateScrollToPage(index)
            } else {
                pagerState.scrollToPage(index)
            }
        }
    }

    // Sync Pager selection back to ViewModel (only on user interaction)
    LaunchedEffect(pagerState, cutOffDates) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (pagerState.isScrollInProgress) {
                val dates = cutOffDates ?: return@collect
                if (page < dates.size) {
                    val newDate = dates[page].ppCutoffDate
                    if (mainViewModel.selectedCutOffDate.value != newDate) {
                        mainViewModel.setCutOffDate(newDate)
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SelectionCard(
            modifier = Modifier.padding(horizontal = SCREEN_PADDING_HORIZONTAL),
            employers = employers,
            selectedEmployer = selectedEmployer,
            onEmployerSelected = {
                if (selectedEmployer?.employerId != it.employerId) {
                    mainViewModel.setEmployer(it)
                    mainViewModel.setCutOffDate("")
                }
            },
            onAddNewEmployer = {
                navController.navigate(Screen.EmployerAdd.route)
            },
            cutOffDates = cutOffDates?.map { it.ppCutoffDate } ?: emptyList(),
            selectedCutOffDate = selectedCutOffDate,
            onCutOffDateSelected = { mainViewModel.setCutOffDate(it) },
            displayDate = { if (it.isBlank()) "" else df.getDisplayDate(it) },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                userScrollEnabled = true
            ) { page ->
                val date = cutOffDates?.getOrNull(page)?.ppCutoffDate ?: return@VerticalPager
                val employer = selectedEmployer ?: return@VerticalPager
                PayDetailPage(
                    employer = employer,
                    cutoffDate = date,
                    mainViewModel = mainViewModel,
                    payDayViewModel = payDayViewModel,
                    payCalculationsViewModel = payCalculationsViewModel,
                    payDetailViewModel = payDetailViewModel,
                    nf = nf,
                    df = df,
                    navController = navController
                )
            }
        }
    }
}