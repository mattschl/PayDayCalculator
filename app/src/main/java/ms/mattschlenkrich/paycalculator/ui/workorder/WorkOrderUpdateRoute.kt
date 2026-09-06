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
import ms.mattschlenkrich.paycalculator.data.viewmodel.AreaViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.JobSpecViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MaterialViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.ui.workorder.composable.WorkOrderUpdateScreen

@Composable
fun WorkOrderUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    jobSpecViewModel: JobSpecViewModel,
    areaViewModel: AreaViewModel,
    materialViewModel: MaterialViewModel,
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

    val currentWo by workOrderViewModel.getWorkOrder(initialWo.workOrderId)
        .observeAsState(initialWo)

    val employer = mainViewModel.getEmployer() ?: return
    val addressSuggestions by workOrderViewModel.getUniqueAddresses(employer.employerId)
        .observeAsState(emptyList())

    var woNumber by rememberSaveable(currentWo.woNumber, currentWo.woUpdateTime) {
        mutableStateOf(
            currentWo.woNumber
        )
    }
    var address by rememberSaveable(currentWo.woAddress, currentWo.woUpdateTime) {
        mutableStateOf(
            currentWo.woAddress
        )
    }
    var description by rememberSaveable(
        currentWo.woDescription,
        currentWo.woUpdateTime
    ) { mutableStateOf(currentWo.woDescription) }

    var woNumberError by rememberSaveable { mutableStateOf(false) }
    var addressError by rememberSaveable { mutableStateOf(false) }
    var descriptionError by rememberSaveable { mutableStateOf(false) }

    var jobSpecText by rememberSaveable { mutableStateOf("") }
    val jobSpecSuggestions by jobSpecViewModel.searchJobSpecs("")
        .observeAsState(emptyList())
    var areaText by rememberSaveable { mutableStateOf("") }
    val areaSuggestions by areaViewModel.searchAreas("").observeAsState(emptyList())
    var workPerformedNote by rememberSaveable { mutableStateOf("") }

    val addedJobSpecs by remember(initialWo.workOrderId) {
        jobSpecViewModel.getWorkOrderJobSpecs(initialWo.workOrderId)
    }.observeAsState(emptyList())
    val historyList by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderHistoriesByWorkOrder(initialWo.workOrderId)
    }.observeAsState(emptyList())

    val workOrderSummary by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderSummary(initialWo.workOrderId)
    }.observeAsState()

    val materialsSummary by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderMaterialsSummary(initialWo.workOrderId)
    }.observeAsState(emptyList())
    val workPerformedSummary by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderWorkPerformedSummary(initialWo.workOrderId)
    }.observeAsState(emptyList())
    val jobSpecsSummary by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderJobSpecsSummary(initialWo.workOrderId)
    }.observeAsState(emptyList())
    val expensesSummary by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderExpensesSummary(initialWo.workOrderId)
    }.observeAsState(emptyList())
    val individualExpenses by remember(initialWo.workOrderId) {
        workOrderViewModel.getWorkOrderExpensesAll(initialWo.workOrderId)
    }.observeAsState(emptyList())

    var laborRate by rememberSaveable { mutableStateOf("") }
    var markupRate by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(settings) {
        settings?.let {
            if (laborRate.isEmpty() && it.defaultLaborRate > 0.0) {
                laborRate = nf.displayNumberFromDouble(it.defaultLaborRate)
            }
            if (markupRate.isEmpty() && it.defaultMarkupRate > 0.0) {
                markupRate = nf.displayNumberFromDouble(it.defaultMarkupRate)
            }
        }
    }

    // Mocking summaries for now as they might need complex calculation
    val jobSpecSummaryText = "${addedJobSpecs.size} items"
    val historySummaryText = "${historyList.size} entries"

    var hoursSummaryText = ""
    workOrderSummary?.let {
        if (it.totalRegHours > 0) hoursSummaryText += "${nf.displayNumberFromDouble(it.totalRegHours)} ${
            stringResource(
                R.string.unit_hour
            )
        } "
        if (it.totalOtHours > 0) hoursSummaryText += "| ${nf.displayNumberFromDouble(it.totalOtHours)} ${
            stringResource(
                R.string.unit_ot
            )
        } "
        if (it.totalDblOtHours > 0) hoursSummaryText += "| ${nf.displayNumberFromDouble(it.totalDblOtHours)} ${
            stringResource(
                R.string.dbl_ot
            )
        } "
    }

    val totalHours = (workOrderSummary?.totalRegHours ?: 0.0) +
            (workOrderSummary?.totalOtHours ?: 0.0) +
            (workOrderSummary?.totalDblOtHours ?: 0.0)
    val laborTotal = nf.getDoubleFromDollars(laborRate) * totalHours

    val markupValue = nf.getDoubleFromDollars(markupRate)
    val factor = (100.0 - markupValue) / 100.0

    val materialsList = materialsSummary.map { item ->
        item.copy(
            totalAmount = ms.mattschlenkrich.paycalculator.common.WorkOrderCalculations.calculateMaterialAmount(
                item,
                factor
            )
        )
    }
    val markedUpMaterials = materialsList.sumOf { it.totalAmount }

    val expensesTotal = expensesSummary.sumOf { it.totalAmount }
    val markedUpExpenses = if (factor > 0.0) expensesTotal / factor else expensesTotal

    val grandTotal = laborTotal + markedUpMaterials + markedUpExpenses

    val laborTotalText = nf.displayDollars(laborTotal)
    val materialTotalText = nf.displayDollars(markedUpMaterials)
    val expenseTotalText = nf.displayDollars(markedUpExpenses)
    val grandTotalText = nf.displayDollars(grandTotal)

    // Need to get these from somewhere, possibly another query
    val workPerformedList = workPerformedSummary

    val context = LocalContext.current
    val errorLabel = stringResource(R.string.prefix_error)
    val errorMessages = mapOf(
        R.string.the_work_order_must_have_a_number to stringResource(R.string.the_work_order_must_have_a_number),
        R.string.the_work_order_must_have_an_address to stringResource(R.string.the_work_order_must_have_an_address),
        R.string.the_work_order_must_have_a_description to stringResource(R.string.the_work_order_must_have_a_description)
    )

    WorkOrderUpdateScreen(
        mainViewModel = mainViewModel,
        navController = navController,
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
                    val js = jobSpecViewModel.getOrCreateJobSpec(jobSpecText.trim())
                    val a = areaViewModel.getOrCreateArea(areaText.trim())
                    jobSpecViewModel.insertWorkOrderJobSpec(
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
        laborRate = laborRate,
        onLaborRateChange = { laborRate = it },
        markupRate = markupRate,
        onMarkupRateChange = { markupRate = it },
        laborTotalText = laborTotalText,
        materialTotalText = materialTotalText,
        expenseTotalText = expenseTotalText,
        grandTotalText = grandTotalText,
        onAddHistoryClick = {
            // Need to set a work date for HistoryAdd, maybe navigate to TimeSheet to pick one?
            // Or use current?
            mainViewModel.setSelectedTopLevelIndex(0)
            navController.popBackStack(Screen.MainPager.route, inclusive = false)
        },
        workPerformedList = workPerformedList,
        jobSpecsSummaryList = jobSpecsSummary,
        materialsList = materialsList,
        onUpdateMaterialCostAndPrice = { materialId, newCost, newPrice ->
            coroutineScope.launch {
                materialViewModel.updateMaterialCostAndPrice(materialId, newCost, newPrice)
            }
        },
        expensesList = expensesSummary,
        individualExpenses = individualExpenses,
        onDoneClick = {
            val errorResId = validateWorkOrder(woNumber, address, description)
            if (errorResId != null) {
                when (errorResId) {
                    R.string.the_work_order_must_have_a_number -> woNumberError = true
                    R.string.the_work_order_must_have_an_address -> addressError = true
                    R.string.the_work_order_must_have_a_description -> descriptionError = true
                }
                Toast.makeText(
                    context,
                    errorLabel + (errorMessages[errorResId] ?: ""),
                    Toast.LENGTH_LONG
                ).show()
                return@WorkOrderUpdateScreen
            }

            coroutineScope.launch {
                if (jobSpecText.isNotBlank()) {
                    val js = jobSpecViewModel.getOrCreateJobSpec(jobSpecText.trim())
                    val a = areaViewModel.getOrCreateArea(areaText.trim())
                    jobSpecViewModel.insertWorkOrderJobSpec(
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