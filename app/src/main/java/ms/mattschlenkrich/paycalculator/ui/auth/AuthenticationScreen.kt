package ms.mattschlenkrich.paycalculator.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.security.AuthResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(
    onPasswordVerify: (String) -> AuthResult,
    onPasswordSet: (String) -> Unit,
    onAuthenticated: () -> Unit
) {
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var failedAttempts by rememberSaveable { mutableIntStateOf(0) }

    if (showResetDialog) {
        var newPassword by rememberSaveable { mutableStateOf("") }
        var confirmPassword by rememberSaveable { mutableStateOf("") }
        var resetError by rememberSaveable { mutableStateOf<String?>(null) }

        ModalBottomSheet(
            onDismissRequest = { /* Force reset - maybe don't allow dismiss? */ },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING / 2)
            ) {
                Text(
                    text = "Reset Your Password",
                    style = MaterialTheme.typography.titleLarge
                )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
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
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL * 2),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "App Protected",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = ELEMENT_SPACING * 2)
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
                    .padding(top = ELEMENT_SPACING)
                    .fillMaxWidth()
            ) {
                Text("Unlock")
            }
        }
    }
}