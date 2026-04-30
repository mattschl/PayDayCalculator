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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    fontSize: Float,
    payPeriodsLimit: Int,
    onFontSizeChange: (Float) -> Unit,
    onPayPeriodsLimitChange: (Int) -> Unit
) {
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

            OutlinedTextField(
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}