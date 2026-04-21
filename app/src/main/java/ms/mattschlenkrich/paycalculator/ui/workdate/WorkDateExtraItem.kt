package ms.mattschlenkrich.paycalculator.ui.workdate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.WorkDateExtras

@Composable
fun WorkDateExtraItem(
    extra: WorkDateExtras,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val nf = NumberFunctions()
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.Companion.CenterVertically
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
            modifier = Modifier.Companion.weight(1f),
            color = if (extra.wdeIsCredit) Color.Companion.Black else Color.Companion.Red
        )
        if (!extra.wdeIsDeleted) {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
            }
        }
    }
}