package ms.mattschlenkrich.paycalculator.ui.workorderhistory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.viewmodel.AreaViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkPerformedViewModel
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable.WorkOrderHistoryWorkPerformedUpdateScreen

@Composable
fun WorkOrderHistoryWorkPerformedUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    workPerformedViewModel: WorkPerformedViewModel,
    areaViewModel: AreaViewModel,
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

    val workOrderHistoryWithDates by workOrderViewModel.getWorkOrderHistory(history.woHistoryId)
        .observeAsState()
    val workPerformedHistory by workPerformedViewModel.getWorkPerformedHistoryById(
        workPerformedHistoryId
    ).observeAsState()
    val workPerformedSuggestions by workPerformedViewModel.getWorkPerformedAll()
        .observeAsState(emptyList())
    val areaSuggestions by areaViewModel.getAreasList().observeAsState(emptyList())

    WorkOrderHistoryWorkPerformedUpdateScreen(
        originalWorkOrderHistory = workOrderHistoryWithDates,
        originalWorkPerformedHistory = workPerformedHistory,
        workPerformedSuggestions = workPerformedSuggestions,
        areaSuggestions = areaSuggestions,
        onUpdate = { wpDescription, areaName, note ->
            coroutineScope.launch {
                val wp = workPerformedViewModel.getWorkPerformedSync(wpDescription)
                val a = areaViewModel.getOrCreateArea(areaName)

                workPerformedHistory?.let { current ->
                    if (wp != null) {
                        workPerformedViewModel.updateWorkOrderHistoryWorkPerformed(
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
        }
    )
}