package ms.mattschlenkrich.paycalculator.ui.settings.composable

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ms.mattschlenkrich.paycalculator.common.compose.NumberOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.common.compose.calculateGridColumns
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
    minColumnWidth: Int,
    employers: List<Employers>,
    onFontSizeChange: (Float) -> Unit,
    onPayPeriodsLimitChange: (Int) -> Unit,
    onIsDarkThemeChange: (Boolean) -> Unit,
    onIsSystemThemeChange: (Boolean) -> Unit,
    onIsPasswordProtectedChange: (Boolean) -> Unit,
    onPasswordSet: (String) -> Unit,
    onPasswordVerify: (String) -> AuthResult,
    onDefaultEmployerChange: (Long?) -> Unit,
    onMinColumnWidthChange: (Int) -> Unit
) {
    val columns = calculateGridColumns(minColumnWidth)
    val dynamicPadding = (16 / columns).dp
    val dynamicVerticalPadding = (24 / columns).dp
    val dynamicItemPadding = (12 / columns).dp

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDisableConfirmDialog by remember { mutableStateOf(false) }

    if (showDisableConfirmDialog) {
        DisableProtectionDialog(
            onDismiss = { showDisableConfirmDialog = false },
            onConfirm = {
                onIsPasswordProtectedChange(false)
                showDisableConfirmDialog = false
            },
            onPasswordVerify = onPasswordVerify
        )
    }

    if (showPasswordDialog) {
        PasswordDialog(
            isChange = isPasswordSet,
            onDismiss = { showPasswordDialog = false },
            onPasswordSet = { password ->
                onPasswordSet(password)
                onIsPasswordProtectedChange(true)
                showPasswordDialog = false
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = dynamicPadding, vertical = dynamicVerticalPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dynamicVerticalPadding)
        ) {
            Text(
                text = "Sample Text at current font size",
                fontSize = fontSize.sp,
                modifier = Modifier.padding(bottom = dynamicVerticalPadding)
            )

            Text("Select Font Size:", style = MaterialTheme.typography.titleMedium)
            FontSizeSelector(
                currentSize = fontSize,
                onFontSizeChange = onFontSizeChange,
                itemPadding = dynamicItemPadding,
                textPadding = dynamicPadding
            )

            Spacer(modifier = Modifier.height(dynamicVerticalPadding))

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

            Spacer(modifier = Modifier.height(dynamicVerticalPadding))

            Text("Grid Column Density:", style = MaterialTheme.typography.titleMedium)
            ColumnDensitySelector(
                currentMinWidth = minColumnWidth,
                onMinColumnWidthChange = onMinColumnWidthChange,
                itemPadding = dynamicItemPadding,
                textPadding = dynamicPadding
            )

            Spacer(modifier = Modifier.height(dynamicVerticalPadding))

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

            HorizontalDivider(modifier = Modifier.padding(vertical = dynamicVerticalPadding))

            Text("Theme Selection:", style = MaterialTheme.typography.titleMedium)
            ThemeSelector(
                isSystemTheme = isSystemTheme,
                isDarkTheme = isDarkTheme,
                onIsSystemThemeChange = onIsSystemThemeChange,
                onIsDarkThemeChange = onIsDarkThemeChange,
                itemPadding = dynamicItemPadding
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = dynamicVerticalPadding))

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
                    .padding(vertical = dynamicItemPadding),
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