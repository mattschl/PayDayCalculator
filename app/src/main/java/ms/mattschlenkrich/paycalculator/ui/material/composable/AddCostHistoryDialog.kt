package ms.mattschlenkrich.paycalculator.ui.material.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField

@Composable
fun AddCostHistoryDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    purchaseDate: String,
    onPurchaseDateClick: () -> Unit,
    costInput: String,
    onCostInputChange: (String) -> Unit,
    onCalculatorClick: () -> Unit,
    onSaveClick: () -> Unit,
    df: DateFunctions
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(stringResource(R.string.add_cost_history)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onPurchaseDateClick) {
                        Text(
                            "${stringResource(R.string.purchase_date)}: ${
                                df.getDisplayDate(
                                    purchaseDate
                                )
                            }"
                        )
                    }
                    DecimalOutlinedTextField(
                        value = costInput,
                        onValueChange = onCostInputChange,
                        label = { Text(stringResource(R.string.cost)) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = onCalculatorClick) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Calculate Cost"
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onSaveClick) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}