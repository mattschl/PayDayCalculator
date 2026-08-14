package ms.mattschlenkrich.paycalculator.ui.workorder.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderJobSpecCombined

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSpecOptionsDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    item: WorkOrderJobSpecCombined?,
    onUpdateInWorkOrder: (WorkOrderJobSpecCombined) -> Unit,
    onUpdateDefinition: (WorkOrderJobSpecCombined) -> Unit
) {
    if (showDialog && item != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
            ) {
                Text(
                    text = stringResource(R.string.job_spec_options),
                    style = MaterialTheme.typography.titleLarge
                )
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
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}