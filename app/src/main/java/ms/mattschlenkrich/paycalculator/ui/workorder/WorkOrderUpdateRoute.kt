package ms.mattschlenkrich.paycalculator.ui.workorder

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.StringFunctions
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.data.model.MaterialAndQuantity
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.ui.workorder.composable.WorkOrderUpdateScreen

@Composable
fun WorkOrderUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val sf = remember { StringFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val settings by settingsViewModel.settings.observeAsState()
    val minColumnWidth = settings?.minColumnWidth ?: DEFAULT_MIN_COLUMN_WIDTH

    val initialWo = mainViewModel.getWorkOrder() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val employer = mainViewModel.getEmployer() ?: return
    val addressSuggestions by workOrderViewModel.getUniqueAddresses(employer.employerId)
        .observeAsState(emptyList())

    var woNumber by rememberSaveable { mutableStateOf(initialWo.woNumber) }
    var address by rememberSaveable { mutableStateOf(initialWo.woAddress) }
    var description by rememberSaveable { mutableStateOf(initialWo.woDescription) }

    var woNumberError by rememberSaveable { mutableStateOf(false) }
    var addressError by rememberSaveable { mutableStateOf(false) }
    var descriptionError by rememberSaveable { mutableStateOf(false) }

    var jobSpecText by rememberSaveable { mutableStateOf("") }
    val jobSpecSuggestions by workOrderViewModel.jobSpecsAll.observeAsState(emptyList())
    var areaText by rememberSaveable { mutableStateOf("") }
    val areaSuggestions by workOrderViewModel.areasList.observeAsState(emptyList())
    var workPerformedNote by rememberSaveable { mutableStateOf("") }

    val addedJobSpecs by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderJobSpecs(initialWo.workOrderId)
    }.observeAsState(emptyList())
    val historyList by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderHistoriesByWorkOrder(initialWo.workOrderId)
    }.observeAsState(emptyList())

    val workOrderSummary by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderSummary(initialWo.workOrderId)
    }.observeAsState()

    var calculationRate by rememberSaveable { mutableStateOf("") }
    val totalHours = (workOrderSummary?.totalRegHours ?: 0.0) +
            (workOrderSummary?.totalOtHours ?: 0.0) +
            (workOrderSummary?.totalDblOtHours ?: 0.0)
    val totalCalculation = nf.displayDollars((calculationRate.toDoubleOrNull() ?: 0.0) * totalHours)

    val materialsSummary by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderMaterialsSummary(initialWo.workOrderId)
    }.observeAsState(emptyList())
    val workPerformedSummary by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderWorkPerformedSummary(initialWo.workOrderId)
    }.observeAsState(emptyList())

    // Mocking summaries for now as they might need complex calculation
    val jobSpecSummaryText = "${addedJobSpecs.size} items"
    val historySummaryText = "${historyList.size} entries"

    var hoursSummaryText = ""
    workOrderSummary?.let {
        if (it.totalRegHours > 0) hoursSummaryText += "${nf.displayNumberFromDouble(it.totalRegHours)} ${
            stringResource(
                R.string.hr
            )
        } "
        if (it.totalOtHours > 0) hoursSummaryText += "| ${nf.displayNumberFromDouble(it.totalOtHours)} ${
            stringResource(
                R.string.ot
            )
        } "
        if (it.totalDblOtHours > 0) hoursSummaryText += "| ${nf.displayNumberFromDouble(it.totalDblOtHours)} ${
            stringResource(
                R.string.dbl_ot
            )
        } "
    }

    // Need to get these from somewhere, possibly another query
    val workPerformedList = workPerformedSummary
    val materialsList = materialsSummary.map {
        MaterialAndQuantity(it.name, it.quantity)
    }

    val context = LocalContext.current
    val errorLabel = stringResource(R.string.error_)
    val noNumberError = stringResource(R.string.the_work_order_must_have_a_number)
    val noAddressError = stringResource(R.string.the_work_order_must_have_an_address)
    val noDescriptionError = stringResource(R.string.the_work_order_must_have_a_description)

    WorkOrderUpdateScreen(
        employerName = employer.employerName,
        woNumber = woNumber,
        onWoNumberChange = {
            woNumber = it
            woNumberError = false
        },
        address = address,
        onAddressChange = {
            address = sf.toTitleCase(it)
            addressError = false
        },
        addressSuggestions = addressSuggestions,
        description = description,
        onDescriptionChange = {
            description = sf.capitalizeFirst(it)
            descriptionError = false
        },
        woNumberError = woNumberError,
        addressError = addressError,
        descriptionError = descriptionError,
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
        onUpdateJobSpecDefinition = { jobSpec ->
            mainViewModel.setJobSpecId(jobSpec.jobSpecId)
            navController.navigate(Screen.JobSpecUpdate.route)
        },
        jobSpecSummaryText = jobSpecSummaryText,
        historyList = historyList,
        onHistoryClick = { history ->
            mainViewModel.setWorkOrderHistory(history.history)
            navController.navigate(Screen.WorkOrderHistoryUpdate.route)
        },
        historySummaryText = historySummaryText,
        hoursSummaryText = hoursSummaryText,
        calculationRate = calculationRate,
        onCalculationRateChange = { calculationRate = it },
        totalCalculationText = totalCalculation,
        onAddHistoryClick = {
            // Need to set a work date for HistoryAdd, maybe navigate to TimeSheet to pick one?
            // Or use current?
            mainViewModel.setSelectedTopLevelIndex(0)
            navController.popBackStack(Screen.MainPager.route, inclusive = false)
        },
        workPerformedList = workPerformedList,
        materialsList = materialsList,
        onDoneClick = {
            if (woNumber.isBlank()) {
                woNumberError = true
                Toast.makeText(
                    context,
                    errorLabel + noNumberError,
                    Toast.LENGTH_LONG
                ).show()
                return@WorkOrderUpdateScreen
            }
            if (address.isBlank()) {
                addressError = true
                Toast.makeText(
                    context,
                    errorLabel + noAddressError,
                    Toast.LENGTH_LONG
                ).show()
                return@WorkOrderUpdateScreen
            }
            if (description.isBlank()) {
                descriptionError = true
                Toast.makeText(
                    context,
                    errorLabel + noDescriptionError,
                    Toast.LENGTH_LONG
                ).show()
                return@WorkOrderUpdateScreen
            }

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
        minColumnWidth = minColumnWidth
    )
}