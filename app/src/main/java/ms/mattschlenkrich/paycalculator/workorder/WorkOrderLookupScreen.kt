package ms.mattschlenkrich.paycalculator.workorder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.StandardTopAppBar
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.WorkOrder

@Composable
fun WorkOrderLookupScreen(
    employer: Employers?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    workOrders: List<WorkOrder>,
    onWorkOrderSelected: (WorkOrder) -> Unit,
    onBackClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf<WorkOrder?>(null) }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(R.string.choose_a_work_order),
                onBackClicked = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL, vertical = SCREEN_PADDING_VERTICAL)
        ) {
            employer?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = ELEMENT_SPACING)
                ) {
                    Text(
                        text = stringResource(R.string.employer) + ": ",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = it.employerName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.search),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = ELEMENT_SPACING)
                )
                SelectAllOutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.type_number_or_address_search)) }
                )
                Button(
                    onClick = { onSearchQueryChange("") },
                    modifier = Modifier.padding(start = ELEMENT_SPACING)
                ) {
                    Text(stringResource(R.string.reset))
                }
            }

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            if (workOrders.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.no_work_orders_to_view),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(workOrders) { workOrder ->
                        WorkOrderLookupItem(
                            workOrder = workOrder,
                            onClick = { showDialog = workOrder }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                }
            }
        }
    }

    showDialog?.let { workOrder ->
        AlertDialog(
            onDismissRequest = { showDialog = null },
            title = { Text(stringResource(R.string.choose) + " " + workOrder.woNumber) },
            text = {
                Text(
                    stringResource(R.string.would_you_like_to_use_this_work_order) +
                            "\n\n" + workOrder.woDescription
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onWorkOrderSelected(workOrder)
                    showDialog = null
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun WorkOrderLookupItem(
    workOrder: WorkOrder,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = workOrder.woNumber,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = workOrder.woAddress,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = workOrder.woDescription,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}