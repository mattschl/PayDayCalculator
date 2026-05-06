package ms.mattschlenkrich.paycalculator.ui.timesheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.data.entity.Employers

@Composable
fun TimeSheetSelectionCard(
    employers: List<Employers>,
    selectedEmployer: Employers?,
    onEmployerSelected: (Employers) -> Unit,
    onAddNewEmployer: () -> Unit,
    cutOffDates: List<String>,
    selectedCutOffDate: String,
    onCutOffDateSelected: (String) -> Unit,
    onGenerateCutoffClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.employer),
                    modifier = Modifier.width(100.dp),
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.cut_off_date),
                    modifier = Modifier.width(100.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                SimpleDropdownField(
                    label = "",
                    items = cutOffDates,
                    selectedItem = selectedCutOffDate,
                    onItemSelected = onCutOffDateSelected,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onGenerateCutoffClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.generate_a_new_cut_off)
                    )
                }
            }
        }
    }
}