package ms.mattschlenkrich.paycalculator.workorder

import android.app.TimePickerDialog
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import java.util.Calendar

@Composable
fun WorkOrderHistoryAddRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
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
    val initialRegHours = tempInfo?.let { nf.getNumberFromDouble(it.woHistoryRegHours) } ?: ""
    val initialOtHours = tempInfo?.let { nf.getNumberFromDouble(it.woHistoryOtHours) } ?: ""
    val initialDblOtHours = tempInfo?.let { nf.getNumberFromDouble(it.woHistoryDblOtHours) } ?: ""
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
                ms.mattschlenkrich.paycalculator.data.TempWorkOrderHistoryInfo(
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
            if (!exists) {
                coroutineScope.launch {
                    val newWo = ms.mattschlenkrich.paycalculator.data.WorkOrder(
                        nf.generateRandomIdAsLong(),
                        number,
                        workDate.wdEmployerId,
                        "", // Address
                        "", // Description
                        false,
                        df.getCurrentTimeAsString()
                    )
                    workOrderViewModel.insertWorkOrder(newWo)
                    delay(WAIT_250)
                    val history = ms.mattschlenkrich.paycalculator.data.WorkOrderHistory(
                        nf.generateRandomIdAsLong(),
                        newWo.workOrderId,
                        workDate.workDateId,
                        reg.toDoubleOrNull() ?: 0.0,
                        ot.toDoubleOrNull() ?: 0.0,
                        dbl.toDoubleOrNull() ?: 0.0,
                        nt,
                        false,
                        df.getCurrentTimeAsString()
                    )
                    workOrderViewModel.insertWorkOrderHistory(history)
                    mainViewModel.setWorkOrderHistory(history)
                    mainViewModel.setTempWorkOrderHistoryInfo(null)
                    mainViewModel.setWorkOrder(null)
                    navController.navigate(Screen.WorkOrderHistoryUpdate.route) {
                        popUpTo(Screen.WorkOrderHistoryAdd.route) { inclusive = true }
                    }
                }
            } else {
                val wo = workOrderList.find { it.woNumber == number }
                if (wo != null) {
                    coroutineScope.launch {
                        val history = ms.mattschlenkrich.paycalculator.data.WorkOrderHistory(
                            nf.generateRandomIdAsLong(),
                            wo.workOrderId,
                            workDate.workDateId,
                            reg.toDoubleOrNull() ?: 0.0,
                            ot.toDoubleOrNull() ?: 0.0,
                            dbl.toDoubleOrNull() ?: 0.0,
                            nt,
                            false,
                            df.getCurrentTimeAsString()
                        )
                        workOrderViewModel.insertWorkOrderHistory(history)
                        mainViewModel.setWorkOrderHistory(history)
                        mainViewModel.setTempWorkOrderHistoryInfo(null)
                        mainViewModel.setWorkOrder(null)
                        navController.navigate(Screen.WorkOrderHistoryUpdate.route) {
                            popUpTo(Screen.WorkOrderHistoryAdd.route) { inclusive = true }
                        }
                    }
                }
            }
        },
        onDone = { number, reg, ot, dbl, nt, _ ->
            val wo = workOrderList.find { it.woNumber == number }
            if (wo != null) {
                coroutineScope.launch {
                    val history = ms.mattschlenkrich.paycalculator.data.WorkOrderHistory(
                        nf.generateRandomIdAsLong(),
                        wo.workOrderId,
                        workDate.workDateId,
                        reg.toDoubleOrNull() ?: 0.0,
                        ot.toDoubleOrNull() ?: 0.0,
                        dbl.toDoubleOrNull() ?: 0.0,
                        nt,
                        false,
                        df.getCurrentTimeAsString()
                    )
                    workOrderViewModel.insertWorkOrderHistory(history)
                    mainViewModel.setTempWorkOrderHistoryInfo(null)
                    mainViewModel.setWorkOrder(null)
                    navController.popBackStack()
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
    navController: androidx.navigation.NavController
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

    var regHours by remember { mutableStateOf(nf.getNumberFromDouble(history.woHistoryRegHours)) }
    var otHours by remember { mutableStateOf(nf.getNumberFromDouble(history.woHistoryOtHours)) }
    var dblOtHours by remember { mutableStateOf(nf.getNumberFromDouble(history.woHistoryDblOtHours)) }
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
        onWorkOrderLongClick = { /* TODO */ },
        workOrderDescription = workOrderDescription,
        onWorkOrderButtonClick = {
            coroutineScope.launch {
                workOrderViewModel.updateWorkOrder(
                    historyWithDates!!.workOrder.copy(
                        woNumber = workOrderNumber,
                        woDescription = workOrderDescription,
                        woUpdateTime = df.getCurrentTimeAsString()
                    )
                )
            }
        },
        workOrderButtonText = stringResource(R.string.update_work_order),
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
        addTimeButtonText = stringResource(R.string.add_time),
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
                        ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWorkPerformed(
                            nf.generateRandomIdAsLong(),
                            history.woHistoryId,
                            wp.workPerformedId,
                            a?.areaId,
                            workPerformedNote,
                            workPerformedActualList.size + 1,
                            false,
                            df.getCurrentTimeAsString()
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
                        df.getCurrentTimeAsString()
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
                        ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryMaterial(
                            nf.generateRandomIdAsLong(),
                            history.woHistoryId,
                            m.materialId,
                            materialQty.toDoubleOrNull() ?: 1.0,
                            materialActualList.size + 1,
                            false,
                            df.getCurrentTimeAsString()
                        )
                    )
                    materialQty = ""
                    materialName = ""
                }
            }
        },
        materialActualList = materialActualList.map {
            ms.mattschlenkrich.paycalculator.data.MaterialInSequence(
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
                        df.getCurrentTimeAsString()
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
                            woHistoryUpdateTime = df.getCurrentTimeAsString()
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
    navController: androidx.navigation.NavController
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
        }
    )
}

@Composable
fun AreaUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
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
                        ms.mattschlenkrich.paycalculator.data.Areas(
                            area.areaId,
                            trimmedName,
                            false,
                            df.getCurrentTimeAsString()
                        )
                    )
                    navController.popBackStack()
                }
            },
            onCancelClick = {
                navController.popBackStack()
            }
        )
    }
}

@Composable
fun JobSpecViewRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
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
        }
    )
}

@Composable
fun JobSpecUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
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
                            jsUpdateTime = df.getCurrentTimeAsString()
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
    navController: androidx.navigation.NavController
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
        mutableStateOf<ms.mattschlenkrich.paycalculator.data.JobSpec?>(
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
                    df.getCurrentTimeAsString()
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
                        ms.mattschlenkrich.paycalculator.data.JobSpecMerged(
                            nf.generateRandomIdAsLong(),
                            jsId,
                            childId,
                            false,
                            df.getCurrentTimeAsString()
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
    navController: androidx.navigation.NavController
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
        }
    )
}

@Composable
fun MaterialUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
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
                val material = ms.mattschlenkrich.paycalculator.data.Material(
                    oldMaterial.materialId,
                    name.trim(),
                    nf.getDoubleFromDollars(cost.trim()),
                    nf.getDoubleFromDollars(price.trim()),
                    oldMaterial.mIsDeleted,
                    df.getCurrentTimeAsString()
                )
                workOrderViewModel.updateMaterial(material)
                mainViewModel.setMaterial(material)
                navController.popBackStack()
            }
        },
        onMergeClick = {
            coroutineScope.launch {
                val material = ms.mattschlenkrich.paycalculator.data.Material(
                    oldMaterial.materialId,
                    name.trim(),
                    nf.getDoubleFromDollars(cost.trim()),
                    nf.getDoubleFromDollars(price.trim()),
                    oldMaterial.mIsDeleted,
                    df.getCurrentTimeAsString()
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
    navController: androidx.navigation.NavController
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
        mutableStateOf<ms.mattschlenkrich.paycalculator.data.Material?>(
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
                    df.getCurrentTimeAsString()
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
                        ms.mattschlenkrich.paycalculator.data.MaterialMerged(
                            nf.generateRandomIdAsLong(),
                            materialId,
                            childId,
                            false,
                            df.getCurrentTimeAsString()
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
    navController: androidx.navigation.NavController
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
        mutableStateOf<ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryMaterialCombined?>(
            null
        )
    }
    LaunchedEffect(materialId) {
        materialHistory = workOrderViewModel.getWorkOrderHistoryMaterialCombined(materialId)
    }

    if (materialHistory == null || historyWithDates == null) return

    var qty by remember { mutableStateOf(nf.getNumberFromDouble(materialHistory!!.workOrderHistoryMaterial.wohmQuantity)) }

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
                        wohmUpdateTime = df.getCurrentTimeAsString()
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
    navController: androidx.navigation.NavController
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
        remember { mutableStateOf(emptyList<ms.mattschlenkrich.paycalculator.data.WorkOrder>()) }
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
        onAddNewWorkOrderClick = { navController.navigate(Screen.WorkOrderAdd.route) }
    )
}

@Composable
fun WorkOrderAddRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    val currentEmployer = mainViewModel.getEmployer()

    var selectedEmployer by remember {
        mutableStateOf<ms.mattschlenkrich.paycalculator.data.Employers?>(
            currentEmployer
        )
    }
    var woNumber by remember { mutableStateOf("") }
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
                    val newWo = ms.mattschlenkrich.paycalculator.data.WorkOrder(
                        nf.generateRandomIdAsLong(),
                        woNumber.trim(),
                        employerId,
                        address.trim(),
                        description.trim(),
                        false,
                        df.getCurrentTimeAsString()
                    )
                    workOrderViewModel.insertWorkOrder(newWo)
                    navController.popBackStack()
                }
            }
        },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun WorkOrderUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
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
        emptyList<ms.mattschlenkrich.paycalculator.data.WorkPerformedAndQuantity>()
    val materialsList = emptyList<ms.mattschlenkrich.paycalculator.data.MaterialAndQuantity>()

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
                        ms.mattschlenkrich.paycalculator.data.WorkOrderJobSpec(
                            nf.generateRandomIdAsLong(),
                            initialWo.workOrderId,
                            js.jobSpecId,
                            a?.areaId,
                            workPerformedNote.trim(),
                            addedJobSpecs.size + 1,
                            false,
                            df.getCurrentTimeAsString()
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
                        woUpdateTime = df.getCurrentTimeAsString()
                    )
                )
                navController.popBackStack()
            }
        }
    )
}

@Composable
fun WorkOrderLookupRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    val employer = mainViewModel.getEmployer()
    var searchQuery by remember { mutableStateOf("") }
    val workOrders by if (employer != null) {
        workOrderViewModel.searchWorkOrders(employer.employerId, "%$searchQuery%")
            .observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList<ms.mattschlenkrich.paycalculator.data.WorkOrder>()) }
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
    navController: androidx.navigation.NavController
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
        }
    )
}

@Composable
fun WorkPerformedUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
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
                            wpUpdateTime = df.getCurrentTimeAsString()
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
    navController: androidx.navigation.NavController
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
        mutableStateOf<ms.mattschlenkrich.paycalculator.data.WorkPerformed?>(
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
                    df.getCurrentTimeAsString()
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
                        ms.mattschlenkrich.paycalculator.data.WorkPerformedMerged(
                            nf.generateRandomIdAsLong(),
                            wpId,
                            childId,
                            false,
                            df.getCurrentTimeAsString()
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
    navController: androidx.navigation.NavController
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
        mutableStateOf<ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryMaterialCombined?>(
            null
        )
    }
    LaunchedEffect(materialId) {
        materialHistory = workOrderViewModel.getWorkOrderHistoryMaterialCombined(materialId)
    }

    if (materialHistory == null || historyWithDates == null) return

    var mName by remember { mutableStateOf(materialHistory!!.material.mName) }
    var qty by remember { mutableStateOf(nf.getNumberFromDouble(materialHistory!!.workOrderHistoryMaterial.wohmQuantity)) }

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
            nf.getNumberFromDouble(
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
                            wohmUpdateTime = df.getCurrentTimeAsString()
                        )
                    )
                }
                navController.popBackStack()
            }
        }
    )
}

@Composable
fun WorkOrderHistoryWorkPerformedUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
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
                                wowpUpdateTime = df.getCurrentTimeAsString()
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
    navController: androidx.navigation.NavController
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

    var startTime by remember {
        mutableStateOf(
            df.getCalendarFromTime(
                df.getCurrentTimeAsString().split(" ")[1]
            )
        )
    }
    var endTime by remember {
        mutableStateOf(
            df.getCalendarFromTime(
                df.getCurrentTimeAsString().split(" ")[1]
            )
        )
    }
    var selectedTimeType by remember { mutableIntStateOf(ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes.REG_HOURS.value) }

    val totalHours = df.getTimeWorked(
        df.getTimeDisplay(startTime),
        df.getTimeDisplay(endTime)
    )

    WorkOrderHistoryTimeScreen(
        infoText = stringResource(R.string.work_order) + " ${historyWithDates!!.workOrder.woNumber}\n" +
                historyWithDates!!.workOrder.woDescription,
        hoursSummaryText = stringResource(R.string.total_hours) + " ${
            nf.getNumberFromDouble(
                existingTimes.sumOf {
                    df.getTimeWorked(it.timeWorked.wohtStartTime, it.timeWorked.wohtEndTime)
                }
            )
        }",
        startTime = startTime,
        endTime = endTime,
        totalTimeText = nf.getNumberFromDouble(totalHours) + " " + stringResource(R.string.hours),
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
                workOrderViewModel.insertWorkOrderHistoryTimeWorked(
                    ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorked(
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
                        df.getCurrentTimeAsString()
                    )
                )
            }
        },
        onDoneClick = { navController.popBackStack() },
        existingTimes = existingTimes,
        onTimeClick = { combined ->
            mainViewModel.setWorkOrderHistoryTimeWorkedCombined(combined)
            navController.navigate(Screen.WorkOrderHistoryTimeUpdate.route)
        }
    )
}

@Composable
fun WorkOrderHistoryTimeUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
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

    val totalHours = df.getTimeWorked(
        df.getTimeDisplay(startTime),
        df.getTimeDisplay(endTime)
    )

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
        totalTimeText = nf.getNumberFromDouble(totalHours) + " " + stringResource(R.string.hours),
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
        onSaveClick = {
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
                        wohtUpdateTime = df.getCurrentTimeAsString()
                    )
                )
                navController.popBackStack()
            }
        },
        onBackClick = { navController.popBackStack() }
    )
}