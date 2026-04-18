package ms.mattschlenkrich.paycalculator.timesheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.WorkDates

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimeSheetScreen(
    employers: List<Employers>,
    selectedEmployer: Employers?,
    onEmployerSelected: (Employers) -> Unit,
    onAddNewEmployer: () -> Unit,
    cutOffDates: List<String>,
    selectedCutOffDate: String,
    onCutOffDateSelected: (String) -> Unit,
    paySummary: TimeSheetPaySummary,
    workDates: List<WorkDates>,
    workDateExtras: Map<Long, List<ms.mattschlenkrich.paycalculator.data.WorkDateExtraAndTypeAndDef>>,
    onWorkDateClick: (WorkDates) -> Unit,
    onWorkDateLongClick: (WorkDates) -> Unit,
    onAddWorkDateClick: () -> Unit,
    onViewPayDetailsClick: () -> Unit,
    onGenerateCutoffClick: () -> Unit,
    displayDate: (String) -> String,
    formatHours: (WorkDates) -> String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TimeSheetScreen", // title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddWorkDateClick,
                containerColor = Color(0xFF1B5E20), // dark_green
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_new))
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = SCREEN_PADDING_HORIZONTAL,
                vertical = SCREEN_PADDING_VERTICAL
            ),
            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
            verticalItemSpacing = ELEMENT_SPACING
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                TimeSheetSelectionCard(
                    employers = employers,
                    selectedEmployer = selectedEmployer,
                    onEmployerSelected = onEmployerSelected,
                    onAddNewEmployer = onAddNewEmployer,
                    cutOffDates = cutOffDates,
                    selectedCutOffDate = selectedCutOffDate,
                    onCutOffDateSelected = onCutOffDateSelected,
                    onGenerateCutoffClick = onGenerateCutoffClick
                )
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                TimeSheetSummaryCard(paySummary, onViewPayDetailsClick)
            }

            if (workDates.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        text = stringResource(R.string.work_dates),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }

                items(workDates) { workDate ->
                    WorkDateCard(
                        workDate = workDate,
                        extras = workDateExtras[workDate.workDateId] ?: emptyList(),
                        onClick = { onWorkDateClick(workDate) },
                        onLongClick = { onWorkDateLongClick(workDate) },
                        displayDate = displayDate,
                        formatHours = formatHours
                    )
                }
            }
        }
    }
}

@Composable
fun TimeSheetSelectionCard(
    employers: List<Employers>,
    selectedEmployer: Employers?,
    onEmployerSelected: (Employers) -> Unit,
    onAddNewEmployer: () -> Unit,
    cutOffDates: List<String>,
    selectedCutOffDate: String,
    onCutOffDateSelected: (String) -> Unit,
    onGenerateCutoffClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.employer),
                    modifier = Modifier.width(90.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                SimpleDropdownField(
                    label = "",
                    items = employers,
                    selectedItem = selectedEmployer,
                    onItemSelected = onEmployerSelected,
                    itemToString = { it.employerName },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onAddNewEmployer) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_new_employer)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.cut_off_date),
                    modifier = Modifier.width(90.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                SimpleDropdownField(
                    label = "",
                    items = cutOffDates,
                    selectedItem = selectedCutOffDate,
                    onItemSelected = onCutOffDateSelected,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onGenerateCutoffClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.generate_a_new_cut_off)
                    )
                }
            }
        }
    }
}

@Composable
fun TimeSheetSummaryCard(
    data: TimeSheetPaySummary,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.pay_summary),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.gross_pay),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(data.grossPay, style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.deductions),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        data.deductions,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.pay_amount),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        data.netPay,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB71C1C)
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = data.totalHoursDescription,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = data.week1Total,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB71C1C) // deep_red
                )
                Text(
                    text = data.week2Total,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1B5E20) // dark_green
                )
            }
        }
    }
}

@Composable
fun WorkDateCard(
    workDate: WorkDates,
    extras: List<ms.mattschlenkrich.paycalculator.data.WorkDateExtraAndTypeAndDef>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    displayDate: (String) -> String,
    formatHours: (WorkDates) -> String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayDate(workDate.wdDate),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (workDate.wdIsDeleted) Color.Red else Color.Black,
                    modifier = Modifier.weight(1f)
                )
                if (!workDate.wdNote.isNullOrBlank()) {
                    Text(
                        text = workDate.wdNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            if (workDate.wdRegHours > 0 || workDate.wdOtHours > 0 ||
                workDate.wdDblOtHours > 0 || workDate.wdStatHours > 0
            ) {
                Text(
                    text = formatHours(workDate),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (extras.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                extras.filter { !it.extra.wdeIsDeleted }.forEach { extra ->
                    Text(
                        text = "${extra.extra.wdeName}: ${
                            if (extra.extra.wdeIsCredit) "" else "-"
                        }${
                            ms.mattschlenkrich.paycalculator.common.NumberFunctions()
                                .displayDollars(extra.extra.wdeValue)
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (extra.extra.wdeIsCredit) Color(0xFF1B5E20) else Color.Red
                    )
                }
            }
        }
    }
}

data class TimeSheetPaySummary(
    val grossPay: String = "$0.00",
    val deductions: String = "$0.00",
    val netPay: String = "$0.00",
    val totalHoursDescription: String = "",
    val week1Total: String = "",
    val week2Total: String = ""
)