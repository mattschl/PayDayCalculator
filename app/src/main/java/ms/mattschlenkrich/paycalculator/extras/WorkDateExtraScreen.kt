package ms.mattschlenkrich.paycalculator.extras

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.ExtraAppliesToFrequencies
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.CapitalizedOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.data.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.WorkDates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkDateExtraScreen(
    initialWorkDate: WorkDates,
    employerName: String,
    initialExtra: WorkDateExtras?,
    existingExtras: List<WorkDateExtras>,
    onUpdate: (WorkDateExtras) -> Unit,
    onDelete: (WorkDateExtras) -> Unit,
    onCancel: () -> Unit
) {
    val nf = NumberFunctions()
    val df = DateFunctions()

    var name by remember { mutableStateOf(initialExtra?.wdeName ?: "") }
    var appliesTo by remember {
        mutableStateOf(
            initialExtra?.wdeAppliesTo ?: ExtraAppliesToFrequencies.HOURLY.value
        )
    }
    var valueString by remember {
        mutableStateOf(
            if (initialExtra == null) "0.00"
            else if (initialExtra.wdeIsFixed) nf.displayDollars(initialExtra.wdeValue)
            else nf.getPercentStringFromDouble(initialExtra.wdeValue)
        )
    }
    var isFixed by remember { mutableStateOf(initialExtra?.wdeIsFixed ?: true) }
    var isCredit by remember { mutableStateOf(initialExtra?.wdeIsCredit ?: false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val nameExistsError = stringResource(R.string.this_extra_name_has_already_been_used)
    val nameMissingError = stringResource(R.string.the_extra_must_have_a_name)
    val valueMissingError = stringResource(R.string.this_extra_must_have_a_value)

    val title = (if (initialExtra == null) stringResource(R.string.add_a_one_time_extra)
    else stringResource(R.string.update_extra_) + initialExtra.wdeName)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WorkDateExtraScreen", // title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = title +
                        stringResource(R.string.__for) + employerName +
                        stringResource(R.string._on_) + df.getDisplayDate(initialWorkDate.wdDate),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = SCREEN_PADDING_VERTICAL)
            )

            CapitalizedOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.enter_extra_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            SimpleDropdownField(
                label = stringResource(R.string.applies_to),
                items = ExtraAppliesToFrequencies.entries,
                selectedItem = ExtraAppliesToFrequencies.entries.find { it.value == appliesTo }
                    ?: ExtraAppliesToFrequencies.HOURLY,
                onItemSelected = { appliesTo = it.value },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            DecimalOutlinedTextField(
                value = valueString,
                onValueChange = { valueString = it },
                label = { Text(stringResource(R.string.enter_amount_or_percentage)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isFixed, onCheckedChange = {
                    val currentVal = nf.getDoubleFromDollarOrPercentString(valueString)
                    isFixed = it
                    valueString = if (it) nf.displayDollars(currentVal)
                    else nf.getPercentStringFromDouble(currentVal / 100.0)
                })
                Text(text = stringResource(R.string.fixed_amount))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isCredit, onCheckedChange = { isCredit = it })
                Text(text = stringResource(R.string.is_a_credit))
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = ELEMENT_SPACING)
                )
            }

            Spacer(modifier = Modifier.height(ELEMENT_SPACING * 3))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val error = validate(
                            name,
                            valueString,
                            existingExtras,
                            initialExtra,
                            nameExistsError,
                            nameMissingError,
                            valueMissingError,
                            nf
                        )
                        if (error == null) {
                            val value = nf.getDoubleFromDollarOrPercentString(valueString)
                            onUpdate(
                                WorkDateExtras(
                                    workDateExtraId = initialExtra?.workDateExtraId
                                        ?: nf.generateRandomIdAsLong(),
                                    wdeWorkDateId = initialWorkDate.workDateId,
                                    wdeExtraTypeId = initialExtra?.wdeExtraTypeId,
                                    wdeName = name.trim(),
                                    wdeAppliesTo = appliesTo,
                                    wdeAttachTo = initialExtra?.wdeAttachTo ?: 1,
                                    wdeValue = if (!isFixed && value >= 1.0) value / 100.0 else value,
                                    wdeIsFixed = isFixed,
                                    wdeIsCredit = isCredit,
                                    wdeIsDeleted = false,
                                    wdeUpdateTime = df.getCurrentTimeAsString()
                                )
                            )
                        } else {
                            errorMessage = error
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.save))
                }

                if (initialExtra != null) {
                    Spacer(modifier = Modifier.width(ELEMENT_SPACING))
                    OutlinedButton(
                        onClick = { onDelete(initialExtra) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }

                Spacer(modifier = Modifier.width(ELEMENT_SPACING))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

private fun validate(
    name: String,
    valueString: String,
    existingExtras: List<WorkDateExtras>,
    currentExtra: WorkDateExtras?,
    nameExistsError: String,
    nameMissingError: String,
    valueMissingError: String,
    nf: NumberFunctions
): String? {
    if (name.isBlank()) {
        return nameMissingError
    }
    if (nf.getDoubleFromDollarOrPercentString(valueString) == 0.0) {
        return valueMissingError
    }

    for (extra in existingExtras) {
        if (extra.wdeName == name.trim() && extra.workDateExtraId != currentExtra?.workDateExtraId) {
            return nameExistsError
        }
    }
    return null
}