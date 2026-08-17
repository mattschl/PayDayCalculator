package ms.mattschlenkrich.paycalculator.ui.material.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.StandardItem
import ms.mattschlenkrich.paycalculator.data.entity.Material

@Composable
fun MaterialItem(
    item: Material,
    nf: NumberFunctions,
    onClick: (Material) -> Unit
) {
    StandardItem(
        text = item.mName,
        isDeleted = item.mIsDeleted,
        onClick = { onClick(item) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cost: ${nf.displayDollars(item.mCost)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Price: ${nf.displayDollars(item.mPrice)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}