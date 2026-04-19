package ms.mattschlenkrich.paycalculator.tax

import android.app.DatePickerDialog
import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
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
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
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
        mainViewModel = mainViewModel,
        nf = nf,
        df = df,
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
        onAddTaxType = {
            navController.navigate(Screen.TaxTypeAdd.route)
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
            datePickerDialog.setTitle(context.getString(R.string.choose_a_universal_effective_date))
            datePickerDialog.show()
        },
        onBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxRulesContent(
    workTaxViewModel: WorkTaxViewModel,
    mainViewModel: MainViewModel,
    nf: NumberFunctions,
    df: DateFunctions,
    selectedTaxType: TaxTypes?,
    onTaxTypeSelected: (TaxTypes) -> Unit,
    selectedEffectiveDate: TaxEffectiveDates?,
    onEffectiveDateSelected: (TaxEffectiveDates) -> Unit,
    onAddTaxRule: (String, String, Int) -> Unit,
    onUpdateTaxType: (TaxTypes) -> Unit,
    onTaxRuleSelected: (WorkTaxRules) -> Unit,
    onAddTaxType: () -> Unit,
    onChooseEffectiveDate: () -> Unit,
    onBack: () -> Unit
) {
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
                    columns = GridCells.Fixed(2),
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

@Composable
fun TaxTypeAddRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workTaxViewModel: WorkTaxViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }

    var taxTypeState by remember { mutableStateOf("") }
    var selectedBasedOn by remember { mutableIntStateOf(0) }
    var showNextStepDialog by remember { mutableStateOf(false) }
    var savedTaxType by remember { mutableStateOf<TaxTypes?>(null) }

    val taxTypeList by workTaxViewModel.getTaxTypes().observeAsState(emptyList())
    val employers by employerViewModel.getEmployers().observeAsState(emptyList())

    if (showNextStepDialog && savedTaxType != null) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss */ },
            title = { Text(stringResource(R.string.choose_next_steps_for) + savedTaxType!!.taxType) },
            text = { Text(stringResource(R.string.the_tax_type_has_been_added_but_there_are_no_rules_yet_)) },
            confirmButton = {
                TextButton(onClick = {
                    mainViewModel.setTaxType(savedTaxType)
                    mainViewModel.setTaxTypeString(savedTaxType!!.taxType)
                    navController.navigate(Screen.TaxRuleAdd.route) {
                        popUpTo(Screen.TaxTypeAdd.route) { inclusive = true }
                    }
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mainViewModel.setTaxType(savedTaxType)
                    mainViewModel.setTaxTypeString(savedTaxType!!.taxType)
                    navController.popBackStack()
                }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    TaxTypeScreen(
        taxType = taxTypeState,
        onTaxTypeChange = { taxTypeState = it },
        selectedBasedOn = selectedBasedOn,
        onBasedOnChange = { selectedBasedOn = it },
        title = stringResource(R.string.add_a_new_tax_type),
        onSaveClick = {
            val errorResId = validateTaxType(taxTypeState, taxTypeList)
            if (errorResId == null) {
                val taxType = TaxTypes(
                    nf.generateRandomIdAsLong(),
                    taxTypeState.trim(),
                    selectedBasedOn,
                    false,
                    df.getCurrentUTCTimeAsString()
                )
                workTaxViewModel.insertTaxTypeWithEmployerLinks(
                    taxType,
                    employers,
                    df.getCurrentUTCTimeAsString()
                )
                savedTaxType = taxType
                showNextStepDialog = true
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_) + context.getString(errorResId),
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun TaxTypeUpdateRoute(
    mainViewModel: MainViewModel,
    workTaxViewModel: WorkTaxViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }

    val curTaxType = mainViewModel.getTaxType() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    var taxTypeState by remember { mutableStateOf(curTaxType.taxType) }
    var selectedBasedOn by remember { mutableIntStateOf(curTaxType.ttBasedOn) }

    val taxTypeList by workTaxViewModel.getTaxTypes().observeAsState(emptyList())

    TaxTypeScreen(
        taxType = taxTypeState,
        onTaxTypeChange = { taxTypeState = it },
        selectedBasedOn = selectedBasedOn,
        onBasedOnChange = { selectedBasedOn = it },
        title = stringResource(R.string.update_tax_type),
        onSaveClick = {
            val errorResId = validateTaxTypeUpdate(taxTypeState, curTaxType, taxTypeList)
            if (errorResId == null) {
                val updatedTaxType = curTaxType.copy(
                    taxType = taxTypeState,
                    ttBasedOn = selectedBasedOn,
                    ttUpdateTime = df.getCurrentUTCTimeAsString()
                )
                workTaxViewModel.updateWorkTaxType(updatedTaxType)
                navController.popBackStack()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_) + context.getString(errorResId),
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        onDeleteClick = {
            val deletedTaxType = curTaxType.copy(
                ttIsDeleted = true,
                ttUpdateTime = df.getCurrentUTCTimeAsString()
            )
            workTaxViewModel.updateWorkTaxType(deletedTaxType)
            navController.popBackStack()
        },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun TaxRuleAddRoute(
    mainViewModel: MainViewModel,
    workTaxViewModel: WorkTaxViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }

    val taxType = mainViewModel.getTaxTypeString() ?: ""
    val taxLevel = mainViewModel.getTaxLevel().toString()
    val effectiveDate = mainViewModel.getEffectiveDateString() ?: ""

    var percentage by remember { mutableStateOf("") }
    var hasExemption by remember { mutableStateOf(false) }
    var exemptionAmount by remember { mutableStateOf("") }
    var hasUpperLimit by remember { mutableStateOf(false) }
    var upperLimit by remember { mutableStateOf("") }

    TaxRuleScreen(
        title = stringResource(R.string.add_tax_rule),
        taxType = taxType,
        taxLevel = taxLevel,
        effectiveDate = effectiveDate,
        percentage = percentage,
        onPercentageChange = { percentage = it },
        hasExemption = hasExemption,
        onHasExemptionChange = { hasExemption = it },
        exemptionAmount = exemptionAmount,
        onExemptionAmountChange = { exemptionAmount = it },
        hasUpperLimit = hasUpperLimit,
        onHasUpperLimitChange = { hasUpperLimit = it },
        upperLimit = upperLimit,
        onUpperLimitChange = { upperLimit = it },
        onSaveClick = {
            val errorResId = validateTaxRule(
                nf,
                percentage,
                hasExemption,
                exemptionAmount,
                hasUpperLimit,
                upperLimit
            )
            if (errorResId == null) {
                val taxRule = WorkTaxRules(
                    nf.generateRandomIdAsLong(),
                    taxType,
                    taxLevel.toInt(),
                    effectiveDate,
                    nf.getDoubleFromPercentString(percentage),
                    hasExemption,
                    if (hasExemption) nf.getDoubleFromDollars(exemptionAmount) else 0.0,
                    hasUpperLimit,
                    if (hasUpperLimit) nf.getDoubleFromDollars(upperLimit) else 0.0,
                    false,
                    df.getCurrentUTCTimeAsString()
                )
                workTaxViewModel.insertTaxRule(taxRule)
                navController.popBackStack()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_) + context.getString(errorResId),
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun TaxRuleUpdateRoute(
    mainViewModel: MainViewModel,
    workTaxViewModel: WorkTaxViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }

    val curTaxRule = mainViewModel.getTaxRule() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    var percentage by remember { mutableStateOf(nf.getPercentStringFromDouble(curTaxRule.wtPercent)) }
    var hasExemption by remember { mutableStateOf(curTaxRule.wtHasExemption) }
    var exemptionAmount by remember { mutableStateOf(nf.displayDollars(curTaxRule.wtExemptionAmount)) }
    var hasUpperLimit by remember { mutableStateOf(curTaxRule.wtHasBracket) }
    var upperLimit by remember { mutableStateOf(nf.displayDollars(curTaxRule.wtBracketAmount)) }

    TaxRuleScreen(
        title = stringResource(R.string.view_or_update_tax_rule),
        taxType = curTaxRule.wtType,
        taxLevel = curTaxRule.wtLevel.toString(),
        effectiveDate = curTaxRule.wtEffectiveDate,
        percentage = percentage,
        onPercentageChange = { percentage = it },
        hasExemption = hasExemption,
        onHasExemptionChange = { hasExemption = it },
        exemptionAmount = exemptionAmount,
        onExemptionAmountChange = { exemptionAmount = it },
        hasUpperLimit = hasUpperLimit,
        onHasUpperLimitChange = { hasUpperLimit = it },
        upperLimit = upperLimit,
        onUpperLimitChange = { upperLimit = it },
        onSaveClick = {
            val errorResId = validateTaxRule(
                nf,
                percentage,
                hasExemption,
                exemptionAmount,
                hasUpperLimit,
                upperLimit
            )
            if (errorResId == null) {
                val updatedTaxRule = curTaxRule.copy(
                    wtPercent = nf.getDoubleFromPercentString(percentage),
                    wtHasExemption = hasExemption,
                    wtExemptionAmount = nf.getDoubleFromDollars(exemptionAmount),
                    wtHasBracket = hasUpperLimit,
                    wtBracketAmount = nf.getDoubleFromDollars(upperLimit),
                    wtUpdateTime = df.getCurrentUTCTimeAsString()
                )
                workTaxViewModel.updateTaxRule(updatedTaxRule)
                navController.popBackStack()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_) + context.getString(errorResId),
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        onDeleteClick = {
            val deletedTaxRule = curTaxRule.copy(
                wtIsDeleted = true,
                wtUpdateTime = df.getCurrentUTCTimeAsString()
            )
            workTaxViewModel.updateTaxRule(deletedTaxRule)
            navController.popBackStack()
        },
        onBackClick = { navController.popBackStack() }
    )
}

private fun validateTaxType(
    name: String,
    list: List<TaxTypes>
): Int? {
    if (name.isBlank()) {
        return R.string.the_tax_type_must_have_a_name
    }
    if (list.any { it.taxType.equals(name.trim(), ignoreCase = true) }) {
        return R.string.this_tax_type_already_exists
    }
    return null
}

private fun validateTaxTypeUpdate(
    name: String,
    cur: TaxTypes,
    list: List<TaxTypes>
): Int? {
    if (name.isBlank()) {
        return R.string.the_tax_type_must_have_a_name
    }
    if (list.any { it.taxType == name.trim() && it.taxType != cur.taxType }) {
        return R.string.this_tax_type_already_exists
    }
    return null
}

private fun validateTaxRule(
    nf: NumberFunctions,
    percentage: String,
    hasExemption: Boolean,
    exemptionAmount: String,
    hasUpperLimit: Boolean,
    upperLimit: String
): Int? {
    if (percentage.isBlank() || nf.getDoubleFromDollarOrPercentString(percentage) == 0.0) {
        return R.string.there_should_be_a_percentage_here
    }
    if (hasExemption && (exemptionAmount.isBlank() || nf.getDoubleFromDollarOrPercentString(
            exemptionAmount
        ) == 0.0)
    ) {
        return R.string.an_exemption_is_indicated_but_no_amount_was_entered
    }
    if (hasUpperLimit && (upperLimit.isBlank() || nf.getDoubleFromDollarOrPercentString(
            upperLimit
        ) == 0.0)
    ) {
        return R.string.an_upper_limit_is_indicated_but_no_amount_was_entered
    }
    return null
}