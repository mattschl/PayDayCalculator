package ms.mattschlenkrich.paycalculator.workdate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.data.WorkExtraTypes

@Composable
fun WorkDateAddScreen(
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
    onSaveClick: () -> Unit,
    extras: List<WorkExtraTypes>,
    selectedExtras: Set<Long>,
    onExtraToggle: (WorkExtraTypes, Boolean) -> Unit,
    onAddExtraClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Done, contentDescription = stringResource(R.string.save))
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

            if (extras.isNotEmpty()) {
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onExtraToggle(
                                                extra,
                                                !selectedExtras.contains(extra.workExtraTypeId)
                                            )
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedExtras.contains(extra.workExtraTypeId),
                                        onCheckedChange = {
                                            onExtraToggle(extra, it)
                                        }
                                    )
                                    Text(
                                        text = extra.wetName,
                                        modifier = Modifier.weight(1f),
                                        color = if (extra.wetIsCredit) Color.Black else Color.Red
                                    )
                                }
                            }
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
                        Text(
                            text = stringResource(R.string.no_work_orders_yet),
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}