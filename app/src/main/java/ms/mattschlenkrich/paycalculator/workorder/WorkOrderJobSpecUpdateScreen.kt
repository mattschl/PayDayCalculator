package ms.mattschlenkrich.paycalculator.workorder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import ms.mattschlenkrich.paycalculator.data.JobSpec
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderJobSpecCombined

@Composable
fun WorkOrderJobSpecUpdateScreen(
    workOrder: WorkOrder?,
    originalJobSpec: WorkOrderJobSpecCombined?,
    jobSpecSuggestions: List<JobSpec>,
    areaSuggestions: List<Areas>,
    onUpdate: (String, String, String?) -> Unit,
    onBack: () -> Unit,
) {
    var jobSpecText by remember { mutableStateOf("") }
    var areaText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    LaunchedEffect(originalJobSpec) {
        originalJobSpec?.let {
            jobSpecText = it.jobSpec.jsName
            areaText = it.area?.areaName ?: ""
            noteText = it.workOrderJobSpec.wojsNote ?: ""
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onUpdate(jobSpecText, areaText, noteText) },
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
            workOrder?.let { wo ->
                val info = stringResource(R.string.edit_the_job_spec_for_wo_) +
                        " ${wo.woNumber}" + stringResource(R.string._at_) + " ${wo.woAddress}\n" +
                        wo.woDescription
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = ELEMENT_SPACING)
                )
            }

            originalJobSpec?.let { spec ->
                Text(
                    text = stringResource(R.string.original_job_spec) + " ${spec.jobSpec.jsName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            AutoCompleteTextField(
                value = jobSpecText,
                onValueChange = { jobSpecText = it },
                label = stringResource(R.string.job_spec),
                suggestions = jobSpecSuggestions,
                onItemSelected = { jobSpecText = it.jsName },
                itemToString = { it.jsName },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words
                )
            )

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            originalJobSpec?.let { spec ->
                val area = spec.area
                val areaLabel = if (area != null) {
                    stringResource(R.string.old_area_of_work) + " ${area.areaName}"
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
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words
                )
            )

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            SelectAllOutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text(stringResource(R.string.enter_note_optional)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences
                )
            )
        }
    }
}