package ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable

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
import ms.mattschlenkrich.paycalculator.data.model.MaterialInSequence

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
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(stringResource(R.string.material_options)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                ) {
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