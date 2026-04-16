package ms.mattschlenkrich.paycalculator.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit
) {
    Scaffold(
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
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
        }
    }
}