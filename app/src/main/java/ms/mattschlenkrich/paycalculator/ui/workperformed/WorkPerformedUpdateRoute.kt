package ms.mattschlenkrich.paycalculator.ui.workperformed

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkPerformedViewModel
import ms.mattschlenkrich.paycalculator.ui.workperformed.composable.WorkPerformedUpdateScreen

@Composable
fun WorkPerformedUpdateRoute(
    mainViewModel: MainViewModel,
    workPerformedViewModel: WorkPerformedViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val wpId = mainViewModel.getWorkPerformedId()
    if (wpId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val originalWp by workPerformedViewModel.getWorkPerformed(wpId).observeAsState()
    val workPerformedList by workPerformedViewModel.getWorkPerformedAll()
        .observeAsState(emptyList())

    if (originalWp == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
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
                        }) {
                        Toast.makeText(
                            context,
                            R.string.msg_description_already_exists,
                            Toast.LENGTH_SHORT
                        ).show()
                        return@WorkPerformedUpdateScreen
                    }

                    coroutineScope.launch {
                        workPerformedViewModel.updateWorkPerformed(
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
                title = stringResource(R.string.prefix_update) + wp.wpDescription
            )
        }
    }
}