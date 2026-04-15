package ms.mattschlenkrich.paycalculator.extras

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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import ms.mattschlenkrich.paycalculator.common.ExtraAttachToFrequencies
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.CapitalizedOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.common.compose.StandardTopAppBar
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.WorkExtraTypes

@Composable
fun WorkExtraTypeScreen(
    initialEmployer: Employers,
    initialExtraType: WorkExtraTypes?,
    existingExtraTypes: List<WorkExtraTypes>,
    onUpdate: (WorkExtraTypes) -> Unit,
    onDelete: (WorkExtraTypes) -> Unit,
    onCancel: () -> Unit
) {
    var wetName by remember { mutableStateOf(initialExtraType?.wetName ?: "") }
    var wetAppliesTo by remember {
        mutableStateOf(
            initialExtraType?.wetAppliesTo ?: ExtraAppliesToFrequencies.HOURLY.value
        )
    }
    var wetAttachTo by remember {
        mutableStateOf(
            initialExtraType?.wetAttachTo ?: ExtraAttachToFrequencies.HOURLY.value
        )
    }
    var wetIsCredit by remember { mutableStateOf(initialExtraType?.wetIsCredit ?: false) }
    var wetIsDefault by remember { mutableStateOf(initialExtraType?.wetIsDefault ?: false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val appliesToAllError =
        stringResource(R.string.there_can_only_be_one_extra_that_uses_the_sum_that_includes_other_extras)
    val nameExistsError = stringResource(R.string.this_extra_type_already_exists)
    val nameMissingError = stringResource(R.string.the_extra_must_have_a_name)

    val title = if (initialExtraType == null) {
        stringResource(R.string.add_a_new_extra_type)
    } else {
        stringResource(R.string.update_extra_type_) + initialExtraType.wetName
    }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = title,
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
                text = title + (if (initialExtraType == null) stringResource(R.string._for_) else stringResource(
                    R.string.__for
                )) + initialEmployer.employerName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = SCREEN_PADDING_VERTICAL)
            )

            CapitalizedOutlinedTextField(
                value = wetName,
                onValueChange = { wetName = it },
                label = { Text(stringResource(R.string.enter_extra_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            SimpleDropdownField(
                label = stringResource(R.string.applies_to),
                items = ExtraAppliesToFrequencies.entries,
                selectedItem = ExtraAppliesToFrequencies.entries.find { it.value == wetAppliesTo }
                    ?: ExtraAppliesToFrequencies.HOURLY,
                onItemSelected = {
                    wetAppliesTo = it.value
                    if (it == ExtraAppliesToFrequencies.PER_PAY_PERCENTAGE_OF_ALL) {
                        wetAttachTo = ExtraAttachToFrequencies.PER_PAY.value
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            SimpleDropdownField(
                label = stringResource(R.string.attach_to),
                items = ExtraAttachToFrequencies.entries,
                selectedItem = ExtraAttachToFrequencies.entries.find { it.value == wetAttachTo }
                    ?: ExtraAttachToFrequencies.HOURLY,
                onItemSelected = { wetAttachTo = it.value },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = wetIsCredit, onCheckedChange = { wetIsCredit = it })
                Text(text = stringResource(R.string.is_a_credit))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = wetIsDefault, onCheckedChange = { wetIsDefault = it })
                Text(text = stringResource(R.string.is_default))
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
                            wetName,
                            wetAppliesTo,
                            existingExtraTypes,
                            initialExtraType,
                            appliesToAllError,
                            nameExistsError,
                            nameMissingError
                        )
                        if (error == null) {
                            onUpdate(
                                WorkExtraTypes(
                                    workExtraTypeId = initialExtraType?.workExtraTypeId
                                        ?: NumberFunctions().generateRandomIdAsLong(),
                                    wetName = wetName.trim(),
                                    wetEmployerId = initialEmployer.employerId,
                                    wetAppliesTo = wetAppliesTo,
                                    wetAttachTo = wetAttachTo,
                                    wetIsCredit = wetIsCredit,
                                    wetIsDefault = wetIsDefault,
                                    wetIsDeleted = false,
                                    wetUpdateTime = DateFunctions().getCurrentTimeAsString()
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

                if (initialExtraType != null) {
                    Spacer(modifier = Modifier.width(ELEMENT_SPACING))
                    OutlinedButton(
                        onClick = { onDelete(initialExtraType) },
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
    appliesTo: Int,
    existingTypes: List<WorkExtraTypes>,
    currentType: WorkExtraTypes?,
    appliesToAllError: String,
    nameExistsError: String,
    nameMissingError: String
): String? {
    if (name.isBlank()) {
        return nameMissingError
    }

    for (extra in existingTypes) {
        if (extra.wetName == name.trim() && extra.workExtraTypeId != currentType?.workExtraTypeId) {
            return nameExistsError
        }
        if (extra.wetAppliesTo == ExtraAppliesToFrequencies.PER_PAY_PERCENTAGE_OF_ALL.value &&
            extra.workExtraTypeId != currentType?.workExtraTypeId &&
            appliesTo == ExtraAppliesToFrequencies.PER_PAY_PERCENTAGE_OF_ALL.value
        ) {
            return appliesToAllError
        }
    }
    return null
}