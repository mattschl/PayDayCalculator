package ms.mattschlenkrich.paycalculator.ui.workorder.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.AutoCompleteTextField
import ms.mattschlenkrich.paycalculator.common.compose.CapitalizedOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.data.entity.Areas
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec

@Composable
fun JobSpecsEntryCard(
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
    onAddJobSpecClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
        ) {
            Text(
                text = stringResource(R.string.add_job_spec_below),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            AutoCompleteTextField(
                value = jobSpecText,
                onValueChange = onJobSpecTextChange,
                label = stringResource(R.string.job_spec),
                suggestions = jobSpecSuggestions,
                onItemSelected = onJobSpecSelected,
                itemToString = { it.jsName },
                modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )

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