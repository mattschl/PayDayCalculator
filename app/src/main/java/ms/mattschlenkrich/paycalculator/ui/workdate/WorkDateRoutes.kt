package ms.mattschlenkrich.paycalculator.ui.workdate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.common.WAIT_1000
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.data.HolidayPayCalculator
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.WorkDates
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTimeViewModel
import ms.mattschlenkrich.paycalculator.ui.extras.WorkDateExtraScreen
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import kotlin.math.round

@Composable
fun WorkDateAddRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
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
                df.getCurrentUTCTimeAsString()
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
                        WorkDateExtras(
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
                            df.getCurrentUTCTimeAsString()
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
                    navController.navigate(Screen.WorkDateUpdate.route) {
                        popUpTo(Screen.WorkDateAdd.route) { inclusive = true }
                    }
                    navController.navigate(Screen.WorkDateTimes.route)
                }

                Screen.WorkOrderHistoryAdd.route -> {
                    navController.navigate(Screen.WorkDateUpdate.route) {
                        popUpTo(Screen.WorkDateAdd.route) { inclusive = true }
                    }
                    navController.navigate(Screen.WorkOrderHistoryAdd.route)
                }
            }
        }
    }

    var showDateUsedDialog by remember { mutableStateOf(false) }
    var existingWorkDate by remember { mutableStateOf<WorkDates?>(null) }

    if (showDateUsedDialog && existingWorkDate != null) {
        AlertDialog(
            onDismissRequest = { showDateUsedDialog = false },
            title = { Text(stringResource(R.string.this_date_is_already_used)) },
            text = { Text(stringResource(R.string.would_you_like_to_replace_the_old_information_for_this_work_date)) },
            confirmButton = {
                TextButton(onClick = {
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
                                wdUpdateTime = df.getCurrentUTCTimeAsString()
                            )
                        )
                        onSaveWorkDate(Screen.TimeSheet.route)
                    }
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateUsedDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    WorkDateAddScreen(
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
                    HolidayPayCalculator(
                        payDayViewModel, payPeriod.ppEmployerId, curDateString
                    )
                delay(WAIT_1000)
                val stat = round(holidayPayCalculator.getStatHours() * 4) / 4
                statHours = nf.displayNumberFromDouble(stat)
            }
        },
        note = note,
        onNoteChange = { note = it },
        onUpdateTimeClick = {
            onSaveWorkDate(Screen.WorkDateUpdate.route)
            coroutineScope.launch {
                delay(WAIT_250)
                navController.navigate(Screen.WorkDateTimes.route)
            }
        },
        onAddHistoryClick = {
            onSaveWorkDate(Screen.WorkDateUpdate.route)
            coroutineScope.launch {
                delay(WAIT_250)
                navController.navigate(Screen.WorkOrderHistoryAdd.route)
            }
        },
        onSaveClick = {
            val existing = usedWorkDatesList.find { it.wdDate == curDateString }
            if (existing != null) {
                existingWorkDate = existing
                showDateUsedDialog = true
            } else {
                onSaveWorkDate(Screen.WorkDateUpdate.route)
            }
        },
        extras = extras,
        selectedExtras = selectedExtras.toSet(),
        onExtraToggle = { extra, selected ->
            if (selected) {
                if (!selectedExtras.contains(extra.workExtraTypeId)) {
                    selectedExtras.add(extra.workExtraTypeId)
                    coroutineScope.launch {
                        val existing = usedWorkDatesList.find { it.wdDate == curDateString }
                        val currentWorkDate = if (existing != null) {
                            existing
                        } else {
                            val newWorkDate = WorkDates(
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
                                df.getCurrentUTCTimeAsString()
                            )
                            payDayViewModel.insertWorkDate(newWorkDate)
                            newWorkDate
                        }
                        mainViewModel.setWorkDateObject(currentWorkDate)

                        val extraTypeAndDef = workExtraViewModel.getExtraTypeAndDefByTypeIdSync(
                            extra.workExtraTypeId, payPeriod.ppCutoffDate
                        )
                        if (extraTypeAndDef != null) {
                            payDayViewModel.insertWorkDateExtra(
                                WorkDateExtras(
                                    nf.generateRandomIdAsLong(),
                                    currentWorkDate.workDateId,
                                    extraTypeAndDef.extraType.workExtraTypeId,
                                    extraTypeAndDef.extraType.wetName,
                                    extraTypeAndDef.extraType.wetAppliesTo,
                                    extraTypeAndDef.extraType.wetAttachTo,
                                    extraTypeAndDef.definition.weValue,
                                    extraTypeAndDef.definition.weIsFixed,
                                    extraTypeAndDef.extraType.wetIsCredit,
                                    false,
                                    df.getCurrentUTCTimeAsString()
                                )
                            )
                        }
                        navController.navigate(Screen.WorkDateUpdate.route) {
                            popUpTo(Screen.WorkDateAdd.route) { inclusive = true }
                        }
                    }
                }
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
    navController: NavController
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
    var regHours by remember { mutableStateOf(nf.displayNumberFromDouble(currentWorkDate.wdRegHours)) }
    var otHours by remember { mutableStateOf(nf.displayNumberFromDouble(currentWorkDate.wdOtHours)) }
    var dblOtHours by remember { mutableStateOf(nf.displayNumberFromDouble(currentWorkDate.wdDblOtHours)) }
    var statHours by remember { mutableStateOf(nf.displayNumberFromDouble(currentWorkDate.wdStatHours)) }
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
                    WorkDateExtras(
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
                        df.getCurrentUTCTimeAsString()
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
                append(nf.displayNumberFromDouble(historyRegHours))
            }
            if (historyOtHours != 0.0) {
                if (isNotEmpty()) append(context.getString(R.string.pipe))
                append(context.getString(R.string.ot_))
                append(nf.displayNumberFromDouble(historyOtHours))
            }
            if (historyDblOtHours != 0.0) {
                if (isNotEmpty()) append(context.getString(R.string.pipe))
                append(context.getString(R.string.dbl_ot_))
                append(nf.displayNumberFromDouble(historyDblOtHours))
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
                wdUpdateTime = df.getCurrentUTCTimeAsString()
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
        mutableStateOf<WorkOrderHistoryWithDates?>(
            null
        )
    }
    var showDeleteHistoryConfirmDialog by remember {
        mutableStateOf<WorkOrderHistoryWithDates?>(
            null
        )
    }
    var showExtraOptionsDialog by remember {
        mutableStateOf<WorkDateExtras?>(
            null
        )
    }
    var showDeleteExtraConfirmDialog by remember {
        mutableStateOf<WorkDateExtras?>(
            null
        )
    }

    if (showReplaceDateDialog) {
        AlertDialog(
            onDismissRequest = { showReplaceDateDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateWorkDate(Screen.TimeSheet.route)
                    showReplaceDateDialog = false
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReplaceDateDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            },
            title = { Text(stringResource(R.string.this_date_is_already_used)) },
            text = { Text(stringResource(R.string.would_you_like_to_replace_the_old_information_for_this_work_date)) }
        )
    }

    if (showHistoryOptionsDialog != null) {
        val history = showHistoryOptionsDialog!!
        AlertDialog(
            onDismissRequest = { showHistoryOptionsDialog = null },
            confirmButton = {
                TextButton(onClick = {
                    mainViewModel.setWorkOrderHistory(history.history)
                    showHistoryOptionsDialog = null
                    navController.navigate(Screen.WorkOrderHistoryUpdate.route)
                }) {
                    Text(stringResource(R.string.open_caps))
                }
            },
            dismissButton = {
                TextButton(onClick = {
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
        AlertDialog(
            onDismissRequest = { showDeleteHistoryConfirmDialog = null },
            confirmButton = {
                TextButton(onClick = {
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
                TextButton(onClick = {
                    showDeleteHistoryConfirmDialog = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_wo) + history.workOrder.woNumber) },
            text = { Text(stringResource(R.string.this_cannot_be_undone)) }
        )
    }

    if (showExtraOptionsDialog != null) {
        val extra = showExtraOptionsDialog!!
        AlertDialog(
            onDismissRequest = { showExtraOptionsDialog = null },
            confirmButton = {
                TextButton(onClick = {
                    mainViewModel.setWorkDateExtra(extra)
                    mainViewModel.setWorkDateExtraList(displayExtras.toCollection(ArrayList()))
                    showExtraOptionsDialog = null
                    navController.navigate(Screen.WorkDateExtraUpdate.route)
                }) {
                    Text(stringResource(R.string.modify))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteExtraConfirmDialog = extra
                    showExtraOptionsDialog = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            title = { Text(stringResource(R.string.extra_options)) },
            text = { Text(extra.wdeName) }
        )
    }

    if (showDeleteExtraConfirmDialog != null) {
        val extra = showDeleteExtraConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteExtraConfirmDialog = null },
            confirmButton = {
                TextButton(onClick = {
                    payDayViewModel.deleteWorkDateExtra(
                        extra.wdeName, extra.wdeWorkDateId, df.getCurrentUTCTimeAsString()
                    )
                    showDeleteExtraConfirmDialog = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteExtraConfirmDialog = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_) + extra.wdeName) },
            text = { Text(stringResource(R.string.this_cannot_be_undone)) }
        )
    }

    WorkDateUpdateScreen(
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
                    HolidayPayCalculator(
                        payDayViewModel, currentWorkDate.wdEmployerId, curDateString
                    )
                delay(WAIT_1000)
                val stat = round(holidayPayCalculator.getStatHours() * 4) / 4
                statHours = nf.displayNumberFromDouble(stat)
            }
        },
        note = note,
        onNoteChange = { note = it },
        onUpdateTimeClick = { onUpdateWorkDate(Screen.WorkDateTimes.route) },
        onAddHistoryClick = { onUpdateWorkDate(Screen.WorkOrderHistoryAdd.route) },
        onTransferClick = {
            regHours = nf.displayNumberFromDouble(historyRegHours)
            otHours = nf.displayNumberFromDouble(historyOtHours)
            dblOtHours = nf.displayNumberFromDouble(historyDblOtHours)
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
                    extra.wdeName, extra.wdeWorkDateId, df.getCurrentUTCTimeAsString()
                )
            } else {
                if (extra.workDateExtraId != 0L) {
                    payDayViewModel.updateWorkDateExtra(
                        extra.copy(
                            wdeIsDeleted = false,
                            wdeUpdateTime = df.getCurrentUTCTimeAsString()
                        )
                    )
                } else {
                    payDayViewModel.insertWorkDateExtra(
                        extra.copy(
                            workDateExtraId = nf.generateRandomIdAsLong(),
                            wdeIsDeleted = false,
                            wdeUpdateTime = df.getCurrentUTCTimeAsString()
                        )
                    )
                }
            }
        },
        onExtraLongClick = { extra ->
            showExtraOptionsDialog = extra
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
                workOrderError = context.getString(R.string.work_order_number_cannot_be_blank)
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
                workOrderError = context.getString(R.string.work_order_number_cannot_be_blank)
                return@WorkDateTimesScreen
            }
            if (history == null) {
                workOrderError = context.getString(R.string.work_order_not_found_please_create_it)
                return@WorkDateTimesScreen
            }

            val startTimeString = df.getDateTimeFromDateAndTime(
                workDate.wdDate,
                df.getTimeDisplay(startTime)
            )

            if (!isOverlapOverride && allTimesByDate.any { it.timeWorked.wohtEndTime > startTimeString }) {
                errorMessage =
                    context.getString(R.string.warning_start_time_overlaps_previous_end_time)
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

@Composable
fun WorkDateExtraAddRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
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
    navController: NavController
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
                    wdeUpdateTime = DateFunctions().getCurrentUTCTimeAsString()
                )
            )
            navController.popBackStack()
        },
        onCancel = { navController.popBackStack() }
    )
}