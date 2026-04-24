package ms.mattschlenkrich.paycalculator.ui.workorderhistory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.MaterialInSequence
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

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
    val workPerformedList by workOrderViewModel.workPerformedAll.observeAsState(emptyList())
    var area by remember { mutableStateOf("") }
    val areaList by workOrderViewModel.areasList.observeAsState(emptyList())
    var workPerformedNote by remember { mutableStateOf("") }
    val workPerformedActualList by remember(history.woHistoryId) {
        workOrderViewModel.getWorkPerformedCombinedByWorkOrderHistory(
            history.woHistoryId
        )
    }.observeAsState(emptyList())

    var materialQty by remember { mutableStateOf("") }
    var materialName by remember { mutableStateOf("") }
    val materialList by workOrderViewModel.materialsList.observeAsState(emptyList())
    val materialActualList by remember(history.woHistoryId) {
        workOrderViewModel.getMaterialsByHistory(history.woHistoryId)
    }.observeAsState(emptyList())

    val timeWorkedList by remember(history.woHistoryId) {
        workOrderViewModel.getTimeWorkedForWorkOrderHistory(history.woHistoryId)
    }.observeAsState(emptyList())

    val isWorkOrderValid = workOrderList.any { it.woNumber == workOrderNumber }

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
                val wp = workOrderViewModel.getOrCreateWorkPerformed(workPerformed)
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
                val m = workOrderViewModel.getOrCreateMaterial(materialName)
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
        onUpdateWorkPerformed = { item ->
            mainViewModel.setWorkPerformedHistoryId(item.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId)
            navController.navigate(Screen.WorkOrderHistoryWorkPerformedUpdate.route)
        }
    )
}