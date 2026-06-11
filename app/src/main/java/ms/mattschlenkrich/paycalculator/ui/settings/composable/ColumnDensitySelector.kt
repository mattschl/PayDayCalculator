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
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH

@Composable
fun ColumnDensitySelector(
    currentMinWidth: Int,
    onMinColumnWidthChange: (Int) -> Unit,
    itemPadding: Dp = 8.dp,
    textPadding: Dp = 16.dp
) {
    val columnOptions = listOf(
        "Ultra High (Most Columns)" to 120,
        "Very High" to 180,
        "High" to 240,
        "Medium (Default)" to DEFAULT_MIN_COLUMN_WIDTH,
        "Low (Fewer Columns)" to 480
    )

    columnOptions.forEach { (label, width) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = (width == currentMinWidth),
                    onClick = { onMinColumnWidthChange(width) },
                    role = Role.RadioButton
                )
                .padding(vertical = itemPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = (width == currentMinWidth),
                onClick = null
            )
            Text(
                text = label,
                modifier = Modifier.padding(start = textPadding)
            )
        }
    }
}