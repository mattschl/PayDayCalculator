package ms.mattschlenkrich.paycalculator.ui.extras

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.ExtraAttachToFrequencies
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.CapitalizedOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.data.ExtraDefinitionAndType
import ms.mattschlenkrich.paycalculator.data.PayPeriods
import ms.mattschlenkrich.paycalculator.data.WorkDateExtraAndTypeAndDef
import ms.mattschlenkrich.paycalculator.data.WorkPayPeriodExtras

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayPeriodExtraScreen(
    curPayPeriod: PayPeriods,
    employerName: String,
    initialExtra: WorkPayPeriodExtras?,
    existingPayPeriodExtras: List<WorkPayPeriodExtras>,
    existingWorkDateExtras: List<WorkDateExtraAndTypeAndDef>,
    defaultExtras: List<ExtraDefinitionAndType>,
    onUpdate: (WorkPayPeriodExtras) -> Unit,
    onDelete: (WorkPayPeriodExtras) -> Unit,
    onCancel: () -> Unit
) {
    val nf = NumberFunctions()
    val df = DateFunctions()

    var name by remember { mutableStateOf(initialExtra?.ppeName ?: "") }
    var appliesTo by remember {
        mutableStateOf(
            initialExtra?.ppeAppliesTo ?: ExtraAttachToFrequencies.PER_PAY.value
        )
    }
    var valueString by remember {
        mutableStateOf(
            if (initialExtra == null) "0.00"
            else if (initialExtra.ppeIsFixed) nf.displayDollars(initialExtra.ppeValue)
            else nf.getPercentStringFromDouble(initialExtra.ppeValue)
        )
    }
    var isFixed by remember { mutableStateOf(initialExtra?.ppeIsFixed ?: true) }
    var isCredit by remember { mutableStateOf(initialExtra?.ppeIsCredit ?: false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDuplicateDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    val nameExistsError = stringResource(R.string.this_extra_name_has_already_been_used)
    val nameMissingError = stringResource(R.string.the_extra_must_have_a_name)
    val valueMissingError = stringResource(R.string.this_extra_must_have_a_value)

    if (showDuplicateDialog != null) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = null },
            title = { Text(stringResource(R.string.confirm_adding_duplicate_extra__) + showDuplicateDialog!!.first) },
            text = { Text(showDuplicateDialog!!.second) },
            confirmButton = {
                TextButton(onClick = {
                    performSave(
                        nf,
                        df,
                        name,
                        appliesTo,
                        valueString,
                        isFixed,
                        isCredit,
                        initialExtra,
                        curPayPeriod,
                        onUpdate
                    )
                    showDuplicateDialog = null
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicateDialog = null }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    val title = (if (initialExtra == null) stringResource(R.string.add_an_extra_to_this_pay_period)
    else stringResource(R.string.update_extra_) + initialExtra.ppeName)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        /*     topBar = {
                 TopAppBar(
                     title = {
                         Text(
                             text = "PayPeriodExtraScreen", // title,
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
             }*/
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
                        stringResource(R.string.pay_cutoff_) + curPayPeriod.ppCutoffDate,
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
                items = ExtraAttachToFrequencies.entries,
                selectedItem = ExtraAttachToFrequencies.entries.find { it.value == appliesTo }
                    ?: ExtraAttachToFrequencies.PER_PAY,
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
                val duplicateAddMsg =
                    stringResource(R.string.this_is_already_used_for_this_payday__add)
                val duplicateOverwriteMsg =
                    stringResource(R.string.this_is_already_used_for_this_payday__overwrite)

                Button(
                    onClick = {
                        val error = validate(
                            name,
                            valueString,
                            existingPayPeriodExtras,
                            initialExtra,
                            nameExistsError,
                            nameMissingError,
                            valueMissingError,
                            nf
                        )
                        if (error == null) {
                            // Check for duplicates in other categories
                            var duplicateFound = false
                            for (extra in existingWorkDateExtras) {
                                if (name.trim() == extra.extra.wdeName && extra.extra.wdeName != initialExtra?.ppeName) {
                                    showDuplicateDialog = extra.extra.wdeName to duplicateAddMsg
                                    duplicateFound = true
                                    break
                                }
                            }
                            if (!duplicateFound) {
                                for (extra in defaultExtras) {
                                    if (name.trim() == extra.extraType.wetName && extra.extraType.wetName != initialExtra?.ppeName) {
                                        showDuplicateDialog =
                                            extra.extraType.wetName to duplicateOverwriteMsg
                                        duplicateFound = true
                                        break
                                    }
                                }
                            }

                            if (!duplicateFound) {
                                performSave(
                                    nf,
                                    df,
                                    name,
                                    appliesTo,
                                    valueString,
                                    isFixed,
                                    isCredit,
                                    initialExtra,
                                    curPayPeriod,
                                    onUpdate
                                )
                            }
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

private fun performSave(
    nf: NumberFunctions,
    df: DateFunctions,
    name: String,
    appliesTo: Int,
    valueString: String,
    isFixed: Boolean,
    isCredit: Boolean,
    initialExtra: WorkPayPeriodExtras?,
    curPayPeriod: PayPeriods,
    onUpdate: (WorkPayPeriodExtras) -> Unit
) {
    var value = nf.getDoubleFromDollarOrPercentString(valueString)
    if (!isFixed && !valueString.contains("%") && value != 0.0) {
        value /= 100.0
    }
    onUpdate(
        WorkPayPeriodExtras(
            workPayPeriodExtraId = initialExtra?.workPayPeriodExtraId
                ?: nf.generateRandomIdAsLong(),
            ppePayPeriodId = curPayPeriod.payPeriodId,
            ppeExtraTypeId = initialExtra?.ppeExtraTypeId,
            ppeName = name.trim(),
            ppeAppliesTo = appliesTo,
            ppeAttachTo = 3, // Per legacy code
            ppeValue = value,
            ppeIsFixed = isFixed,
            ppeIsCredit = isCredit,
            ppeIsDeleted = false,
            ppeUpdateTime = df.getCurrentUTCTimeAsString()
        )
    )
}

private fun validate(
    name: String,
    valueString: String,
    existingExtras: List<WorkPayPeriodExtras>,
    currentExtra: WorkPayPeriodExtras?,
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
        if (extra.ppeName == name.trim() && extra.workPayPeriodExtraId != currentExtra?.workPayPeriodExtraId) {
            return nameExistsError
        }
    }
    return null
}