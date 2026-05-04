package ms.mattschlenkrich.paycalculator.ui.workdate

import android.app.DatePickerDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
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
import ms.mattschlenkrich.paycalculator.common.WAIT_1000
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.data.HolidayPayCalculator
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import kotlin.math.round

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

    var curDateString by remember(currentWorkDate.wdDate) { mutableStateOf(currentWorkDate.wdDate) }
    var regHours by remember(currentWorkDate.wdRegHours) {
        mutableStateOf(
            nf.displayNumberFromDouble(
                currentWorkDate.wdRegHours
            )
        )
    }
    var otHours by remember(currentWorkDate.wdOtHours) {
        mutableStateOf(
            nf.displayNumberFromDouble(
                currentWorkDate.wdOtHours
            )
        )
    }
    var dblOtHours by remember(currentWorkDate.wdDblOtHours) {
        mutableStateOf(
            nf.displayNumberFromDouble(
                currentWorkDate.wdDblOtHours
            )
        )
    }
    var statHours by remember(currentWorkDate.wdStatHours) {
        mutableStateOf(
            nf.displayNumberFromDouble(
                currentWorkDate.wdStatHours
            )
        )
    }
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

    val regLabel = stringResource(R.string.reg_)
    val otLabel = stringResource(R.string.ot_)
    val dblOtLabel = stringResource(R.string.dbl_ot_)
    val pipeLabel = stringResource(R.string.pipe)

    val workOrderSummary = remember(
        historyRegHours,
        historyOtHours,
        historyDblOtHours,
        regLabel,
        otLabel,
        dblOtLabel,
        pipeLabel
    ) {
        buildString {
            if (historyRegHours != 0.0) {
                append(regLabel)
                append(nf.displayNumberFromDouble(historyRegHours))
            }
            if (historyOtHours != 0.0) {
                if (isNotEmpty()) append(pipeLabel)
                append(otLabel)
                append(nf.displayNumberFromDouble(historyOtHours))
            }
            if (historyDblOtHours != 0.0) {
                if (isNotEmpty()) append(pipeLabel)
                append(dblOtLabel)
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