package ms.mattschlenkrich.paycalculator.workorder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.AutoCompleteTextField
import ms.mattschlenkrich.paycalculator.common.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.StandardTopAppBar
import ms.mattschlenkrich.paycalculator.data.WorkOrder

@Composable
fun WorkOrderHistoryAddScreen(
    workOrderList: List<WorkOrder>,
    initialWorkOrderNumber: String,
    initialRegHours: String,
    initialOtHours: String,
    initialDblOtHours: String,
    initialNote: String,
    onWorkOrderSearch: (String, String, String, String, String) -> Unit,
    onWorkOrderAddEdit: (String, String, String, String, String, Boolean) -> Unit,
    onDone: (String, String, String, String, String, Boolean) -> Unit,
    onBack: () -> Unit,
    displayDate: String,
    displayEmployer: String
) {
    var workOrderNumber by remember { mutableStateOf(initialWorkOrderNumber) }
    var regHours by remember { mutableStateOf(initialRegHours) }
    var otHours by remember { mutableStateOf(initialOtHours) }
    var dblOtHours by remember { mutableStateOf(initialDblOtHours) }
    var note by remember { mutableStateOf(initialNote) }

    var showCreateDialog by remember { mutableStateOf(false) }

    val currentWorkOrder = workOrderList.find { it.woNumber == workOrderNumber }
    val isWorkOrderValid = currentWorkOrder != null

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(R.string.add_time_to_work_order),
                onBackClicked = onBack
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = {
                    if (isWorkOrderValid) {
                        onDone(workOrderNumber, regHours, otHours, dblOtHours, note, false)
                    } else {
                        showCreateDialog = true
                    }
                },
                containerColor = colorResource(id = R.color.dark_green),
                contentColor = Color.White
            ) {
                androidx.compose.material3.Icon(
                    painter = painterResource(id = R.drawable.ic_done),
                    contentDescription = stringResource(R.string.done)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = displayDate,
                        fontSize = 20.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color.Black
                    )

                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.employer) + " ",
                            fontSize = 20.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color.Black
                        )
                        Text(
                            text = displayEmployer,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AutoCompleteTextField(
                            value = workOrderNumber,
                            onValueChange = { workOrderNumber = it },
                            label = stringResource(R.string.work_order),
                            suggestions = workOrderList,
                            onItemSelected = { workOrderNumber = it.woNumber },
                            modifier = Modifier.weight(1f),
                            onLongClick = {
                                onWorkOrderSearch(
                                    workOrderNumber,
                                    regHours,
                                    otHours,
                                    dblOtHours,
                                    note
                                )
                            },
                            itemToString = { it.woNumber }
                        )

                        Button(
                            onClick = {
                                onWorkOrderAddEdit(
                                    workOrderNumber,
                                    regHours,
                                    otHours,
                                    dblOtHours,
                                    note,
                                    isWorkOrderValid
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.dark_green)
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = if (isWorkOrderValid) stringResource(R.string.edit)
                                else stringResource(R.string.create)
                            )
                        }
                    }

                    if (isWorkOrderValid) {
                        Text(
                            text = "${currentWorkOrder!!.woAddress} - ${currentWorkOrder.woNumber}",
                            color = Color.Red,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DecimalOutlinedTextField(
                            value = regHours,
                            onValueChange = { regHours = it },
                            label = { Text(stringResource(R.string.hr)) },
                            modifier = Modifier.weight(1f)
                        )
                        DecimalOutlinedTextField(
                            value = otHours,
                            onValueChange = { otHours = it },
                            label = { Text(stringResource(R.string.ot)) },
                            modifier = Modifier.weight(1f)
                        )
                        DecimalOutlinedTextField(
                            value = dblOtHours,
                            onValueChange = { dblOtHours = it },
                            label = { Text(stringResource(R.string.dbl_ot)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(stringResource(R.string.enter_note_optional)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(stringResource(R.string.create_work_order_) + " $workOrderNumber?") },
            text = { Text(stringResource(R.string.this_work_order_does_not_exist)) },
            confirmButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    onWorkOrderAddEdit(workOrderNumber, regHours, otHours, dblOtHours, note, false)
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
}