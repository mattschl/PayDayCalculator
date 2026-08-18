package ms.mattschlenkrich.paycalculator.ui.workorderhistory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.model.MaterialInSequence
import ms.mattschlenkrich.paycalculator.data.viewmodel.AreaViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MaterialViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkPerformedViewModel
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable.WorkOrderHistoryUpdateScreen

@Composable
fun WorkOrderHistoryUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    workPerformedViewModel: WorkPerformedViewModel,
    materialViewModel: MaterialViewModel,
    areaViewModel: AreaViewModel,
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val settings by settingsViewModel.settings.observeAsState()
    val minColumnWidth = settings?.minColumnWidth ?: DEFAULT_MIN_COLUMN_WIDTH

    val initialHistory = mainViewModel.getWorkOrderHistory() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val historyWithDates by workOrderViewModel.getWorkOrderHistory(initialHistory.woHistoryId)
        .observeAsState()

    if (historyWithDates == null) {
        return
    }

    val history = historyWithDates!!.history
    val workDate = historyWithDates!!.workDate
    val employer = mainViewModel.getEmployer() ?: return

    var workOrderNumber by rememberSaveable { mutableStateOf(historyWithDates!!.workOrder.woNumber) }
    val workOrderList by workOrderViewModel.getWorkOrdersByEmployerId(employer.employerId)
        .observeAsState(emptyList())
    var workOrderDescription by rememberSaveable { mutableStateOf(historyWithDates!!.workOrder.woDescription) }

    var regHours by rememberSaveable(history.woHistoryUpdateTime) {
        mutableStateOf(
            nf.displayNumberFromDouble(
                history.woHistoryRegHours
            )
        )
    }
    var otHours by rememberSaveable(history.woHistoryUpdateTime) {
        mutableStateOf(
            nf.displayNumberFromDouble(
                history.woHistoryOtHours
            )
        )
    }
    var dblOtHours by rememberSaveable(history.woHistoryUpdateTime) {
        mutableStateOf(
            nf.displayNumberFromDouble(
                history.woHistoryDblOtHours
            )
        )
    }
    var note by rememberSaveable { mutableStateOf(history.woHistoryNote ?: "") }

    var workPerformed by rememberSaveable { mutableStateOf("") }
    val workPerformedList by workPerformedViewModel.getWorkPerformedAll()
        .observeAsState(emptyList())
    var area by rememberSaveable { mutableStateOf("") }
    val areaList by areaViewModel.getAreasList().observeAsState(emptyList())
    var workPerformedNote by rememberSaveable { mutableStateOf("") }
    val workPerformedActualList by remember(history.woHistoryId) {
        workPerformedViewModel.getWorkPerformedCombinedByWorkOrderHistory(
            history.woHistoryId
        )
    }.observeAsState(emptyList())

    var materialQty by rememberSaveable { mutableStateOf("") }
    var materialName by rememberSaveable { mutableStateOf("") }
    val materialList by materialViewModel.getMaterialsList().observeAsState(emptyList())
    val materialActualList by remember(history.woHistoryId) {
        materialViewModel.getMaterialsByHistory(history.woHistoryId)
    }.observeAsState(emptyList())

    val timeWorkedList by remember(history.woHistoryId) {
        workOrderViewModel.getTimeWorkedForWorkOrderHistory(history.woHistoryId)
    }.observeAsState(emptyList())

    val isWorkOrderValid = workOrderList.any { it.woNumber == workOrderNumber }

    var isSaving by rememberSaveable { mutableStateOf(false) }

    WorkOrderHistoryUpdateScreen(
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
                mainViewModel.setWorkOrderHistory(history)
                navController.navigate(Screen.WorkOrderHistoryTime.route)
            }
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
                val wp = workPerformedViewModel.getOrCreateWorkPerformed(workPerformed)
                val a = areaViewModel.getOrCreateArea(area)
                if (wp != null) {
                    workPerformedViewModel.insertWorkOrderHistoryWorkPerformed(
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
                    // area = "" // Do not reset the area as per user request
                    workPerformedNote = ""
                }
            }
        },
        workPerformedActualList = workPerformedActualList,
        onWorkPerformedItemClick = { item, action ->
            if (action == 0) { // Delete
                coroutineScope.launch {
                    workPerformedViewModel.deleteWorkOrderHistoryWorkPerformed(
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
                val m = materialViewModel.getOrCreateMaterial(materialName)
                if (m != null) {
                    materialViewModel.insertWorkOrderHistoryMaterial(
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
                    materialViewModel.deleteWorkOrderHistoryMaterial(
                        item.workOrderHistoryMaterialId,
                        df.getCurrentUTCTimeAsString()
                    )
                }
            }
        },
        onDone = {
            if (!isSaving) {
                isSaving = true
                coroutineScope.launch {
                    try {
                        if (workPerformed.isNotBlank()) {
                            val wp = workPerformedViewModel.getOrCreateWorkPerformed(workPerformed)
                            val a = areaViewModel.getOrCreateArea(area)
                            if (wp != null) {
                                workPerformedViewModel.insertWorkOrderHistoryWorkPerformed(
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
                            }
                        }
                        if (materialName.isNotBlank()) {
                            val m = materialViewModel.getOrCreateMaterial(materialName)
                            if (m != null) {
                                materialViewModel.insertWorkOrderHistoryMaterial(
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
                            }
                        }
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
                    } catch (_: Exception) {
                        isSaving = false
                    }
                }
            }
        },
        onUpdateWorkPerformed = { item ->
            mainViewModel.setWorkPerformedHistoryId(item.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId)
            navController.navigate(Screen.WorkOrderHistoryWorkPerformedUpdate.route)
        },
        onUpdateWorkPerformedDefinition = { item ->
            mainViewModel.setWorkPerformedId(item.workPerformed.workPerformedId)
            navController.navigate(Screen.WorkPerformedUpdate.route)
        },
        onUpdateMaterialInHistory = { item ->
            mainViewModel.setMaterialId(item.workOrderHistoryMaterialId)
            navController.navigate(Screen.WorkOrderHistoryMaterialUpdate.route)
        },
        onUpdateMaterialDefinition = { item ->
            coroutineScope.launch {
                val material = materialViewModel.getMaterialSync(item.materialId)
                if (material != null) {
                    mainViewModel.setMaterial(material)
                    navController.navigate(Screen.MaterialUpdate.route)
                }
            }
        },
        isSaving = isSaving,
        minColumnWidth = minColumnWidth
    )
}