package ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.LocalMinColumnWidth
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.calculateGridColumns
import ms.mattschlenkrich.paycalculator.common.compose.draggableFab
import ms.mattschlenkrich.paycalculator.data.entity.Areas
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryExpense
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.model.MaterialInSequence
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryWorkPerformedCombined

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderHistoryUpdateScreen(
    mainViewModel: ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel,
    navController: androidx.navigation.NavController,
    workDateDisplay: String,
    employerName: String,
    workOrderNumber: String,
    onWorkOrderNumberChange: (String) -> Unit,
    workOrderList: List<WorkOrder>,
    onWorkOrderSelected: (WorkOrder) -> Unit,
    onWorkOrderLongClick: () -> Unit,
    workOrderDescription: String,
    onWorkOrderButtonClick: () -> Unit,
    workOrderButtonText: String,
    regHours: String,
    onRegHoursChange: (String) -> Unit,
    otHours: String,
    onOtHoursChange: (String) -> Unit,
    dblOtHours: String,
    onDblOtHoursChange: (String) -> Unit,
    onRefreshHours: () -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    onAddTimeClick: () -> Unit,
    addTimeButtonText: String,
    // Work Performed
    workPerformed: String,
    onWorkPerformedChange: (String) -> Unit,
    workPerformedList: List<WorkPerformed>,
    onWorkPerformedSelected: (WorkPerformed) -> Unit,
    area: String,
    onAreaChange: (String) -> Unit,
    areaList: List<Areas>,
    onAreaSelected: (Areas) -> Unit,
    workPerformedNote: String,
    onWorkPerformedNoteChange: (String) -> Unit,
    onAddWorkPerformed: () -> Unit,
    workPerformedActualList: List<WorkOrderHistoryWorkPerformedCombined>,
    onWorkPerformedItemClick: (WorkOrderHistoryWorkPerformedCombined, Int) -> Unit,
    // Materials
    materialQty: String,
    onMaterialQtyChange: (String) -> Unit,
    material: String,
    onMaterialChange: (String) -> Unit,
    materialList: List<Material>,
    onMaterialSelected: (Material) -> Unit,
    onAddMaterial: () -> Unit,
    materialActualList: List<MaterialInSequence>,
    onMaterialItemClick: (MaterialInSequence, Int) -> Unit,
    // Expenses
    expenseActualList: List<WorkOrderHistoryExpense>,
    onAddExpense: (String, String, String, String) -> Unit,
    onUpdateExpense: (WorkOrderHistoryExpense) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    // Actions
    onDone: () -> Unit,
    onUpdateWorkPerformed: (WorkOrderHistoryWorkPerformedCombined) -> Unit,
    onUpdateWorkPerformedDefinition: (WorkOrderHistoryWorkPerformedCombined) -> Unit,
    onUpdateMaterialInHistory: (MaterialInSequence) -> Unit,
    onUpdateMaterialDefinition: (MaterialInSequence) -> Unit,
    isSaving: Boolean = false,
    minColumnWidth: Int = LocalMinColumnWidth.current,
) {
    var showWorkPerformedDialog by rememberSaveable { mutableStateOf(false) }
    var selectedWorkPerformed by rememberSaveable {
        mutableStateOf<WorkOrderHistoryWorkPerformedCombined?>(
            null
        )
    }

    var showMaterialDialog by rememberSaveable { mutableStateOf(false) }
    var selectedMaterial by rememberSaveable { mutableStateOf<MaterialInSequence?>(null) }

    var showExpenseDialog by rememberSaveable { mutableStateOf(false) }
    var selectedExpense by rememberSaveable { mutableStateOf<WorkOrderHistoryExpense?>(null) }

    val columns = calculateGridColumns(minColumnWidth)

    WorkPerformedOptionsDialog(
        showDialog = showWorkPerformedDialog,
        onDismissRequest = { showWorkPerformedDialog = false },
        item = selectedWorkPerformed,
        onWorkPerformedItemClick = onWorkPerformedItemClick,
        onUpdateWorkPerformed = onUpdateWorkPerformed,
        onEditWorkPerformedDefinition = onUpdateWorkPerformedDefinition
    )

    MaterialOptionsDialog(
        showDialog = showMaterialDialog,
        onDismissRequest = { showMaterialDialog = false },
        item = selectedMaterial,
        onDelete = { onMaterialItemClick(it, 0) },
        onEditInHistory = onUpdateMaterialInHistory,
        onEditMaterialDefinition = onUpdateMaterialDefinition
    )

    WorkOrderHistoryExpenseDialog(
        mainViewModel = mainViewModel,
        navController = navController,
        showDialog = showExpenseDialog,
        onDismissRequest = { showExpenseDialog = false },
        expense = selectedExpense,
        onAddExpense = onAddExpense,
        onUpdateExpense = onUpdateExpense,
        onDeleteExpense = onDeleteExpense
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (!isSaving) onDone() },
                containerColor = if (isSaving) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                contentColor = if (isSaving) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.draggableFab()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = stringResource(id = R.string.done)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL),
            verticalItemSpacing = ELEMENT_SPACING,
            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(modifier = Modifier.padding(vertical = SCREEN_PADDING_VERTICAL))
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                WorkOrderHistoryInfoCard(
                    workDateDisplay = workDateDisplay,
                    employerName = employerName,
                    workOrderNumber = workOrderNumber,
                    onWorkOrderNumberChange = onWorkOrderNumberChange,
                    workOrderList = workOrderList,
                    onWorkOrderSelected = onWorkOrderSelected,
                    onWorkOrderLongClick = onWorkOrderLongClick,
                    workOrderDescription = workOrderDescription,
                    onWorkOrderButtonClick = onWorkOrderButtonClick,
                    workOrderButtonText = workOrderButtonText,
                    regHours = regHours,
                    onRegHoursChange = onRegHoursChange,
                    otHours = otHours,
                    onOtHoursChange = onOtHoursChange,
                    dblOtHours = dblOtHours,
                    onDblOtHoursChange = onDblOtHoursChange,
                    onRefreshHours = onRefreshHours,
                    note = note,
                    onNoteChange = { nt -> onNoteChange(nt) },
                    onAddTimeClick = onAddTimeClick,
                    addTimeButtonText = addTimeButtonText
                )
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                WorkPerformedEntryCard(
                    workPerformed = workPerformed,
                    onWorkPerformedChange = onWorkPerformedChange,
                    workPerformedList = workPerformedList,
                    onWorkPerformedSelected = onWorkPerformedSelected,
                    area = area,
                    onAreaChange = onAreaChange,
                    areaList = areaList,
                    onAreaSelected = onAreaSelected,
                    workPerformedNote = workPerformedNote,
                    onWorkPerformedNoteChange = onWorkPerformedNoteChange,
                    onAddWorkPerformed = onAddWorkPerformed
                )
            }

            if (workPerformedActualList.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        text = stringResource(R.string.work_performed),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(workPerformedActualList) { item ->
                    WorkPerformedItem(
                        item = item,
                        index = workPerformedActualList.indexOf(item)
                    ) {
                        selectedWorkPerformed = item
                        showWorkPerformedDialog = true
                    }
                }
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                MaterialEntryCard(
                    materialQty = materialQty,
                    onMaterialQtyChange = onMaterialQtyChange,
                    material = material,
                    onMaterialChange = onMaterialChange,
                    materialList = materialList,
                    onMaterialSelected = onMaterialSelected,
                    onAddMaterial = onAddMaterial
                )
            }

            if (materialActualList.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        text = stringResource(R.string.materials),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(materialActualList) { item ->
                    WorkOrderHistoryMaterialItem(
                        item = item,
                        index = materialActualList.indexOf(item)
                    ) {
                        selectedMaterial = item
                        showMaterialDialog = true
                    }
                }
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Button(
                    onClick = {
                        selectedExpense = null
                        showExpenseDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.add_expense))
                }
            }

            if (expenseActualList.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        text = stringResource(R.string.expenses),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(expenseActualList) { item ->
                    WorkOrderHistoryExpenseItem(
                        item = item,
                        index = expenseActualList.indexOf(item)
                    ) {
                        selectedExpense = item
                        showExpenseDialog = true
                    }
                }
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(modifier = Modifier.padding(vertical = SCREEN_PADDING_VERTICAL))
            }
        }
    }
}