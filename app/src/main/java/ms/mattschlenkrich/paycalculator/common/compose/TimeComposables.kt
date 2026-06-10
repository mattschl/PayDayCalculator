package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryTimeWorkedCombined

@Composable
fun TimeTypeRadioButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimeWorkedItem(
    item: WorkOrderHistoryTimeWorkedCombined,
    df: DateFunctions,
    nf: NumberFunctions,
    onClick: (WorkOrderHistoryTimeWorkedCombined) -> Unit,
    onLongClick: (WorkOrderHistoryTimeWorkedCombined) -> Unit = {},
    isCurrentWorkOrder: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(item) },
                onLongClick = { onLongClick(item) }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentWorkOrder) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        val tempStart = df.splitTimeFromDateTime(item.timeWorked.wohtStartTime)
        val startTime = df.get12HourDisplay("${tempStart[0]}:${tempStart[1]}")
        val tempEnd = df.splitTimeFromDateTime(item.timeWorked.wohtEndTime)
        val endTime = df.get12HourDisplay("${tempEnd[0]}:${tempEnd[1]}")
        val hours = df.getTimeWorked(item.timeWorked.wohtStartTime, item.timeWorked.wohtEndTime)

        val typeText = when (item.timeWorked.wohtTimeType) {
            TimeWorkedTypes.REG_HOURS.value -> stringResource(R.string.reg_hrs_)
            TimeWorkedTypes.OT_HOURS.value -> stringResource(R.string.ot_hrs_)
            TimeWorkedTypes.DBL_OT_HOURS.value -> stringResource(R.string.dblot_hrs_)
            else -> stringResource(R.string._break)
        }

        Column(modifier = Modifier.padding(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$startTime - $endTime",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!isCurrentWorkOrder) {
                    Text(
                        text = "WO: ${item.workOrderHistory.workOrder.woNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "$typeText: ${nf.displayNumberFromDouble(hours)} hours",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}