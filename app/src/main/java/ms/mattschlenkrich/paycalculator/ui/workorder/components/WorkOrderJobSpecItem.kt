package ms.mattschlenkrich.paycalculator.ui.workorder.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderJobSpecCombined

@Composable
fun WorkOrderJobSpecItem(
    combined: WorkOrderJobSpecCombined,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = "${combined.workOrderJobSpec.wojsSequence}) ${combined.jobSpec.jsName}" +
                    (combined.area?.let { " ${stringResource(R.string._in_)} ${it.areaName}" }
                        ?: "") +
                    (combined.workOrderJobSpec.wojsNote?.let { " - $it" } ?: ""),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
}