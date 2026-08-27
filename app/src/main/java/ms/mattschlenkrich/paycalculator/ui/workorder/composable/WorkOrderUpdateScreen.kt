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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.calculateGridColumns
import ms.mattschlenkrich.paycalculator.common.compose.draggableFab
import ms.mattschlenkrich.paycalculator.data.entity.Areas
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryExpense
import ms.mattschlenkrich.paycalculator.data.model.ExpenseSummary
import ms.mattschlenkrich.paycalculator.data.model.JobSpecAndQuantity
import ms.mattschlenkrich.paycalculator.data.model.MaterialAndQuantity
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paycalculator.data.model.WorkPerformedAndQuantity
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable.WorkOrderHistoryExpenseItem

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
    onUpdateMaterialPrice: (Long, Double) -> Unit,
    expensesList: List<ExpenseSummary>,
    individualExpenses: List<WorkOrderHistoryExpense>,
    onDoneClick: () -> Unit,
    minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH
) {
    val columns = calculateGridColumns(minColumnWidth)
    val df = DateFunctions()
    val nf = NumberFunctions()

    var showJobSpecDialog by remember { mutableStateOf(false) }
    var selectedJobSpecCombined by remember { mutableStateOf<WorkOrderJobSpecCombined?>(null) }

    var showMaterialPriceDialog by rememberSaveable { mutableStateOf<MaterialAndQuantity?>(null) }

    JobSpecOptionsDialog(
        showDialog = showJobSpecDialog,
        onDismissRequest = { showJobSpecDialog = false },
        item = selectedJobSpecCombined,
        onUpdateInWorkOrder = { onJobSpecClick(it) },
        onUpdateDefinition = { onUpdateJobSpecDefinition(it.jobSpec) }
    )

    if (showMaterialPriceDialog != null) {
        val material = showMaterialPriceDialog!!
        var newPrice by remember {
            mutableStateOf(
                nf.displayDollars(
                    if (material.quantity != 0.0) material.totalAmount / material.quantity
                    else 0.0
                )
            )
        }

        LaunchedEffect(mainViewModel.getTransferNum()) {
            val transferNum = mainViewModel.getTransferNum()
            if (transferNum != 0.0) {
                newPrice = nf.displayDollars(transferNum)
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
            ) {
                Text(
                    text = stringResource(R.string.update_material_used),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(material.name)
                SelectAllOutlinedTextField(
                    value = newPrice,
                    onValueChange = { newPrice = it },
                    label = { Text(stringResource(R.string.price)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = {
                        IconButton(onClick = {
                            mainViewModel.setTransferNum(nf.getDoubleFromDollars(newPrice))
                            navController.navigate(ms.mattschlenkrich.paycalculator.Screen.Calculator.route)
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
                        onUpdateMaterialPrice(
                            material.materialId,
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(ELEMENT_SPACING)
                    ) {
                        Text(
                            text = stringResource(R.string.total_summary),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                        ) {
                            SelectAllOutlinedTextField(
                                value = laborRate,
                                onValueChange = onLaborRateChange,
                                label = { Text(stringResource(R.string.labor_rate)) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                )
                            )
                            SelectAllOutlinedTextField(
                                value = markupRate,
                                onValueChange = onMarkupRateChange,
                                label = { Text(stringResource(R.string.markup_rate)) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                )
                            )
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Row {
                                    Text(
                                        text = "${stringResource(R.string.labor)}: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = laborTotalText,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row {
                                    Text(
                                        text = "${stringResource(R.string.material)}: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = materialTotalText,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row {
                                    Text(
                                        text = "${stringResource(R.string.expenses)}: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = expenseTotalText,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = grandTotalText,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.work_order_history),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (historySummaryText.isNotBlank()) {
                            Text(
                                text = historySummaryText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (hoursSummaryText.isNotBlank()) {
                            Text(
                                text = hoursSummaryText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    FloatingActionButton(
                        onClick = onAddHistoryClick,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .padding(4.dp)
                            .draggableFab()
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_new_extra)
                        )
                    }
                }
            }

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
                                WorkPerformedSummaryItem(wp)
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
                                WorkOrderJobSpecSummaryItem(js)
                            }
                        }
                        repeat(columns - chunk.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (materialsList.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.materials),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

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

            if (expensesList.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.invoices),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

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
                item {
                    Text(
                        text = stringResource(R.string.expenses),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(individualExpenses) { expense ->
                    WorkOrderHistoryExpenseItem(
                        item = expense,
                        index = individualExpenses.indexOf(expense),
                        onClick = {} // View only at work order level? Or navigate to history?
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.padding(vertical = SCREEN_PADDING_VERTICAL))
            }
        }
    }
}