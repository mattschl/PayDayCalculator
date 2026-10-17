package ms.mattschlenkrich.paycalculator.ui.workdate.composable

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras

@Composable
fun WorkDateExtraItem(
    extra: WorkDateExtras,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    index: Int = 0
) {
    val nf = NumberFunctions()
    val containerColor = if (index % 2 == 0) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
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
                color = if (extra.wdeIsCredit) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            )
        }
    }
}