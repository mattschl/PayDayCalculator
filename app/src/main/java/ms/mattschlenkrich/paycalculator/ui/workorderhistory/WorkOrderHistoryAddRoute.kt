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
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.model.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable.WorkOrderHistoryAddScreen

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

    LaunchedEffect(Unit) {
        mainViewModel.setWorkOrderHistory(null)
    }

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

    var isSaving by rememberSaveable { mutableStateOf(false) }

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
            if (!isSaving) {
                val wo = workOrderList.find { it.woNumber == number }
                if (wo != null) {
                    isSaving = true
                    coroutineScope.launch {
                        try {
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
                            mainViewModel.setWorkOrderHistory(history)
                            navController.navigate(Screen.WorkOrderHistoryUpdate.route) {
                                popUpTo(Screen.WorkOrderHistoryAdd.route) { inclusive = true }
                            }
                        } catch (_: Exception) {
                            isSaving = false
                        }
                    }
                }
            }
        },
        onAddTime = { number, reg, ot, dbl, nt, _ ->
            if (!isSaving) {
                val wo = workOrderList.find { it.woNumber == number }
                if (wo != null) {
                    isSaving = true
                    coroutineScope.launch {
                        try {
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
                            navController.navigate(Screen.WorkOrderHistoryUpdate.route) {
                                popUpTo(Screen.WorkOrderHistoryAdd.route) { inclusive = true }
                            }
                            navController.navigate(Screen.WorkOrderHistoryTime.route)
                        } catch (_: Exception) {
                            isSaving = false
                        }
                    }
                }
            }
        },
        displayDate = df.getDisplayDate(workDate.wdDate),
        displayEmployer = employer.employerName
    )
}