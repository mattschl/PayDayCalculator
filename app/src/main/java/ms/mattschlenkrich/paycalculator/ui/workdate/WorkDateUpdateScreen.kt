package ms.mattschlenkrich.paycalculator.ui.workdate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.data.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWithDates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkDateUpdateScreen(
    dateText: String,
    onDateClick: () -> Unit,
    regHours: String,
    onRegHoursChange: (String) -> Unit,
    otHours: String,
    onOtHoursChange: (String) -> Unit,
    dblOtHours: String,
    onDblOtHoursChange: (String) -> Unit,
    statHours: String,
    onStatHoursChange: (String) -> Unit,
    onStatHoursLongClick: () -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    onUpdateTimeClick: () -> Unit,
    onAddHistoryClick: () -> Unit,
    onTransferClick: () -> Unit,
    onDoneClick: () -> Unit,
    histories: List<WorkOrderHistoryWithDates>,
    onHistoryClick: (WorkOrderHistoryWithDates) -> Unit,
    onHistoryLongClick: (WorkOrderHistoryWithDates) -> Unit,
    workOrderSummary: String,
    extras: List<WorkDateExtras>,
    onExtraClick: (WorkDateExtras) -> Unit,
    onExtraEditClick: (WorkDateExtras) -> Unit,
    onAddExtraClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.update_this_work_date),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onDoneClick,
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Done, contentDescription = stringResource(R.string.done))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(vertical = SCREEN_PADDING_VERTICAL),
                    verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDateClick() },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.work_date),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DecimalOutlinedTextField(
                            value = regHours,
                            onValueChange = onRegHoursChange,
                            label = { Text(stringResource(R.string.reg_hours)) },
                            modifier = Modifier.weight(1f)
                        )
                        DecimalOutlinedTextField(
                            value = otHours,
                            onValueChange = onOtHoursChange,
                            label = { Text(stringResource(R.string.overtime_hours)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DecimalOutlinedTextField(
                            value = dblOtHours,
                            onValueChange = onDblOtHoursChange,
                            label = { Text(stringResource(R.string.double_overtime)) },
                            modifier = Modifier.weight(1f)
                        )
                        DecimalOutlinedTextField(
                            value = statHours,
                            onValueChange = onStatHoursChange,
                            label = {
                                Text(
                                    text = stringResource(R.string.other_hours),
                                    modifier = Modifier.clickable { onStatHoursLongClick() }
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Button(
                        onClick = onUpdateTimeClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text(stringResource(R.string.enter_time_before_wo))
                    }

                    SelectAllOutlinedTextField(
                        value = note,
                        onValueChange = onNoteChange,
                        label = { Text(stringResource(R.string.enter_note_optional)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onAddExtraClick) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = stringResource(R.string.add_new_extra)
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.extras_for_this_date),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        extras.forEach { extra ->
                            WorkDateExtraItem(
                                extra = extra,
                                onClick = { onExtraClick(extra) },
                                onEditClick = { onExtraEditClick(extra) }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onAddHistoryClick) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = stringResource(R.string.add_new_extra)
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.work_orders),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        histories.forEach { history ->
                            WorkOrderHistoryItem(
                                history = history,
                                onClick = onHistoryClick,
                                onLongClick = onHistoryLongClick
                            )
                        }

                        if (workOrderSummary.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = workOrderSummary,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = onTransferClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text(stringResource(R.string.transfer))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun WorkDateExtraItem(
    extra: WorkDateExtras,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val nf = NumberFunctions()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = !extra.wdeIsDeleted,
            onCheckedChange = { onClick() }
        )
        val display = buildString {
            append(extra.wdeName)
            append(if (extra.wdeIsCredit) " (add) " else " (subtract) ")
            append(
                if (extra.wdeIsFixed) nf.displayDollars(extra.wdeValue)
                else nf.getPercentStringFromDouble(extra.wdeValue)
            )
        }
        Text(
            text = display,
            modifier = Modifier.weight(1f),
            color = if (extra.wdeIsCredit) Color.Black else Color.Red
        )
        if (!extra.wdeIsDeleted) {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
            }
        }
    }
}