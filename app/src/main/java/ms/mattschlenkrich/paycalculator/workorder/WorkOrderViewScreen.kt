package ms.mattschlenkrich.paycalculator.workorder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.WorkOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderViewScreen(
    employers: List<Employers>,
    selectedEmployer: Employers?,
    onEmployerSelected: (Employers?) -> Unit,
    onAddNewEmployerClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onResetSearchClick: () -> Unit,
    workOrders: List<WorkOrder>,
    onWorkOrderClick: (WorkOrder) -> Unit,
    onAddNewWorkOrderClick: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewWorkOrderClick,
                containerColor = Color(0xFF1B5E20), // dark_green
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_new))
            }
        }
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column {
                    Spacer(modifier = Modifier.height(ELEMENT_SPACING))

                    // Employer Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SimpleDropdownField(
                            label = stringResource(R.string.employer),
                            items = employers,
                            selectedItem = selectedEmployer,
                            onItemSelected = { onEmployerSelected(it) },
                            modifier = Modifier.weight(1f),
                            itemToString = { it.employerName }
                        )
                        Button(
                            onClick = onAddNewEmployerClick,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(stringResource(R.string.add))
                        }
                    }

                    Spacer(modifier = Modifier.height(ELEMENT_SPACING))

                    // Search Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SelectAllOutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(R.string.type_number_or_address_search)) },
                            label = { Text(stringResource(R.string.search)) }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = onResetSearchClick) {
                            Text(stringResource(R.string.reset))
                        }
                    }

                    Spacer(modifier = Modifier.height(ELEMENT_SPACING))
                }
            }

            if (workOrders.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = stringResource(R.string.no_work_orders_to_view),
                                modifier = Modifier.padding(50.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black
                            )
                        }
                    }
                }
            } else {
                items(workOrders) { workOrder ->
                    WorkOrderListItem(
                        workOrder = workOrder,
                        onClick = { onWorkOrderClick(workOrder) }
                    )
                }
            }
        }
    }
}

@Composable
fun WorkOrderListItem(
    workOrder: WorkOrder,
    onClick: () -> Unit
) {
    // Generate a pseudo-random color based on workOrderId
    val randomColor = remember(workOrder.workOrderId) {
        val colors = listOf(
            Color(0xFFE57373), Color(0xFFF06292), Color(0xFFBA68C8), Color(0xFF9575CD),
            Color(0xFF7986CB), Color(0xFF64B5F6), Color(0xFF4FC3F7), Color(0xFF4DD0E1),
            Color(0xFF4DB6AC), Color(0xFF81C784), Color(0xFFAED581), Color(0xFFFFD54F),
            Color(0xFFFFB74D), Color(0xFFFF8A65)
        )
        val index = (workOrder.workOrderId % colors.size).toInt()
        colors[if (index < 0) index + colors.size else index]
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.height(intrinsicSize = androidx.compose.foundation.layout.IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .fillMaxSize()
                    .background(randomColor)
            )

            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = workOrder.woNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Text(
                    text = workOrder.woAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = workOrder.woDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
            }
        }
    }
}