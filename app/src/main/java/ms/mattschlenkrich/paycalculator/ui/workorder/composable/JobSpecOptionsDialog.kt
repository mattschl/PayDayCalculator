package ms.mattschlenkrich.paycalculator.ui.workorder.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderJobSpecCombined

@Composable
fun JobSpecOptionsDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    item: WorkOrderJobSpecCombined?,
    onUpdateInWorkOrder: (WorkOrderJobSpecCombined) -> Unit,
    onUpdateDefinition: (WorkOrderJobSpecCombined) -> Unit
) {
    if (showDialog && item != null) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(stringResource(R.string.job_spec_options)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                ) {
                    Text(
                        text = item.jobSpec.jsName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = {
                            onUpdateInWorkOrder(item)
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.update_job_spec_in_work_order))
                    }
                    Button(
                        onClick = {
                            onUpdateDefinition(item)
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.update_the_job_spec_definition))
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