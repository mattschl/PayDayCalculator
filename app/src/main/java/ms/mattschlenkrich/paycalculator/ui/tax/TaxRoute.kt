package ms.mattschlenkrich.paycalculator.ui.tax

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.common.compose.calculateGridColumns
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.TaxEffectiveDates
import ms.mattschlenkrich.paycalculator.data.TaxTypes
import ms.mattschlenkrich.paycalculator.data.WorkTaxRules
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel

@Composable
fun TaxRoute(
    mainViewModel: MainViewModel,
    workTaxViewModel: WorkTaxViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val universalEffectiveDateLabel = stringResource(R.string.choose_a_universal_effective_date)

    val taxTypes by workTaxViewModel.getTaxTypes().observeAsState(emptyList())
    val effectiveDates by workTaxViewModel.getTaxEffectiveDates().observeAsState(emptyList())

    var selectedTaxType by remember { mutableStateOf<TaxTypes?>(null) }
    var selectedEffectiveDate by remember { mutableStateOf<TaxEffectiveDates?>(null) }

    LaunchedEffect(taxTypes) {
        if (selectedTaxType == null && taxTypes.isNotEmpty()) {
            selectedTaxType = taxTypes.find { it.taxType == mainViewModel.getTaxTypeString() }
                ?: taxTypes.first()
        }
    }

    LaunchedEffect(effectiveDates) {
        if (selectedEffectiveDate == null && effectiveDates.isNotEmpty()) {
            selectedEffectiveDate =
                effectiveDates.find { it.tdEffectiveDate == mainViewModel.getEffectiveDateString() }
                    ?: effectiveDates.first()
        }
    }

    TaxRulesContent(
        workTaxViewModel = workTaxViewModel,
        nf = nf,
        selectedTaxType = selectedTaxType,
        onTaxTypeSelected = { selectedTaxType = it },
        selectedEffectiveDate = selectedEffectiveDate,
        onEffectiveDateSelected = { selectedEffectiveDate = it },
        onAddTaxRule = { taxType: String, effectiveDate: String, level: Int ->
            mainViewModel.setTaxTypeString(taxType)
            mainViewModel.setEffectiveDateString(effectiveDate)
            mainViewModel.setTaxLevel(level)
            navController.navigate(Screen.TaxRuleAdd.route)
        },
        onUpdateTaxType = { type: TaxTypes ->
            mainViewModel.setTaxType(type)
            navController.navigate(Screen.TaxTypeUpdate.route)
        },
        onTaxRuleSelected = { rule: WorkTaxRules ->
            mainViewModel.setTaxRule(rule)
            navController.navigate(Screen.TaxRuleUpdate.route)
        },
        onChooseEffectiveDate = {
            val curDateAll = df.getCurrentDateAsString().split("-")
            val datePickerDialog = DatePickerDialog(
                context, { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${
                        month.toString().padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    workTaxViewModel.insertEffectiveDate(
                        TaxEffectiveDates(
                            display,
                            nf.generateRandomIdAsLong(),
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    )
                }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(universalEffectiveDateLabel)
            datePickerDialog.show()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxRulesContent(
    workTaxViewModel: WorkTaxViewModel,
    nf: NumberFunctions,
    selectedTaxType: TaxTypes?,
    onTaxTypeSelected: (TaxTypes) -> Unit,
    selectedEffectiveDate: TaxEffectiveDates?,
    onEffectiveDateSelected: (TaxEffectiveDates) -> Unit,
    onAddTaxRule: (String, String, Int) -> Unit,
    onUpdateTaxType: (TaxTypes) -> Unit,
    onTaxRuleSelected: (WorkTaxRules) -> Unit,
    onChooseEffectiveDate: () -> Unit
) {
    val columns = calculateGridColumns()

    val taxTypes by workTaxViewModel.getTaxTypes().observeAsState(emptyList())
    val effectiveDates by workTaxViewModel.getTaxEffectiveDates().observeAsState(emptyList())
    val taxRules by if (selectedTaxType != null && selectedEffectiveDate != null) {
        workTaxViewModel.getTaxRules(
            selectedTaxType.taxType,
            selectedEffectiveDate.tdEffectiveDate
        ).observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList<WorkTaxRules>()) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimpleDropdownField(
                    label = stringResource(R.string.tax_type),
                    items = taxTypes,
                    selectedItem = selectedTaxType,
                    onItemSelected = onTaxTypeSelected,
                    itemToString = { it.taxType },
                    modifier = Modifier.weight(1f)
                )
                if (selectedTaxType != null) {
                    IconButton(onClick = { onUpdateTaxType(selectedTaxType) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.update_tax_type)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimpleDropdownField(
                    label = stringResource(R.string.effective_date),
                    items = effectiveDates,
                    selectedItem = selectedEffectiveDate,
                    onItemSelected = onEffectiveDateSelected,
                    itemToString = { it.tdEffectiveDate },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onChooseEffectiveDate) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.choose_a_universal_effective_date)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            if (selectedTaxType != null && selectedEffectiveDate != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.tax_rules),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = {
                        onAddTaxRule(
                            selectedTaxType.taxType,
                            selectedEffectiveDate.tdEffectiveDate,
                            (taxRules.maxOfOrNull { it.wtLevel } ?: 0) + 1
                        )
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_tax_rule)
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(taxRules.sortedBy { it.wtLevel }) { rule ->
                        TaxRuleItem(
                            rule = rule,
                            nf = nf,
                            onClick = { onTaxRuleSelected(rule) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaxRuleItem(
    rule: WorkTaxRules,
    nf: NumberFunctions,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lvl ${rule.wtLevel}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = nf.getPercentStringFromDouble(rule.wtPercent),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (rule.wtHasExemption) {
                    Text(
                        text = "Ex: ${nf.displayDollars(rule.wtExemptionAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                if (rule.wtHasBracket) {
                    Text(
                        text = "Lim: ${nf.displayDollars(rule.wtBracketAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}