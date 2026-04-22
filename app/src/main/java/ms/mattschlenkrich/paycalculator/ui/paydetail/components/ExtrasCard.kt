package ms.mattschlenkrich.paycalculator.ui.paydetail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.ExtraContainer
import ms.mattschlenkrich.paycalculator.data.TaxAndAmount

@Composable
fun ExtrasCard(
    title: String,
    extras: List<ExtraContainer>,
    taxes: List<TaxAndAmount> = emptyList(),
    total: String,
    onAddClick: () -> Unit,
    onExtraClick: (ExtraContainer) -> Unit,
    onActiveChange: (ExtraContainer, Boolean) -> Unit,
    addButtonContentDescription: String
) {
    val nf = NumberFunctions()
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                FloatingActionButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(40.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = addButtonContentDescription)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            extras.forEach { extra ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExtraClick(extra) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        extra.extraName,
                        modifier = Modifier.weight(1.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        nf.displayDollars(extra.amount),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Checkbox(
                        checked = extra.amount > 0.0,
                        onCheckedChange = { onActiveChange(extra, it) },
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            if (taxes.isNotEmpty()) {
                taxes.forEach { tax ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            tax.taxType,
                            modifier = Modifier.weight(1.5f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            nf.displayDollars(tax.amount),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.bodySmall
                        )
                        // Empty spacer to align with the Checkbox in extras
                        Spacer(
                            modifier = Modifier
                                .width(48.dp)
                                .padding(start = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 0.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (title == stringResource(R.string.credits)) stringResource(R.string.total_credits) else stringResource(
                        R.string.total_deductions
                    ),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    total,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}