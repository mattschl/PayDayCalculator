package ms.mattschlenkrich.paycalculator.ui.jobspec

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
import ms.mattschlenkrich.paycalculator.data.JobSpec
import ms.mattschlenkrich.paycalculator.data.JobSpecMerged
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

@Composable
fun JobSpecMergeRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val jsId = mainViewModel.getJobSpecId()
    if (jsId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val jobSpecList by workOrderViewModel.getJobSpecsAll().observeAsState(emptyList())
    val parentJobSpec by workOrderViewModel.getJobSpec(jsId).observeAsState()
    val childList by workOrderViewModel.getJobSpecAndChildList(jsId)
        .observeAsState(emptyList())

    var parentDescription by remember { mutableStateOf("") }
    var childDescription by remember { mutableStateOf("") }
    var selectedChild by remember {
        mutableStateOf<JobSpec?>(
            null
        )
    }

    LaunchedEffect(parentJobSpec) {
        parentJobSpec?.let {
            parentDescription = it.jsName
        }
    }

    JobSpecMergeScreen(
        jobSpecList = jobSpecList,
        parentName = parentDescription,
        onParentNameChange = { parentDescription = it },
        onParentSelected = {
            mainViewModel.setJobSpecId(it.jobSpecId)
            parentDescription = it.jsName
        },
        childList = childList,
        onRemoveChild = { child ->
            coroutineScope.launch {
                workOrderViewModel.deleteJobSpecMerged(
                    child.jobSpecMerged.jobSpecMergedId,
                    df.getCurrentUTCTimeAsString()
                )
            }
        },
        childName = childDescription,
        onChildNameChange = { childDescription = it },
        onChildSelected = {
            selectedChild = it
            childDescription = it.jsName
        },
        onMergeClick = {
            val childId = selectedChild?.jobSpecId
            if (childId != null && childId != jsId) {
                coroutineScope.launch {
                    workOrderViewModel.insertJobSpecMerged(
                        JobSpecMerged(
                            nf.generateRandomIdAsLong(),
                            jsId,
                            childId,
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    )
                    childDescription = ""
                    selectedChild = null
                }
            }
        },
        onDoneClick = {
            navController.popBackStack()
        },
        onListItemSelected = {
            if (mainViewModel.getJobSpecIsMaster()) {
                mainViewModel.setJobSpecId(it.jobSpecId)
                parentDescription = it.jsName
            } else {
                selectedChild = it
                childDescription = it.jsName
            }
        }
    )
}