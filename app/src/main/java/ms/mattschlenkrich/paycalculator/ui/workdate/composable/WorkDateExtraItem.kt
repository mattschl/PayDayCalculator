package ms.mattschlenkrich.paycalculator.ui.workdate.composable

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras

@Composable
fun WorkDateExtraItem(
    extra: WorkDateExtras,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val nf = NumberFunctions()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = !extra.wdeIsDeleted,
            onCheckedChange = { onClick() }
        )
        val display = buildString {
            append(extra.wdeName)
            append(if (extra.wdeIsCredit) " (add) " else " (subtract) ")
            append(
                if (extra.wdeIsFixed) nf.displayDollars(extra.wdeValue)
                else nf.getPercentStringFromDouble(extra.wdeValue)
            )
        }
        Text(
            text = display,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = if (extra.wdeIsCredit) Color.Unspecified else Color.Red
        )
    }
}