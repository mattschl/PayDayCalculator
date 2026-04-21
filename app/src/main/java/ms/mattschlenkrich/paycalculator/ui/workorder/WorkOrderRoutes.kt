package ms.mattschlenkrich.paycalculator.ui.workorder

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
import ms.mattschlenkrich.paycalculator.data.Areas
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.JobSpec
import ms.mattschlenkrich.paycalculator.data.JobSpecMerged
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.Material
import ms.mattschlenkrich.paycalculator.data.MaterialAndQuantity
import ms.mattschlenkrich.paycalculator.data.MaterialInSequence
import ms.mattschlenkrich.paycalculator.data.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryMaterialCombined
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.WorkPerformedAndQuantity
import ms.mattschlenkrich.paycalculator.data.WorkPerformedMerged
import ms.mattschlenkrich.paycalculator.ui.areas.AreaUpdateScreen
import ms.mattschlenkrich.paycalculator.ui.areas.AreaViewScreen
import ms.mattschlenkrich.paycalculator.ui.jobspec.JobSpecMergeScreen
import ms.mattschlenkrich.paycalculator.ui.jobspec.JobSpecUpdateScreen
import ms.mattschlenkrich.paycalculator.ui.jobspec.JobSpecViewScreen
import ms.mattschlenkrich.paycalculator.ui.material.MaterialMergeScreen
import ms.mattschlenkrich.paycalculator.ui.material.MaterialQuantityUpdateScreen
import ms.mattschlenkrich.paycalculator.ui.material.MaterialUpdateScreen
import ms.mattschlenkrich.paycalculator.ui.material.MaterialViewScreen
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.WorkOrderHistoryAddScreen
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.WorkOrderHistoryMaterialUpdateScreen
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.WorkOrderHistoryTimeScreen
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.WorkOrderHistoryTimeUpdateScreen
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.WorkOrderHistoryUpdateScreen
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.WorkOrderHistoryWorkPerformedUpdateScreen
import ms.mattschlenkrich.paycalculator.ui.workperformed.WorkPerformedMergeScreen
import ms.mattschlenkrich.paycalculator.ui.workperformed.WorkPerformedUpdateScreen
import ms.mattschlenkrich.paycalculator.ui.workperformed.WorkPerformedViewScreen
import java.util.Calendar

@Composable
fun WorkOrderHistoryAddRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }

    val workDate = mainViewModel.getWorkDateObject() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val employers by employerViewModel.getEmployers().observeAsState()
    if (employers == null) return

    val employer = employers!!.find { it.employerId == workDate.wdEmployerId } ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val workOrderList by workOrderViewModel.getWorkOrdersByEmployerId(workDate.wdEmployerId)
        .observeAsState(emptyList())

    val tempInfo = mainViewModel.getTempWorkOrderHistoryInfo()
    val initialWorkOrderNumber = tempInfo?.woHistoryWorkOrderNumber ?: ""
    val initialRegHours = tempInfo?.let { nf.displayNumberFromDouble(it.woHistoryRegHours) } ?: ""
    val initialOtHours = tempInfo?.let { nf.displayNumberFromDouble(it.woHistoryOtHours) } ?: ""
    val initialDblOtHours =
        tempInfo?.let { nf.displayNumberFromDouble(it.woHistoryDblOtHours) } ?: ""
    val initialNote = tempInfo?.woHistoryNote ?: ""

    val selectedWo = mainViewModel.getWorkOrder()
    val finalWoNumber = selectedWo?.woNumber ?: initialWorkOrderNumber

    WorkOrderHistoryAddScreen(
        workOrderList = workOrderList,
        initialWorkOrderNumber = finalWoNumber,
        initialRegHours = initialRegHours,
        initialOtHours = initialOtHours,
        initialDblOtHours = initialDblOtHours,
        initialNote = initialNote,
        onWorkOrderSearch = { number, reg, ot, dbl, nt ->
            mainViewModel.setTempWorkOrderHistoryInfo(
                TempWorkOrderHistoryInfo(
                    0L,
                    number,
                    workDate.wdDate,
                    reg.toDoubleOrNull() ?: 0.0,
                    ot.toDoubleOrNull() ?: 0.0,
                    dbl.toDoubleOrNull() ?: 0.0,
                    nt,
                    "",
                    "",
                    "",
                    0.0,
                    ""
                )
            )
            navController.navigate(Screen.WorkOrderLookup.route)
        },
        onWorkOrderAddEdit = { number, reg, ot, dbl, nt, exists ->
            mainViewModel.setTempWorkOrderHistoryInfo(
                TempWorkOrderHistoryInfo(
                    0L,
                    number,
                    workDate.wdDate,
                    reg.toDoubleOrNull() ?: 0.0,
                    ot.toDoubleOrNull() ?: 0.0,
                    dbl.toDoubleOrNull() ?: 0.0,
                    nt,
                    "",
                    "",
                    "",
                    0.0,
                    ""
                )
            )
            if (!exists) {
                mainViewModel.setWorkOrderNumber(number)
                navController.navigate(Screen.WorkOrderAdd.route)
            } else {
                val wo = workOrderList.find { it.woNumber == number }
                if (wo != null) {
                    mainViewModel.setWorkOrder(wo)
                    navController.navigate(Screen.WorkOrderUpdate.route)
                }
            }
        },
        onDone = { number, reg, ot, dbl, nt, _ ->
            val wo = workOrderList.find { it.woNumber == number }
            if (wo != null) {
                coroutineScope.launch {
                    val history = WorkOrderHistory(
                        nf.generateRandomIdAsLong(),
                        wo.workOrderId,
                        workDate.workDateId,
                        reg.toDoubleOrNull() ?: 0.0,
                        ot.toDoubleOrNull() ?: 0.0,
                        dbl.toDoubleOrNull() ?: 0.0,
                        nt,
                        false,
                        df.getCurrentUTCTimeAsString()
                    )
                    workOrderViewModel.insertWorkOrderHistory(history)
                    mainViewModel.setTempWorkOrderHistoryInfo(null)
                    mainViewModel.setWorkOrder(null)
                    navController.navigate(Screen.WorkOrderHistoryUpdate.route) {
                        popUpTo(Screen.WorkOrderHistoryAdd.route) { inclusive = true }
                    }
                }
            }
        },
        onAddTime = { number, reg, ot, dbl, nt, _ ->
            val wo = workOrderList.find { it.woNumber == number }
            if (wo != null) {
                coroutineScope.launch {
                    val historyId = nf.generateRandomIdAsLong()
                    val history = WorkOrderHistory(
                        historyId,
                        wo.workOrderId,
                        workDate.workDateId,
                        reg.toDoubleOrNull() ?: 0.0,
                        ot.toDoubleOrNull() ?: 0.0,
                        dbl.toDoubleOrNull() ?: 0.0,
                        nt,
                        false,
                        df.getCurrentUTCTimeAsString()
                    )
                    workOrderViewModel.insertWorkOrderHistory(history)
                    mainViewModel.setTempWorkOrderHistoryInfo(null)
                    mainViewModel.setWorkOrder(null)
                    mainViewModel.setWorkOrderHistory(history)
                    navController.navigate(Screen.WorkOrderHistoryTime.route) {
                        popUpTo(Screen.WorkOrderHistoryAdd.route) { inclusive = true }
                    }
                }
            }
        },
        onBack = { navController.popBackStack() },
        displayDate = df.getDisplayDate(workDate.wdDate),
        displayEmployer = employer.employerName
    )
}

@Composable
fun WorkOrderHistoryUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val initialHistory = mainViewModel.getWorkOrderHistory() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val historyWithDates by workOrderViewModel.getWorkOrderHistoriesById(initialHistory.woHistoryId)
        .observeAsState()

    if (historyWithDates == null) {
        return
    }

    val history = historyWithDates!!.history
    val workDate = historyWithDates!!.workDate
    val employer = mainViewModel.getEmployer() ?: return

    var workOrderNumber by remember { mutableStateOf(historyWithDates!!.workOrder.woNumber) }
    val workOrderList by workOrderViewModel.getWorkOrdersByEmployerId(employer.employerId)
        .observeAsState(emptyList())
    var workOrderDescription by remember { mutableStateOf(historyWithDates!!.workOrder.woDescription) }

    var regHours by remember(history.woHistoryRegHours) {
        mutableStateOf(
            nf.displayNumberFromDouble(
                history.woHistoryRegHours
            )
        )
    }
    var otHours by remember(history.woHistoryOtHours) {
        mutableStateOf(
            nf.displayNumberFromDouble(
                history.woHistoryOtHours
            )
        )
    }
    var dblOtHours by remember(history.woHistoryDblOtHours) {
        mutableStateOf(
            nf.displayNumberFromDouble(
                history.woHistoryDblOtHours
            )
        )
    }
    var note by remember { mutableStateOf(history.woHistoryNote ?: "") }

    var workPerformed by remember { mutableStateOf("") }
    val workPerformedList by workOrderViewModel.getWorkPerformedAll().observeAsState(emptyList())
    var area by remember { mutableStateOf("") }
    val areaList by workOrderViewModel.getAreasList().observeAsState(emptyList())
    var workPerformedNote by remember { mutableStateOf("") }
    val workPerformedActualList by workOrderViewModel.getWorkPerformedCombinedByWorkOrderHistory(
        history.woHistoryId
    ).observeAsState(emptyList())

    var materialQty by remember { mutableStateOf("") }
    var materialName by remember { mutableStateOf("") }
    val materialList by workOrderViewModel.getMaterialsList().observeAsState(emptyList())
    val materialActualList by workOrderViewModel.getMaterialsByHistory(history.woHistoryId)
        .observeAsState(emptyList())

    val timeWorkedList by workOrderViewModel.getTimeWorkedForWorkOrderHistory(history.woHistoryId)
        .observeAsState(emptyList())

    val isWorkOrderValid = workOrderList.any { it.woNumber == workOrderNumber }

    WorkOrderHistoryUpdateScreen(
        title = stringResource(R.string.update_work_order),
        workDateDisplay = df.getDisplayDate(workDate.wdDate),
        employerName = employer.employerName,
        workOrderNumber = workOrderNumber,
        onWorkOrderNumberChange = { workOrderNumber = it },
        workOrderList = workOrderList,
        onWorkOrderSelected = { wo ->
            workOrderNumber = wo.woNumber
            workOrderDescription = wo.woDescription
        },
        onWorkOrderLongClick = {
            val wo = workOrderList.find { it.woNumber == workOrderNumber }
            if (wo != null) {
                mainViewModel.setWorkOrder(wo)
                navController.navigate(Screen.WorkOrderUpdate.route)
            }
        },
        workOrderDescription = workOrderDescription,
        onWorkOrderButtonClick = {
            if (isWorkOrderValid) {
                val wo = workOrderList.find { it.woNumber == workOrderNumber }
                if (wo != null) {
                    mainViewModel.setWorkOrder(wo)
                    navController.navigate(Screen.WorkOrderUpdate.route)
                }
            } else {
                mainViewModel.setWorkOrderNumber(workOrderNumber)
                navController.navigate(Screen.WorkOrderAdd.route)
            }
        },
        workOrderButtonText = if (isWorkOrderValid) stringResource(R.string.edit)
        else stringResource(R.string.create),
        regHours = regHours,
        onRegHoursChange = { regHours = it },
        otHours = otHours,
        onOtHoursChange = { otHours = it },
        dblOtHours = dblOtHours,
        onDblOtHoursChange = { dblOtHours = it },
        note = note,
        onNoteChange = { note = it },
        onAddTimeClick = {
            mainViewModel.setWorkOrderHistory(history)
            navController.navigate(Screen.WorkOrderHistoryTime.route)
        },
        addTimeButtonText = if (timeWorkedList.isNotEmpty()) stringResource(R.string.edit_times)
        else stringResource(R.string.add_time),
        workPerformed = workPerformed,
        onWorkPerformedChange = { workPerformed = it },
        workPerformedList = workPerformedList,
        onWorkPerformedSelected = { wp ->
            workPerformed = wp.wpDescription
        },
        area = area,
        onAreaChange = { area = it },
        areaList = areaList,
        onAreaSelected = { a ->
            area = a.areaName
        },
        workPerformedNote = workPerformedNote,
        onWorkPerformedNoteChange = { workPerformedNote = it },
        onAddWorkPerformed = {
            coroutineScope.launch {
                val wp = workOrderViewModel.getWorkPerformedSync(workPerformed)
                val a = workOrderViewModel.getOrCreateArea(area)
                if (wp != null) {
                    workOrderViewModel.insertWorkOrderHistoryWorkPerformed(
                        WorkOrderHistoryWorkPerformed(
                            nf.generateRandomIdAsLong(),
                            history.woHistoryId,
                            wp.workPerformedId,
                            a?.areaId,
                            workPerformedNote,
                            workPerformedActualList.size + 1,
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    )
                    workPerformed = ""
                    area = ""
                    workPerformedNote = ""
                }
            }
        },
        workPerformedActualList = workPerformedActualList,
        onWorkPerformedItemClick = { item, action ->
            if (action == 0) { // Delete
                coroutineScope.launch {
                    workOrderViewModel.deleteWorkOrderHistoryWorkPerformed(
                        item.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId,
                        df.getCurrentUTCTimeAsString()
                    )
                }
            }
        },
        materialQty = materialQty,
        onMaterialQtyChange = { materialQty = it },
        material = materialName,
        onMaterialChange = { materialName = it },
        materialList = materialList,
        onMaterialSelected = { m ->
            materialName = m.mName
        },
        onAddMaterial = {
            coroutineScope.launch {
                val m = workOrderViewModel.getMaterialSync(materialName)
                if (m != null) {
                    workOrderViewModel.insertWorkOrderHistoryMaterial(
                        WorkOrderHistoryMaterial(
                            nf.generateRandomIdAsLong(),
                            history.woHistoryId,
                            m.materialId,
                            materialQty.toDoubleOrNull() ?: 1.0,
                            materialActualList.size + 1,
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    )
                    materialQty = ""
                    materialName = ""
                }
            }
        },
        materialActualList = materialActualList.map {
            MaterialInSequence(
                it.workOrderHistoryMaterial.workOrderHistoryMaterialId,
                it.workOrderHistoryMaterial.wohmHistoryId,
                it.workOrderHistoryMaterial.wohmMaterialId,
                it.material.mName,
                it.workOrderHistoryMaterial.wohmQuantity,
                it.workOrderHistoryMaterial.wohmSequence
            )
        },
        onMaterialItemClick = { item, action ->
            if (action == 0) { // Delete
                coroutineScope.launch {
                    workOrderViewModel.deleteWorkOrderHistoryMaterial(
                        item.workOrderHistoryMaterialId,
                        df.getCurrentUTCTimeAsString()
                    )
                }
            }
        },
        onDone = {
            coroutineScope.launch {
                val wo = workOrderViewModel.findWorkOrder(
                    workOrderNumber,
                    employer.employerId
                )
                if (wo != null) {
                    workOrderViewModel.updateWorkOrderHistory(
                        history.copy(
                            woHistoryWorkOrderId = wo.workOrderId,
                            woHistoryRegHours = regHours.toDoubleOrNull() ?: 0.0,
                            woHistoryOtHours = otHours.toDoubleOrNull() ?: 0.0,
                            woHistoryDblOtHours = dblOtHours.toDoubleOrNull() ?: 0.0,
                            woHistoryNote = note,
                            woHistoryUpdateTime = df.getCurrentUTCTimeAsString()
                        )
                    )
                }
                navController.popBackStack()
            }
        },
        onBack = { navController.popBackStack() },
        onUpdateWorkPerformed = { item ->
            mainViewModel.setWorkPerformedHistoryId(item.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId)
            navController.navigate(Screen.WorkOrderHistoryWorkPerformedUpdate.route)
        }
    )
}

@Composable
fun AreaViewRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    val areaList by if (searchQuery.isEmpty()) {
        workOrderViewModel.getAreasList().observeAsState(emptyList())
    } else {
        workOrderViewModel.searchAreas("%$searchQuery%").observeAsState(emptyList())
    }

    AreaViewScreen(
        areaList = areaList,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onAreaClick = { area ->
            mainViewModel.setAreaId(area.areaId)
            navController.navigate(Screen.AreaUpdate.route)
        },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun AreaUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val areaId = mainViewModel.getAreaId()
    if (areaId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val areaList by workOrderViewModel.getAreasList().observeAsState(emptyList())
    val oldArea by workOrderViewModel.getArea(areaId).observeAsState()

    oldArea?.let { area ->
        var name by remember(area.areaId) { mutableStateOf(area.areaName) }

        AreaUpdateScreen(
            name = name,
            onNameChange = { name = it },
            title = stringResource(R.string.update_area_description_for) + area.areaName,
            onUpdateClick = {
                val trimmedName = name.trim()
                if (trimmedName.isBlank()) {
                    // In a real app, use a proper snackbar or state-based error
                    return@AreaUpdateScreen
                }

                if (areaList.any { it.areaName == trimmedName && it.areaId != area.areaId }) {
                    return@AreaUpdateScreen
                }

                coroutineScope.launch {
                    workOrderViewModel.updateArea(
                        Areas(
                            area.areaId,
                            trimmedName,
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    )
                    navController.popBackStack()
                }
            },
            onCancelClick = {
                navController.popBackStack()
            },
            onBackClick = {
                navController.popBackStack()
            }
        )
    }
}

@Composable
fun JobSpecViewRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    val jobSpecList by if (searchQuery.isEmpty()) {
        workOrderViewModel.getJobSpecsAll().observeAsState(emptyList())
    } else {
        workOrderViewModel.searchJobSpecs("%$searchQuery%").observeAsState(emptyList())
    }

    JobSpecViewScreen(
        jobSpecList = jobSpecList,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onJobSpecClick = { js ->
            mainViewModel.setJobSpecId(js.jobSpecId)
            navController.navigate(Screen.JobSpecUpdate.route)
        },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun JobSpecUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val jsId = mainViewModel.getJobSpecId()
    if (jsId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val originalJs by workOrderViewModel.getJobSpec(jsId).observeAsState()
    val jobSpecList by workOrderViewModel.getJobSpecsAll().observeAsState(emptyList())

    originalJs?.let { js ->
        var name by remember(js.jobSpecId) { mutableStateOf(js.jsName) }

        JobSpecUpdateScreen(
            title = stringResource(R.string.update_) + js.jsName,
            jobSpecName = name,
            onJobSpecNameChange = { name = it },
            onUpdateClick = {
                val trimmedName = name.trim()
                if (trimmedName.isEmpty()) return@JobSpecUpdateScreen
                if (jobSpecList.any { it.jsName == trimmedName && it.jobSpecId != js.jobSpecId })
                    return@JobSpecUpdateScreen

                coroutineScope.launch {
                    workOrderViewModel.updateJobSpec(
                        js.copy(
                            jsName = trimmedName,
                            jsUpdateTime = df.getCurrentUTCTimeAsString()
                        )
                    )
                    navController.popBackStack()
                }
            },
            onCancelClick = {
                navController.popBackStack()
            },
            onMergeClick = {
                mainViewModel.setJobSpecId(js.jobSpecId)
                mainViewModel.setJobSpecIsMaster(true)
                navController.navigate(Screen.JobSpecMerge.route)
            }
        )
    }
}

@Composable
fun JobSpecMergeRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    var jsId = mainViewModel.getJobSpecId()
    if (jsId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val jobSpecList by workOrderViewModel.getJobSpecsAll().observeAsState(emptyList())
    val parentJobSpec by workOrderViewModel.getJobSpec(jsId).observeAsState()
    val childList by workOrderViewModel.getJobSpecAndChildList(jsId)
        .observeAsState(emptyList())

    var parentDescription by remember { mutableStateOf("") }
    var childDescription by remember { mutableStateOf("") }
    var selectedChild by remember {
        mutableStateOf<JobSpec?>(
            null
        )
    }

    LaunchedEffect(parentJobSpec) {
        parentJobSpec?.let {
            parentDescription = it.jsName
        }
    }

    JobSpecMergeScreen(
        jobSpecList = jobSpecList,
        parentName = parentDescription,
        onParentNameChange = { parentDescription = it },
        onParentSelected = {
            mainViewModel.setJobSpecId(it.jobSpecId)
            parentDescription = it.jsName
        },
        childList = childList,
        onRemoveChild = { child ->
            coroutineScope.launch {
                workOrderViewModel.deleteJobSpecMerged(
                    child.jobSpecMerged.jobSpecMergedId,
                    df.getCurrentUTCTimeAsString()
                )
            }
        },
        childName = childDescription,
        onChildNameChange = { childDescription = it },
        onChildSelected = {
            selectedChild = it
            childDescription = it.jsName
        },
        onMergeClick = {
            val childId = selectedChild?.jobSpecId
            if (childId != null && childId != jsId) {
                coroutineScope.launch {
                    workOrderViewModel.insertJobSpecMerged(
                        JobSpecMerged(
                            nf.generateRandomIdAsLong(),
                            jsId,
                            childId,
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    )
                    childDescription = ""
                    selectedChild = null
                }
            }
        },
        onDoneClick = {
            navController.popBackStack()
        },
        onListItemSelected = {
            if (mainViewModel.getJobSpecIsMaster()) {
                mainViewModel.setJobSpecId(it.jobSpecId)
                parentDescription = it.jsName
            } else {
                selectedChild = it
                childDescription = it.jsName
            }
        }
    )
}

@Composable
fun MaterialViewRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    val materialList by if (searchQuery.isEmpty()) {
        workOrderViewModel.getMaterialsList().observeAsState(emptyList())
    } else {
        workOrderViewModel.searchMaterials("%$searchQuery%").observeAsState(emptyList())
    }

    MaterialViewScreen(
        materialList = materialList,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onMaterialClick = { material ->
            mainViewModel.setMaterial(material)
            navController.navigate(Screen.MaterialUpdate.route)
        },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun MaterialUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val oldMaterial = mainViewModel.getMaterial() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val materialList by workOrderViewModel.getMaterialsList().observeAsState(emptyList())

    var name by remember { mutableStateOf(oldMaterial.mName) }
    var cost by remember { mutableStateOf(nf.displayDollars(oldMaterial.mCost)) }
    var price by remember { mutableStateOf(nf.displayDollars(oldMaterial.mPrice)) }

    MaterialUpdateScreen(
        name = name,
        onNameChange = { name = it },
        cost = cost,
        onCostChange = { cost = it },
        price = price,
        onPriceChange = { price = it },
        onUpdateClick = {
            if (name.isBlank() || cost.isBlank() || price.isBlank()) {
                return@MaterialUpdateScreen
            }
            if (materialList.any { it.mName == name.trim() && it.materialId != oldMaterial.materialId }) {
                return@MaterialUpdateScreen
            }

            coroutineScope.launch {
                val material = Material(
                    oldMaterial.materialId,
                    name.trim(),
                    nf.getDoubleFromDollars(cost.trim()),
                    nf.getDoubleFromDollars(price.trim()),
                    oldMaterial.mIsDeleted,
                    df.getCurrentUTCTimeAsString()
                )
                workOrderViewModel.updateMaterial(material)
                mainViewModel.setMaterial(material)
                navController.popBackStack()
            }
        },
        onMergeClick = {
            coroutineScope.launch {
                val material = Material(
                    oldMaterial.materialId,
                    name.trim(),
                    nf.getDoubleFromDollars(cost.trim()),
                    nf.getDoubleFromDollars(price.trim()),
                    oldMaterial.mIsDeleted,
                    df.getCurrentUTCTimeAsString()
                )
                workOrderViewModel.updateMaterial(material)
                mainViewModel.setMaterial(material)
                mainViewModel.setMaterialId(oldMaterial.materialId)
                // Defaulting to Master for now, or could show dialog
                mainViewModel.setMaterialIsParent(true)
                navController.navigate(Screen.MaterialMerge.route)
            }
        },
        onCancelClick = { navController.popBackStack() },
        title = stringResource(R.string.update_) + oldMaterial.mName
    )
}

@Composable
fun MaterialMergeRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    var materialId = mainViewModel.getMaterialId()
    if (materialId == null) {
        val mat = mainViewModel.getMaterial()
        if (mat != null) {
            materialId = mat.materialId
            mainViewModel.setMaterialId(materialId)
        } else {
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
            return
        }
    }

    val materialList by workOrderViewModel.getMaterialsList().observeAsState(emptyList())
    val parentMaterial by workOrderViewModel.getMaterial(materialId).observeAsState()
    val childList by workOrderViewModel.getMaterialAndChildList(materialId)
        .observeAsState(emptyList())

    var parentDescription by remember { mutableStateOf("") }
    var childDescription by remember { mutableStateOf("") }
    var selectedChild by remember {
        mutableStateOf<Material?>(
            null
        )
    }

    LaunchedEffect(parentMaterial) {
        parentMaterial?.let {
            parentDescription = it.mName
        }
    }

    MaterialMergeScreen(
        materialList = materialList,
        parentDescription = parentDescription,
        onParentDescriptionChange = { parentDescription = it },
        onParentSelected = {
            mainViewModel.setMaterialId(it.materialId)
            parentDescription = it.mName
        },
        childList = childList,
        onRemoveChild = { child ->
            coroutineScope.launch {
                workOrderViewModel.deleteMaterialMerged(
                    child.materialMerged.materialMergeId,
                    df.getCurrentUTCTimeAsString()
                )
            }
        },
        childDescription = childDescription,
        onChildDescriptionChange = { childDescription = it },
        onChildSelected = {
            selectedChild = it
            childDescription = it.mName
        },
        onMergeClick = {
            val childId = selectedChild?.materialId
            if (childId != null && childId != materialId) {
                coroutineScope.launch {
                    workOrderViewModel.insertMaterialMerged(
                        MaterialMerged(
                            nf.generateRandomIdAsLong(),
                            materialId,
                            childId,
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    )
                    childDescription = ""
                    selectedChild = null
                }
            }
        },
        onDoneClick = {
            navController.popBackStack()
        },
        onListItemSelected = {
            if (mainViewModel.getMaterialIsParent()) {
                mainViewModel.setMaterialId(it.materialId)
                parentDescription = it.mName
            } else {
                selectedChild = it
                childDescription = it.mName
            }
        }
    )
}

@Composable
fun MaterialQuantityUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val initialHistory = mainViewModel.getWorkOrderHistory()
    val materialId = mainViewModel.getMaterialId()

    if (initialHistory == null || materialId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val historyWithDates by workOrderViewModel.getWorkOrderHistoriesById(initialHistory.woHistoryId)
        .observeAsState()

    var materialHistory by remember {
        mutableStateOf<WorkOrderHistoryMaterialCombined?>(
            null
        )
    }
    LaunchedEffect(materialId) {
        materialHistory = workOrderViewModel.getWorkOrderHistoryMaterialCombined(materialId)
    }

    if (materialHistory == null || historyWithDates == null) return

    var qty by remember { mutableStateOf(nf.displayNumberFromDouble(materialHistory!!.workOrderHistoryMaterial.wohmQuantity)) }

    MaterialQuantityUpdateScreen(
        details = stringResource(R.string.edit_material_used_for_wo_) +
                " ${historyWithDates!!.workOrder.woNumber} " +
                stringResource(R.string._at_) + " ${historyWithDates!!.workOrder.woAddress}\n" +
                historyWithDates!!.workOrder.woDescription + "\n\n" +
                stringResource(R.string.material) + " ${materialHistory!!.material.mName}",
        quantity = qty,
        onQuantityChange = { qty = it },
        onDoneClick = {
            coroutineScope.launch {
                workOrderViewModel.updateWorkOrderHistoryMaterial(
                    materialHistory!!.workOrderHistoryMaterial.copy(
                        wohmQuantity = qty.toDoubleOrNull() ?: 0.0,
                        wohmUpdateTime = df.getCurrentUTCTimeAsString()
                    )
                )
                navController.popBackStack()
            }
        },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun WorkOrderViewRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    var selectedEmployer by remember { mutableStateOf(mainViewModel.getEmployer()) }
    var searchQuery by remember { mutableStateOf("") }

    val workOrders by if (selectedEmployer != null) {
        if (searchQuery.isEmpty()) {
            workOrderViewModel.getWorkOrdersByEmployerId(selectedEmployer!!.employerId)
                .observeAsState(emptyList())
        } else {
            workOrderViewModel.searchWorkOrders(selectedEmployer!!.employerId, "%$searchQuery%")
                .observeAsState(emptyList())
        }
    } else {
        remember { mutableStateOf(emptyList<WorkOrder>()) }
    }

    WorkOrderViewScreen(
        employers = employers,
        selectedEmployer = selectedEmployer,
        onEmployerSelected = {
            selectedEmployer = it
            mainViewModel.setEmployer(it)
        },
        onAddNewEmployerClick = { navController.navigate(Screen.EmployerAdd.route) },
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onResetSearchClick = { searchQuery = "" },
        workOrders = workOrders,
        onWorkOrderClick = {
            mainViewModel.setWorkOrder(it)
            navController.navigate(Screen.WorkOrderUpdate.route)
        },
        onAddNewWorkOrderClick = { navController.navigate(Screen.WorkOrderAdd.route) },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun WorkOrderAddRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    val currentEmployer = mainViewModel.getEmployer()

    var selectedEmployer by remember {
        mutableStateOf<Employers?>(
            currentEmployer
        )
    }
    val initialWoNumber = mainViewModel.getWorkOrderNumber() ?: ""
    var woNumber by remember { mutableStateOf(initialWoNumber) }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    WorkOrderAddScreen(
        employers = employers,
        selectedEmployer = selectedEmployer,
        onEmployerSelected = { selectedEmployer = it },
        fixedEmployerName = currentEmployer?.employerName,
        woNumber = woNumber,
        onWoNumberChange = { woNumber = it },
        address = address,
        onAddressChange = { address = it },
        description = description,
        onDescriptionChange = { description = it },
        onDoneClick = {
            val employerId = selectedEmployer?.employerId
            if (employerId != null && woNumber.isNotBlank()) {
                coroutineScope.launch {
                    val newWo = WorkOrder(
                        nf.generateRandomIdAsLong(),
                        woNumber.trim(),
                        employerId,
                        address.trim(),
                        description.trim(),
                        false,
                        df.getCurrentUTCTimeAsString()
                    )
                    workOrderViewModel.insertWorkOrder(newWo)
                    mainViewModel.setWorkOrderNumber(null)
                    navController.popBackStack()
                }
            }
        },
        onBackClick = {
            mainViewModel.setWorkOrderNumber(null)
            navController.popBackStack()
        }
    )
}

@Composable
fun WorkOrderUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val initialWo = mainViewModel.getWorkOrder() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val employer = mainViewModel.getEmployer() ?: return

    var woNumber by remember { mutableStateOf(initialWo.woNumber) }
    var address by remember { mutableStateOf(initialWo.woAddress) }
    var description by remember { mutableStateOf(initialWo.woDescription) }

    var jobSpecText by remember { mutableStateOf("") }
    val jobSpecSuggestions by workOrderViewModel.getJobSpecsAll().observeAsState(emptyList())
    var areaText by remember { mutableStateOf("") }
    val areaSuggestions by workOrderViewModel.getAreasList().observeAsState(emptyList())
    var workPerformedNote by remember { mutableStateOf("") }

    val addedJobSpecs by workOrderViewModel.getWorkOrderJobSpecs(initialWo.workOrderId)
        .observeAsState(emptyList())
    val historyList by workOrderViewModel.getWorkOrderHistoriesByWorkOrder(initialWo.workOrderId)
        .observeAsState(emptyList())

    // Mocking summaries for now as they might need complex calculation
    val jobSpecSummaryText = "${addedJobSpecs.size} items"
    val historySummaryText = "${historyList.size} entries"

    // Need to get these from somewhere, possibly another query
    val workPerformedList =
        emptyList<WorkPerformedAndQuantity>()
    val materialsList = emptyList<MaterialAndQuantity>()

    WorkOrderUpdateScreen(
        employerName = employer.employerName,
        woNumber = woNumber,
        onWoNumberChange = { woNumber = it },
        address = address,
        onAddressChange = { address = it },
        description = description,
        onDescriptionChange = { description = it },
        jobSpecText = jobSpecText,
        onJobSpecTextChange = { jobSpecText = it },
        jobSpecSuggestions = jobSpecSuggestions,
        onJobSpecSelected = { jobSpecText = it.jsName },
        areaText = areaText,
        onAreaTextChange = { areaText = it },
        areaSuggestions = areaSuggestions,
        onAreaSelected = { areaText = it.areaName },
        workPerformedNote = workPerformedNote,
        onWorkPerformedNoteChange = { workPerformedNote = it },
        onAddJobSpecClick = {
            if (jobSpecText.isNotBlank()) {
                coroutineScope.launch {
                    val js = workOrderViewModel.getOrCreateJobSpec(jobSpecText.trim())
                    val a = workOrderViewModel.getOrCreateArea(areaText.trim())
                    workOrderViewModel.insertWorkOrderJobSpec(
                        WorkOrderJobSpec(
                            nf.generateRandomIdAsLong(),
                            initialWo.workOrderId,
                            js.jobSpecId,
                            a?.areaId,
                            workPerformedNote.trim(),
                            addedJobSpecs.size + 1,
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    )
                    jobSpecText = ""
                    areaText = ""
                    workPerformedNote = ""
                }
            }
        },
        addedJobSpecs = addedJobSpecs,
        onJobSpecClick = { combined ->
            mainViewModel.setWorkOrderJobSpecId(combined.workOrderJobSpec.workOrderJobSpecId)
            navController.navigate(Screen.WorkOrderJobSpecUpdate.route)
        },
        jobSpecSummaryText = jobSpecSummaryText,
        historyList = historyList,
        onHistoryClick = { history ->
            mainViewModel.setWorkOrderHistory(history.history)
            navController.navigate(Screen.WorkOrderHistoryUpdate.route)
        },
        historySummaryText = historySummaryText,
        onAddHistoryClick = {
            // Need to set a work date for HistoryAdd, maybe navigate to TimeSheet to pick one?
            // Or use current?
            navController.navigate(Screen.TimeSheet.route)
        },
        workPerformedList = workPerformedList,
        materialsList = materialsList,
        onDoneClick = {
            coroutineScope.launch {
                workOrderViewModel.updateWorkOrder(
                    initialWo.copy(
                        woNumber = woNumber.trim(),
                        woAddress = address.trim(),
                        woDescription = description.trim(),
                        woUpdateTime = df.getCurrentUTCTimeAsString()
                    )
                )
                navController.popBackStack()
            }
        },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun WorkOrderLookupRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val employer = mainViewModel.getEmployer()
    var searchQuery by remember { mutableStateOf("") }
    val workOrders by if (employer != null) {
        workOrderViewModel.searchWorkOrders(employer.employerId, "%$searchQuery%")
            .observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList<WorkOrder>()) }
    }

    WorkOrderLookupScreen(
        employer = employer,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        workOrders = workOrders,
        onWorkOrderSelected = { wo ->
            mainViewModel.setWorkOrder(wo)
            navController.popBackStack()
        },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun WorkPerformedViewRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    val workPerformedList by if (searchQuery.isEmpty()) {
        workOrderViewModel.getWorkPerformedAll().observeAsState(emptyList())
    } else {
        workOrderViewModel.searchFromWorkPerformed("%$searchQuery%").observeAsState(emptyList())
    }

    WorkPerformedViewScreen(
        workPerformedList = workPerformedList,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onWorkPerformedClick = { wp ->
            mainViewModel.setWorkPerformedId(wp.workPerformedId)
            navController.navigate(Screen.WorkPerformedUpdate.route)
        },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun WorkPerformedUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val wpId = mainViewModel.getWorkPerformedId()
    if (wpId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val originalWp by workOrderViewModel.getWorkPerformed(wpId).observeAsState()
    val workPerformedList by workOrderViewModel.getWorkPerformedAll().observeAsState(emptyList())

    originalWp?.let { wp ->
        var description by remember(wp.workPerformedId) { mutableStateOf(wp.wpDescription) }

        WorkPerformedUpdateScreen(
            currentDescription = description,
            onDescriptionChange = { description = it },
            onUpdateClick = {
                val trimmedDescription = description.trim()
                if (trimmedDescription.isEmpty()) return@WorkPerformedUpdateScreen
                if (workPerformedList.any {
                        it.wpDescription == trimmedDescription && it.workPerformedId != wp.workPerformedId
                    }) return@WorkPerformedUpdateScreen

                coroutineScope.launch {
                    workOrderViewModel.updateWorkPerformed(
                        wp.copy(
                            wpDescription = trimmedDescription,
                            wpUpdateTime = df.getCurrentUTCTimeAsString()
                        )
                    )
                    navController.popBackStack()
                }
            },
            onMergeClick = {
                mainViewModel.setWorkPerformedId(wp.workPerformedId)
                mainViewModel.setWorkPerformedIsMaster(true)
                navController.navigate(Screen.WorkPerformedMerge.route)
            },
            onCancelClick = {
                navController.popBackStack()
            },
            title = stringResource(R.string.update_) + wp.wpDescription
        )
    }
}

@Composable
fun WorkPerformedMergeRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    var wpId = mainViewModel.getWorkPerformedId()
    if (wpId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val workPerformedList by workOrderViewModel.getWorkPerformedAll().observeAsState(emptyList())
    val parentWorkPerformed by workOrderViewModel.getWorkPerformed(wpId).observeAsState()
    val childList by workOrderViewModel.getWorkPerformedAndChildList(wpId)
        .observeAsState(emptyList())

    var parentDescription by remember { mutableStateOf("") }
    var childDescription by remember { mutableStateOf("") }
    var selectedChild by remember {
        mutableStateOf<WorkPerformed?>(
            null
        )
    }

    LaunchedEffect(parentWorkPerformed) {
        parentWorkPerformed?.let {
            parentDescription = it.wpDescription
        }
    }

    WorkPerformedMergeScreen(
        workPerformedList = workPerformedList,
        parentDescription = parentDescription,
        onParentDescriptionChange = { parentDescription = it },
        onParentSelected = {
            mainViewModel.setWorkPerformedId(it.workPerformedId)
            parentDescription = it.wpDescription
        },
        childList = childList,
        onRemoveChild = { child ->
            coroutineScope.launch {
                workOrderViewModel.deleteWorkPerformedMerged(
                    child.workPerformedMerged.workPerformedMergeId,
                    df.getCurrentUTCTimeAsString()
                )
            }
        },
        childDescription = childDescription,
        onChildDescriptionChange = { childDescription = it },
        onChildSelected = {
            selectedChild = it
            childDescription = it.wpDescription
        },
        onMergeClick = {
            val childId = selectedChild?.workPerformedId
            if (childId != null && childId != wpId) {
                coroutineScope.launch {
                    workOrderViewModel.insertWorkPerformedMerged(
                        WorkPerformedMerged(
                            nf.generateRandomIdAsLong(),
                            wpId,
                            childId,
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    )
                    childDescription = ""
                    selectedChild = null
                }
            }
        },
        onDoneClick = {
            navController.popBackStack()
        },
        onListItemSelected = {
            if (mainViewModel.getWorkPerformedIsMaster()) {
                mainViewModel.setWorkPerformedId(it.workPerformedId)
                parentDescription = it.wpDescription
            } else {
                selectedChild = it
                childDescription = it.wpDescription
            }
        }
    )
}

@Composable
fun WorkOrderHistoryMaterialUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val initialHistory = mainViewModel.getWorkOrderHistory()
    val materialId = mainViewModel.getMaterialId()

    if (initialHistory == null || materialId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val historyWithDates by workOrderViewModel.getWorkOrderHistoriesById(initialHistory.woHistoryId)
        .observeAsState()
    val materialSuggestions by workOrderViewModel.getMaterialsList().observeAsState(emptyList())

    var materialHistory by remember {
        mutableStateOf<WorkOrderHistoryMaterialCombined?>(
            null
        )
    }
    LaunchedEffect(materialId) {
        materialHistory = workOrderViewModel.getWorkOrderHistoryMaterialCombined(materialId)
    }

    if (materialHistory == null || historyWithDates == null) return

    var mName by remember { mutableStateOf(materialHistory!!.material.mName) }
    var qty by remember { mutableStateOf(nf.displayNumberFromDouble(materialHistory!!.workOrderHistoryMaterial.wohmQuantity)) }

    WorkOrderHistoryMaterialUpdateScreen(
        info = stringResource(R.string.edit_material_used_for_wo_) +
                " ${historyWithDates!!.workOrder.woNumber} " +
                stringResource(R.string._at_) + " ${historyWithDates!!.workOrder.woAddress}\n" +
                historyWithDates!!.workOrder.woDescription,
        materialName = mName,
        onMaterialNameChange = { mName = it },
        materialSuggestions = materialSuggestions.map { it.mName },
        quantity = qty,
        onQuantityChange = { qty = it },
        originalMaterialLabel = stringResource(R.string.original_material_) + " ${materialHistory!!.material.mName}",
        originalQuantityLabel = stringResource(R.string.original_quantity_) + " ${
            nf.displayNumberFromDouble(
                materialHistory!!.workOrderHistoryMaterial.wohmQuantity
            )
        }",
        onDoneClick = {
            coroutineScope.launch {
                val material = workOrderViewModel.getMaterialSync(mName)
                if (material != null) {
                    workOrderViewModel.updateWorkOrderHistoryMaterial(
                        materialHistory!!.workOrderHistoryMaterial.copy(
                            wohmMaterialId = material.materialId,
                            wohmQuantity = qty.toDoubleOrNull() ?: 0.0,
                            wohmUpdateTime = df.getCurrentUTCTimeAsString()
                        )
                    )
                }
                navController.popBackStack()
            }
        },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun WorkOrderHistoryWorkPerformedUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val history = mainViewModel.getWorkOrderHistory()
    val workPerformedHistoryId = mainViewModel.getWorkPerformedHistoryId()

    if (history == null || workPerformedHistoryId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val workOrderHistoryWithDates by workOrderViewModel.getWorkOrderHistoriesById(history.woHistoryId)
        .observeAsState()
    val workPerformedHistory by workOrderViewModel.getWorkPerformedHistoryById(
        workPerformedHistoryId
    ).observeAsState()
    val workPerformedSuggestions by workOrderViewModel.getWorkPerformedAll()
        .observeAsState(emptyList())
    val areaSuggestions by workOrderViewModel.getAreasList().observeAsState(emptyList())

    WorkOrderHistoryWorkPerformedUpdateScreen(
        originalWorkOrderHistory = workOrderHistoryWithDates,
        originalWorkPerformedHistory = workPerformedHistory,
        workPerformedSuggestions = workPerformedSuggestions,
        areaSuggestions = areaSuggestions,
        onUpdate = { wpDescription, areaName, note ->
            coroutineScope.launch {
                val wp = workOrderViewModel.getWorkPerformedSync(wpDescription)
                val a = workOrderViewModel.getOrCreateArea(areaName)

                workPerformedHistory?.let { current ->
                    if (wp != null) {
                        workOrderViewModel.updateWorkOrderHistoryWorkPerformed(
                            current.workOrderHistoryWorkPerformed.copy(
                                wowpWorkPerformedId = wp.workPerformedId,
                                wowpAreaId = a?.areaId,
                                wowpNote = note,
                                wowpUpdateTime = df.getCurrentUTCTimeAsString()
                            )
                        )
                    }
                }
                navController.popBackStack()
            }
        },
        onBack = { navController.popBackStack() }
    )
}

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
        val totalHours = allTimesByDate.sumOf {
            df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime)
        }
        selectedTimeType = when {
            totalHours < 8.0 -> TimeWorkedTypes.REG_HOURS.value
            totalHours < 12.0 -> TimeWorkedTypes.OT_HOURS.value
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
            append(stringResource(R.string.total_hours))
            append(" ")
            val totalHours = existingTimes.sumOf {
                df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime)
            }
            append(nf.displayNumberFromDouble(totalHours))

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
                    errorMessage =
                        context.getString(R.string.time_adjusted_to_not_exceed_8_reg_hours)
                } else if (selectedTimeType == TimeWorkedTypes.OT_HOURS.value &&
                    hoursBefore + newSegmentHours > 12.0
                ) {
                    val allowedHours = 12.0 - hoursBefore
                    endTime = df.addHoursToCalendar(startTime, allowedHours)
                    errorMessage =
                        context.getString(R.string.time_adjusted_to_not_exceed_12_ot_hours)
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
                errorMessage =
                    context.getString(R.string.warning_start_time_overlaps_previous_end_time)
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

    val combined = mainViewModel.getWorkOrderHistoryTimeWorkedCombined() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    var startTime by remember {
        mutableStateOf(
            df.getCalendarFromTime(
                df.splitTimeFromDateTime(combined.timeWorked.wohtStartTime).joinToString(":")
            )
        )
    }
    var endTime by remember {
        mutableStateOf(
            df.getCalendarFromTime(
                df.splitTimeFromDateTime(combined.timeWorked.wohtEndTime).joinToString(":")
            )
        )
    }
    var selectedTimeType by remember { mutableIntStateOf(combined.timeWorked.wohtTimeType) }

    val allTimesByDate by workOrderViewModel.getTimeWorkedPerDay(combined.workDate.workDateId)
        .observeAsState(emptyList())

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(3000)
            errorMessage = null
        }
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

    WorkOrderHistoryTimeUpdateScreen(
        infoText = stringResource(R.string.work_order) + " ${combined.workOrderHistory.workOrder.woNumber}\n" +
                combined.workOrderHistory.workOrder.woDescription,
        originalTimeText = stringResource(R.string.original_time) + " " +
                df.get12HourDisplay(
                    df.splitTimeFromDateTime(combined.timeWorked.wohtStartTime).joinToString(":")
                ) +
                " - " +
                df.get12HourDisplay(
                    df.splitTimeFromDateTime(combined.timeWorked.wohtEndTime).joinToString(":")
                ),
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
                    errorMessage =
                        context.getString(R.string.time_adjusted_to_not_exceed_8_reg_hours)
                } else if (selectedTimeType == TimeWorkedTypes.OT_HOURS.value &&
                    hoursBefore + newSegmentHours > 12.0
                ) {
                    val allowedHours = 12.0 - hoursBefore
                    endTime = df.addHoursToCalendar(startTime, allowedHours)
                    errorMessage =
                        context.getString(R.string.time_adjusted_to_not_exceed_12_ot_hours)
                } else {
                    endTime = newEnd
                }
                showStartTimeWarning = false
                errorMessage = null
            }, endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE), false).show()
        },
        onSaveClick = {
            val latestTime = if (allTimesByDate.isNotEmpty()) {
                allTimesByDate.filter { it.timeWorked.woHistoryTimeWorkedId != combined.timeWorked.woHistoryTimeWorkedId }
                    .maxOfOrNull { it.timeWorked.wohtEndTime }
            } else null

            val currentStart = df.getDateTimeFromDateAndTime(
                combined.workDate.wdDate,
                df.getTimeDisplay(startTime)
            )

            if (!showStartTimeWarning && latestTime != null && currentStart < latestTime) {
                errorMessage =
                    context.getString(R.string.warning_start_time_overlaps_previous_end_time)
                showStartTimeWarning = true
            } else {
                coroutineScope.launch {
                    workOrderViewModel.updateWorkOrderHistoryTimeWorked(
                        combined.timeWorked.copy(
                            wohtStartTime = df.getDateTimeFromDateAndTime(
                                combined.workDate.wdDate,
                                df.getTimeDisplay(startTime)
                            ),
                            wohtEndTime = df.getDateTimeFromDateAndTime(
                                combined.workDate.wdDate,
                                df.getTimeDisplay(endTime)
                            ),
                            wohtTimeType = selectedTimeType,
                            wohtUpdateTime = df.getCurrentUTCTimeAsString()
                        )
                    )
                    showStartTimeWarning = false
                    navController.popBackStack()
                }
            }
        },
        onBackClick = { navController.popBackStack() },
        allTimesForDay = allTimesByDate.filter { it.timeWorked.wohtHistoryId == combined.timeWorked.wohtHistoryId },
        currentHistoryId = combined.timeWorked.wohtHistoryId,
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