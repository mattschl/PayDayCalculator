package ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import ms.mattschlenkrich.paycalculator.data.model.MaterialInSequence

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialOptionsDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    item: MaterialInSequence?,
    onDelete: (MaterialInSequence) -> Unit,
    onEditInHistory: (MaterialInSequence) -> Unit,
    onEditMaterialDefinition: (MaterialInSequence) -> Unit
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
                    text = stringResource(R.string.material_options),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(item.mName)
                Button(
                    onClick = {
                        onEditInHistory(item)
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.update_material_in_history))
                }
                Button(
                    onClick = {
                        onEditMaterialDefinition(item)
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.edit_the_material_in_the_database))
                }
                Button(
                    onClick = {
                        onDelete(item)
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}