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
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

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
    val workPerformedSuggestions by workOrderViewModel.workPerformedAll
        .observeAsState(emptyList())
    val areaSuggestions by workOrderViewModel.areasList.observeAsState(emptyList())

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