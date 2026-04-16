package ms.mattschlenkrich.paycalculator.workorder

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.AutoCompleteTextField
import ms.mattschlenkrich.paycalculator.common.compose.CapitalizedOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.data.Areas
import ms.mattschlenkrich.paycalculator.data.JobSpec
import ms.mattschlenkrich.paycalculator.data.MaterialAndQuantity
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.data.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paycalculator.data.WorkPerformedAndQuantity

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
) {
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

            // Work Order Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                    ) {
                        Text(
                            text = "${stringResource(R.string.employer)}: $employerName",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )

                        SelectAllOutlinedTextField(
                            value = woNumber,
                            onValueChange = onWoNumberChange,
                            label = { Text(stringResource(R.string.work_order_number)) },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        CapitalizedOutlinedTextField(
                            value = address,
                            onValueChange = onAddressChange,
                            label = { Text(stringResource(R.string.address)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        CapitalizedOutlinedTextField(
                            value = description,
                            onValueChange = onDescriptionChange,
                            label = { Text(stringResource(R.string.general_job_description)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false
                        )
                    }
                }
            }

            // Job Specs Entry Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                    ) {
                        Text(
                            text = stringResource(R.string.add_job_spec_below),
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                        ) {
                            AutoCompleteTextField(
                                value = jobSpecText,
                                onValueChange = onJobSpecTextChange,
                                label = stringResource(R.string.job_spec),
                                suggestions = jobSpecSuggestions,
                                onItemSelected = onJobSpecSelected,
                                itemToString = { it.jsName },
                                modifier = Modifier.weight(1.5f),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words
                                )
                            )
                            AutoCompleteTextField(
                                value = areaText,
                                onValueChange = onAreaTextChange,
                                label = stringResource(R.string.area),
                                suggestions = areaSuggestions,
                                onItemSelected = onAreaSelected,
                                itemToString = { it.areaName },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words
                                )
                            )
                        }

                        CapitalizedOutlinedTextField(
                            value = workPerformedNote,
                            onValueChange = onWorkPerformedNoteChange,
                            label = { Text(stringResource(R.string.enter_note_optional)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = onAddJobSpecClick,
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(stringResource(R.string.add))
                        }
                    }
                }
            }

            // Added Job Specs List
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onJobSpecClick(combined) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = "${combined.workOrderJobSpec.wojsSequence}) ${combined.jobSpec.jsName}" +
                                    (combined.area?.let { " ${stringResource(R.string._in_)} ${it.areaName}" }
                                        ?: "") +
                                    (combined.workOrderJobSpec.wojsNote?.let { " - $it" } ?: ""),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // History Header
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

                val wpChunks = workPerformedList.chunked(2)
                items(wpChunks) { chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                    ) {
                        chunk.forEach { wp ->
                            Box(modifier = Modifier.weight(1f)) {
                                WorkPerformedSummaryItem(wp, nf)
                            }
                        }
                        if (chunk.size == 1) {
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

                val matChunks = materialsList.chunked(2)
                items(matChunks) { chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                    ) {
                        chunk.forEach { material ->
                            Box(modifier = Modifier.weight(1f)) {
                                MaterialItem(material, nf)
                            }
                        }
                        if (chunk.size == 1) {
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

@Composable
fun HistoryItem(
    history: WorkOrderHistoryWithDates,
    df: DateFunctions,
    nf: NumberFunctions,
    onClick: (WorkOrderHistoryWithDates) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(history) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = df.getDisplayDate(history.workDate.wdDate),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = getHistoryHoursDisplay(history, nf),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            history.history.woHistoryNote?.let { note ->
                if (note.isNotBlank()) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun getHistoryHoursDisplay(history: WorkOrderHistoryWithDates, nf: NumberFunctions): String {
    val parts = mutableListOf<String>()
    if (history.history.woHistoryRegHours > 0) parts.add("Reg: ${nf.getNumberFromDouble(history.history.woHistoryRegHours)}")
    if (history.history.woHistoryOtHours > 0) parts.add("OT: ${nf.getNumberFromDouble(history.history.woHistoryOtHours)}")
    if (history.history.woHistoryDblOtHours > 0) parts.add("Dbl: ${nf.getNumberFromDouble(history.history.woHistoryDblOtHours)}")
    return parts.joinToString(" | ")
}

@Composable
fun WorkPerformedSummaryItem(wp: WorkPerformedAndQuantity, nf: NumberFunctions) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = wp.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            wp.area?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
fun MaterialItem(material: MaterialAndQuantity, nf: NumberFunctions) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = material.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = nf.getNumberFromDouble(material.quantity),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}