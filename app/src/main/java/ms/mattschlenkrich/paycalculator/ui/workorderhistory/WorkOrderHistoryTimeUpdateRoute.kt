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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable.WorkOrderHistoryTimeUpdateScreen
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
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
    val settings = remember { mainViewModel.loadSettings() }

    val combined = mainViewModel.getWorkOrderHistoryTimeWorkedCombined() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    var startTime by remember {
        mutableStateOf(
            df.getCalendarFromDateTime(combined.timeWorked.wohtStartTime)
        )
    }
    var endTime by remember {
        mutableStateOf(
            df.getCalendarFromDateTime(combined.timeWorked.wohtEndTime)
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

    LaunchedEffect(startTime) {
        val totalWorkedHours = allTimesByDate
            .filter { it.timeWorked.woHistoryTimeWorkedId != combined.timeWorked.woHistoryTimeWorkedId }
            .filter { it.timeWorked.wohtTimeType != TimeWorkedTypes.BREAK.value }
            .sumOf {
                df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime)
            }

        val workDayCalendar = df.getCalendarFromDateTime("${combined.workDate.wdDate} 00:00:00")
        val dayOfWeek = workDayCalendar.get(Calendar.DAY_OF_WEEK)
        val isRegularDay = settings.regularDays.contains(dayOfWeek)

        val regStartCal = df.getCalendarFromTime(settings.regularStartTime)
        val regEndCal = df.getCalendarFromTime(settings.regularEndTime)

        val startTimeMinutes =
            startTime.get(Calendar.HOUR_OF_DAY) * 60 + startTime.get(Calendar.MINUTE)
        val regStartMinutes =
            regStartCal.get(Calendar.HOUR_OF_DAY) * 60 + regStartCal.get(Calendar.MINUTE)
        val regEndMinutes =
            regEndCal.get(Calendar.HOUR_OF_DAY) * 60 + regEndCal.get(Calendar.MINUTE)

        val isWithinRegularTime = if (regStartMinutes < regEndMinutes) {
            startTimeMinutes in regStartMinutes until regEndMinutes
        } else {
            startTimeMinutes >= regStartMinutes || startTimeMinutes < regEndMinutes
        }

        selectedTimeType = when {
            totalWorkedHours >= 12.0 -> TimeWorkedTypes.DBL_OT_HOURS.value
            !isRegularDay || !isWithinRegularTime -> TimeWorkedTypes.OT_HOURS.value
            totalWorkedHours < 8.0 -> TimeWorkedTypes.REG_HOURS.value
            else -> TimeWorkedTypes.OT_HOURS.value
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
        ModalBottomSheet(
            onDismissRequest = { showOverlapConfirmDialog = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(stringResource(R.string.confirm_overlap))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        showOverlapConfirmDialog = null
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = {
                        coroutineScope.launch {
                            workOrderViewModel.updateWorkOrderHistoryTimeWorked(entry)
                            navController.popBackStack()
                        }
                    }) {
                        Text(stringResource(R.string.save))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    WorkOrderHistoryTimeUpdateScreen(
        infoText = "${stringResource(R.string.work_order)} ${combined.workOrderHistory.workOrder.woNumber}\n" +
                combined.workOrderHistory.workOrder.woDescription,
        hoursSummaryText = buildString {
            val workedHours = allTimesByDate
                .filter { it.timeWorked.wohtHistoryId == combined.timeWorked.wohtHistoryId }
                .filter { it.timeWorked.wohtTimeType != TimeWorkedTypes.BREAK.value }
                .sumOf { df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime) }

            val totalWorkedDayHours = allTimesByDate
                .filter { it.timeWorked.wohtTimeType != TimeWorkedTypes.BREAK.value }
                .sumOf { df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime) }

            append("${stringResource(R.string.total_hours)} ${nf.displayNumberFromDouble(workedHours)}")

            if (totalWorkedDayHours > workedHours) {
                append("\nDay Total: ${nf.displayNumberFromDouble(totalWorkedDayHours)}")
            }
        },
        originalTimeText = "${stringResource(R.string.original_time)} " +
                df.get12HourDisplay(combined.timeWorked.wohtStartTime) +
                " - " +
                df.get12HourDisplay(combined.timeWorked.wohtEndTime),
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

                if (roundedEnd.before(startTime)) {
                    roundedEnd.add(Calendar.DAY_OF_YEAR, 1)
                } else if (roundedEnd.timeInMillis - startTime.timeInMillis > 24 * 60 * 60 * 1000) {
                    roundedEnd.add(Calendar.DAY_OF_YEAR, -1)
                }

                val hoursBefore = allTimesByDate
                    .filter { it.timeWorked.woHistoryTimeWorkedId != combined.timeWorked.woHistoryTimeWorkedId }
                    .filter { it.timeWorked.wohtTimeType != TimeWorkedTypes.BREAK.value }
                    .sumOf {
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
                    errorMessage = null
                }
            }
        },
        onSaveClick = {
            val currentStart = df.getDateTimeDisplay(startTime)
            val currentEnd = df.getDateTimeDisplay(endTime)

            val otherTimes =
                allTimesByDate.filter { it.timeWorked.woHistoryTimeWorkedId != combined.timeWorked.woHistoryTimeWorkedId }
            val isDuplicateStart = otherTimes.any { it.timeWorked.wohtStartTime == currentStart }
            val isDuplicateEnd = otherTimes.any { it.timeWorked.wohtEndTime == currentEnd }

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

            if (isDuplicateStart || isDuplicateEnd || hasOverlap) {
                showOverlapConfirmDialog = entry
            } else {
                coroutineScope.launch {
                    workOrderViewModel.updateWorkOrderHistoryTimeWorked(entry)
                    navController.popBackStack()
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