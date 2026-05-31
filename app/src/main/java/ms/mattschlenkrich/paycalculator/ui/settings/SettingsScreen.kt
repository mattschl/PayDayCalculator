package ms.mattschlenkrich.paycalculator.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import ms.mattschlenkrich.paycalculator.common.compose.NumberOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.common.security.AuthResult
import ms.mattschlenkrich.paycalculator.data.entity.Employers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    fontSize: Float,
    payPeriodsLimit: Int,
    isDarkTheme: Boolean,
    isSystemTheme: Boolean,
    isPasswordProtected: Boolean,
    isPasswordSet: Boolean,
    defaultEmployerId: Long?,
    employers: List<Employers>,
    onFontSizeChange: (Float) -> Unit,
    onPayPeriodsLimitChange: (Int) -> Unit,
    onIsDarkThemeChange: (Boolean) -> Unit,
    onIsSystemThemeChange: (Boolean) -> Unit,
    onIsPasswordProtectedChange: (Boolean) -> Unit,
    onPasswordSet: (String) -> Unit,
    onPasswordVerify: (String) -> AuthResult,
    onDefaultEmployerChange: (Long?) -> Unit
) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showDisableConfirmDialog by remember { mutableStateOf(false) }
    var disablePasswordInput by remember { mutableStateOf("") }
    var disablePasswordError by remember { mutableStateOf<String?>(null) }

    if (showDisableConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showDisableConfirmDialog = false
                disablePasswordInput = ""
                disablePasswordError = null
            },
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
                        onIsPasswordProtectedChange(false)
                        showDisableConfirmDialog = false
                        disablePasswordInput = ""
                        disablePasswordError = null
                    } else {
                        disablePasswordError = "Incorrect Password"
                    }
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDisableConfirmDialog = false
                    disablePasswordInput = ""
                    disablePasswordError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                passwordInput = ""
                confirmPasswordInput = ""
                passwordError = null
            },
            title = { Text("Set App Password") },
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
                        onIsPasswordProtectedChange(true)
                        showPasswordDialog = false
                        passwordInput = ""
                        confirmPasswordInput = ""
                        passwordError = null
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPasswordDialog = false
                    passwordInput = ""
                    confirmPasswordInput = ""
                    passwordError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        /*  topBar = {
              TopAppBar(
                  title = { Text("SettingsScreen") }
              )
          },*/
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sample Text at current font size",
                fontSize = fontSize.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text("Select Font Size:", style = MaterialTheme.typography.titleMedium)

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
                            selected = (size == fontSize),
                            onClick = { onFontSizeChange(size) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (size == fontSize),
                        onClick = null // null because the row handles the click
                    )
                    Text(
                        text = "$label ($size sp)",
                        fontSize = size.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Pay Periods to Show:", style = MaterialTheme.typography.titleMedium)

            var textValue by remember { mutableStateOf(payPeriodsLimit.toString()) }

            NumberOutlinedTextField(
                value = textValue,
                onValueChange = {
                    textValue = it
                    it.toIntOrNull()?.let { limit ->
                        if (limit > 0) {
                            onPayPeriodsLimitChange(limit)
                        }
                    }
                },
                label = { Text("Limit") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Default Employer on Launch:", style = MaterialTheme.typography.titleMedium)

            val noneEmployer = Employers(
                employerId = -1L,
                employerName = "None (Last Selected)",
                payFrequency = "",
                startDate = "",
                dayOfWeek = "",
                cutoffDaysBefore = 0,
                midMonthlyDate = 0,
                mainMonthlyDate = 0,
                employerIsDeleted = false,
                employerUpdateTime = ""
            )

            val dropdownItems = listOf(noneEmployer) + employers
            val currentDefault = dropdownItems.find { it.employerId == (defaultEmployerId ?: -1L) }

            SimpleDropdownField(
                label = "Default Employer",
                items = dropdownItems,
                selectedItem = currentDefault,
                onItemSelected = {
                    if (it.employerId == -1L) {
                        onDefaultEmployerChange(null)
                    } else {
                        onDefaultEmployerChange(it.employerId)
                    }
                },
                itemToString = { it.employerName },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Theme Selection:", style = MaterialTheme.typography.titleMedium)

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

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Security:", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isPasswordProtected,
                        onClick = {
                            if (!isPasswordProtected) {
                                showPasswordDialog = true
                            } else {
                                showDisableConfirmDialog = true
                            }
                        },
                        role = Role.Switch
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Password Protection")
                Switch(
                    checked = isPasswordProtected,
                    onCheckedChange = null
                )
            }

            if (isPasswordProtected) {
                TextButton(onClick = { showPasswordDialog = true }) {
                    Text(if (isPasswordSet) "Change Password" else "Set Password")
                }
            }
        }
    }
}