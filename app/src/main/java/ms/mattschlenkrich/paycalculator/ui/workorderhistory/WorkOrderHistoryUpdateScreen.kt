package ms.mattschlenkrich.paycalculator.ui.workorderhistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.data.Areas
import ms.mattschlenkrich.paycalculator.data.Material
import ms.mattschlenkrich.paycalculator.data.MaterialInSequence
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWorkPerformedCombined
import ms.mattschlenkrich.paycalculator.data.WorkPerformed
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.components.MaterialEntryCard
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.components.MaterialOptionsDialog
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.components.WorkOrderHistoryInfoCard
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.components.WorkOrderHistoryMaterialItem
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.components.WorkPerformedEntryCard
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.components.WorkPerformedItem
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.components.WorkPerformedOptionsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderHistoryUpdateScreen(
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
    // Actions
    onDone: () -> Unit,
    onUpdateWorkPerformed: (WorkOrderHistoryWorkPerformedCombined) -> Unit,
    onUpdateWorkPerformedDefinition: (WorkOrderHistoryWorkPerformedCombined) -> Unit,
    onUpdateMaterialInHistory: (MaterialInSequence) -> Unit,
    onUpdateMaterialDefinition: (MaterialInSequence) -> Unit,
) {
    var showWorkPerformedDialog by remember { mutableStateOf(false) }
    var selectedWorkPerformed by remember {
        mutableStateOf<WorkOrderHistoryWorkPerformedCombined?>(
            null
        )
    }

    var showMaterialDialog by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf<MaterialInSequence?>(null) }

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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onDone,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Done,
                    contentDescription = stringResource(id = R.string.done)
                )
            }
        }
    ) { paddingValues ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
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
                    note = note,
                    onNoteChange = onNoteChange,
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
                        index = workPerformedActualList.indexOf(item),
                        onClick = {
                            selectedWorkPerformed = item
                            showWorkPerformedDialog = true
                        }
                    )
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
                        index = materialActualList.indexOf(item),
                        onClick = {
                            selectedMaterial = item
                            showMaterialDialog = true
                        }
                    )
                }
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(modifier = Modifier.padding(vertical = SCREEN_PADDING_VERTICAL))
            }
        }
    }
}