package ms.mattschlenkrich.paycalculator.ui.workorderhistory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.AutoCompleteTextField
import ms.mattschlenkrich.paycalculator.common.compose.CapitalizedOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.data.Areas
import ms.mattschlenkrich.paycalculator.data.WorkPerformed

@Composable
fun WorkPerformedEntryCard(
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
    onAddWorkPerformed: () -> Unit
) {
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