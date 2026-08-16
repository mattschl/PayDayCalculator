package ms.mattschlenkrich.paycalculator.ui.workdate

import android.app.TimePickerDialog
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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTimeViewModel
import ms.mattschlenkrich.paycalculator.ui.workdate.composable.WorkDateTimesScreen
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
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
    val settings = remember { mainViewModel.loadSettings() }

    var history by rememberSaveable { mutableStateOf(mainViewModel.getWorkOrderHistory()) }
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
        rememberSaveable { mutableStateOf(null) }
    }

    var workOrderNumber by rememberSaveable {
        mutableStateOf(
            historyCombined?.workOrder?.woNumber ?: ""
        )
    }

    val employer = mainViewModel.getEmployer() ?: return
    val workOrderSuggestions by remember(employer.employerId) {
        workTimeViewModel.getWorkOrderNumbers(
            employer.employerId
        )
    }.observeAsState(emptyList())

    val existingTimes by if (history != null) {
        remember(history!!.woHistoryId) {
            workOrderViewModel.getTimeWorkedForWorkOrderHistory(history!!.woHistoryId)
        }.observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val allTimesByDate by remember(workDate.workDateId) {
        workTimeViewModel.getTimesWorkedByDate(workDate.workDateId)
    }.observeAsState(emptyList())

    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val duplicateStartTimeError = stringResource(R.string.error_start_time_already_used)

    var selectedTimeType by remember { mutableIntStateOf(0) }

    val woBlankError = stringResource(R.string.work_order_number_cannot_be_blank)
    val woNotFoundError = stringResource(R.string.work_order_not_found_please_create_it)

    var startTime by remember(allTimesByDate) {
        if (allTimesByDate.isNotEmpty()) {
            val latestTime = allTimesByDate.maxOf { it.timeWorked.wohtEndTime }
            mutableStateOf(df.getCalendarFromDateTime(latestTime))
        } else {
            mutableStateOf(df.getCalendarFromDateAndTime(workDate.wdDate, "08:30"))
        }
    }
    var endTime by remember(startTime) {
        val now = df.roundCalendarTimeUpTo15Minutes(Calendar.getInstance())
        val end = if (now.after(startTime)) {
            now
        } else {
            startTime.clone() as Calendar
        }
        mutableStateOf(end)
    }

    LaunchedEffect(allTimesByDate, startTime) {
        val totalWorkedHours = allTimesByDate
            .filter { it.timeWorked.wohtTimeType != TimeWorkedTypes.BREAK.value }
            .sumOf {
                df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime)
            }

        val workDayCalendar = df.getCalendarFromDateTime("${workDate.wdDate} 00:00:00")
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
            // Over midnight schedule
            startTimeMinutes >= regStartMinutes || startTimeMinutes < regEndMinutes
        }

        selectedTimeType = when {
            totalWorkedHours >= 12.0 -> TimeWorkedTypes.DBL_OT_HOURS.value
            !isRegularDay || !isWithinRegularTime -> TimeWorkedTypes.OT_HOURS.value
            totalWorkedHours < 8.0 -> TimeWorkedTypes.REG_HOURS.value
            else -> TimeWorkedTypes.OT_HOURS.value
        }
    }

    val isWorkOrderValid = workOrderSuggestions.any { it.woNumber == workOrderNumber }
    var workOrderError by rememberSaveable { mutableStateOf<String?>(null) }

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
                    TextButton(onClick = { showOverlapConfirmDialog = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = {
                        coroutineScope.launch {
                            workOrderViewModel.insertWorkOrderHistoryTimeWorked(entry)
                            startTime = endTime.clone() as Calendar
                            showOverlapConfirmDialog = null
                        }
                    }) {
                        Text(stringResource(R.string.save))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showTimeOptionsDialog != null) {
        val combinedItem = showTimeOptionsDialog!!
        ModalBottomSheet(
            onDismissRequest = { showTimeOptionsDialog = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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

    if (showDeleteConfirmDialog != null) {
        val combinedItem = showDeleteConfirmDialog!!
        ModalBottomSheet(
            onDismissRequest = { showDeleteConfirmDialog = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.delete_time_entry),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(stringResource(R.string.this_cannot_be_undone))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDeleteConfirmDialog = null }) {
                        Text(stringResource(R.string.cancel))
                    }
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
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
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
            LocalLocale.current.platformLocale,
            "%.2f",
            df.getTimeWorked(startTime, endTime)
        ),
        selectedTimeType = selectedTimeType,
        onTimeTypeChange = { selectedTimeType = it },
        onStartTimeClick = {
            TimePickerDialog(context, { _, h, m ->
                val newStart = (startTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                }
                startTime = df.roundCalendarTimeDownTo15Minutes(newStart)
                errorMessage = null
            }, startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), false).show()
        },
        onEndTimeClick = {
            TimePickerDialog(context, { _, h, m ->
                var newEnd = (endTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                }
                newEnd = df.roundCalendarTimeUpTo15Minutes(newEnd)
                if (newEnd.before(startTime)) {
                    newEnd.add(Calendar.DAY_OF_YEAR, 1)
                } else if (newEnd.timeInMillis - startTime.timeInMillis > 24 * 60 * 60 * 1000) {
                    newEnd.add(Calendar.DAY_OF_YEAR, -1)
                }
                endTime = newEnd
                errorMessage = null
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

            val currentStart = df.getDateTimeDisplay(startTime)
            val currentEnd = df.getDateTimeDisplay(endTime)

            val isDuplicateStart =
                allTimesByDate.any { it.timeWorked.wohtStartTime == currentStart }

            if (isDuplicateStart) {
                errorMessage = duplicateStartTimeError
            } else {
                val hasOverlap = allTimesByDate.any {
                    (currentStart >= it.timeWorked.wohtStartTime && currentStart < it.timeWorked.wohtEndTime) ||
                            (currentEnd > it.timeWorked.wohtStartTime && currentEnd <= it.timeWorked.wohtEndTime) ||
                            (currentStart <= it.timeWorked.wohtStartTime && currentEnd >= it.timeWorked.wohtEndTime)
                }

                val entry = WorkOrderHistoryTimeWorked(
                    nf.generateRandomIdAsLong(),
                    history!!.woHistoryId,
                    workDate.workDateId,
                    currentStart,
                    currentEnd,
                    selectedTimeType,
                    false,
                    df.getCurrentUTCTimeAsString()
                )

                if (hasOverlap) {
                    showOverlapConfirmDialog = entry
                } else {
                    coroutineScope.launch {
                        workOrderViewModel.insertWorkOrderHistoryTimeWorked(entry)
                        startTime = endTime.clone() as Calendar
                    }
                }
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
        errorMessage = errorMessage,
        isStartTimeError = errorMessage == duplicateStartTimeError
    )
}