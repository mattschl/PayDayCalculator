package ms.mattschlenkrich.paycalculator.workdate

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.common.compose.AutoCompleteTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.workorder.TimeTypeRadioButton
import ms.mattschlenkrich.paycalculator.workorder.TimeWorkedItem
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkDateTimesScreen(
    infoText: String,
    hoursSummaryText: String,
    workOrderNumber: String,
    onWorkOrderNumberChange: (String) -> Unit,
    workOrderSuggestions: List<String>,
    workOrderButtonText: String,
    onWorkOrderButtonClick: () -> Unit,
    workOrderInfoText: String,
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
    onTimeClick: (WorkOrderHistoryTimeWorkedCombined) -> Unit,
    workOrderError: String? = null,
) {
    val df = DateFunctions()
    val nf = NumberFunctions()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.enter_work_time),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            )
        },
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AutoCompleteTextField(
                        value = workOrderNumber,
                        onValueChange = onWorkOrderNumberChange,
                        label = stringResource(R.string.work_order),
                        suggestions = workOrderSuggestions,
                        onItemSelected = { onWorkOrderNumberChange(it) },
                        modifier = Modifier.weight(1f),
                        isError = workOrderError != null
                    )

                    Button(
                        onClick = onWorkOrderButtonClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text(workOrderButtonText)
                    }
                }

                if (workOrderError != null) {
                    Text(
                        text = workOrderError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (workOrderInfoText.isNotEmpty()) {
                    Text(
                        text = workOrderInfoText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }

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
                        label = stringResource(R.string.reg_hours),
                        selected = selectedTimeType == TimeWorkedTypes.REG_HOURS.value,
                        onClick = { onTimeTypeChange(TimeWorkedTypes.REG_HOURS.value) }
                    )
                    TimeTypeRadioButton(
                        label = stringResource(R.string.ot_hrs),
                        selected = selectedTimeType == TimeWorkedTypes.OT_HOURS.value,
                        onClick = { onTimeTypeChange(TimeWorkedTypes.OT_HOURS.value) }
                    )
                    TimeTypeRadioButton(
                        label = stringResource(R.string.dbl_ot_hrs),
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
                items(existingTimes) { time ->
                    TimeWorkedItem(time, df, nf, onTimeClick)
                }
            }
        }
    }
}