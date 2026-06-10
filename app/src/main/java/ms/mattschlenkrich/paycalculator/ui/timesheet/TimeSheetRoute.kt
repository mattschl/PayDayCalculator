package ms.mattschlenkrich.paycalculator.ui.timesheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectionCard
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.logic.PayDateProjections
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.ui.timesheet.composable.TimeSheetPage
import java.time.LocalDate

@Composable
fun TimeSheetRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    payDayViewModel: PayDayViewModel,
    payCalculationsViewModel: PayCalculationsViewModel,
    payDetailViewModel: PayDetailViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }
    val projections = remember { PayDateProjections() }

    val settings by settingsViewModel.settings.observeAsState()
    val minColumnWidth = settings?.minColumnWidth ?: 360
    val payPeriodsLimit = settings?.payPeriodsLimit ?: 15

    val hrLabel = stringResource(R.string.hr)
    val otLabel = stringResource(R.string.ot)
    val dblOtLabel = stringResource(R.string.dbl_ot)
    val otherHoursLabel = stringResource(R.string.other)
    val pipeLabel = stringResource(R.string.pipe)
    val hrsLabel = stringResource(R.string.hrs)
    val otHrsLabel = stringResource(R.string.ot)
    val dblOtHrsLabel = stringResource(R.string.dbl_ot)
    val otherHrsLabel = stringResource(R.string.other)

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
        initialPage = 0,
    ) { cutOffDates?.size ?: 0 }

    LaunchedEffect(employers) {
        if ((selectedEmployer == null) && employers.isNotEmpty()) {
            val savedEmployer = employers.find { it.employerId == mainViewModel.selectedEmployerId }
            mainViewModel.setEmployer(savedEmployer ?: employers.first())
        }
    }

    LaunchedEffect(cutOffDates, selectedEmployer) {
        val dates = cutOffDates ?: return@LaunchedEffect
        if (selectedEmployer != null) {
            val today = LocalDate.now().toString()
            if (dates.isEmpty()) {
                coroutineScope.launch {
                    val nextCutOff = projections.getCutOffForDate(
                        selectedEmployer,
                        today
                    )
                    if (nextCutOff.isNotEmpty()) {
                        mainViewModel.setPayPeriod(null)
                        payDayViewModel.insertPayPeriodSync(
                            PayPeriods(
                                nf.generateRandomIdAsLong(),
                                nextCutOff,
                                selectedEmployer.employerId,
                                ppIsDeleted = false,
                                ppUpdateTime = df.getCurrentUTCTimeAsString()
                            )
                        )
                        mainViewModel.setCutOffDate(nextCutOff)
                    }
                }
            } else if (dates.first().ppCutoffDate < today) {
                coroutineScope.launch {
                    val nextCutOff = projections.generateNextCutOff(
                        selectedEmployer,
                        dates.first().ppCutoffDate
                    )
                    if (nextCutOff.isNotEmpty()) {
                        mainViewModel.setPayPeriod(null)
                        payDayViewModel.insertPayPeriodSync(
                            PayPeriods(
                                nf.generateRandomIdAsLong(),
                                nextCutOff,
                                selectedEmployer.employerId,
                                ppIsDeleted = false,
                                ppUpdateTime = df.getCurrentUTCTimeAsString()
                            )
                        )
                        mainViewModel.setCutOffDate(nextCutOff)
                    }
                }
            } else {
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
    }

    // Sync ViewModel selection to Pager
    LaunchedEffect(selectedCutOffDate, cutOffDates) {
        val dates = cutOffDates ?: return@LaunchedEffect
        val index = dates.indexOfFirst { it.ppCutoffDate == selectedCutOffDate }
        if (index != -1 && pagerState.currentPage != index) {
            if (mainViewModel.selectedTopLevelIndex.intValue == 0) {
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

    var showWorkDateOptionsDialog by remember { mutableStateOf<WorkDates?>(null) }
    var showDeleteWorkDateConfirmDialog by remember { mutableStateOf<WorkDates?>(null) }

    if (showWorkDateOptionsDialog != null) {
        val workDate = showWorkDateOptionsDialog!!
        AlertDialog(
            onDismissRequest = { showWorkDateOptionsDialog = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        mainViewModel.setWorkDateObject(workDate)
                        showWorkDateOptionsDialog = null
                        navController.navigate(Screen.WorkDateUpdate.route)
                    }
                ) {
                    Text(stringResource(R.string.open_caps))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteWorkDateConfirmDialog = workDate
                    showWorkDateOptionsDialog = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            title = {
                Text(
                    stringResource(R.string.choose_option_for) + df.getDisplayDate(workDate.wdDate)
                )
            }
        )
    }

    if (showDeleteWorkDateConfirmDialog != null) {
        val workDate = showDeleteWorkDateConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteWorkDateConfirmDialog = null },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        payDayViewModel.updateWorkDate(
                            workDate.copy(
                                wdIsDeleted = true,
                                wdUpdateTime = df.getCurrentUTCTimeAsString()
                            )
                        )
                    }
                    showDeleteWorkDateConfirmDialog = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteWorkDateConfirmDialog = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = {
                Text(
                    stringResource(R.string.are_you_sure_you_want_to_delete) + df.getDisplayDate(
                        workDate.wdDate
                    )
                )
            },
            text = { Text(stringResource(R.string.this_cannot_be_undone)) }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SelectionCard(
            modifier = Modifier.padding(horizontal = SCREEN_PADDING_HORIZONTAL),
            employers = employers,
            selectedEmployer = selectedEmployer,
            onEmployerSelected = {
                if (selectedEmployer?.employerId != it.employerId) {
                    mainViewModel.setEmployer(it)
                    mainViewModel.setCutOffDate("")
                    mainViewModel.setPayPeriod(null)
                }
            },
            onAddNewEmployer = {
                navController.navigate(Screen.EmployerAdd.route)
            },
            cutOffDates = cutOffDates?.map { it.ppCutoffDate } ?: emptyList(),
            selectedCutOffDate = selectedCutOffDate,
            onCutOffDateSelected = { mainViewModel.setCutOffDate(it) },
            onGenerateCutoffClick = {
                if (selectedEmployer != null) {
                    coroutineScope.launch {
                        val dates = payDayViewModel.getCutOffDatesSync(
                            selectedEmployer.employerId,
                            payPeriodsLimit
                        )
                        val nextCutOff = projections.generateNextCutOff(
                            selectedEmployer,
                            dates.firstOrNull()?.ppCutoffDate ?: ""
                        )
                        if (nextCutOff.isNotEmpty()) {
                            payDayViewModel.insertPayPeriodSync(
                                PayPeriods(
                                    nf.generateRandomIdAsLong(),
                                    nextCutOff,
                                    selectedEmployer.employerId,
                                    false,
                                    df.getCurrentUTCTimeAsString()
                                )
                            )
                        }
                    }
                }
            },
            displayDate = { if (it.isBlank()) "" else df.getDisplayDate(it) }
        )

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.WorkDateAdd.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_a_new_work_date)
                    )
                }
            }
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
                TimeSheetPage(
                    employer = employer,
                    cutoffDate = date,
                    payDayViewModel = payDayViewModel,
                    payCalculationsViewModel = payCalculationsViewModel,
                    payDetailViewModel = payDetailViewModel,
                    nf = nf,
                    df = df,
                    onWorkDateClick = { workDate ->
                        mainViewModel.setWorkDateObject(workDate)
                        navController.navigate(Screen.WorkDateUpdate.route)
                    },
                    onWorkDateLongClick = { workDate ->
                        showWorkDateOptionsDialog = workDate
                    },
                    onViewPayDetailsClick = {
                        mainViewModel.setSelectedTopLevelIndex(1)
                    },
                    hrLabel = hrLabel,
                    otLabel = otLabel,
                    dblOtLabel = dblOtLabel,
                    otherHoursLabel = otherHoursLabel,
                    pipeLabel = pipeLabel,
                    hrsLabel = hrsLabel,
                    otHrsLabel = otHrsLabel,
                    dblOtHrsLabel = dblOtHrsLabel,
                    otherHrsLabel = otherHrsLabel,
                    minColumnWidth = minColumnWidth
                )
            }
        }
    }
}