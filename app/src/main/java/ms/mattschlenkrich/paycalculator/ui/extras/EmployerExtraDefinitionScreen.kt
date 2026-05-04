package ms.mattschlenkrich.paycalculator.ui.extras

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.ExtraAppliesToFrequencies
import ms.mattschlenkrich.paycalculator.common.ExtraAttachToFrequencies
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefTypeAndEmployer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerExtraDefinitionScreen(
    initialDefinitionFull: ExtraDefTypeAndEmployer? = null,
    onUpdate: (WorkExtrasDefinitions) -> Unit,
    onDelete: (WorkExtrasDefinitions) -> Unit,
    onCancel: () -> Unit
) {
    val nf = remember { NumberFunctions() }
    val df = remember { DateFunctions() }
    val context = LocalContext.current

    val forcedFixed = initialDefinitionFull?.extraType?.wetAppliesTo == 1
    val forcedPercent = initialDefinitionFull?.extraType?.wetAppliesTo == 4

    var isFixed by remember {
        mutableStateOf(
            if (forcedFixed) true
            else if (forcedPercent) false
            else initialDefinitionFull?.definition?.weIsFixed ?: true
        )
    }

    var valueString by remember {
        mutableStateOf(
            initialDefinitionFull?.let {
                if (isFixed) nf.displayDollars(it.definition.weValue)
                else nf.getPercentStringFromDouble(it.definition.weValue)
            } ?: ""
        )
    }
    var effectiveDate by remember {
        mutableStateOf(
            initialDefinitionFull?.definition?.weEffectiveDate ?: df.getCurrentDateAsString()
        )
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm && initialDefinitionFull != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    onDelete(initialDefinitionFull.definition)
                    showDeleteConfirm = false
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.confirm_leave)) }, // Need a delete confirm string
            text = {
                Text(
                    stringResource(R.string.are_you_sure_you_want_to_delete_) +
                            " " + initialDefinitionFull.extraType.wetName +
                            " (" + initialDefinitionFull.definition.weEffectiveDate + ")?"
                )
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialDefinitionFull == null || initialDefinitionFull.definition.workExtraDefId == 0L)
                            stringResource(R.string.add_new_definition)
                        else stringResource(R.string.update_this_extra),
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
                },
                actions = {
                    if (initialDefinitionFull != null && initialDefinitionFull.definition.workExtraDefId != 0L) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL, vertical = SCREEN_PADDING_VERTICAL)
                .verticalScroll(rememberScrollState())
        ) {
            initialDefinitionFull?.let {
                Text(
                    text = it.employer.employerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = ELEMENT_SPACING)
                )
                Text(
                    text = it.extraType.wetName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = ELEMENT_SPACING)
                )

                val creditLabel = stringResource(R.string.credit)
                val debitLabel = stringResource(R.string.debit)
                val isAutomaticLabel = stringResource(R.string.is_automatic)
                val addedManuallyLabel = stringResource(R.string.added_manually)

                val description = remember(it.extraType) {
                    val creditDebit =
                        if (it.extraType.wetIsCredit) creditLabel else debitLabel
                    val appliesTo =
                        ExtraAppliesToFrequencies.entries.getOrNull(it.extraType.wetAppliesTo)?.frequency
                            ?: ""
                    val attachTo =
                        ExtraAttachToFrequencies.entries.getOrNull(it.extraType.wetAttachTo)?.frequency
                            ?: ""
                    val automatic =
                        if (it.extraType.wetIsDefault) isAutomaticLabel else addedManuallyLabel

                    "$creditDebit calculated $appliesTo period - attaches to $attachTo. $automatic"
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = ELEMENT_SPACING)
                )
            }

            DecimalOutlinedTextField(
                value = valueString,
                onValueChange = { valueString = it },
                label = { Text(stringResource(R.string.value)) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = ELEMENT_SPACING)
            ) {
                Checkbox(
                    checked = isFixed,
                    onCheckedChange = {
                        val currentVal = nf.getDoubleFromDollarOrPercentString(valueString)
                        isFixed = it
                        valueString = if (it) nf.displayDollars(currentVal)
                        else nf.getPercentStringFromDouble(currentVal)
                    },
                    enabled = !forcedFixed && !forcedPercent
                )
                Text(
                    text = when (initialDefinitionFull?.extraType?.wetAppliesTo) {
                        4 -> stringResource(R.string.defaults_to_percentage)
                        1 -> stringResource(R.string.defaults_to_fixed)
                        else -> stringResource(R.string.fixed_amount)
                    },
                    modifier = Modifier.padding(start = ELEMENT_SPACING)
                )
            }

            SelectAllOutlinedTextField(
                value = effectiveDate,
                onValueChange = { effectiveDate = it },
                label = { Text(stringResource(R.string.effective_date)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        df.showDatePicker(context, effectiveDate) {
                            effectiveDate = it
                        }
                    }) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = stringResource(R.string.choose_date)
                        )
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SCREEN_PADDING_VERTICAL),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onCancel) {
                    Text(stringResource(R.string.cancel))
                }
                if (initialDefinitionFull != null && initialDefinitionFull.definition.workExtraDefId != 0L) {
                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }
                Button(onClick = {
                    if (valueString.isNotBlank()) {
                        var value = nf.getDoubleFromDollarOrPercentString(valueString)
                        if (!isFixed && !valueString.contains("%") && value != 0.0) {
                            value /= 100.0
                        }
                        val currentId = initialDefinitionFull?.definition?.workExtraDefId ?: 0L
                        val employerId = initialDefinitionFull?.employer?.employerId ?: 0L
                        val typeId = initialDefinitionFull?.extraType?.workExtraTypeId ?: 0L

                        onUpdate(
                            WorkExtrasDefinitions(
                                workExtraDefId = if (currentId == 0L) nf.generateRandomIdAsLong() else currentId,
                                weEmployerId = employerId,
                                weExtraTypeId = typeId,
                                weValue = value,
                                weIsFixed = isFixed,
                                weEffectiveDate = effectiveDate,
                                weIsDeleted = false,
                                weUpdateTime = df.getCurrentUTCTimeAsString()
                            )
                        )
                    }
                }) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}