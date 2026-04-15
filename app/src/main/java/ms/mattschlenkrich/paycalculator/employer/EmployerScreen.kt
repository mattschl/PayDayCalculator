package ms.mattschlenkrich.paycalculator.employer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.INTERVAL_MONTHLY
import ms.mattschlenkrich.paycalculator.common.INTERVAL_SEMI_MONTHLY
import ms.mattschlenkrich.paycalculator.common.PayDayFrequencies
import ms.mattschlenkrich.paycalculator.common.WorkDayOfWeek
import ms.mattschlenkrich.paycalculator.common.compose.CapitalizedOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.common.compose.StandardTopAppBar
import ms.mattschlenkrich.paycalculator.data.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.data.WorkExtraTypes

@Composable
fun EmployerScreen(
    isUpdate: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    frequency: String,
    onFrequencyChange: (String) -> Unit,
    startDate: String,
    onStartDateClick: () -> Unit,
    dayOfWeek: String,
    onDayOfWeekChange: (String) -> Unit,
    daysBefore: String,
    onDaysBeforeChange: (String) -> Unit,
    midMonthDate: String,
    onMidMonthDateChange: (String) -> Unit,
    mainMonthDate: String,
    onMainMonthDateChange: (String) -> Unit,
    taxes: List<EmployerTaxTypes>,
    onTaxIncludeChange: (EmployerTaxTypes, Boolean) -> Unit,
    onAddTaxClick: () -> Unit,
    extras: List<WorkExtraTypes>,
    onExtraClick: (WorkExtraTypes) -> Unit,
    onAddExtraClick: () -> Unit,
    onViewWagesClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val df = DateFunctions()

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = if (isUpdate) stringResource(R.string.update) + " " + name
                else stringResource(R.string.add_an_employer),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                },
                actions = {
                    if (isUpdate) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                containerColor = Color(0xFF2E7D32), // dark_green
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.save)
                )
            }
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                ) {
                    EmployerField(stringResource(R.string.employer_name)) {
                        CapitalizedOutlinedTextField(
                            value = name,
                            onValueChange = onNameChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    EmployerField(stringResource(R.string.pay_day_frequency)) {
                        SimpleDropdownField(
                            label = "",
                            items = PayDayFrequencies.entries.map { it.toString() },
                            selectedItem = frequency,
                            onItemSelected = onFrequencyChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    EmployerField(stringResource(R.string.first_cheque_date)) {
                        Button(
                            onClick = onStartDateClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(df.getDisplayDate(startDate))
                        }
                    }

                    EmployerField(stringResource(R.string.day_of_week)) {
                        SimpleDropdownField(
                            label = "",
                            items = WorkDayOfWeek.entries.map { it.toString() },
                            selectedItem = dayOfWeek,
                            onItemSelected = onDayOfWeekChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    EmployerField(stringResource(R.string.how_many_days_before_is_cutoff)) {
                        SelectAllOutlinedTextField(
                            value = daysBefore,
                            onValueChange = onDaysBeforeChange,
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    if (frequency == INTERVAL_SEMI_MONTHLY) {
                        EmployerField(stringResource(R.string.mid_month_pay_day)) {
                            SelectAllOutlinedTextField(
                                value = midMonthDate,
                                onValueChange = onMidMonthDateChange,
                                modifier = Modifier.width(80.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }

                    if (frequency == INTERVAL_SEMI_MONTHLY || frequency == INTERVAL_MONTHLY) {
                        EmployerField(stringResource(R.string.main_monthly_pay_date)) {
                            SelectAllOutlinedTextField(
                                value = mainMonthDate,
                                onValueChange = onMainMonthDateChange,
                                modifier = Modifier.width(80.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }

                    if (isUpdate) {
                        Button(
                            onClick = onViewWagesClick,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text(stringResource(R.string.view_or_edit_wages))
                        }
                    }
                }
            }

            if (isUpdate) {
                TaxesCard(
                    taxes = taxes,
                    onIncludeChange = onTaxIncludeChange,
                    onAddTaxClick = onAddTaxClick
                )

                ExtrasCard(
                    extras = extras,
                    onExtraClick = onExtraClick,
                    onAddExtraClick = onAddExtraClick
                )
            }
        }
    }
}

@Composable
fun EmployerField(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            modifier = Modifier.weight(1.5f),
            horizontalArrangement = Arrangement.End
        ) {
            content()
        }
    }
}

@Composable
fun TaxesCard(
    taxes: List<EmployerTaxTypes>,
    onIncludeChange: (EmployerTaxTypes, Boolean) -> Unit,
    onAddTaxClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onAddTaxClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_a_new_tax_type)
                    )
                }
                Text(
                    stringResource(R.string.taxes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            taxes.forEach { tax ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tax.etrTaxType, modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = tax.etrInclude,
                        onCheckedChange = { onIncludeChange(tax, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExtrasCard(
    extras: List<WorkExtraTypes>,
    onExtraClick: (WorkExtraTypes) -> Unit,
    onAddExtraClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onAddExtraClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_new_extra)
                    )
                }
                Text(
                    stringResource(R.string.extra_pay_items),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            extras.forEach { extra ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExtraClick(extra) }
                        .padding(vertical = 4.dp)
                ) {
                    Text(extra.wetName, style = MaterialTheme.typography.bodyLarge)
                    HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}