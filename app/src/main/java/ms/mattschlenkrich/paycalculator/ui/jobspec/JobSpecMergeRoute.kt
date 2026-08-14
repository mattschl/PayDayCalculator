package ms.mattschlenkrich.paycalculator.ui.jobspec

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.entity.JobSpecMerged
import ms.mattschlenkrich.paycalculator.data.viewmodel.JobSpecViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.ui.jobspec.composable.JobSpecMergeScreen
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel

@Composable
fun JobSpecMergeRoute(
    mainViewModel: MainViewModel,
    jobSpecViewModel: JobSpecViewModel,
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val settings by settingsViewModel.settings.observeAsState()
    val minColumnWidth = settings?.minColumnWidth ?: DEFAULT_MIN_COLUMN_WIDTH

    val jsId = mainViewModel.getJobSpecId()
    if (jsId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val jobSpecList by jobSpecViewModel.searchJobSpecs("").observeAsState(emptyList())
    val parentJobSpec by jobSpecViewModel.getJobSpec(jsId).observeAsState()
    val childList by jobSpecViewModel.getJobSpecAndChildList(jsId)
        .observeAsState(emptyList())

    var parentDescription by rememberSaveable { mutableStateOf("") }
    var childDescription by rememberSaveable { mutableStateOf("") }
    var selectedChild by rememberSaveable {
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
                jobSpecViewModel.deleteJobSpecMerged(
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
        onMergeAction = { action ->
            val childId = selectedChild?.jobSpecId
            if (childId != null && childId != jsId) {
                coroutineScope.launch {
                    if (action == 1) { // Keep
                        jobSpecViewModel.insertJobSpecMerged(
                            JobSpecMerged(
                                nf.generateRandomIdAsLong(),
                                jsId,
                                childId,
                                false,
                                df.getCurrentUTCTimeAsString()
                            )
                        )
                    } else if (action == 2) { // Replace and delete
                        jobSpecViewModel.updateJobSpecMerged(childId, jsId)
                        jobSpecViewModel.deleteJobSpec(
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
            if (mainViewModel.getJobSpecIsMaster()) {
                mainViewModel.setJobSpecId(it.jobSpecId)
                parentDescription = it.jsName
            } else {
                selectedChild = it
                childDescription = it.jsName
            }
        },
        minColumnWidth = minColumnWidth
    )
}