package ms.mattschlenkrich.paycalculator.ui.workdate

import android.app.TimePickerDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTimeViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun WorkDateTimesRoute(
    mainViewModel: MainViewModel,
    workTimeViewModel: WorkTimeViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    var history by remember { mutableStateOf(mainViewModel.getWorkOrderHistory()) }
    val workDate = mainViewModel.getWorkDateObject() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val historyCombined by if (history != null) {
        workOrderViewModel.getWorkOrderHistoryCombined(history!!.woHistoryId)
            .observeAsState()
    } else {
        remember { mutableStateOf(null) }
    }

    var workOrderNumber by remember {
        mutableStateOf(
            historyCombined?.workOrder?.woNumber ?: ""
        )
    }

    val employer = mainViewModel.getEmployer() ?: return
    val workOrderSuggestions by workTimeViewModel.getWorkOrderNumbers(
        employer.employerId
    ).observeAsState(emptyList())

    val existingTimes by if (history != null) {
        workOrderViewModel.getTimeWorkedForWorkOrderHistory(history!!.woHistoryId)
            .observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val allTimesByDate by workTimeViewModel.getTimesWorkedByDate(workDate.workDateId)
        .observeAsState(emptyList())

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isOverlapOverride by remember { mutableStateOf(false) }

    var selectedTimeType by remember { mutableIntStateOf(0) }

    LaunchedEffect(allTimesByDate) {
        val totalHours = allTimesByDate
            .filter { it.timeWorked.wohtTimeType != TimeWorkedTypes.BREAK.value }
            .sumOf {
                df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime)
            }
        selectedTimeType = when {
            totalHours < 8.0 -> TimeWorkedTypes.REG_HOURS.value
            totalHours < 12.0 -> TimeWorkedTypes.OT_HOURS.value
            else -> TimeWorkedTypes.DBL_OT_HOURS.value
        }
    }

    val woBlankError = stringResource(R.string.work_order_number_cannot_be_blank)
    val woNotFoundError = stringResource(R.string.work_order_not_found_please_create_it)
    val overlapWarning = stringResource(R.string.warning_start_time_overlaps_previous_end_time)

    var startTime by remember(allTimesByDate) {
        if (allTimesByDate.isNotEmpty()) {
            val latestTime = allTimesByDate.maxOf { it.timeWorked.wohtEndTime }
            val timePart = df.splitTimeFromDateTime(latestTime).joinToString(":")
            mutableStateOf(df.getCalendarFromTime(timePart))
        } else {
            mutableStateOf(df.getCalendarFromTime("08:30"))
        }
    }
    var endTime by remember(startTime) {
        val end = startTime.clone() as Calendar
        if (allTimesByDate.isEmpty() && df.getTimeDisplay(startTime) == "08:30") {
            end.add(Calendar.HOUR_OF_DAY, 8)
        }
        mutableStateOf(end)
    }

    val isWorkOrderValid = workOrderSuggestions.any { it.woNumber == workOrderNumber }
    var workOrderError by remember { mutableStateOf<String?>(null) }

    var showTimeOptionsDialog by remember { mutableStateOf<WorkOrderHistoryTimeWorkedCombined?>(null) }
    var showDeleteConfirmDialog by remember {
        mutableStateOf<WorkOrderHistoryTimeWorkedCombined?>(
            null
        )
    }

    if (showTimeOptionsDialog != null) {
        val combinedItem = showTimeOptionsDialog!!
        AlertDialog(
            onDismissRequest = { showTimeOptionsDialog = null },
            confirmButton = {
                TextButton(onClick = {
                    mainViewModel.setWorkOrderHistoryTimeWorkedCombined(combinedItem)
                    showTimeOptionsDialog = null
                    navController.navigate(Screen.WorkOrderHistoryTimeUpdate.route)
                }) {
                    Text(stringResource(R.string.modify_time_entry))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = combinedItem
                    showTimeOptionsDialog = null
                }) {
                    Text(stringResource(R.string.delete_time_entry))
                }
            },
            title = {
                Text(stringResource(R.string.time_entry_options))
            },
            text = {
                Text(
                    df.get12HourDisplay(
                        df.splitTimeFromDateTime(combinedItem.timeWorked.wohtStartTime)
                            .joinToString(":")
                    ) + " - " +
                            df.get12HourDisplay(
                                df.splitTimeFromDateTime(combinedItem.timeWorked.wohtEndTime)
                                    .joinToString(":")
                            )
                )
            }
        )
    }

    if (showDeleteConfirmDialog != null) {
        val combinedItem = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        workOrderViewModel.deleteTimeWorked(
                            combinedItem.timeWorked.woHistoryTimeWorkedId,
                            df.getCurrentUTCTimeAsString()
                        )
                    }
                    showDeleteConfirmDialog = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.delete_time_entry)) },
            text = { Text(stringResource(R.string.this_cannot_be_undone)) }
        )
    }

    WorkDateTimesScreen(
        infoText = df.getDisplayDate(workDate.wdDate),
        hoursSummaryText = if (history != null) {
            "${nf.displayNumberFromDouble(history!!.woHistoryRegHours)} Reg | " +
                    "${nf.displayNumberFromDouble(history!!.woHistoryOtHours)} OT | " +
                    "${nf.displayNumberFromDouble(history!!.woHistoryDblOtHours)} Dbl"
        } else {
            stringResource(R.string.there_is_no_work_order_selected)
        },
        workOrderNumber = workOrderNumber,
        onWorkOrderNumberChange = {
            workOrderNumber = it
            workOrderError = null
            coroutineScope.launch {
                val wo = workOrderViewModel.findWorkOrder(it, employer.employerId)
                if (wo != null) {
                    val existingHistory = workOrderViewModel.getWorkOrderHistory(
                        wo.workOrderId,
                        workDate.workDateId
                    )
                    if (existingHistory != null) {
                        history = existingHistory
                    } else {
                        val newHistory = WorkOrderHistory(
                            nf.generateRandomIdAsLong(),
                            wo.workOrderId,
                            workDate.workDateId,
                            0.0, 0.0, 0.0,
                            null, false,
                            df.getCurrentUTCTimeAsString()
                        )
                        workOrderViewModel.insertWorkOrderHistory(newHistory)
                        history = newHistory
                    }
                } else {
                    history = null
                }
            }
        },
        workOrderSuggestions = workOrderSuggestions.map { it.woNumber },
        workOrderButtonText = if (isWorkOrderValid) stringResource(R.string.edit)
        else stringResource(R.string.create),
        onWorkOrderButtonClick = {
            if (workOrderNumber.isBlank()) {
                workOrderError = woBlankError
                return@WorkDateTimesScreen
            }
            if (isWorkOrderValid) {
                val wo = workOrderSuggestions.find { it.woNumber == workOrderNumber }
                if (wo != null) {
                    mainViewModel.setWorkOrder(wo)
                    navController.navigate(Screen.WorkOrderUpdate.route)
                }
            } else {
                mainViewModel.setWorkOrderNumber(workOrderNumber)
                navController.navigate(Screen.WorkOrderAdd.route)
            }
        },
        workOrderInfoText = historyCombined?.workOrder?.woDescription ?: "",
        workOrderError = workOrderError,
        startTime = startTime,
        endTime = endTime,
        totalTimeText = String.format(
            Locale.getDefault(),
            "%.2f",
            df.getTimeWorked(startTime, endTime)
        ),
        selectedTimeType = selectedTimeType,
        onTimeTypeChange = { selectedTimeType = it },
        onStartTimeClick = {
            TimePickerDialog(context, { _, h, m ->
                val (roundedH, roundedM) = df.roundTimeTo15Minutes(h, m)
                startTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, roundedH)
                    set(Calendar.MINUTE, roundedM)
                }
                errorMessage = null
                isOverlapOverride = false
            }, startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), false).show()
        },
        onEndTimeClick = {
            TimePickerDialog(context, { _, h, m ->
                val (roundedH, roundedM) = df.roundTimeTo15Minutes(h, m)
                endTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, roundedH)
                    set(Calendar.MINUTE, roundedM)
                }
                errorMessage = null
                isOverlapOverride = false
            }, endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE), false).show()
        },
        onEnterTimeClick = {
            if (workOrderNumber.isBlank()) {
                workOrderError = woBlankError
                return@WorkDateTimesScreen
            }
            if (history == null) {
                workOrderError = woNotFoundError
                return@WorkDateTimesScreen
            }

            val startTimeString = df.getDateTimeFromDateAndTime(
                workDate.wdDate,
                df.getTimeDisplay(startTime)
            )

            if (!isOverlapOverride && allTimesByDate.any { it.timeWorked.wohtEndTime > startTimeString }) {
                errorMessage = overlapWarning
                isOverlapOverride = true
                return@WorkDateTimesScreen
            }

            coroutineScope.launch {
                var currentHistory = history
                if (currentHistory == null) {
                    val wo = workOrderViewModel.findWorkOrder(workOrderNumber, employer.employerId)
                        ?: run {
                            val newWo = WorkOrder(
                                nf.generateRandomIdAsLong(),
                                workOrderNumber,
                                employer.employerId,
                                "", "", false,
                                df.getCurrentUTCTimeAsString()
                            )
                            workOrderViewModel.insertWorkOrder(newWo)
                            newWo
                        }

                    val existingHistory = workOrderViewModel.getWorkOrderHistory(
                        wo.workOrderId,
                        workDate.workDateId
                    )
                    currentHistory = if (existingHistory != null) {
                        existingHistory
                    } else {
                        val newHistory = WorkOrderHistory(
                            nf.generateRandomIdAsLong(),
                            wo.workOrderId,
                            workDate.workDateId,
                            0.0, 0.0, 0.0,
                            null, false,
                            df.getCurrentUTCTimeAsString()
                        )
                        workOrderViewModel.insertWorkOrderHistory(newHistory)
                        newHistory
                    }
                    history = currentHistory
                }

                workOrderViewModel.insertTimeWorked(
                    WorkOrderHistoryTimeWorked(
                        nf.generateRandomIdAsLong(),
                        currentHistory!!.woHistoryId,
                        workDate.workDateId,
                        startTimeString,
                        df.getDateTimeFromDateAndTime(
                            workDate.wdDate,
                            df.getTimeDisplay(endTime)
                        ),
                        selectedTimeType,
                        false,
                        df.getCurrentUTCTimeAsString()
                    )
                )
                startTime = endTime.clone() as Calendar
                errorMessage = null
                isOverlapOverride = false
            }
        },
        onDoneClick = { navController.popBackStack() },
        existingTimes = existingTimes,
        allTimesForDay = allTimesByDate,
        onTimeClick = { item ->
            mainViewModel.setWorkOrderHistoryTimeWorkedCombined(item)
            navController.navigate(Screen.WorkOrderHistoryTimeUpdate.route)
        },
        onTimeLongClick = { item ->
            showTimeOptionsDialog = item
        },
        errorMessage = errorMessage
    )
}