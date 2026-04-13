package ms.mattschlenkrich.paycalculator.workorder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.AutoCompleteTextField
import ms.mattschlenkrich.paycalculator.common.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.StandardTopAppBar

@Composable
fun WorkOrderHistoryMaterialUpdateScreen(
    info: String,
    materialName: String,
    onMaterialNameChange: (String) -> Unit,
    materialSuggestions: List<String>,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    originalMaterialLabel: String,
    originalQuantityLabel: String,
    onDoneClick: () -> Unit
) {
    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(R.string.update_material_in_history)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onDoneClick) {
                Icon(Icons.Default.Done, contentDescription = stringResource(R.string.done))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = SCREEN_PADDING_HORIZONTAL,
                    vertical = SCREEN_PADDING_VERTICAL
                ),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
        ) {
            Text(
                text = info,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = originalMaterialLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            AutoCompleteTextField(
                value = materialName,
                onValueChange = onMaterialNameChange,
                suggestions = materialSuggestions,
                onItemSelected = { onMaterialNameChange(it) },
                label = stringResource(R.string.material_name),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = originalQuantityLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = onQuantityChange,
                label = { Text(stringResource(R.string.qty)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}