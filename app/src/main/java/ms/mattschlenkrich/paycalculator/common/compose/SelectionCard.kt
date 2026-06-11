package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.data.entity.Employers

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectionCard(
    modifier: Modifier = Modifier,
    employers: List<Employers>,
    selectedEmployer: Employers?,
    onEmployerSelected: (Employers) -> Unit,
    onAddNewEmployer: () -> Unit,
    cutOffDates: List<String>,
    selectedCutOffDate: String,
    onCutOffDateSelected: (String) -> Unit,
    onGenerateCutoffClick: (() -> Unit)? = null,
    onDeleteCutoffClick: (() -> Unit)? = null,
    displayDate: (String) -> String = { it },
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SCREEN_PADDING_HORIZONTAL / 2),
            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
            maxItemsInEachRow = 2
        ) {
            // Employer Selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 280.dp)
            ) {
                Text(
                    text = stringResource(R.string.employer),
                    modifier = Modifier.padding(end = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                SimpleDropdownField(
                    label = "",
                    items = employers,
                    selectedItem = selectedEmployer,
                    onItemSelected = onEmployerSelected,
                    itemToString = { it.employerName },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onAddNewEmployer) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_new_employer)
                    )
                }
            }

            // Cut-off Date Selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 280.dp)
            ) {
                Text(
                    text = stringResource(R.string.cut_off),
                    modifier = Modifier.padding(end = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                SimpleDropdownField(
                    label = "",
                    items = cutOffDates,
                    selectedItem = selectedCutOffDate,
                    onItemSelected = onCutOffDateSelected,
                    itemToString = { if (it.isEmpty()) "" else displayDate(it) },
                    modifier = Modifier.weight(1f)
                )
                if (onGenerateCutoffClick != null) {
                    IconButton(onClick = onGenerateCutoffClick) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.generate_a_new_cut_off)
                        )
                    }
                }
                if (onDeleteCutoffClick != null && selectedCutOffDate.isNotEmpty()) {
                    IconButton(onClick = onDeleteCutoffClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete this pay period",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}