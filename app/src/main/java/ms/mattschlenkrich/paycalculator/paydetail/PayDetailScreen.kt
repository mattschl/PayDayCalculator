package ms.mattschlenkrich.paycalculator.paydetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.common.compose.StandardTopAppBar
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.ExtraContainer
import ms.mattschlenkrich.paycalculator.data.TaxAndAmount

@Composable
fun PayDetailScreen(
    employers: List<Employers>,
    selectedEmployer: Employers?,
    onEmployerSelected: (Employers) -> Unit,
    onAddNewEmployer: () -> Unit,
    cutOffDates: List<String>,
    selectedCutOffDate: String,
    onCutOffDateSelected: (String) -> Unit,
    paySummary: PaySummaryData,
    hourlyBreakdown: HourlyBreakdownData,
    credits: List<ExtraContainer>,
    deductions: List<ExtraContainer>,
    taxes: List<TaxAndAmount>,
    onAddCreditClick: () -> Unit,
    onAddDeductionClick: () -> Unit,
    onExtraClick: (ExtraContainer) -> Unit,
    onExtraActiveChange: (ExtraContainer, Boolean) -> Unit,
    onDeleteCutOffClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(R.string.pay_details),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDeleteCutOffClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = SCREEN_PADDING_HORIZONTAL,
                    vertical = SCREEN_PADDING_VERTICAL
                ),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
        ) {
            SelectionCard(
                employers = employers,
                selectedEmployer = selectedEmployer,
                onEmployerSelected = onEmployerSelected,
                onAddNewEmployer = onAddNewEmployer,
                cutOffDates = cutOffDates,
                selectedCutOffDate = selectedCutOffDate,
                onCutOffDateSelected = onCutOffDateSelected
            )

            SummaryCard(paySummary)

            HourlyBreakdownCard(hourlyBreakdown)

            ExtrasCard(
                title = stringResource(R.string.credits),
                extras = credits,
                total = paySummary.totalCredits,
                onAddClick = onAddCreditClick,
                onExtraClick = onExtraClick,
                onActiveChange = onExtraActiveChange,
                addButtonContentDescription = stringResource(R.string.add_new_credit)
            )

            ExtrasCard(
                title = stringResource(R.string.deductions),
                extras = deductions,
                taxes = taxes,
                total = paySummary.totalDeductions,
                onAddClick = onAddDeductionClick,
                onExtraClick = onExtraClick,
                onActiveChange = onExtraActiveChange,
                addButtonContentDescription = stringResource(R.string.add_new_deductions)
            )
        }
    }
}

@Composable
fun SelectionCard(
    employers: List<Employers>,
    selectedEmployer: Employers?,
    onEmployerSelected: (Employers) -> Unit,
    onAddNewEmployer: () -> Unit,
    cutOffDates: List<String>,
    selectedCutOffDate: String,
    onCutOffDateSelected: (String) -> Unit
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
                    modifier = Modifier.width(100.dp),
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
                    modifier = Modifier.width(100.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                SimpleDropdownField(
                    label = "",
                    items = cutOffDates,
                    selectedItem = selectedCutOffDate,
                    onItemSelected = onCutOffDateSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SummaryCard(data: PaySummaryData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = data.payDayMessage,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
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
        }
    }
}

@Composable
fun HourlyBreakdownCard(data: HourlyBreakdownData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.description),
                    modifier = Modifier.weight(1.5f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.qty),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.rate),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.total),
                    modifier = Modifier.weight(1.2f),
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            data.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(item.description, modifier = Modifier.weight(1.5f))
                    Text(item.qty, modifier = Modifier.weight(1f))
                    Text(item.rate, modifier = Modifier.weight(1f))
                    Text(item.total, modifier = Modifier.weight(1.2f))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.total_hourly),
                    modifier = Modifier.weight(3.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    data.totalHourly,
                    modifier = Modifier.weight(1.2f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun ExtrasCard(
    title: String,
    extras: List<ExtraContainer>,
    taxes: List<TaxAndAmount> = emptyList(),
    total: String,
    onAddClick: () -> Unit,
    onExtraClick: (ExtraContainer) -> Unit,
    onActiveChange: (ExtraContainer, Boolean) -> Unit,
    addButtonContentDescription: String
) {
    val nf = NumberFunctions()
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                FloatingActionButton(
                    onClick = onAddClick,
                    modifier = Modifier.padding(end = 4.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = addButtonContentDescription)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            extras.forEach { extra ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExtraClick(extra) }
                        .padding(vertical = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(extra.extraName, modifier = Modifier.weight(1.5f))
                    Text(
                        nf.displayDollars(extra.amount),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                    Checkbox(
                        checked = extra.amount > 0.0,
                        onCheckedChange = { onActiveChange(extra, it) },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (taxes.isNotEmpty()) {
                taxes.forEach { tax ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tax.taxType, modifier = Modifier.weight(1.5f))
                        Text(
                            nf.displayDollars(tax.amount),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                        // Empty spacer to align with the Checkbox in extras
                        Spacer(
                            modifier = Modifier
                                .width(48.dp)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 0.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (title == stringResource(R.string.credits)) stringResource(R.string.total_credits) else stringResource(
                        R.string.total_deductions
                    ),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    total,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

data class PaySummaryData(
    val payDayMessage: String = "",
    val grossPay: String = "$0.00",
    val deductions: String = "$0.00",
    val netPay: String = "$0.00",
    val totalCredits: String = "$0.00",
    val totalDeductions: String = "$0.00"
)

data class HourlyBreakdownData(
    val items: List<HourlyItem> = emptyList(),
    val totalHourly: String = "$0.00"
)

data class HourlyItem(
    val description: String,
    val qty: String,
    val rate: String,
    val total: String
)