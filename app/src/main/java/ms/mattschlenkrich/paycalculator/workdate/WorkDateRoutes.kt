package ms.mattschlenkrich.paycalculator.workdate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_1000
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.WorkDates
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTimeViewModel
import ms.mattschlenkrich.paycalculator.extras.WorkDateExtraScreen
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import kotlin.math.round

@Composable
fun WorkDateAddRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: androidx.navigation.NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }

    val payPeriod = mainViewModel.getPayPeriod() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    var curDateString by remember { mutableStateOf("") }
    var regHours by remember { mutableStateOf("") }
    var otHours by remember { mutableStateOf("") }
    var dblOtHours by remember { mutableStateOf("") }
    var statHours by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val usedWorkDatesList by payDayViewModel.getWorkDateListUsed(
        payPeriod.ppEmployerId, payPeriod.ppCutoffDate
    ).observeAsState(emptyList())

    val extras by workExtraViewModel.getExtraTypesByDaily(payPeriod.ppEmployerId)
        .observeAsState(emptyList())

    val selectedExtras = remember { mutableStateListOf<Long>() }

    // Initial date calculation
    LaunchedEffect(usedWorkDatesList) {
        if (curDateString.isEmpty()) {
            var date = LocalDate.now().toString()
            val existingDates = usedWorkDatesList.filter { !it.wdIsDeleted }.map { it.wdDate }
            while (existingDates.contains(date)) {
                date = LocalDate.parse(date).plusDays(1L).toString()
            }
            curDateString = date
        }
    }

    // Initial extras selection
    LaunchedEffect(extras) {
        extras.forEach {
            if (it.wetIsDefault && !selectedExtras.contains(it.workExtraTypeId)) {
                selectedExtras.add(it.workExtraTypeId)
            }
        }
    }

    val onSaveWorkDate = { fragmentToGoTo: String ->
        coroutineScope.launch {
            val workDate = WorkDates(
                nf.generateRandomIdAsLong(),
                payPeriod.payPeriodId,
                payPeriod.ppEmployerId,
                payPeriod.ppCutoffDate,
                curDateString,
                regHours.toDoubleOrNull() ?: 0.0,
                otHours.toDoubleOrNull() ?: 0.0,
                dblOtHours.toDoubleOrNull() ?: 0.0,
                statHours.toDoubleOrNull() ?: 0.0,
                note.ifBlank { null },
                false,
                df.getCurrentTimeAsString()
            )
            payDayViewModel.insertWorkDate(workDate)
            mainViewModel.setWorkDateObject(workDate)
            delay(WAIT_250)

            // Save selected extras
            selectedExtras.forEach { typeId ->
                val extraTypeAndDef = workExtraViewModel.getExtraTypeAndDefByTypeIdSync(
                    typeId, payPeriod.ppCutoffDate
                )
                if (extraTypeAndDef != null) {
                    payDayViewModel.insertWorkDateExtra(
                        ms.mattschlenkrich.paycalculator.data.WorkDateExtras(
                            nf.generateRandomIdAsLong(),
                            workDate.workDateId,
                            extraTypeAndDef.extraType.workExtraTypeId,
                            extraTypeAndDef.extraType.wetName,
                            extraTypeAndDef.extraType.wetAppliesTo,
                            extraTypeAndDef.extraType.wetAttachTo,
                            extraTypeAndDef.definition.weValue,
                            extraTypeAndDef.definition.weIsFixed,
                            extraTypeAndDef.extraType.wetIsCredit,
                            false,
                            df.getCurrentTimeAsString()
                        )
                    )
                }
            }

            when (fragmentToGoTo) {
                Screen.TimeSheet.route -> navController.popBackStack()
                Screen.WorkDateUpdate.route -> {
                    navController.navigate(Screen.WorkDateUpdate.route) {
                        popUpTo(Screen.WorkDateAdd.route) { inclusive = true }
                    }
                }

                Screen.WorkDateTimes.route -> {
                    navController.navigate(Screen.WorkDateTimes.route)
                }

                Screen.WorkOrderHistoryAdd.route -> {
                    navController.navigate(Screen.WorkOrderHistoryAdd.route)
                }
            }
        }
    }

    var showDateUsedDialog by remember { mutableStateOf(false) }
    var existingWorkDate by remember { mutableStateOf<WorkDates?>(null) }

    if (showDateUsedDialog && existingWorkDate != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDateUsedDialog = false },
            title = { Text(stringResource(R.string.this_date_is_already_used)) },
            text = { Text(stringResource(R.string.would_you_like_to_replace_the_old_information_for_this_work_date)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDateUsedDialog = false
                    coroutineScope.launch {
                        payDayViewModel.updateWorkDate(
                            existingWorkDate!!.copy(
                                wdRegHours = regHours.toDoubleOrNull() ?: 0.0,
                                wdOtHours = otHours.toDoubleOrNull() ?: 0.0,
                                wdDblOtHours = dblOtHours.toDoubleOrNull() ?: 0.0,
                                wdStatHours = statHours.toDoubleOrNull() ?: 0.0,
                                wdNote = note.ifBlank { null },
                                wdIsDeleted = false,
                                wdUpdateTime = df.getCurrentTimeAsString()
                            )
                        )
                        onSaveWorkDate(Screen.TimeSheet.route)
                    }
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDateUsedDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    ms.mattschlenkrich.paycalculator.workdate.WorkDateAddScreen(
        dateText = if (curDateString.isNotEmpty()) df.getDisplayDate(curDateString) else "",
        onDateClick = {
            val curDateAll = (if (curDateString.isEmpty()) LocalDate.now()
                .toString() else curDateString).split("-")
            DatePickerDialog(
                context, { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    curDateString = "$year-${
                        month.toString().padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
            ).show()
        },
        regHours = regHours,
        onRegHoursChange = { regHours = it },
        otHours = otHours,
        onOtHoursChange = { otHours = it },
        dblOtHours = dblOtHours,
        onDblOtHoursChange = { dblOtHours = it },
        statHours = statHours,
        onStatHoursChange = { statHours = it },
        onStatHoursLongClick = {
            coroutineScope.launch {
                val holidayPayCalculator =
                    ms.mattschlenkrich.paycalculator.data.HolidayPayCalculator(
                        payDayViewModel, payPeriod.ppEmployerId, curDateString
                    )
                delay(WAIT_1000)
                val stat = round(holidayPayCalculator.getStatHours() * 4) / 4
                statHours = nf.getNumberFromDouble(stat)
            }
        },
        note = note,
        onNoteChange = { note = it },
        onUpdateTimeClick = { onSaveWorkDate(Screen.WorkDateTimes.route) },
        onAddHistoryClick = { onSaveWorkDate(Screen.WorkOrderHistoryAdd.route) },
        onSaveClick = {
            val existing = usedWorkDatesList.find { it.wdDate == curDateString }
            if (existing != null) {
                existingWorkDate = existing
                showDateUsedDialog = true
            } else {
                onSaveWorkDate(Screen.TimeSheet.route)
            }
        },
        extras = extras,
        selectedExtras = selectedExtras.toSet(),
        onExtraToggle = { extra, selected ->
            if (selected) {
                if (!selectedExtras.contains(extra.workExtraTypeId)) selectedExtras.add(extra.workExtraTypeId)
            } else {
                selectedExtras.remove(extra.workExtraTypeId)
            }
        },
        onAddExtraClick = { /* Navigate to Extra Add */ },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun WorkDateUpdateRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }

    val currentWorkDate = mainViewModel.getWorkDateObject() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    var curDateString by remember { mutableStateOf(currentWorkDate.wdDate) }
    var regHours by remember { mutableStateOf(nf.getNumberFromDouble(currentWorkDate.wdRegHours)) }
    var otHours by remember { mutableStateOf(nf.getNumberFromDouble(currentWorkDate.wdOtHours)) }
    var dblOtHours by remember { mutableStateOf(nf.getNumberFromDouble(currentWorkDate.wdDblOtHours)) }
    var statHours by remember { mutableStateOf(nf.getNumberFromDouble(currentWorkDate.wdStatHours)) }
    var note by remember { mutableStateOf(currentWorkDate.wdNote ?: "") }

    val usedWorkDatesList by payDayViewModel.getWorkDateList(
        currentWorkDate.wdEmployerId, currentWorkDate.wdCutoffDate
    ).observeAsState(emptyList())

    val histories by workOrderViewModel.getWorkOrderHistoriesByDate(
        currentWorkDate.workDateId
    ).observeAsState(emptyList())

    val currentExtras by payDayViewModel.getWorkDateExtras(currentWorkDate.workDateId)
        .observeAsState(emptyList())

    val allPossibleExtras by workExtraViewModel.getExtraTypesAndDefByDaily(
        currentWorkDate.wdEmployerId, currentWorkDate.wdCutoffDate
    ).observeAsState(emptyList())

    val displayExtras = remember(currentExtras, allPossibleExtras) {
        val list = currentExtras.toMutableList()
        allPossibleExtras.forEach { typeDef ->
            if (!list.any { it.wdeName == typeDef.extraType.wetName }) {
                list.add(
                    ms.mattschlenkrich.paycalculator.data.WorkDateExtras(
                        0,
                        currentWorkDate.workDateId,
                        null,
                        typeDef.extraType.wetName,
                        typeDef.extraType.wetAppliesTo,
                        typeDef.extraType.wetAttachTo,
                        typeDef.definition.weValue,
                        typeDef.definition.weIsFixed,
                        typeDef.extraType.wetIsCredit,
                        true,
                        df.getCurrentTimeAsString()
                    )
                )
            }
        }
        list.sortedBy { it.wdeName }
    }

    var historyRegHours by remember { mutableDoubleStateOf(0.0) }
    var historyOtHours by remember { mutableDoubleStateOf(0.0) }
    var historyDblOtHours by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(histories) {
        historyRegHours = histories.sumOf { it.history.woHistoryRegHours }
        historyOtHours = histories.sumOf { it.history.woHistoryOtHours }
        historyDblOtHours = histories.sumOf { it.history.woHistoryDblOtHours }
    }

    val workOrderSummary = remember(historyRegHours, historyOtHours, historyDblOtHours) {
        buildString {
            if (historyRegHours != 0.0) {
                append(context.getString(R.string.reg_))
                append(nf.getNumberFromDouble(historyRegHours))
            }
            if (historyOtHours != 0.0) {
                if (isNotEmpty()) append(context.getString(R.string.pipe))
                append(context.getString(R.string.ot_))
                append(nf.getNumberFromDouble(historyOtHours))
            }
            if (historyDblOtHours != 0.0) {
                if (isNotEmpty()) append(context.getString(R.string.pipe))
                append(context.getString(R.string.dbl_ot_))
                append(nf.getNumberFromDouble(historyDblOtHours))
            }
        }
    }

    val onUpdateWorkDate = { fragmentToGoTo: String ->
        coroutineScope.launch {
            val updated = currentWorkDate.copy(
                wdDate = curDateString,
                wdRegHours = regHours.toDoubleOrNull() ?: 0.0,
                wdOtHours = otHours.toDoubleOrNull() ?: 0.0,
                wdDblOtHours = dblOtHours.toDoubleOrNull() ?: 0.0,
                wdStatHours = statHours.toDoubleOrNull() ?: 0.0,
                wdNote = note.ifBlank { null },
                wdIsDeleted = false,
                wdUpdateTime = df.getCurrentTimeAsString()
            )
            payDayViewModel.updateWorkDate(updated)
            mainViewModel.setWorkDateObject(updated)

            when (fragmentToGoTo) {
                Screen.TimeSheet.route -> navController.popBackStack()
                Screen.WorkDateTimes.route -> {
                    navController.navigate(Screen.WorkDateTimes.route)
                }

                Screen.WorkOrderHistoryAdd.route -> {
                    navController.navigate(Screen.WorkOrderHistoryAdd.route)
                }
            }
        }
    }

    var showReplaceDateDialog by remember { mutableStateOf(false) }
    var showHistoryOptionsDialog by remember {
        mutableStateOf<ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWithDates?>(
            null
        )
    }
    var showDeleteHistoryConfirmDialog by remember {
        mutableStateOf<ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWithDates?>(
            null
        )
    }

    if (showReplaceDateDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showReplaceDateDialog = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    onUpdateWorkDate(Screen.TimeSheet.route)
                    showReplaceDateDialog = false
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showReplaceDateDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            },
            title = { Text(stringResource(R.string.this_date_is_already_used)) },
            text = { Text(stringResource(R.string.would_you_like_to_replace_the_old_information_for_this_work_date)) }
        )
    }

    if (showHistoryOptionsDialog != null) {
        val history = showHistoryOptionsDialog!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showHistoryOptionsDialog = null },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    mainViewModel.setWorkOrderHistory(history.history)
                    showHistoryOptionsDialog = null
                    navController.navigate(Screen.WorkOrderHistoryUpdate.route)
                }) {
                    Text(stringResource(R.string.open_caps))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDeleteHistoryConfirmDialog = history
                    showHistoryOptionsDialog = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            title = {
                Text(
                    stringResource(R.string.choose_option_for_wo) + history.workOrder.woNumber +
                            stringResource(R.string._on_) + df.getDisplayDate(history.workDate.wdDate)
                )
            }
        )
    }

    if (showDeleteHistoryConfirmDialog != null) {
        val history = showDeleteHistoryConfirmDialog!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteHistoryConfirmDialog = null },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    coroutineScope.launch {
                        workOrderViewModel.removeAllWorkPerformedFromWorkOderHistory(history.history.woHistoryId)
                        workOrderViewModel.removeAllMaterialsFromWorkOrderHistory(history.history.woHistoryId)
                        delay(WAIT_500)
                        workOrderViewModel.deleteWorkOrderHistory(history.history.woHistoryId)
                    }
                    showDeleteHistoryConfirmDialog = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDeleteHistoryConfirmDialog = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_wo) + history.workOrder.woNumber) },
            text = { Text(stringResource(R.string.this_cannot_be_undone)) }
        )
    }

    ms.mattschlenkrich.paycalculator.workdate.WorkDateUpdateScreen(
        dateText = df.getDisplayDate(curDateString),
        onDateClick = {
            val curDateAll = curDateString.split("-")
            DatePickerDialog(
                context, { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    curDateString = "$year-${
                        month.toString().padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
            ).show()
        },
        regHours = regHours,
        onRegHoursChange = { regHours = it },
        otHours = otHours,
        onOtHoursChange = { otHours = it },
        dblOtHours = dblOtHours,
        onDblOtHoursChange = { dblOtHours = it },
        statHours = statHours,
        onStatHoursChange = { statHours = it },
        onStatHoursLongClick = {
            coroutineScope.launch {
                val holidayPayCalculator =
                    ms.mattschlenkrich.paycalculator.data.HolidayPayCalculator(
                        payDayViewModel, currentWorkDate.wdEmployerId, curDateString
                    )
                delay(WAIT_1000)
                val stat = round(holidayPayCalculator.getStatHours() * 4) / 4
                statHours = nf.getNumberFromDouble(stat)
            }
        },
        note = note,
        onNoteChange = { note = it },
        onUpdateTimeClick = { onUpdateWorkDate(Screen.WorkDateTimes.route) },
        onAddHistoryClick = { onUpdateWorkDate(Screen.WorkOrderHistoryAdd.route) },
        onTransferClick = {
            regHours = nf.getNumberFromDouble(historyRegHours)
            otHours = nf.getNumberFromDouble(historyOtHours)
            dblOtHours = nf.getNumberFromDouble(historyDblOtHours)
        },
        onDoneClick = {
            if (curDateString != currentWorkDate.wdDate && usedWorkDatesList.any { it.wdDate == curDateString }) {
                showReplaceDateDialog = true
            } else {
                onUpdateWorkDate(Screen.TimeSheet.route)
            }
        },
        histories = histories,
        onHistoryClick = { history ->
            mainViewModel.setWorkOrderHistory(history.history)
            navController.navigate(Screen.WorkOrderHistoryUpdate.route)
        },
        onHistoryLongClick = { history ->
            showHistoryOptionsDialog = history
        },
        workOrderSummary = if (historyRegHours > (regHours.toDoubleOrNull() ?: 0.0) ||
            historyOtHours > (otHours.toDoubleOrNull() ?: 0.0) ||
            historyDblOtHours > (dblOtHours.toDoubleOrNull() ?: 0.0)
        ) workOrderSummary else "",
        extras = displayExtras,
        onExtraClick = { extra ->
            if (!extra.wdeIsDeleted) {
                payDayViewModel.deleteWorkDateExtra(
                    extra.wdeName, extra.wdeWorkDateId, extra.wdeUpdateTime
                )
            } else {
                if (extra.workDateExtraId != 0L) {
                    payDayViewModel.updateWorkDateExtra(
                        extra.copy(
                            wdeIsDeleted = false,
                            wdeUpdateTime = df.getCurrentTimeAsString()
                        )
                    )
                } else {
                    payDayViewModel.insertWorkDateExtra(
                        extra.copy(
                            workDateExtraId = nf.generateRandomIdAsLong(),
                            wdeIsDeleted = false,
                            wdeUpdateTime = df.getCurrentTimeAsString()
                        )
                    )
                }
            }
        },
        onExtraEditClick = { extra ->
            mainViewModel.setWorkDateExtra(extra)
            mainViewModel.setWorkDateExtraList(displayExtras.toCollection(ArrayList()))
            navController.navigate(Screen.WorkDateExtraUpdate.route)
        },
        onAddExtraClick = {
            mainViewModel.setWorkDateObject(currentWorkDate)
            navController.navigate(Screen.WorkDateExtraAdd.route)
        }
    )
}

@Composable
fun WorkDateTimesRoute(
    mainViewModel: MainViewModel,
    workTimeViewModel: WorkTimeViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val history = mainViewModel.getWorkOrderHistory() ?: return
    val historyCombined by workOrderViewModel.getWorkOrderHistoryCombined(history.woHistoryId)
        .observeAsState()

    if (historyCombined == null) return

    val workDate = historyCombined!!.workDate
    val workOrder = historyCombined!!.workOrder

    var workOrderNumber by remember { mutableStateOf(workOrder.woNumber) }
    val workOrderSuggestions by workTimeViewModel.getWorkOrderNumbers(
        mainViewModel.getEmployer()!!.employerId
    ).observeAsState(emptyList())

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var startTime by remember { mutableStateOf(Calendar.getInstance()) }
    var endTime by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTimeType by remember { mutableIntStateOf(0) }

    val existingTimes by workOrderViewModel.getTimeWorkedForWorkOrderHistory(history.woHistoryId)
        .observeAsState(emptyList())

    WorkDateTimesScreen(
        infoText = df.getDisplayDate(workDate.wdDate),
        hoursSummaryText = "${nf.getNumberFromDouble(history.woHistoryRegHours)} Reg | " +
                "${nf.getNumberFromDouble(history.woHistoryOtHours)} OT | " +
                "${nf.getNumberFromDouble(history.woHistoryDblOtHours)} Dbl",
        workOrderNumber = workOrderNumber,
        onWorkOrderNumberChange = { workOrderNumber = it },
        workOrderSuggestions = workOrderSuggestions.map { it.woNumber },
        workOrderButtonText = stringResource(R.string.update_work_order),
        onWorkOrderButtonClick = { /* TODO */ },
        workOrderInfoText = workOrder.woDescription,
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
                val newStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                }
                startTime = newStart
            }, startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), false).show()
        },
        onEndTimeClick = {
            TimePickerDialog(context, { _, h, m ->
                val newEnd = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                }
                endTime = newEnd
            }, endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE), false).show()
        },
        onEnterTimeClick = {
            coroutineScope.launch {
                workOrderViewModel.insertTimeWorked(
                    ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorked(
                        nf.generateRandomIdAsLong(),
                        history.woHistoryId,
                        workDate.workDateId,
                        timeFormat.format(startTime.time),
                        timeFormat.format(endTime.time),
                        selectedTimeType,
                        false,
                        df.getCurrentTimeAsString()
                    )
                )
            }
        },
        onDoneClick = { navController.popBackStack() },
        existingTimes = existingTimes,
        onTimeClick = { item ->
            coroutineScope.launch {
                workOrderViewModel.deleteTimeWorked(
                    item.timeWorked.woHistoryTimeWorkedId,
                    df.getCurrentTimeAsString()
                )
            }
        }
    )
}

@Composable
fun WorkDateExtraAddRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: androidx.navigation.NavController
) {
    val workDate = mainViewModel.getWorkDateObject() ?: return
    val employer = mainViewModel.getEmployer() ?: return

    val existingExtras by payDayViewModel.getWorkDateExtras(workDate.workDateId)
        .observeAsState(emptyList())

    WorkDateExtraScreen(
        initialWorkDate = workDate,
        employerName = employer.employerName,
        initialExtra = null,
        existingExtras = existingExtras,
        onUpdate = { extra ->
            payDayViewModel.insertWorkDateExtra(extra)
            navController.popBackStack()
        },
        onDelete = {},
        onCancel = { navController.popBackStack() }
    )
}

@Composable
fun WorkDateExtraUpdateRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: androidx.navigation.NavController
) {
    val workDate = mainViewModel.getWorkDateObject() ?: return
    val initialExtra = mainViewModel.getWorkDateExtra() ?: return
    val employer = mainViewModel.getEmployer() ?: return

    val existingExtras by payDayViewModel.getWorkDateExtras(workDate.workDateId)
        .observeAsState(emptyList())

    WorkDateExtraScreen(
        initialWorkDate = workDate,
        employerName = employer.employerName,
        initialExtra = initialExtra,
        existingExtras = existingExtras,
        onUpdate = { extra ->
            payDayViewModel.updateWorkDateExtra(extra)
            navController.popBackStack()
        },
        onDelete = { extra ->
            payDayViewModel.updateWorkDateExtra(
                (extra as WorkDateExtras).copy(
                    wdeIsDeleted = true,
                    wdeUpdateTime = DateFunctions().getCurrentTimeAsString()
                )
            )
            navController.popBackStack()
        },
        onCancel = { navController.popBackStack() }
    )
}