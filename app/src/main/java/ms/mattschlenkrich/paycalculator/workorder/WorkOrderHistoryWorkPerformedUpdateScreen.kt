package ms.mattschlenkrich.paycalculator.workorder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.AutoCompleteTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.data.Areas
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWorkPerformedCombined
import ms.mattschlenkrich.paycalculator.data.WorkPerformed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderHistoryWorkPerformedUpdateScreen(
    originalWorkOrderHistory: WorkOrderHistoryWithDates?,
    originalWorkPerformedHistory: WorkOrderHistoryWorkPerformedCombined?,
    workPerformedSuggestions: List<WorkPerformed>,
    areaSuggestions: List<Areas>,
    onUpdate: (String, String, String?) -> Unit,
    onBack: () -> Unit,
) {
    var workPerformedText by remember { mutableStateOf("") }
    var areaText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    LaunchedEffect(originalWorkPerformedHistory) {
        originalWorkPerformedHistory?.let {
            workPerformedText = it.workPerformed.wpDescription
            areaText = it.area?.areaName ?: ""
            noteText = it.workOrderHistoryWorkPerformed.wowpNote ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WorkOrderHistoryWorkPerformedUpdateScreen") },
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
                onClick = { onUpdate(workPerformedText, areaText, noteText) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_done),
                    contentDescription = stringResource(R.string.done)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL, vertical = SCREEN_PADDING_VERTICAL)
                .verticalScroll(rememberScrollState())
        ) {
            originalWorkOrderHistory?.let { history ->
                val info = stringResource(R.string.update_work_performed_on) +
                        " ${history.workDate.wdDate}\n" +
                        stringResource(R.string.for_work_order) +
                        " ${history.workOrder.woNumber} @ ${history.workOrder.woAddress}\n" +
                        history.workOrder.woDescription
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = ELEMENT_SPACING)
                )
            }

            originalWorkPerformedHistory?.let { historyItem ->
                Text(
                    text = stringResource(R.string.old_work_description) +
                            " ${historyItem.workPerformed.wpDescription}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            AutoCompleteTextField(
                value = workPerformedText,
                onValueChange = { workPerformedText = it },
                label = stringResource(R.string.work_performed),
                suggestions = workPerformedSuggestions,
                onItemSelected = { workPerformedText = it.wpDescription },
                itemToString = { it.wpDescription },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            originalWorkPerformedHistory?.let { historyItem ->
                val areaLabel = if (historyItem.area != null) {
                    stringResource(R.string.old_area_of_work) + " ${historyItem.area.areaName}"
                } else {
                    stringResource(R.string.no_area_was_indicated)
                }
                Text(
                    text = areaLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            AutoCompleteTextField(
                value = areaText,
                onValueChange = { areaText = it },
                label = stringResource(R.string.area),
                suggestions = areaSuggestions,
                onItemSelected = { areaText = it.areaName },
                itemToString = { it.areaName },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            SelectAllOutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text(stringResource(R.string.enter_note_optional)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )
        }
    }
}