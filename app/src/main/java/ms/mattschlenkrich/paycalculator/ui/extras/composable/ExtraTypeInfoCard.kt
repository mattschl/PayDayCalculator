package ms.mattschlenkrich.paycalculator.ui.extras.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import ms.mattschlenkrich.paycalculator.common.ExtraAppliesToFrequencies
import ms.mattschlenkrich.paycalculator.common.ExtraAttachToFrequencies
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes

@Composable
fun ExtraTypeInfoCard(
    extraType: WorkExtraTypes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (extraType.wetIsDeleted) {
                Text(
                    text = stringResource(R.string.deleted),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.calculated) + " " +
                                (ExtraAppliesToFrequencies.entries.getOrNull(extraType.wetAppliesTo)?.frequency
                                    ?: ""),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.attaches_to) + " " +
                                (ExtraAttachToFrequencies.entries.getOrNull(extraType.wetAttachTo)?.frequency
                                    ?: ""),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.this_is_a) + " " +
                                if (extraType.wetIsCredit) stringResource(R.string.credit) else stringResource(
                                    R.string.deduction
                                ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.applied) + " " +
                                if (extraType.wetIsDefault) stringResource(R.string.by_default) else stringResource(
                                    R.string.manually
                                ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}