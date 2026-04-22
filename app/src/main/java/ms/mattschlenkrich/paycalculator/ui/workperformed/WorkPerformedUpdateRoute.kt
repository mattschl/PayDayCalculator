package ms.mattschlenkrich.paycalculator.ui.workperformed

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
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

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