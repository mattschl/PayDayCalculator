package ms.mattschlenkrich.paycalculator.ui.workorderhistory

import android.app.TimePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable.WorkOrderHistoryTimeUpdateScreen
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun WorkOrderHistoryTimeUpdateRoute(
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

    val combined = mainViewModel.getWorkOrderHistoryTimeWorkedCombined() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    var startTime by remember {
        mutableStateOf(
            df.getCalendarFromTime(combined.timeWorked.wohtStartTime.substringAfter(" "))
        )
    }
    var endTime by remember {
        mutableStateOf(
            df.getCalendarFromTime(combined.timeWorked.wohtEndTime.substringAfter(" "))
        )
    }
    var selectedTimeType by remember { mutableIntStateOf(combined.timeWorked.wohtTimeType) }

    val allTimesByDate by workOrderViewModel.getTimeWorkedPerDay(combined.workDate.workDateId)
        .observeAsState(emptyList())

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(3000.milliseconds)
            errorMessage = null
        }
    }

    val totalHours by remember {
        derivedStateOf {
            df.getTimeWorked(startTime, endTime)
        }
    }

    var showOverlapConfirmDialog by remember { mutableStateOf<WorkOrderHistoryTimeWorked?>(null) }

    if (showOverlapConfirmDialog != null) {
        val entry = showOverlapConfirmDialog!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { },
            title = { androidx.compose.material3.Text(stringResource(R.string.save)) },
            text = { androidx.compose.material3.Text(stringResource(R.string.confirm_overlap)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    coroutineScope.launch {
                        workOrderViewModel.updateWorkOrderHistoryTimeWorked(entry)
                        navController.popBackStack()
                    }
                }) {
                    androidx.compose.material3.Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                }) {
                    androidx.compose.material3.Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    WorkOrderHistoryTimeUpdateScreen(
        infoText = stringResource(R.string.work_order) + " ${combined.workOrderHistory.workOrder.woNumber}\n" +
                combined.workOrderHistory.workOrder.woDescription,
        originalTimeText = stringResource(R.string.original_time) + " " +
                df.get12HourDisplay(combined.timeWorked.wohtStartTime.substringAfter(" ")) +
                " - " +
                df.get12HourDisplay(combined.timeWorked.wohtEndTime.substringAfter(" ")),
        startTime = startTime,
        endTime = endTime,
        totalTimeText = nf.displayNumberFromDouble(totalHours) + " " + stringResource(R.string.hours),
        selectedTimeType = selectedTimeType,
        onTimeTypeChange = { },
        onStartTimeClick = {
            TimePickerDialog(context, { _, h, m ->
                val newStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                }
                startTime = df.roundCalendarTimeDownTo15Minutes(newStart)
                errorMessage = null
            }, startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), false).show()
        },
        onEndTimeClick = {
            TimePickerDialog(context, { _, h, m ->
                val newEnd = df.roundCalendarTimeUpTo15Minutes(
                    Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, m)
                    }
                )

                val hoursBefore = allTimesByDate
                    .filter { it.timeWorked.woHistoryTimeWorkedId != combined.timeWorked.woHistoryTimeWorkedId }
                    .filter { it.timeWorked.wohtTimeType != TimeWorkedTypes.BREAK.value }
                    .sumOf {
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
                    errorMessage = null
                }
            }, endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE), false).show()
        },
        onSaveClick = {
            val currentStart = df.getDateTimeFromDateAndTime(
                combined.workDate.wdDate,
                df.getTimeDisplay(startTime)
            )
            val currentEnd = df.getDateTimeFromDateAndTime(
                combined.workDate.wdDate,
                df.getTimeDisplay(endTime)
            )

            val otherTimes =
                allTimesByDate.filter { it.timeWorked.woHistoryTimeWorkedId != combined.timeWorked.woHistoryTimeWorkedId }
            val isDuplicateStart = otherTimes.any { it.timeWorked.wohtStartTime == currentStart }

            if (isDuplicateStart) {
                errorMessage = duplicateStartTimeError
            } else {
                val hasOverlap = otherTimes.any {
                    (currentStart >= it.timeWorked.wohtStartTime && currentStart < it.timeWorked.wohtEndTime) ||
                            (currentEnd > it.timeWorked.wohtStartTime && currentEnd <= it.timeWorked.wohtEndTime) ||
                            (currentStart <= it.timeWorked.wohtStartTime && currentEnd >= it.timeWorked.wohtEndTime)
                }

                val entry = combined.timeWorked.copy(
                    wohtStartTime = currentStart,
                    wohtEndTime = currentEnd,
                    wohtTimeType = selectedTimeType,
                    wohtUpdateTime = df.getCurrentUTCTimeAsString()
                )

                if (!hasOverlap) {
                    coroutineScope.launch {
                        workOrderViewModel.updateWorkOrderHistoryTimeWorked(entry)
                        navController.popBackStack()
                    }
                }
            }
        },
        allTimesForDay = allTimesByDate.filter { it.timeWorked.wohtHistoryId == combined.timeWorked.wohtHistoryId },
        currentHistoryId = combined.timeWorked.wohtHistoryId,
        onTimeClick = { item ->
            mainViewModel.setWorkOrderHistoryTimeWorkedCombined(item)
            navController.navigate(Screen.WorkOrderHistoryTimeUpdate.route)
        },
        onTimeLongClick = { },
        errorMessage = errorMessage,
        isStartTimeError = errorMessage == duplicateStartTimeError
    )
}