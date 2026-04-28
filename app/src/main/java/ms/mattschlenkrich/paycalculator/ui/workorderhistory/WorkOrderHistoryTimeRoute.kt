package ms.mattschlenkrich.paycalculator.ui.workorderhistory

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Row
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import java.util.Calendar

@Composable
fun WorkOrderHistoryTimeRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()
    val overlapWarning = stringResource(R.string.warning_start_time_overlaps_previous_end_time)
    val adjustedRegHours = stringResource(R.string.time_adjusted_to_not_exceed_8_reg_hours)
    val adjustedOtHours = stringResource(R.string.time_adjusted_to_not_exceed_12_ot_hours)

    val history = mainViewModel.getWorkOrderHistory() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val historyWithDates by workOrderViewModel.getWorkOrderHistoriesById(history.woHistoryId)
        .observeAsState()

    if (historyWithDates == null) return

    val existingTimes by workOrderViewModel.getWorkOrderHistoryTimesByHistory(history.woHistoryId)
        .observeAsState(emptyList())

    val allTimesByDate by workOrderViewModel.getTimeWorkedPerDay(historyWithDates!!.workDate.workDateId)
        .observeAsState(emptyList())

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(3000)
            errorMessage = null
        }
    }

    var selectedTimeType by remember { mutableIntStateOf(TimeWorkedTypes.REG_HOURS.value) }

    LaunchedEffect(allTimesByDate) {
        val totalWorkedHours = allTimesByDate
            .filter { it.timeWorked.wohtTimeType != TimeWorkedTypes.BREAK.value }
            .sumOf {
                df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime)
            }
        selectedTimeType = when {
            totalWorkedHours < 8.0 -> TimeWorkedTypes.REG_HOURS.value
            totalWorkedHours < 12.0 -> TimeWorkedTypes.OT_HOURS.value
            else -> TimeWorkedTypes.DBL_OT_HOURS.value
        }
    }

    var startTime by remember(allTimesByDate) {
        val latestTime = allTimesByDate.maxOfOrNull { it.timeWorked.wohtEndTime }
        val timePart = latestTime?.let {
            df.splitTimeFromDateTime(it).joinToString(":")
        } ?: "08:30"
        mutableStateOf(df.getCalendarFromTime(timePart))
    }
    var endTime by remember(startTime) {
        mutableStateOf(startTime.clone() as Calendar)
    }

    val totalHours = df.getTimeWorked(
        df.getTimeDisplay(startTime),
        df.getTimeDisplay(endTime)
    )

    var showStartTimeWarning by remember { mutableStateOf(false) }

    var showTimeOptionsDialog by remember { mutableStateOf<WorkOrderHistoryTimeWorkedCombined?>(null) }
    var showDeleteConfirmDialog by remember {
        mutableStateOf<WorkOrderHistoryTimeWorkedCombined?>(
            null
        )
    }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(stringResource(R.string.confirm_leave)) },
            text = { Text(stringResource(R.string.would_you_like_to_save_time_entered)) },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        workOrderViewModel.insertWorkOrderHistoryTimeWorked(
                            WorkOrderHistoryTimeWorked(
                                nf.generateRandomIdAsLong(),
                                history.woHistoryId,
                                historyWithDates!!.workDate.workDateId,
                                df.getDateTimeFromDateAndTime(
                                    historyWithDates!!.workDate.wdDate,
                                    df.getTimeDisplay(startTime)
                                ),
                                df.getDateTimeFromDateAndTime(
                                    historyWithDates!!.workDate.wdDate,
                                    df.getTimeDisplay(endTime)
                                ),
                                selectedTimeType,
                                false,
                                df.getCurrentUTCTimeAsString()
                            )
                        )
                        showUnsavedDialog = false
                        navController.navigate(Screen.WorkOrderHistoryUpdate.route) {
                            popUpTo(Screen.WorkOrderHistoryTime.route) { inclusive = true }
                        }
                    }
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showUnsavedDialog = false
                        navController.navigate(Screen.WorkOrderHistoryUpdate.route) {
                            popUpTo(Screen.WorkOrderHistoryTime.route) { inclusive = true }
                        }
                    }) {
                        Text(stringResource(R.string.no))
                    }
                    TextButton(onClick = {
                        showUnsavedDialog = false
                    }) {
                        Text(stringResource(R.string.go_back))
                    }
                }
            }
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

    WorkOrderHistoryTimeScreen(
        infoText = stringResource(R.string.work_order) + " ${historyWithDates!!.workOrder.woNumber}\n" +
                historyWithDates!!.workOrder.woDescription,
        hoursSummaryText = buildString {
            val workedHours = existingTimes.filter {
                it.timeWorked.wohtTimeType != TimeWorkedTypes.BREAK.value
            }.sumOf { df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime) }

            val breakHours = existingTimes.filter {
                it.timeWorked.wohtTimeType == TimeWorkedTypes.BREAK.value
            }.sumOf { df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime) }

            append(stringResource(R.string.total_hours))
            append(" ")
            append(nf.displayNumberFromDouble(workedHours))

            if (breakHours > 0.0) {
                append(" (")
                append(nf.displayNumberFromDouble(breakHours))
                append(" break)")
            }

            val reg = existingTimes.filter {
                it.timeWorked.wohtTimeType == TimeWorkedTypes.REG_HOURS.value
            }.sumOf { df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime) }

            val ot = existingTimes.filter {
                it.timeWorked.wohtTimeType == TimeWorkedTypes.OT_HOURS.value
            }.sumOf { df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime) }

            val dbl = existingTimes.filter {
                it.timeWorked.wohtTimeType == TimeWorkedTypes.DBL_OT_HOURS.value
            }.sumOf { df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime) }

            val details = buildString {
                if (reg > 0) append("reg ${nf.displayNumberFromDouble(reg)}")
                if (ot > 0) {
                    if (isNotEmpty()) append(" | ")
                    append("ot ${nf.displayNumberFromDouble(ot)}")
                }
                if (dbl > 0) {
                    if (isNotEmpty()) append(" | ")
                    append("dbl ${nf.displayNumberFromDouble(dbl)}")
                }
            }
            if (details.isNotEmpty()) {
                append("\n")
                append(details)
            }
        },
        startTime = startTime,
        endTime = endTime,
        totalTimeText = nf.displayNumberFromDouble(totalHours) + " " + stringResource(R.string.hours),
        selectedTimeType = selectedTimeType,
        onTimeTypeChange = { selectedTimeType = it },
        onStartTimeClick = {
            TimePickerDialog(context, { _, h, m ->
                val (roundedHour, roundedMinute) = df.roundTimeTo15Minutes(h, m)
                val newStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, roundedHour)
                    set(Calendar.MINUTE, roundedMinute)
                }
                startTime = newStart
                showStartTimeWarning = false
                errorMessage = null
            }, startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), false).show()
        },
        onEndTimeClick = {
            TimePickerDialog(context, { _, h, m ->
                val (roundedHour, roundedMinute) = df.roundTimeTo15Minutes(h, m)
                val newEnd = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, roundedHour)
                    set(Calendar.MINUTE, roundedMinute)
                }

                val hoursBefore = allTimesByDate.filter {
                    it.timeWorked.wohtTimeType != TimeWorkedTypes.BREAK.value
                }.sumOf {
                    df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime)
                }
                val newSegmentHours = df.getTimeWorked(startTime, newEnd)

                if (selectedTimeType == TimeWorkedTypes.REG_HOURS.value &&
                    hoursBefore + newSegmentHours > 8.0
                ) {
                    val allowedHours = 8.0 - hoursBefore
                    endTime = df.addHoursToCalendar(startTime, allowedHours)
                    errorMessage = adjustedRegHours
                } else if (selectedTimeType == TimeWorkedTypes.OT_HOURS.value &&
                    hoursBefore + newSegmentHours > 12.0
                ) {
                    val allowedHours = 12.0 - hoursBefore
                    endTime = df.addHoursToCalendar(startTime, allowedHours)
                    errorMessage = adjustedOtHours
                } else {
                    endTime = newEnd
                }
                showStartTimeWarning = false
                errorMessage = null
            }, endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE), false).show()
        },
        onEnterTimeClick = {
            val latestTime = if (allTimesByDate.isNotEmpty()) {
                allTimesByDate.maxOf { it.timeWorked.wohtEndTime }
            } else null

            val currentStart = df.getDateTimeFromDateAndTime(
                historyWithDates!!.workDate.wdDate,
                df.getTimeDisplay(startTime)
            )

            if (latestTime != null && currentStart < latestTime && !showStartTimeWarning) {
                errorMessage = overlapWarning
                showStartTimeWarning = true
            } else {
                coroutineScope.launch {
                    workOrderViewModel.insertWorkOrderHistoryTimeWorked(
                        WorkOrderHistoryTimeWorked(
                            nf.generateRandomIdAsLong(),
                            history.woHistoryId,
                            historyWithDates!!.workDate.workDateId,
                            currentStart,
                            df.getDateTimeFromDateAndTime(
                                historyWithDates!!.workDate.wdDate,
                                df.getTimeDisplay(endTime)
                            ),
                            selectedTimeType,
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    )
                    startTime = endTime.clone() as Calendar
                    showStartTimeWarning = false
                }
            }
        },
        onDoneClick = {
            if (totalHours > 0.0) {
                showUnsavedDialog = true
            } else {
                navController.navigate(Screen.WorkOrderHistoryUpdate.route) {
                    popUpTo(Screen.WorkOrderHistoryTime.route) { inclusive = true }
                }
            }
        },
        existingTimes = existingTimes,
        allTimesForDay = existingTimes,
        onTimeClick = { combined ->
            mainViewModel.setWorkOrderHistoryTimeWorkedCombined(combined)
            navController.navigate(Screen.WorkOrderHistoryTimeUpdate.route)
        },
        onTimeLongClick = { combined ->
            showTimeOptionsDialog = combined
        },
        onBackClick = { navController.popBackStack() }
    )
}