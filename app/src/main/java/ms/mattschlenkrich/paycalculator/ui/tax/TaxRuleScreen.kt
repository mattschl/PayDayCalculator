package ms.mattschlenkrich.paycalculator.ui.tax

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxRuleScreen(
    title: String,
    taxType: String,
    taxLevel: String,
    effectiveDate: String,
    percentage: String,
    onPercentageChange: (String) -> Unit,
    hasExemption: Boolean,
    onHasExemptionChange: (Boolean) -> Unit,
    exemptionAmount: String,
    onExemptionAmountChange: (String) -> Unit,
    hasUpperLimit: Boolean,
    onHasUpperLimitChange: (Boolean) -> Unit,
    upperLimit: String,
    onUpperLimitChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm && onDeleteClick != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    onDeleteClick()
                    showDeleteConfirm = false
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.modify_or_delete)) },
            text = {
                Text(
                    stringResource(R.string.are_you_sure_you_want_to_delete_) +
                            " " + taxType + " " + stringResource(R.string.level) + " " + taxLevel + "?"
                )
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                },
                actions = {
                    if (onDeleteClick != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSaveClick) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = stringResource(R.string.save)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL, vertical = SCREEN_PADDING_VERTICAL)
                .verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoRow(
                    label = stringResource(R.string.tax_type),
                    value = taxType,
                    modifier = Modifier.weight(1f)
                )
                InfoRow(
                    label = stringResource(R.string.level_bracket),
                    value = taxLevel,
                    modifier = Modifier.weight(1f)
                )
            }
            InfoRow(label = stringResource(R.string.effective_date), value = effectiveDate)

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    SelectAllOutlinedTextField(
                        value = percentage,
                        onValueChange = onPercentageChange,
                        label = { Text(stringResource(R.string.percentage)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(ELEMENT_SPACING))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasExemption, onCheckedChange = onHasExemptionChange)
                        Text(
                            text = stringResource(R.string.has_exemption_amount),
                            modifier = Modifier.padding(start = 4.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (hasExemption) {
                        SelectAllOutlinedTextField(
                            value = exemptionAmount,
                            onValueChange = onExemptionAmountChange,
                            label = { Text(stringResource(R.string.exemption_)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.width(ELEMENT_SPACING))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = hasUpperLimit,
                            onCheckedChange = { onHasUpperLimitChange(it) })
                        Text(
                            text = stringResource(R.string.has_upper_limit),
                            modifier = Modifier.padding(start = 4.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (hasUpperLimit) {
                        SelectAllOutlinedTextField(
                            value = upperLimit,
                            onValueChange = onUpperLimitChange,
                            label = { Text(stringResource(R.string.upper_limit_)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}