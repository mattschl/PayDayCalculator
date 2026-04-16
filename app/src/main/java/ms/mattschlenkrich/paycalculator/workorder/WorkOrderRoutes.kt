package ms.mattschlenkrich.paycalculator.workorder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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

@Composable
fun WorkOrderHistoryAddRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    val context = LocalContext.current
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

    WorkOrderHistoryAddScreen(
        workOrderList = workOrderList,
        initialWorkOrderNumber = "",
        initialRegHours = "",
        initialOtHours = "",
        initialDblOtHours = "",
        initialNote = "",
        onWorkOrderSearch = { _, _, _, _, _ ->
            // TODO: Implement Search
        },
        onWorkOrderAddEdit = { number, reg, ot, dbl, note, exists ->
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
                        workDate.workDateId,
                        newWo.workOrderId,
                        reg.toDoubleOrNull() ?: 0.0,
                        ot.toDoubleOrNull() ?: 0.0,
                        dbl.toDoubleOrNull() ?: 0.0,
                        note,
                        false,
                        df.getCurrentTimeAsString()
                    )
                    workOrderViewModel.insertWorkOrderHistory(history)
                    mainViewModel.setWorkOrderHistory(history)
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
                            workDate.workDateId,
                            wo.workOrderId,
                            reg.toDoubleOrNull() ?: 0.0,
                            ot.toDoubleOrNull() ?: 0.0,
                            dbl.toDoubleOrNull() ?: 0.0,
                            note,
                            false,
                            df.getCurrentTimeAsString()
                        )
                        workOrderViewModel.insertWorkOrderHistory(history)
                        mainViewModel.setWorkOrderHistory(history)
                        navController.navigate(Screen.WorkOrderHistoryUpdate.route) {
                            popUpTo(Screen.WorkOrderHistoryAdd.route) { inclusive = true }
                        }
                    }
                }
            }
        },
        onDone = { number, reg, ot, dbl, note, _ ->
            val wo = workOrderList.find { it.woNumber == number }
            if (wo != null) {
                coroutineScope.launch {
                    val history = ms.mattschlenkrich.paycalculator.data.WorkOrderHistory(
                        nf.generateRandomIdAsLong(),
                        workDate.workDateId,
                        wo.workOrderId,
                        reg.toDoubleOrNull() ?: 0.0,
                        ot.toDoubleOrNull() ?: 0.0,
                        dbl.toDoubleOrNull() ?: 0.0,
                        note,
                        false,
                        df.getCurrentTimeAsString()
                    )
                    workOrderViewModel.insertWorkOrderHistory(history)
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
    val context = LocalContext.current
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
            // Logic to update WO
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
            navController.navigate(Screen.WorkDateTimes.route)
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
                        item.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId
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
        onBack = { navController.popBackStack() }
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
    val context = LocalContext.current
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
    // TODO: Implement JobSpecViewScreen
}

@Composable
fun JobSpecUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    // TODO: Implement JobSpecUpdateScreen
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
    val context = LocalContext.current
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
    // TODO: Implement MaterialMergeScreen
}

@Composable
fun MaterialQuantityUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    // TODO: Implement MaterialQuantityUpdateScreen
}

@Composable
fun WorkOrderViewRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    // TODO: Implement WorkOrderViewScreen
}

@Composable
fun WorkOrderAddRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    // TODO: Implement WorkOrderAddScreen
}

@Composable
fun WorkOrderUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    // TODO: Implement WorkOrderUpdateScreen
}

@Composable
fun WorkOrderLookupRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    // TODO: Implement WorkOrderLookupScreen
}

@Composable
fun WorkPerformedViewRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    // TODO: Implement WorkPerformedViewScreen
}

@Composable
fun WorkPerformedUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    // TODO: Implement WorkPerformedUpdateScreen
}

@Composable
fun WorkPerformedMergeRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    // TODO: Implement WorkPerformedMergeScreen
}

@Composable
fun WorkOrderHistoryMaterialUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: androidx.navigation.NavController
) {
    // TODO: Implement WorkOrderHistoryMaterialUpdateScreen
}