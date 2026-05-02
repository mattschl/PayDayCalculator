package ms.mattschlenkrich.paycalculator.ui.workorder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.calculateGridColumns
import ms.mattschlenkrich.paycalculator.data.Areas
import ms.mattschlenkrich.paycalculator.data.JobSpec
import ms.mattschlenkrich.paycalculator.data.MaterialAndQuantity
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.data.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paycalculator.data.WorkPerformedAndQuantity
import ms.mattschlenkrich.paycalculator.ui.workorder.components.HistoryItem
import ms.mattschlenkrich.paycalculator.ui.workorder.components.JobSpecsEntryCard
import ms.mattschlenkrich.paycalculator.ui.workorder.components.WorkOrderDetailsCard
import ms.mattschlenkrich.paycalculator.ui.workorder.components.WorkOrderJobSpecItem
import ms.mattschlenkrich.paycalculator.ui.workorder.components.WorkOrderMaterialSummaryItem
import ms.mattschlenkrich.paycalculator.ui.workorder.components.WorkPerformedSummaryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderUpdateScreen(
    employerName: String,
    woNumber: String,
    onWoNumberChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
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
    jobSpecSummaryText: String,
    historyList: List<WorkOrderHistoryWithDates>,
    onHistoryClick: (WorkOrderHistoryWithDates) -> Unit,
    historySummaryText: String,
    onAddHistoryClick: () -> Unit,
    workPerformedList: List<WorkPerformedAndQuantity>,
    materialsList: List<MaterialAndQuantity>,
    onDoneClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val columns = calculateGridColumns()
    val df = DateFunctions()
    val nf = NumberFunctions()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onDoneClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Done, contentDescription = stringResource(R.string.done))
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
                    address = address,
                    onAddressChange = onAddressChange,
                    description = description,
                    onDescriptionChange = onDescriptionChange
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
                        onClick = { onJobSpecClick(combined) }
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
                    }
                    FloatingActionButton(
                        onClick = onAddHistoryClick,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(4.dp)
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
                                WorkOrderMaterialSummaryItem(material, nf)
                            }
                        }
                        repeat(columns - chunk.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.padding(vertical = SCREEN_PADDING_VERTICAL))
            }
        }
    }
}