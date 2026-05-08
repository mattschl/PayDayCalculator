package ms.mattschlenkrich.paycalculator.ui.workorder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.ui.workorder.composable.WorkOrderJobSpecUpdateScreen

@Composable
fun WorkOrderJobSpecUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val wo = mainViewModel.getWorkOrder()
    val wojsId = mainViewModel.getWorkOrderJobSpecId()

    if (wo == null || wojsId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val originalWojs by workOrderViewModel.getWorkOrderJobSpec(wojsId).observeAsState()
    val jobSpecSuggestions by workOrderViewModel.jobSpecsAll.observeAsState(emptyList())
    val areaSuggestions by workOrderViewModel.areasList.observeAsState(emptyList())

    WorkOrderJobSpecUpdateScreen(
        workOrder = wo,
        originalJobSpec = originalWojs,
        jobSpecSuggestions = jobSpecSuggestions,
        areaSuggestions = areaSuggestions,
        onUpdate = { jsName, areaName, note ->
            coroutineScope.launch {
                val js = workOrderViewModel.getOrCreateJobSpec(jsName.trim())
                val a = workOrderViewModel.getOrCreateArea(areaName.trim())
                originalWojs?.let { combined ->
                    workOrderViewModel.updateWorkOrderJobSpec(
                        combined.workOrderJobSpec.copy(
                            wojsJobSpecId = js.jobSpecId,
                            wojsAreaId = a?.areaId,
                            wojsNote = note?.trim(),
                            wojsUpdateTime = df.getCurrentUTCTimeAsString()
                        )
                    )
                }
                navController.popBackStack()
            }
        },
        onBack = {
            navController.popBackStack()
        }
    )
}