package ms.mattschlenkrich.paycalculator.tax

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.Composable
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
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.go_back)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (onDeleteClick != null) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            }
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
            InfoRow(label = stringResource(R.string.tax_type), value = taxType)
            InfoRow(label = stringResource(R.string.level_bracket), value = taxLevel)
            InfoRow(label = stringResource(R.string.effective_date), value = effectiveDate)

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

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
                    modifier = Modifier.padding(start = 8.dp)
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

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = hasUpperLimit, onCheckedChange = onHasUpperLimitChange)
                Text(
                    text = stringResource(R.string.has_upper_limit),
                    modifier = Modifier.padding(start = 8.dp)
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

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}