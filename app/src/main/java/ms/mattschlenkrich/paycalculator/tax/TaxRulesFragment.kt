package ms.mattschlenkrich.paycalculator.tax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_TAX_RULES
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TaxBasedOn
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.common.compose.StandardTopAppBar
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.TaxEffectiveDates
import ms.mattschlenkrich.paycalculator.data.TaxTypes
import ms.mattschlenkrich.paycalculator.data.WorkTaxRules
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel

private const val TAG = FRAG_TAX_RULES

class TaxRulesFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workTaxViewModel: WorkTaxViewModel
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workTaxViewModel = mainActivity.workTaxViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    TaxRulesContent(
                        workTaxViewModel = workTaxViewModel,
                        mainViewModel = mainViewModel,
                        nf = nf,
                        df = df,
                        onAddTaxRule = { taxType, effectiveDate, level ->
                            gotoTaxRuleAdd(taxType, effectiveDate, level)
                        },
                        onUpdateTaxType = { type -> gotoTaxTypeUpdate(type) },
                        onTaxRuleSelected = { rule ->
                            mainViewModel.setTaxRule(rule)
                            findNavController().navigate(
                                TaxRulesFragmentDirections.actionTaxRulesFragmentToTaxRuleUpdateFragment()
                            )
                        },
                        onAddTaxType = { gotoTaxTypeAdd() },
                        onChooseEffectiveDate = { chooseNewEffectiveDate() },
                        onBack = { findNavController().navigateUp() }
                    )
                }
            }
        }
    }

    private fun chooseNewEffectiveDate() {
        val curDateAll = df.getCurrentDateAsString().split("-")
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(), { _, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                val display = "$year-${
                    month.toString().padStart(2, '0')
                }-${
                    dayOfMonth.toString().padStart(2, '0')
                }"
                workTaxViewModel.insertEffectiveDate(
                    TaxEffectiveDates(
                        display, nf.generateRandomIdAsLong(), false, df.getCurrentTimeAsString()
                    )
                )
            }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
        )
        datePickerDialog.setTitle(getString(R.string.choose_a_universal_effective_date))
        datePickerDialog.show()
    }

    private fun gotoTaxTypeUpdate(type: TaxTypes) {
        mainViewModel.setTaxType(type)
        mainViewModel.setCallingFragment(TAG)
        findNavController().navigate(
            TaxRulesFragmentDirections.actionTaxRulesFragmentToTaxTypeUpdateFragment()
        )
    }

    private fun gotoTaxRuleAdd(taxType: String, effectiveDate: String, level: Int) {
        mainViewModel.setTaxTypeString(taxType)
        mainViewModel.setEffectiveDateString(effectiveDate)
        mainViewModel.setTaxLevel(level)
        mainViewModel.addCallingFragment(TAG)
        findNavController().navigate(
            TaxRulesFragmentDirections.actionTaxRulesFragmentToTaxRuleAddFragment()
        )
    }

    private fun gotoTaxTypeAdd() {
        mainViewModel.addCallingFragment(TAG)
        findNavController().navigate(
            TaxRulesFragmentDirections.actionTaxRulesFragmentToTaxTypeAddFragment()
        )
    }
}

@Composable
fun TaxRulesContent(
    workTaxViewModel: WorkTaxViewModel,
    mainViewModel: MainViewModel,
    nf: NumberFunctions,
    df: DateFunctions,
    onAddTaxRule: (String, String, Int) -> Unit,
    onUpdateTaxType: (TaxTypes) -> Unit,
    onTaxRuleSelected: (WorkTaxRules) -> Unit,
    onAddTaxType: () -> Unit,
    onChooseEffectiveDate: () -> Unit,
    onBack: () -> Unit
) {
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

    val taxRules by if (selectedTaxType != null && selectedEffectiveDate != null) {
        workTaxViewModel.getTaxRules(
            selectedTaxType!!.taxType,
            selectedEffectiveDate!!.tdEffectiveDate
        ).observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(R.string.view_universal_tax_rules),
                onBackClicked = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTaxType != null && selectedEffectiveDate != null) {
                        onAddTaxRule(
                            selectedTaxType!!.taxType,
                            selectedEffectiveDate!!.tdEffectiveDate,
                            taxRules.size
                        )
                    }
                },
                containerColor = colorResource(id = R.color.dark_green),
                contentColor = colorResource(id = R.color.white)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_new))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = SCREEN_PADDING_HORIZONTAL,
                    vertical = SCREEN_PADDING_VERTICAL
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tax Type Dropdown
            val taxTypeOptions = taxTypes + TaxTypes(
                -1L, stringResource(R.string.add_a_new_tax_type), 0, false, ""
            )
            SimpleDropdownField(
                label = stringResource(R.string.tax_type),
                items = taxTypeOptions,
                selectedItem = selectedTaxType ?: taxTypeOptions.last(),
                onItemSelected = {
                    if (it.taxTypeId == -1L) {
                        onAddTaxType()
                    } else {
                        selectedTaxType = it
                    }
                },
                itemToString = { it.taxType },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            // Effective Date Dropdown
            val dateOptions = effectiveDates + TaxEffectiveDates(
                stringResource(R.string.add_new_effective_date), -1L, false, ""
            )
            SimpleDropdownField(
                label = stringResource(R.string.effective_date),
                items = dateOptions,
                selectedItem = selectedEffectiveDate ?: dateOptions.last(),
                onItemSelected = {
                    if (it.tdEffectiveDateId == -1L) {
                        onChooseEffectiveDate()
                    } else {
                        selectedEffectiveDate = it
                    }
                },
                itemToString = { it.tdEffectiveDate },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            // Summary Card
            selectedTaxType?.let { type ->
                if (type.taxTypeId != -1L) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUpdateTaxType(type) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "${stringResource(R.string.base_on)} ${TaxBasedOn.entries[type.ttBasedOn].basedOn}",
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            Text(
                text = stringResource(R.string.rates),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (taxRules.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.no_tax_rules_to_view),
                        modifier = Modifier.padding(32.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(taxRules) { rule ->
                        TaxRuleItem(rule, nf, onTaxRuleSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun TaxRuleItem(rule: WorkTaxRules, nf: NumberFunctions, onClick: (WorkTaxRules) -> Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable { onClick(rule) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "${stringResource(R.string.level)} ${rule.wtLevel}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = nf.getPercentStringFromDouble(rule.wtPercent),
                style = MaterialTheme.typography.bodyLarge
            )
            if (rule.wtHasExemption) {
                Text(
                    text = "${stringResource(R.string.exemption_)} ${
                        nf.displayDollarsWithoutZeros(
                            rule.wtExemptionAmount
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if (rule.wtHasBracket) {
                Text(
                    text = "${stringResource(R.string.upper_limit_)} ${
                        nf.displayDollarsWithoutZeros(
                            rule.wtBracketAmount
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}