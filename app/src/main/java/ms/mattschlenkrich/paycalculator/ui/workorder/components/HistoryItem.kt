package ms.mattschlenkrich.paycalculator.ui.workorder.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryWithDates

@Composable
fun HistoryItem(
    history: WorkOrderHistoryWithDates,
    df: DateFunctions,
    nf: NumberFunctions,
    onHistoryClick: (WorkOrderHistoryWithDates) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHistoryClick(history) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = df.getDisplayDate(history.workDate.wdDate),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                val note = history.history.woHistoryNote
                if (!note.isNullOrBlank()) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = getHistoryHoursDisplay(history, nf),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun getHistoryHoursDisplay(
    history: WorkOrderHistoryWithDates,
    nf: NumberFunctions
): String {
    val parts = mutableListOf<String>()
    if (history.history.woHistoryRegHours > 0) parts.add(nf.displayNumberFromDouble(history.history.woHistoryRegHours))
    if (history.history.woHistoryOtHours > 0) parts.add(nf.displayNumberFromDouble(history.history.woHistoryOtHours))
    if (history.history.woHistoryDblOtHours > 0) parts.add(nf.displayNumberFromDouble(history.history.woHistoryDblOtHours))
    return if (parts.isEmpty()) "0" else parts.joinToString("/")
}