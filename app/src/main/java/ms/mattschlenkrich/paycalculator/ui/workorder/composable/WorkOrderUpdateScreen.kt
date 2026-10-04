package ms.mattschlenkrich.paycalculator.ui.workorder.composable


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.LocalMinColumnWidth
import ms.mattschlenkrich.paycalculator.common.compose.PictureAttachmentManager
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.calculateGridColumns
import ms.mattschlenkrich.paycalculator.common.compose.draggableFab
import ms.mattschlenkrich.paycalculator.data.entity.Areas
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryExpense
import ms.mattschlenkrich.paycalculator.data.model.ExpenseSummary
import ms.mattschlenkrich.paycalculator.data.model.JobSpecAndQuantity
import ms.mattschlenkrich.paycalculator.data.model.MaterialAndQuantity
import ms.mattschlenkrich.paycalculator.data.model.PictureItem
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paycalculator.data.model.WorkPerformedAndQuantity
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable.WorkOrderHistoryExpenseItem
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderUpdateScreen(
    mainViewModel: MainViewModel,
    navController: NavController,
    employerName: String,
    woNumber: String,
    onWoNumberChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    addressSuggestions: List<String>,
    description: String,
    onDescriptionChange: (String) -> Unit,
    woNumberError: Boolean,
    addressError: Boolean,
    descriptionError: Boolean,
    jobSpecText: String,
    onJobSpecTextChange: (String) -> Unit,
    jobSpecSuggestions: List<JobSpec>,
    onJobSpecSelected: (JobSpec) -> Unit,
    areaText: String,
    onAreaTextChange: (String) -> Unit,
    areaSuggestions: List<Areas>,
    onAreaSelected: (Areas) -> Unit,
    workPerformedNote: String,
    onWorkPerformedNoteChange: (String) -> Unit,
    onAddJobSpecClick: () -> Unit,
    addedJobSpecs: List<WorkOrderJobSpecCombined>,
    onJobSpecClick: (WorkOrderJobSpecCombined) -> Unit,
    onUpdateJobSpecDefinition: (JobSpec) -> Unit,
    jobSpecSummaryText: String,
    historyList: List<WorkOrderHistoryWithDates>,
    onHistoryClick: (WorkOrderHistoryWithDates) -> Unit,
    historySummaryText: String,
    hoursSummaryText: String,
    laborRate: String,
    onLaborRateChange: (String) -> Unit,
    markupRate: String,
    onMarkupRateChange: (String) -> Unit,
    laborTotalText: String,
    materialTotalText: String,
    expenseTotalText: String,
    grandTotalText: String,
    onAddHistoryClick: () -> Unit,
    workPerformedList: List<WorkPerformedAndQuantity>,
    jobSpecsSummaryList: List<JobSpecAndQuantity>,
    materialsList: List<MaterialAndQuantity>,
    onUpdateMaterialCostAndPrice: (Long, Double, Double) -> Unit,
    onWorkPerformedSummaryClick: (WorkPerformedAndQuantity) -> Unit,
    onJobSpecSummaryClick: (JobSpecAndQuantity) -> Unit,
    expensesList: List<ExpenseSummary>,
    individualExpenses: List<WorkOrderHistoryExpense>,
    pictures: List<PictureItem>,
    onPictureTaken: (File) -> Unit,
    onDeletePicture: (PictureItem) -> Unit,
    onDownloadPicture: (PictureItem) -> Unit,
    onDoneClick: () -> Unit,
    minColumnWidth: Int = LocalMinColumnWidth.current,
) {
    val columns = calculateGridColumns(minColumnWidth)
    val df = DateFunctions()
    val nf = NumberFunctions()

    var showJobSpecDialog by remember { mutableStateOf(value = false) }
    var selectedJobSpecCombined by remember { mutableStateOf<WorkOrderJobSpecCombined?>(null) }

    var showMaterialPriceDialog by rememberSaveable { mutableStateOf<MaterialAndQuantity?>(null) }

    var isLaborExpanded by rememberSaveable { mutableStateOf(false) }
    var isMaterialsExpanded by rememberSaveable { mutableStateOf(false) }
    var isExpensesExpanded by rememberSaveable { mutableStateOf(false) }

    JobSpecOptionsDialog(
        showDialog = showJobSpecDialog,
        onDismissRequest = { showJobSpecDialog = false },
        item = selectedJobSpecCombined,
        onUpdateInWorkOrder = { onJobSpecClick(it) },
    ) { onUpdateJobSpecDefinition(it.jobSpec) }

    if (showMaterialPriceDialog != null) {
        val material = showMaterialPriceDialog!!
        var newCost by remember {
            mutableStateOf(nf.displayDollars(material.cost))
        }
        var newPrice by remember {
            mutableStateOf(nf.displayDollars(material.price))
        }

        var isCostActive by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(mainViewModel.getTransferNum()) {
            val transferNum = mainViewModel.getTransferNum()
            if (transferNum != 0.0) {
                if (isCostActive) {
                    newCost = nf.displayDollars(transferNum)
                } else {
                    newPrice = nf.displayDollars(transferNum)
                }
                mainViewModel.setTransferNum(0.0)
            }
        }
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showMaterialPriceDialog = null },
            sheetState = androidx.compose.material3.rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SCREEN_PADDING_HORIZONTAL),
                verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
            ) {
                Text(
                    text = stringResource(R.string.update_material_used),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = material.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = ELEMENT_SPACING / 2)
                )
                DecimalOutlinedTextField(
                    value = newCost,
                    onValueChange = { newCost = it },
                    label = { Text(stringResource(R.string.cost)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                isCostActive = true
                                mainViewModel.setTransferNum(nf.getDoubleFromDollars(newCost))
                                navController.navigate(Screen.Calculator.route)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculate Cost"
                            )
                        }
                    }
                )
                DecimalOutlinedTextField(
                    value = newPrice,
                    onValueChange = { newPrice = it },
                    label = { Text(stringResource(R.string.price)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            isCostActive = false
                            mainViewModel.setTransferNum(nf.getDoubleFromDollars(newPrice))
                            navController.navigate(Screen.Calculator.route)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculate Price"
                            )
                        }
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(onClick = {
                        showMaterialPriceDialog = null
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                    androidx.compose.material3.TextButton(onClick = {
                        onUpdateMaterialCostAndPrice(
                            material.materialId,
                            nf.getDoubleFromDollars(newCost),
                            nf.getDoubleFromDollars(newPrice)
                        )
                        showMaterialPriceDialog = null
                    }) {
                        Text(stringResource(R.string.save))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onDoneClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.draggableFab()
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = stringResource(R.string.done),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
        ) {
            item {
                Spacer(modifier = Modifier.padding(vertical = SCREEN_PADDING_VERTICAL))
            }

            item {
                WorkOrderDetailsCard(
                    employerName = employerName,
                    woNumber = woNumber,
                    onWoNumberChange = onWoNumberChange,
                    woNumberError = woNumberError,
                    address = address,
                    onAddressChange = onAddressChange,
                    addressSuggestions = addressSuggestions,
                    addressError = addressError,
                    description = description,
                    onDescriptionChange = onDescriptionChange,
                    descriptionError = descriptionError
                )
            }

            item {
                WorkOrderTotalsSummaryCard(
                    laborRate = laborRate,
                    onLaborRateChange = onLaborRateChange,
                    markupRate = markupRate,
                    onMarkupRateChange = onMarkupRateChange,
                    laborTotalText = laborTotalText,
                    materialTotalText = materialTotalText,
                    expenseTotalText = expenseTotalText,
                    grandTotalText = grandTotalText
                )
            }

            item {
                JobSpecsEntryCard(
                    jobSpecText = jobSpecText,
                    onJobSpecTextChange = onJobSpecTextChange,
                    jobSpecSuggestions = jobSpecSuggestions,
                    onJobSpecSelected = onJobSpecSelected,
                    areaText = areaText,
                    onAreaTextChange = onAreaTextChange,
                    areaSuggestions = areaSuggestions,
                    onAreaSelected = onAreaSelected,
                    workPerformedNote = workPerformedNote,
                    onWorkPerformedNoteChange = onWorkPerformedNoteChange,
                    onAddJobSpecClick = onAddJobSpecClick
                )
            }

            if (addedJobSpecs.isNotEmpty()) {
                item {
                    Text(
                        text = jobSpecSummaryText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(addedJobSpecs) { combined ->
                    WorkOrderJobSpecItem(
                        combined = combined,
                        onClick = {
                            selectedJobSpecCombined = combined
                            showJobSpecDialog = true
                        }
                    )
                }
            }

            item {
                WorkOrderLaborHeaderCard(
                    isLaborExpanded = isLaborExpanded,
                    onToggleExpanded = { isLaborExpanded = !isLaborExpanded },
                    historySummaryText = historySummaryText,
                    hoursSummaryText = hoursSummaryText,
                    onAddHistoryClick = onAddHistoryClick,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (isLaborExpanded) {
                items(historyList) { history ->
                    HistoryItem(history, df, nf, onHistoryClick)
                }

                if (workPerformedList.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.work_performed),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    val wpChunks = workPerformedList.chunked(columns)
                    items(wpChunks) { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                        ) {
                            chunk.forEach { wp ->
                                Box(modifier = Modifier.weight(1f)) {
                                    WorkPerformedSummaryItem(
                                        wp = wp,
                                        onClick = onWorkPerformedSummaryClick
                                    )
                                }
                            }
                            repeat(columns - chunk.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                if (jobSpecsSummaryList.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.job_specs),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    val jsChunks = jobSpecsSummaryList.chunked(columns)
                    items(jsChunks) { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                        ) {
                            chunk.forEach { js ->
                                Box(modifier = Modifier.weight(1f)) {
                                    WorkOrderJobSpecSummaryItem(
                                        js = js,
                                        onClick = onJobSpecSummaryClick
                                    )
                                }
                            }
                            repeat(columns - chunk.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (materialsList.isNotEmpty()) {
                item {
                    WorkOrderMaterialsCard(
                        isMaterialsExpanded = isMaterialsExpanded,
                        onToggleExpanded = { isMaterialsExpanded = !isMaterialsExpanded },
                        materialsList = materialsList,
                        nf = nf,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                if (isMaterialsExpanded) {
                    val matChunks = materialsList.chunked(columns)
                    items(matChunks) { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                        ) {
                            chunk.forEach { material ->
                                Box(modifier = Modifier.weight(1f)) {
                                    WorkOrderMaterialSummaryItem(
                                        material = material,
                                        nf = nf,
                                        onClick = { showMaterialPriceDialog = it }
                                    )
                                }
                            }
                            repeat(columns - chunk.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (expensesList.isNotEmpty() || individualExpenses.isNotEmpty()) {
                item {
                    WorkOrderExpensesCard(
                        isExpensesExpanded = isExpensesExpanded,
                        onToggleExpanded = { isExpensesExpanded = !isExpensesExpanded },
                        expensesList = expensesList,
                        individualExpenses = individualExpenses,
                        nf = nf,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                if (isExpensesExpanded) {
                    if (expensesList.isNotEmpty()) {
                        val expChunks = expensesList.chunked(columns)
                        items(expChunks) { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                            ) {
                                chunk.forEach { expense ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        WorkOrderExpenseSummaryItem(expense, nf)
                                    }
                                }
                                repeat(columns - chunk.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    if (individualExpenses.isNotEmpty()) {
                        items(individualExpenses) { expense ->
                            WorkOrderHistoryExpenseItem(
                                item = expense,
                                index = individualExpenses.indexOf(expense),
                                onClick = {}
                            )
                        }
                    }
                }
            }

            item {
                PictureAttachmentManager(
                    pictures = pictures,
                    onPictureTaken = onPictureTaken,
                    onDeletePicture = onDeletePicture,
                    onDownloadPicture = onDownloadPicture,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.padding(vertical = SCREEN_PADDING_VERTICAL))
            }
        }
    }
}