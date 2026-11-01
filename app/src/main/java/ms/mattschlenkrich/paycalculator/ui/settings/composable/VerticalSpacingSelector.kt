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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VerticalSpacingSelector(
    currentSpacing: Float,
    onSpacingChange: (Float) -> Unit,
    itemPadding: Dp = 8.dp,
    textPadding: Dp = 16.dp
) {
    val spacingOptions = listOf(
        "Tight" to 4f,
        "Compact" to 6f,
        "Normal" to 10f,
        "Comfortable" to 14f,
        "Spaced" to 18f
    )

    spacingOptions.forEach { (label, spacing) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = (spacing == currentSpacing),
                    onClick = { onSpacingChange(spacing) },
                    role = Role.RadioButton
                )
                .padding(vertical = itemPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = (spacing == currentSpacing),
                onClick = null
            )
            Text(
                text = "$label (${spacing.toInt()} dp)",
                modifier = Modifier.padding(start = textPadding)
            )
        }
    }
}