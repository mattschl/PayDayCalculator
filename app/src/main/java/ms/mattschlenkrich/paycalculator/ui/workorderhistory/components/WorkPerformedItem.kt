package ms.mattschlenkrich.paycalculator.ui.workorderhistory.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryWorkPerformedCombined

@Composable
fun WorkPerformedItem(
    item: WorkOrderHistoryWorkPerformedCombined,
    index: Int,
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
        Column(modifier = Modifier.padding(12.dp)) {
            val display = "${index + 1}) " + item.workPerformed.wpDescription +
                    (item.area?.let { " ${stringResource(R.string._in_)} ${it.areaName}" } ?: "") +
                    (if (item.workOrderHistoryWorkPerformed.wowpNote.isNullOrBlank()) "" else " - ${item.workOrderHistoryWorkPerformed.wowpNote}.")

            Text(
                text = display,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}