package ms.mattschlenkrich.paycalculator.ui.employer.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.PayDayFrequencies
import ms.mattschlenkrich.paycalculator.common.WorkDayOfWeek
import ms.mattschlenkrich.paycalculator.common.compose.CapitalizedOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.data.entity.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes

@OptIn(ExperimentalMaterial3Api::class)
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
    onDeleteClick: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Done, contentDescription = stringResource(R.string.save))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
        ) {
            Spacer(modifier = Modifier.height(SCREEN_PADDING_VERTICAL))

            CapitalizedOutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.employer_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            SimpleDropdownField(
                label = stringResource(R.string.pay_day_frequency),
                items = PayDayFrequencies.entries,
                selectedItem = PayDayFrequencies.findByString(frequency),
                onItemSelected = { onFrequencyChange(it.frequency) },
                itemToString = { it.frequency },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onStartDateClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.first_cheque_date) + ": " + startDate)
            }

            SimpleDropdownField(
                label = stringResource(R.string.day_of_week),
                items = WorkDayOfWeek.entries,
                selectedItem = WorkDayOfWeek.findByString(dayOfWeek),
                onItemSelected = { onDayOfWeekChange(it.day) },
                itemToString = { it.day },
                modifier = Modifier.fillMaxWidth()
            )

            SelectAllOutlinedTextField(
                value = daysBefore,
                onValueChange = onDaysBeforeChange,
                label = { Text(stringResource(R.string.how_many_days_before_is_cutoff)) },
                modifier = Modifier.fillMaxWidth()
            )

            if (PayDayFrequencies.findByString(frequency) == PayDayFrequencies.SEMI_MONTHLY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                ) {
                    DecimalOutlinedTextField(
                        value = midMonthDate,
                        onValueChange = onMidMonthDateChange,
                        label = { Text(stringResource(R.string.mid_month_pay_day)) },
                        modifier = Modifier.weight(1f)
                    )
                    DecimalOutlinedTextField(
                        value = mainMonthDate,
                        onValueChange = onMainMonthDateChange,
                        label = { Text(stringResource(R.string.main_monthly_pay_date)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (isUpdate) {
                Button(
                    onClick = onViewWagesClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.view_or_edit_wages))
                }

                EmployerTaxesCard(
                    taxes = taxes,
                    onTaxIncludeChange = onTaxIncludeChange,
                    onAddTaxClick = onAddTaxClick
                )

                EmployerExtrasCard(
                    extras = extras,
                    onExtraClick = onExtraClick,
                    onAddExtraClick = onAddExtraClick
                )
            }

            Spacer(modifier = Modifier.height(SCREEN_PADDING_VERTICAL))
        }
    }
}