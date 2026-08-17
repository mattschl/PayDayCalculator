package ms.mattschlenkrich.paycalculator.ui.workdate

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.ConfirmationBottomSheet
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.util.HolidayPayCalculator
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.ui.workdate.composable.WorkDateAddScreen
import java.time.LocalDate
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
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

    var curDateString by rememberSaveable { mutableStateOf("") }
    var regHours by rememberSaveable { mutableStateOf("") }
    var otHours by rememberSaveable { mutableStateOf("") }
    var dblOtHours by rememberSaveable { mutableStateOf("") }
    var statHours by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }

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

    val onSaveWorkDate: suspend (String) -> Unit = { fragmentToGoTo: String ->
        var workDate = payDayViewModel.getWorkDateSync(
            payPeriod.ppEmployerId, curDateString, payPeriod.ppCutoffDate
        )
        if (workDate != null) {
            workDate = workDate.copy(
                wdRegHours = regHours.toDoubleOrNull() ?: 0.0,
                wdOtHours = otHours.toDoubleOrNull() ?: 0.0,
                wdDblOtHours = dblOtHours.toDoubleOrNull() ?: 0.0,
                wdStatHours = statHours.toDoubleOrNull() ?: 0.0,
                wdNote = note.ifBlank { null },
                wdIsDeleted = false,
                wdUpdateTime = df.getCurrentUTCTimeAsString()
            )
            payDayViewModel.updateWorkDate(workDate)
        } else {
            workDate = WorkDates(
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
        }
        mainViewModel.setWorkDateObject(workDate)

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
            Screen.TimeSheet.route -> {
                mainViewModel.setSelectedTopLevelIndex(0)
                navController.popBackStack(Screen.MainPager.route, inclusive = false)
            }

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

    var showDateUsedDialog by rememberSaveable { mutableStateOf(false) }
    var existingWorkDate by rememberSaveable { mutableStateOf<WorkDates?>(null) }

    ConfirmationBottomSheet(
        showDialog = showDateUsedDialog && existingWorkDate != null,
        onDismissRequest = { showDateUsedDialog = false },
        title = stringResource(R.string.this_date_is_already_used),
        message = stringResource(R.string.would_you_like_to_replace_the_old_information_for_this_work_date),
        onConfirm = {
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
        }
    )

    WorkDateAddScreen(
        dateText = if (curDateString.isNotEmpty()) df.getDisplayDate(curDateString) else "",
        onDateClick = {
            df.showDatePicker(context, curDateString.ifEmpty { LocalDate.now().toString() }) {
                curDateString = it
            }
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
                val stat = round(holidayPayCalculator.calculateStatHours() * 4) / 4
                statHours = nf.displayNumberFromDouble(stat)
            }
        },
        note = note,
        onNoteChange = { note = it },
        onUpdateTimeClick = {
            coroutineScope.launch {
                onSaveWorkDate(Screen.WorkDateTimes.route)
            }
        },
        onAddHistoryClick = {
            coroutineScope.launch {
                onSaveWorkDate(Screen.WorkOrderHistoryAdd.route)
            }
        },
        onSaveClick = {
            val existing = usedWorkDatesList.find { it.wdDate == curDateString }
            if (existing != null) {
                existingWorkDate = existing
                showDateUsedDialog = true
            } else {
                coroutineScope.launch {
                    onSaveWorkDate(Screen.WorkDateUpdate.route)
                }
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
                            if (existing.wdIsDeleted) {
                                val revived = existing.copy(
                                    wdIsDeleted = false,
                                    wdUpdateTime = df.getCurrentUTCTimeAsString()
                                )
                                payDayViewModel.updateWorkDate(revived)
                                revived
                            } else {
                                existing
                            }
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
        onAddExtraClick = { /* Navigate to Extra Add */ }
    )
}