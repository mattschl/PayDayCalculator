package ms.mattschlenkrich.paycalculator.ui.workorder

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
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.MaterialAndQuantity
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.WorkPerformedAndQuantity
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel

@Composable
fun WorkOrderUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val initialWo = mainViewModel.getWorkOrder() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val employer = mainViewModel.getEmployer() ?: return

    var woNumber by remember { mutableStateOf(initialWo.woNumber) }
    var address by remember { mutableStateOf(initialWo.woAddress) }
    var description by remember { mutableStateOf(initialWo.woDescription) }

    var jobSpecText by remember { mutableStateOf("") }
    val jobSpecSuggestions by workOrderViewModel.jobSpecsAll.observeAsState(emptyList())
    var areaText by remember { mutableStateOf("") }
    val areaSuggestions by workOrderViewModel.areasList.observeAsState(emptyList())
    var workPerformedNote by remember { mutableStateOf("") }

    val addedJobSpecs by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderJobSpecs(initialWo.workOrderId)
    }.observeAsState(emptyList())
    val historyList by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderHistoriesByWorkOrder(initialWo.workOrderId)
    }.observeAsState(emptyList())

    // Mocking summaries for now as they might need complex calculation
    val jobSpecSummaryText = "${addedJobSpecs.size} items"
    val historySummaryText = "${historyList.size} entries"

    // Need to get these from somewhere, possibly another query
    val workPerformedList =
        emptyList<WorkPerformedAndQuantity>()
    val materialsList = emptyList<MaterialAndQuantity>()

    WorkOrderUpdateScreen(
        employerName = employer.employerName,
        woNumber = woNumber,
        onWoNumberChange = { woNumber = it },
        address = address,
        onAddressChange = { address = it },
        description = description,
        onDescriptionChange = { description = it },
        jobSpecText = jobSpecText,
        onJobSpecTextChange = { jobSpecText = it },
        jobSpecSuggestions = jobSpecSuggestions,
        onJobSpecSelected = { jobSpecText = it.jsName },
        areaText = areaText,
        onAreaTextChange = { areaText = it },
        areaSuggestions = areaSuggestions,
        onAreaSelected = { areaText = it.areaName },
        workPerformedNote = workPerformedNote,
        onWorkPerformedNoteChange = { workPerformedNote = it },
        onAddJobSpecClick = {
            if (jobSpecText.isNotBlank()) {
                coroutineScope.launch {
                    val js = workOrderViewModel.getOrCreateJobSpec(jobSpecText.trim())
                    val a = workOrderViewModel.getOrCreateArea(areaText.trim())
                    workOrderViewModel.insertWorkOrderJobSpec(
                        WorkOrderJobSpec(
                            nf.generateRandomIdAsLong(),
                            initialWo.workOrderId,
                            js.jobSpecId,
                            a?.areaId,
                            workPerformedNote.trim(),
                            addedJobSpecs.size + 1,
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    )
                    jobSpecText = ""
                    areaText = ""
                    workPerformedNote = ""
                }
            }
        },
        addedJobSpecs = addedJobSpecs,
        onJobSpecClick = { combined ->
            mainViewModel.setWorkOrderJobSpecId(combined.workOrderJobSpec.workOrderJobSpecId)
            navController.navigate(Screen.WorkOrderJobSpecUpdate.route)
        },
        jobSpecSummaryText = jobSpecSummaryText,
        historyList = historyList,
        onHistoryClick = { history ->
            mainViewModel.setWorkOrderHistory(history.history)
            navController.navigate(Screen.WorkOrderHistoryUpdate.route)
        },
        historySummaryText = historySummaryText,
        onAddHistoryClick = {
            // Need to set a work date for HistoryAdd, maybe navigate to TimeSheet to pick one?
            // Or use current?
            navController.navigate(Screen.TimeSheet.route)
        },
        workPerformedList = workPerformedList,
        materialsList = materialsList,
        onDoneClick = {
            coroutineScope.launch {
                if (jobSpecText.isNotBlank()) {
                    val js = workOrderViewModel.getOrCreateJobSpec(jobSpecText.trim())
                    val a = workOrderViewModel.getOrCreateArea(areaText.trim())
                    workOrderViewModel.insertWorkOrderJobSpec(
                        WorkOrderJobSpec(
                            nf.generateRandomIdAsLong(),
                            initialWo.workOrderId,
                            js.jobSpecId,
                            a?.areaId,
                            workPerformedNote.trim(),
                            addedJobSpecs.size + 1,
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    )
                }
                workOrderViewModel.updateWorkOrder(
                    initialWo.copy(
                        woNumber = woNumber.trim(),
                        woAddress = address.trim(),
                        woDescription = description.trim(),
                        woUpdateTime = df.getCurrentUTCTimeAsString()
                    )
                )
                navController.popBackStack()
            }
        },
        onBackClick = { navController.popBackStack() }
    )
}