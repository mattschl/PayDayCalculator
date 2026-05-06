package ms.mattschlenkrich.paycalculator.ui.workorderhistory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.AutoCompleteTextField
import ms.mattschlenkrich.paycalculator.common.compose.CapitalizedOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder

@Composable
fun WorkOrderHistoryInfoCard(
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
    addTimeButtonText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(6.dp)
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