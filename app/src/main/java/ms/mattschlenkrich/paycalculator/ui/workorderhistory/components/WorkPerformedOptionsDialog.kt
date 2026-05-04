package ms.mattschlenkrich.paycalculator.ui.workorderhistory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryWorkPerformedCombined

@Composable
fun WorkPerformedOptionsDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    item: WorkOrderHistoryWorkPerformedCombined?,
    onWorkPerformedItemClick: (WorkOrderHistoryWorkPerformedCombined, Int) -> Unit,
    onUpdateWorkPerformed: (WorkOrderHistoryWorkPerformedCombined) -> Unit,
    onEditWorkPerformedDefinition: (WorkOrderHistoryWorkPerformedCombined) -> Unit
) {
    if (showDialog && item != null) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(stringResource(R.string.work_performed_options)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                ) {
                    Text(item.workPerformed.wpDescription)
                    Button(
                        onClick = {
                            onUpdateWorkPerformed(item)
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.update_work_performed_in_history))
                    }
                    Button(
                        onClick = {
                            onEditWorkPerformedDefinition(item)
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.edit_description_in_database))
                    }
                    Button(
                        onClick = {
                            onWorkPerformedItemClick(item, 0) // 0 for Delete
                            onDismissRequest()
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
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}