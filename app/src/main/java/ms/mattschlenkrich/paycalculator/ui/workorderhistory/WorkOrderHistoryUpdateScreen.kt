package ms.mattschlenkrich.paycalculator.ui.workorderhistory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.AutoCompleteTextField
import ms.mattschlenkrich.paycalculator.common.compose.CapitalizedOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.data.Areas
import ms.mattschlenkrich.paycalculator.data.Material
import ms.mattschlenkrich.paycalculator.data.MaterialInSequence
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWorkPerformedCombined
import ms.mattschlenkrich.paycalculator.data.WorkPerformed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderHistoryUpdateScreen(
    title: String,
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
    onBack: () -> Unit,
    onUpdateWorkPerformed: (WorkOrderHistoryWorkPerformedCombined) -> Unit,
) {
    var showWorkPerformedDialog by remember { mutableStateOf(false) }
    var selectedWorkPerformed by remember {
        mutableStateOf<WorkOrderHistoryWorkPerformedCombined?>(
            null
        )
    }

    if (showWorkPerformedDialog && selectedWorkPerformed != null) {
        AlertDialog(
            onDismissRequest = { showWorkPerformedDialog = false },
            title = { Text(stringResource(R.string.work_performed_options)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                ) {
                    Text(selectedWorkPerformed!!.workPerformed.wpDescription)
                    Button(
                        onClick = {
                            onWorkPerformedItemClick(
                                selectedWorkPerformed!!,
                                1
                            ) // 1 for Change Description
                            showWorkPerformedDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.change_description))
                    }
                    Button(
                        onClick = {
                            onUpdateWorkPerformed(selectedWorkPerformed!!)
                            showWorkPerformedDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.update_in_history))
                    }
                    Button(
                        onClick = {
                            onWorkPerformedItemClick(selectedWorkPerformed!!, 0) // 0 for Delete
                            showWorkPerformedDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWorkPerformedDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WorkOrderHistoryUpdateScreen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
        ) {
            item {
                Spacer(modifier = Modifier.padding(vertical = SCREEN_PADDING_VERTICAL))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                    ) {
                        Text(
                            text = workDateDisplay,
                            style = MaterialTheme.typography.titleMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.employer),
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = employerName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                        ) {
                            AutoCompleteTextField(
                                value = workOrderNumber,
                                onValueChange = onWorkOrderNumberChange,
                                label = stringResource(id = R.string.work_order_number),
                                suggestions = workOrderList,
                                onItemSelected = onWorkOrderSelected,
                                modifier = Modifier.weight(1f),
                                onLongClick = onWorkOrderLongClick,
                                itemToString = { it.woNumber }
                            )
                            Button(
                                onClick = onWorkOrderButtonClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(workOrderButtonText)
                            }
                        }

                        if (workOrderDescription.isNotEmpty()) {
                            Text(
                                text = workOrderDescription,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = onAddTimeClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(addTimeButtonText)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                        ) {
                            DecimalOutlinedTextField(
                                value = regHours,
                                onValueChange = onRegHoursChange,
                                label = { Text(stringResource(id = R.string.hr)) },
                                modifier = Modifier.weight(1f)
                            )
                            DecimalOutlinedTextField(
                                value = otHours,
                                onValueChange = onOtHoursChange,
                                label = { Text(stringResource(id = R.string.ot)) },
                                modifier = Modifier.weight(1f)
                            )
                            DecimalOutlinedTextField(
                                value = dblOtHours,
                                onValueChange = onDblOtHoursChange,
                                label = { Text(stringResource(id = R.string.dbl_ot)) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        CapitalizedOutlinedTextField(
                            value = note,
                            onValueChange = onNoteChange,
                            label = { Text(stringResource(id = R.string.enter_note_optional)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                    ) {
                        Text(
                            text = stringResource(id = R.string.enter_work_performed),
                            style = MaterialTheme.typography.titleMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                        ) {
                            AutoCompleteTextField(
                                value = workPerformed,
                                onValueChange = onWorkPerformedChange,
                                label = stringResource(id = R.string.work_performed),
                                suggestions = workPerformedList,
                                onItemSelected = onWorkPerformedSelected,
                                modifier = Modifier.weight(1f),
                                itemToString = { it.wpDescription }
                            )
                            AutoCompleteTextField(
                                value = area,
                                onValueChange = onAreaChange,
                                label = stringResource(id = R.string.area_optional),
                                suggestions = areaList,
                                onItemSelected = onAreaSelected,
                                modifier = Modifier.weight(1f),
                                itemToString = { it.areaName }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CapitalizedOutlinedTextField(
                                value = workPerformedNote,
                                onValueChange = onWorkPerformedNoteChange,
                                label = { Text(stringResource(id = R.string.enter_note_optional)) },
                                modifier = Modifier.weight(1f),
                                singleLine = false
                            )
                            Button(
                                onClick = onAddWorkPerformed,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(stringResource(id = R.string.add))
                            }
                        }
                    }
                }
            }

            if (workPerformedActualList.isNotEmpty()) {
                item {
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

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                    ) {
                        Text(
                            text = stringResource(id = R.string.add_materials_used_below),
                            style = MaterialTheme.typography.titleMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                        ) {
                            DecimalOutlinedTextField(
                                value = materialQty,
                                onValueChange = onMaterialQtyChange,
                                label = { Text(stringResource(id = R.string.qty)) },
                                modifier = Modifier.width(80.dp)
                            )
                            AutoCompleteTextField(
                                value = material,
                                onValueChange = onMaterialChange,
                                label = stringResource(id = R.string.material),
                                suggestions = materialList,
                                onItemSelected = onMaterialSelected,
                                modifier = Modifier.weight(1f),
                                itemToString = { it.mName }
                            )
                            Button(
                                onClick = onAddMaterial,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(stringResource(id = R.string.add))
                            }
                        }
                    }
                }
            }

            if (materialActualList.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.materials),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(materialActualList) { item ->
                    MaterialItem(
                        item = item,
                        index = materialActualList.indexOf(item),
                        onClick = { onMaterialItemClick(item, it) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.padding(vertical = SCREEN_PADDING_VERTICAL))
            }
        }
    }
}

@Composable
fun WorkPerformedItem(
    item: WorkOrderHistoryWorkPerformedCombined,
    index: Int,
    onClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(index) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val display = "${index + 1}) " + item.workPerformed.wpDescription +
                    (item.area?.let { " ${stringResource(R.string._in_)} ${it.areaName}" } ?: "") +
                    (if (item.workOrderHistoryWorkPerformed.wowpNote.isNullOrBlank()) "" else " - ${item.workOrderHistoryWorkPerformed.wowpNote}.")

            Text(
                text = display,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MaterialItem(
    item: MaterialInSequence,
    index: Int,
    onClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(index) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val display = "${index + 1}) ${item.mQty} x ${item.mName}"
            Text(
                text = display,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}