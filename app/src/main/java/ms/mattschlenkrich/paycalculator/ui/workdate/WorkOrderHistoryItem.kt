package ms.mattschlenkrich.paycalculator.ui.workdate

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryWithDates

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkOrderHistoryItem(
    history: WorkOrderHistoryWithDates,
    onClick: (WorkOrderHistoryWithDates) -> Unit,
    onLongClick: (WorkOrderHistoryWithDates) -> Unit
) {
    val nf = NumberFunctions()
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    onClick = { onClick(history) },
                    onLongClick = { onLongClick(history) }
                )
                .padding(8.dp)
        ) {
            Text(
                text = "${history.workOrder.woNumber} - ${history.workOrder.woAddress}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            val hoursText = buildString {
                val reg = history.history.woHistoryRegHours
                val ot = history.history.woHistoryOtHours
                val dbl = history.history.woHistoryDblOtHours

                append("Total hours: ${nf.displayNumberFromDouble(reg + ot + dbl)}")

                val details = buildString {
                    if (reg > 0) append("reg ${nf.displayNumberFromDouble(reg)}")
                    if (ot > 0) {
                        if (isNotEmpty()) append(" | ")
                        append("ot ${nf.displayNumberFromDouble(ot)}")
                    }
                    if (dbl > 0) {
                        if (isNotEmpty()) append(" | ")
                        append("dbl ${nf.displayNumberFromDouble(dbl)}")
                    }
                }
                if (details.isNotEmpty()) {
                    append(" -> ")
                    append(details)
                }
            }
            if (hoursText.isNotEmpty()) {
                Text(
                    text = hoursText.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}