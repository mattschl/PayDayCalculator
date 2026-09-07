package ms.mattschlenkrich.paycalculator.ui.jobspec

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.viewmodel.JobSpecViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.ui.jobspec.composable.JobSpecUpdateScreen

@Composable
fun JobSpecUpdateRoute(
    mainViewModel: MainViewModel,
    jobSpecViewModel: JobSpecViewModel,
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

    val originalJs by jobSpecViewModel.getJobSpec(jsId).observeAsState()
    val jobSpecList by jobSpecViewModel.searchJobSpecs("").observeAsState(emptyList())

    if (originalJs == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        originalJs?.let { js ->
            var name by remember(js.jobSpecId) { mutableStateOf(js.jsName) }

            JobSpecUpdateScreen(
                title = stringResource(R.string.prefix_update) + js.jsName,
                jobSpecName = name,
                onJobSpecNameChange = { name = it },
                onUpdateClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) return@JobSpecUpdateScreen
                    if (jobSpecList.any { it.jsName == trimmedName && it.jobSpecId != js.jobSpecId }) {
                        Toast.makeText(
                            mainViewModel.getApplication(),
                            "Name already exists",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@JobSpecUpdateScreen
                    }

                    coroutineScope.launch {
                        jobSpecViewModel.updateJobSpec(
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
}