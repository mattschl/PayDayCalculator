package ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryTimeWorkedCombined
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderHistoryTimeScreen(
    infoText: String,
    hoursSummaryText: String,
    startTime: Calendar,
    endTime: Calendar,
    totalTimeText: String,
    selectedTimeType: Int,
    onTimeTypeChange: (Int) -> Unit,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    onEnterTimeClick: () -> Unit,
    onDoneClick: () -> Unit,
    existingTimes: List<WorkOrderHistoryTimeWorkedCombined>,
    allTimesForDay: List<WorkOrderHistoryTimeWorkedCombined>,
    onTimeClick: (WorkOrderHistoryTimeWorkedCombined) -> Unit,
    onTimeLongClick: (WorkOrderHistoryTimeWorkedCombined) -> Unit = {},
    onBackClick: () -> Unit,
    errorMessage: String? = null
) {
    val df = DateFunctions()
    val nf = NumberFunctions()

    Scaffold(
        /*topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.add_time))
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Red
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },*/
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onDoneClick,
                containerColor = Color(0xFF2E7D32), // Dark Green
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = stringResource(R.string.done)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = SCREEN_PADDING_VERTICAL),
                verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
            ) {
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )

                Text(
                    text = hoursSummaryText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.start_time),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = df.get12HourDisplay(startTime),
                            modifier = Modifier
                                .clickable { onStartTimeClick() }
                                .padding(vertical = 4.dp),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Text(
                        text = totalTimeText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB71C1C) // Deep Red
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = stringResource(R.string.end_time),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = df.get12HourDisplay(endTime),
                            modifier = Modifier
                                .clickable { onEndTimeClick() }
                                .padding(vertical = 4.dp),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Button(
                    onClick = onEnterTimeClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text(stringResource(R.string.enter_time))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TimeTypeRadioButton(
                        label = stringResource(R.string.reg),
                        selected = selectedTimeType == TimeWorkedTypes.REG_HOURS.value,
                        onClick = { onTimeTypeChange(TimeWorkedTypes.REG_HOURS.value) }
                    )
                    TimeTypeRadioButton(
                        label = stringResource(R.string.ot),
                        selected = selectedTimeType == TimeWorkedTypes.OT_HOURS.value,
                        onClick = { onTimeTypeChange(TimeWorkedTypes.OT_HOURS.value) }
                    )
                    TimeTypeRadioButton(
                        label = stringResource(R.string.dbl_ot),
                        selected = selectedTimeType == TimeWorkedTypes.DBL_OT_HOURS.value,
                        onClick = { onTimeTypeChange(TimeWorkedTypes.DBL_OT_HOURS.value) }
                    )
                    TimeTypeRadioButton(
                        label = stringResource(R.string._break),
                        selected = selectedTimeType == TimeWorkedTypes.BREAK.value,
                        onClick = { onTimeTypeChange(TimeWorkedTypes.BREAK.value) }
                    )
                }
            }

            Text(
                text = stringResource(R.string.existing_times),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(allTimesForDay) { time ->
                    TimeWorkedItem(
                        item = time,
                        df = df,
                        nf = nf,
                        onClick = onTimeClick,
                        onLongClick = onTimeLongClick,
                        isCurrentWorkOrder = time.timeWorked.wohtHistoryId == existingTimes.firstOrNull()?.timeWorked?.wohtHistoryId
                    )
                }
            }
        }
    }
}

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
            containerColor = if (isCurrentWorkOrder) Color.White else Color(0xFFF5F5F5)
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
                horizontalArrangement = Arrangement.SpaceBetween,
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
                        color = Color.Gray
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