package ms.mattschlenkrich.paycalculator.ui.settings.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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