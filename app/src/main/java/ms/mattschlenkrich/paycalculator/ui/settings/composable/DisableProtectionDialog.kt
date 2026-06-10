package ms.mattschlenkrich.paycalculator.ui.settings.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.common.security.AuthResult

@Composable
fun DisableProtectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onPasswordVerify: (String) -> AuthResult
) {
    var disablePasswordInput by remember { mutableStateOf("") }
    var disablePasswordError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Disabling Protection") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter your current password or the recovery password to disable protection.")
                OutlinedTextField(
                    value = disablePasswordInput,
                    onValueChange = {
                        disablePasswordInput = it
                        disablePasswordError = null
                    },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = disablePasswordError != null
                )
                if (disablePasswordError != null) {
                    Text(
                        text = disablePasswordError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val result = onPasswordVerify(disablePasswordInput)
                if (result == AuthResult.SUCCESS_CUSTOM || result == AuthResult.SUCCESS_MASTER) {
                    onConfirm(disablePasswordInput)
                } else {
                    disablePasswordError = "Incorrect Password"
                }
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}