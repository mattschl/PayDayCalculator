package ms.mattschlenkrich.paycalculator.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
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

@Composable
fun PasswordDialog(
    isChange: Boolean,
    onDismiss: () -> Unit,
    onPasswordSet: (String) -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isChange) "Change App Password" else "Set App Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPasswordInput,
                    onValueChange = { confirmPasswordInput = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (passwordInput.isBlank()) {
                    passwordError = "Password cannot be empty"
                } else if (passwordInput != confirmPasswordInput) {
                    passwordError = "Passwords do not match"
                } else {
                    onPasswordSet(passwordInput)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FontSizeSelector(
    currentSize: Float,
    onFontSizeChange: (Float) -> Unit
) {
    val fontSizes = listOf(
        "Small" to 12f,
        "Normal" to 16f,
        "Large" to 20f,
        "Extra Large" to 24f
    )

    fontSizes.forEach { (label, size) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = (size == currentSize),
                    onClick = { onFontSizeChange(size) },
                    role = Role.RadioButton
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = (size == currentSize),
                onClick = null
            )
            Text(
                text = "$label ($size sp)",
                fontSize = size.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun ColumnDensitySelector(
    currentMinWidth: Int,
    onMinColumnWidthChange: (Int) -> Unit
) {
    val columnOptions = listOf(
        "Ultra High (Most Columns)" to 120,
        "Very High" to 180,
        "High" to 240,
        "Medium (Default)" to DEFAULT_MIN_COLUMN_WIDTH,
        "Low (Fewer Columns)" to 480
    )

    columnOptions.forEach { (label, width) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = (width == currentMinWidth),
                    onClick = { onMinColumnWidthChange(width) },
                    role = Role.RadioButton
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = (width == currentMinWidth),
                onClick = null
            )
            Text(
                text = label,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun ThemeSelector(
    isSystemTheme: Boolean,
    isDarkTheme: Boolean,
    onIsSystemThemeChange: (Boolean) -> Unit,
    onIsDarkThemeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSystemTheme,
                onClick = { onIsSystemThemeChange(!isSystemTheme) },
                role = Role.Switch
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Use System Theme")
        Switch(
            checked = isSystemTheme,
            onCheckedChange = null
        )
    }

    if (!isSystemTheme) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.selectable(
                    selected = !isDarkTheme,
                    onClick = { onIsDarkThemeChange(false) },
                    role = Role.RadioButton
                )
            ) {
                RadioButton(selected = !isDarkTheme, onClick = null)
                Text("Light", modifier = Modifier.padding(start = 8.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.selectable(
                    selected = isDarkTheme,
                    onClick = { onIsDarkThemeChange(true) },
                    role = Role.RadioButton
                )
            ) {
                RadioButton(selected = isDarkTheme, onClick = null)
                Text("Dark", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}