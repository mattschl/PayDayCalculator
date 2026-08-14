package ms.mattschlenkrich.paycalculator.ui.settings.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.common.security.AuthResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisableProtectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onPasswordVerify: (String) -> AuthResult
) {
    var disablePasswordInput by remember { mutableStateOf("") }
    var disablePasswordError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Confirm Disabling Protection",
                style = MaterialTheme.typography.titleLarge
            )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
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
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}