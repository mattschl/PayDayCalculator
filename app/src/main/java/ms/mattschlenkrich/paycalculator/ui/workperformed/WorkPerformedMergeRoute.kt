package ms.mattschlenkrich.paycalculator.ui.workperformed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformedMerged
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel

@Composable
fun WorkPerformedMergeRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val wpId = mainViewModel.getWorkPerformedId()
    if (wpId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val workPerformedList by workOrderViewModel.workPerformedAll.observeAsState(emptyList())
    val parentWorkPerformed by workOrderViewModel.getWorkPerformed(wpId).observeAsState()
    val childList by workOrderViewModel.getWorkPerformedAndChildList(wpId)
        .observeAsState(emptyList())

    var parentDescription by remember { mutableStateOf("") }
    var childDescription by remember { mutableStateOf("") }
    var selectedChild by remember {
        mutableStateOf<WorkPerformed?>(
            null
        )
    }

    LaunchedEffect(parentWorkPerformed) {
        parentWorkPerformed?.let {
            parentDescription = it.wpDescription
        }
    }

    WorkPerformedMergeScreen(
        workPerformedList = workPerformedList,
        parentDescription = parentDescription,
        onParentDescriptionChange = { parentDescription = it },
        onParentSelected = {
            mainViewModel.setWorkPerformedId(it.workPerformedId)
            parentDescription = it.wpDescription
        },
        childList = childList,
        onRemoveChild = { child ->
            coroutineScope.launch {
                workOrderViewModel.deleteWorkPerformedMerged(
                    child.workPerformedMerged.workPerformedMergeId,
                    df.getCurrentUTCTimeAsString()
                )
            }
        },
        childDescription = childDescription,
        onChildDescriptionChange = { childDescription = it },
        onChildSelected = {
            selectedChild = it
            childDescription = it.wpDescription
        },
        onMergeAction = { action ->
            val childId = selectedChild?.workPerformedId
            if (childId != null && childId != wpId) {
                coroutineScope.launch {
                    if (action == 1) { // Keep
                        workOrderViewModel.insertWorkPerformedMerged(
                            WorkPerformedMerged(
                                nf.generateRandomIdAsLong(),
                                wpId,
                                childId,
                                false,
                                df.getCurrentUTCTimeAsString()
                            )
                        )
                    } else if (action == 2) { // Replace and delete
                        workOrderViewModel.updateWorkPerformedMerged(childId, wpId)
                        workOrderViewModel.deleteWorkPerformed(
                            childId,
                            df.getCurrentUTCTimeAsString()
                        )
                    }
                    childDescription = ""
                    selectedChild = null
                }
            }
        },
        onDoneClick = {
            navController.popBackStack()
        },
        onListItemSelected = {
            if (mainViewModel.getWorkPerformedIsMaster()) {
                mainViewModel.setWorkPerformedId(it.workPerformedId)
                parentDescription = it.wpDescription
            } else {
                selectedChild = it
                childDescription = it.wpDescription
            }
        }
    )
}