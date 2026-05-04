package ms.mattschlenkrich.paycalculator.ui.jobspec

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
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel

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
    val jobSpecList by workOrderViewModel.jobSpecsAll.observeAsState(emptyList())

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