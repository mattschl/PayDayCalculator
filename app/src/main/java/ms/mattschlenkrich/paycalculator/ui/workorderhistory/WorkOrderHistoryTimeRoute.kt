package ms.mattschlenkrich.paycalculator.ui.workorderhistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.common.compose.ConfirmationBottomSheet
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable.WorkOrderHistoryTimeScreen
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
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
    val duplicateStartTimeError = stringResource(R.string.error_start_time_already_used)
    val adjustedRegHours = stringResource(R.string.time_adjusted_to_not_exceed_8_reg_hours)
    val adjustedOtHours = stringResource(R.string.time_adjusted_to_not_exceed_12_ot_hours)

    val history = mainViewModel.getWorkOrderHistory() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val historyWithDates by workOrderViewModel.getWorkOrderHistory(history.woHistoryId)
        .observeAsState()

    if (historyWithDates == null) return

    val existingTimes by workOrderViewModel.getWorkOrderHistoryTimesByHistory(history.woHistoryId)
        .observeAsState(emptyList())

    val allTimesByDate by workOrderViewModel.getTimeWorkedPerDay(historyWithDates!!.workDate.workDateId)
        .observeAsState(emptyList())

    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(3000.milliseconds)
            errorMessage = null
        }
    }

    var selectedTimeType by remember { mutableIntStateOf(TimeWorkedTypes.REG_HOURS.value) }

    LaunchedEffect(allTimesByDate) {
        val totalWorkedHours = allTimesByDate
            .filter {
                it.timeWorked.wohtTimeType != TimeWorkedTypes.BREAK.value &&
                        !it.workOrderHistory.workOrderHistory.woHistoryDeleted
            }
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
        val latestTime = allTimesByDate
            .filter { !it.workOrderHistory.workOrderHistory.woHistoryDeleted }
            .maxOfOrNull { it.timeWorked.wohtEndTime }
        val timePart = latestTime?.let {
            df.splitTimeFromDateTime(it).joinToString(":")
        } ?: "08:30"
        mutableStateOf(df.getCalendarFromTime(timePart))
    }
    var endTime by remember(startTime, selectedTimeType) {
        val now = df.roundCalendarTimeUpTo15Minutes(Calendar.getInstance())
        val hoursBefore = allTimesByDate
            .filter {
                it.timeWorked.wohtTimeType != TimeWorkedTypes.BREAK.value &&
                        !it.workOrderHistory.workOrderHistory.woHistoryDeleted
            }
            .sumOf {
                df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime)
            }

        val limitHours = when (selectedTimeType) {
            TimeWorkedTypes.REG_HOURS.value -> 8.0 - hoursBefore
            TimeWorkedTypes.OT_HOURS.value -> 12.0 - hoursBefore
            else -> Double.MAX_VALUE
        }

        val limitTime = if (limitHours > 0 && limitHours != Double.MAX_VALUE) {
            df.addHoursToCalendar(startTime, limitHours)
        } else {
            null
        }

        val end = if (limitTime != null && now.after(limitTime)) {
            limitTime
        } else if (now.after(startTime)) {
            now
        } else {
            startTime.clone() as Calendar
        }
        mutableStateOf(end)
    }

    val totalHours = df.getTimeWorked(
        df.getTimeDisplay(startTime),
        df.getTimeDisplay(endTime)
    )

    var showTimeOptionsDialog by rememberSaveable {
        mutableStateOf<WorkOrderHistoryTimeWorkedCombined?>(
            null
        )
    }
    var showDeleteConfirmDialog by rememberSaveable {
        mutableStateOf<WorkOrderHistoryTimeWorkedCombined?>(
            null
        )
    }
    var showOverlapConfirmDialog by rememberSaveable {
        mutableStateOf<WorkOrderHistoryTimeWorked?>(
            null
        )
    }
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }

    ConfirmationBottomSheet(
        showDialog = showOverlapConfirmDialog != null,
        onDismissRequest = { showOverlapConfirmDialog = null },
        title = stringResource(R.string.save),
        message = stringResource(R.string.confirm_overlap),
        onConfirm = {
            showOverlapConfirmDialog?.let { entry ->
                coroutineScope.launch {
                    workOrderViewModel.insertWorkOrderHistoryTimeWorked(entry)
                    startTime = endTime.clone() as Calendar
                }
            }
        }
    )

    ConfirmationBottomSheet(
        showDialog = showUnsavedDialog,
        onDismissRequest = { showUnsavedDialog = false },
        title = stringResource(R.string.confirm_leave),
        message = stringResource(R.string.would_you_like_to_save_time_entered),
        dismissButtonText = stringResource(R.string.go_back),
        onConfirm = {
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
                navController.popBackStack()
            }
        },
        content = {
            TextButton(
                onClick = {
                    showUnsavedDialog = false
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.no))
            }
        }
    )

    if (showTimeOptionsDialog != null) {
        val combinedItem = showTimeOptionsDialog!!
        ModalBottomSheet(
            onDismissRequest = { showTimeOptionsDialog = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING / 2)
            ) {
                Text(
                    text = stringResource(R.string.time_entry_options),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "${df.get12HourDisplay(combinedItem.timeWorked.wohtStartTime)} - ${
                        df.get12HourDisplay(
                            combinedItem.timeWorked.wohtEndTime
                        )
                    }"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        showDeleteConfirmDialog = combinedItem
                        showTimeOptionsDialog = null
                    }) {
                        Text(stringResource(R.string.delete_time_entry))
                    }
                    TextButton(onClick = {
                        mainViewModel.setWorkOrderHistoryTimeWorkedCombined(combinedItem)
                        showTimeOptionsDialog = null
                        navController.navigate(Screen.WorkOrderHistoryTimeUpdate.route)
                    }) {
                        Text(stringResource(R.string.modify_time_entry))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    ConfirmationBottomSheet(
        showDialog = showDeleteConfirmDialog != null,
        onDismissRequest = { showDeleteConfirmDialog = null },
        title = stringResource(R.string.delete_time_entry),
        message = stringResource(R.string.this_cannot_be_undone),
        confirmButtonText = stringResource(R.string.delete),
        dismissButtonText = stringResource(R.string.cancel),
        isDelete = true,
        onConfirm = {
            showDeleteConfirmDialog?.let { combinedItem ->
                coroutineScope.launch {
                    workOrderViewModel.deleteTimeWorked(
                        combinedItem.timeWorked.woHistoryTimeWorkedId,
                        df.getCurrentUTCTimeAsString()
                    )
                }
            }
        }
    )

    WorkOrderHistoryTimeScreen(
        infoText = "${stringResource(R.string.work_order)} ${historyWithDates!!.workOrder.woNumber}\n" +
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
        totalTimeText = "${nf.displayNumberFromDouble(totalHours)} ${stringResource(R.string.hours)}",
        selectedTimeType = selectedTimeType,
        onTimeTypeChange = { selectedTimeType = it },
        onStartTimeClick = {
            df.showTimePicker(context, startTime) { newStart ->
                startTime = df.roundCalendarTimeDownTo15Minutes(newStart)
                errorMessage = null
            }
        },
        onEndTimeClick = {
            df.showTimePicker(context, endTime) { newEnd ->
                val roundedEnd = df.roundCalendarTimeUpTo15Minutes(newEnd)

                val hoursBefore = allTimesByDate.filter {
                    it.timeWorked.wohtTimeType != TimeWorkedTypes.BREAK.value
                }.sumOf {
                    df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime)
                }
                val newSegmentHours = df.getTimeWorked(startTime, roundedEnd)

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
                    endTime = roundedEnd
                }
                errorMessage = null
            }
        },
        onEnterTimeClick = {
            val currentStart = df.getDateTimeFromDateAndTime(
                historyWithDates!!.workDate.wdDate,
                df.getTimeDisplay(startTime)
            )
            val currentEnd = df.getDateTimeFromDateAndTime(
                historyWithDates!!.workDate.wdDate,
                df.getTimeDisplay(endTime)
            )

            val isDuplicateStart =
                allTimesByDate.any { it.timeWorked.wohtStartTime == currentStart }
            val isDuplicateEnd =
                allTimesByDate.any { it.timeWorked.wohtEndTime == currentEnd }

            val hasOverlap = allTimesByDate.any {
                (currentStart >= it.timeWorked.wohtStartTime && currentStart < it.timeWorked.wohtEndTime) ||
                        (currentEnd > it.timeWorked.wohtStartTime && currentEnd <= it.timeWorked.wohtEndTime) ||
                        (currentStart <= it.timeWorked.wohtStartTime && currentEnd >= it.timeWorked.wohtEndTime)
            }

            val entry = WorkOrderHistoryTimeWorked(
                nf.generateRandomIdAsLong(),
                history.woHistoryId,
                historyWithDates!!.workDate.workDateId,
                currentStart,
                currentEnd,
                selectedTimeType,
                false,
                df.getCurrentUTCTimeAsString()
            )

            if (isDuplicateStart || isDuplicateEnd || hasOverlap) {
                showOverlapConfirmDialog = entry
            } else {
                coroutineScope.launch {
                    workOrderViewModel.insertWorkOrderHistoryTimeWorked(entry)
                    startTime = endTime.clone() as Calendar
                }
            }
        },
        onDoneClick = {
            if (totalHours > 0.0) {
                showUnsavedDialog = true
            } else {
                navController.popBackStack()
            }
        },
        existingTimes = existingTimes,
        allTimesForDay = allTimesByDate,
        onTimeClick = { combined ->
            mainViewModel.setWorkOrderHistoryTimeWorkedCombined(combined)
            navController.navigate(Screen.WorkOrderHistoryTimeUpdate.route)
        },
        onTimeLongClick = { combined ->
            showTimeOptionsDialog = combined
        },
        errorMessage = errorMessage,
        isStartTimeError = errorMessage == duplicateStartTimeError
    )
}