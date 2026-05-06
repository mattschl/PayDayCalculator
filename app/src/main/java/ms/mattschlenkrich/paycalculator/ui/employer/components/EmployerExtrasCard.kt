package ms.mattschlenkrich.paycalculator.ui.employer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes

@Composable
fun EmployerExtrasCard(
    extras: List<WorkExtraTypes>,
    onExtraClick: (WorkExtraTypes) -> Unit,
    onAddExtraClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.extra_pay_items),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onAddExtraClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_new_extra),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            extras.forEach { extra ->
                Text(
                    text = extra.wetName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExtraClick(extra) }
                        .padding(4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}