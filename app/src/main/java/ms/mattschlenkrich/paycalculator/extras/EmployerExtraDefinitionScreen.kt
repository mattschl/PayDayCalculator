package ms.mattschlenkrich.paycalculator.extras

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
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
import ms.mattschlenkrich.paycalculator.data.ExtraDefTypeAndEmployer
import ms.mattschlenkrich.paycalculator.data.WorkExtrasDefinitions

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

    var valueString by remember {
        mutableStateOf(
            initialDefinitionFull?.let {
                if (it.definition.weIsFixed) nf.displayDollars(it.definition.weValue)
                else nf.getPercentStringFromDouble(it.definition.weValue / 100)
            } ?: ""
        )
    }
    var isFixed by remember { mutableStateOf(initialDefinitionFull?.definition?.weIsFixed ?: true) }
    var effectiveDate by remember {
        mutableStateOf(
            initialDefinitionFull?.definition?.weEffectiveDate ?: df.getCurrentDateAsString()
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "EmployerExtraDefinitionScreen", // title,
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
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL, vertical = SCREEN_PADDING_VERTICAL)
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

                val description = remember(it.extraType) {
                    val creditDebit =
                        if (it.extraType.wetIsCredit) context.getString(R.string.credit) else context.getString(
                            R.string.debit
                        )
                    val appliesTo =
                        ExtraAppliesToFrequencies.entries.getOrNull(it.extraType.wetAppliesTo)?.frequency
                            ?: ""
                    val attachTo =
                        ExtraAttachToFrequencies.entries.getOrNull(it.extraType.wetAttachTo)?.frequency
                            ?: ""
                    val automatic =
                        if (it.extraType.wetIsDefault) context.getString(R.string.is_automatic) else context.getString(
                            R.string.added_manually
                        )

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
                        else nf.getPercentStringFromDouble(currentVal / 100)
                    },
                    enabled = initialDefinitionFull?.extraType?.let {
                        it.wetAppliesTo != 4 && it.wetAppliesTo != 1
                    } ?: true
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
        }
    }
}