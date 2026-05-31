package ms.mattschlenkrich.paycalculator.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.common.security.AuthResult

@Composable
fun AuthenticationScreen(
    onPasswordVerify: (String) -> AuthResult,
    onPasswordSet: (String) -> Unit,
    onAuthenticated: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var failedAttempts by remember { mutableIntStateOf(0) }

    if (showResetDialog) {
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var resetError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { /* Force reset */ },
            title = { Text("Reset Your Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "You have used the master backup password. Please set a new custom password to continue.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (resetError != null) {
                        Text(
                            text = resetError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPassword.isBlank()) {
                        resetError = "Password cannot be empty"
                    } else if (newPassword != confirmPassword) {
                        resetError = "Passwords do not match"
                    } else {
                        onPasswordSet(newPassword)
                        onAuthenticated()
                        showResetDialog = false
                    }
                }) {
                    Text("Save")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "App Protected",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = passwordInput,
                onValueChange = {
                    passwordInput = it
                    error = null
                },
                label = { Text("Enter Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                isError = error != null
            )

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = {
                    val result = onPasswordVerify(passwordInput)
                    when (result) {
                        AuthResult.SUCCESS_CUSTOM -> {
                            failedAttempts = 0
                            onAuthenticated()
                        }

                        AuthResult.SUCCESS_MASTER -> {
                            failedAttempts = 0
                            showResetDialog = true
                        }

                        AuthResult.FAILURE -> {
                            failedAttempts++
                            error = if (failedAttempts >= 3) {
                                "Incorrect Password. Please email the developer at matt_schl@hotmail.com to receive a reset password."
                            } else {
                                "Incorrect Password"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
            ) {
                Text("Unlock")
            }
        }
    }
}