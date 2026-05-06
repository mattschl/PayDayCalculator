package ms.mattschlenkrich.paycalculator.ui.extras.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefTypeAndEmployer

@Composable
fun ExtraDefinitionItem(
    item: ExtraDefTypeAndEmployer,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val nf = NumberFunctions()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.effective_date) + " " + item.definition.weEffectiveDate,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (item.definition.weIsFixed) nf.displayDollars(item.definition.weValue)
                    else nf.getPercentStringFromDouble(item.definition.weValue),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (item.definition.weIsDeleted) {
                Text(
                    text = stringResource(R.string.deleted),
                    color = Color.Red,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}