package ms.mattschlenkrich.paycalculator.ui.tax

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.TaxTypes
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.ui.tax.composable.TaxTypeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxTypeAddRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workTaxViewModel: WorkTaxViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val errorLabel = stringResource(R.string.error_)
    val errorMessages = mapOf(
        R.string.the_tax_type_must_have_a_name to stringResource(R.string.the_tax_type_must_have_a_name),
        R.string.this_tax_type_already_exists to stringResource(R.string.this_tax_type_already_exists)
    )

    var taxTypeState by remember { mutableStateOf("") }
    var selectedBasedOn by remember { mutableIntStateOf(0) }
    var showNextStepDialog by remember { mutableStateOf(false) }
    var savedTaxType by remember { mutableStateOf<TaxTypes?>(null) }

    val taxTypeList by workTaxViewModel.getTaxTypes().observeAsState(emptyList())
    val employers by employerViewModel.getEmployers().observeAsState(emptyList())

    if (showNextStepDialog && savedTaxType != null) {
        ModalBottomSheet(
            onDismissRequest = { /* Prevent dismiss */ },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.choose_next_steps_for) + savedTaxType!!.taxType,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(stringResource(R.string.the_tax_type_has_been_added_but_there_are_no_rules_yet_))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        mainViewModel.setTaxType(savedTaxType)
                        mainViewModel.setTaxTypeString(savedTaxType!!.taxType)
                        navController.popBackStack()
                    }) {
                        Text(stringResource(R.string.no))
                    }
                    TextButton(onClick = {
                        mainViewModel.setTaxType(savedTaxType)
                        mainViewModel.setTaxTypeString(savedTaxType!!.taxType)
                        navController.navigate(Screen.TaxRuleAdd.route) {
                            popUpTo(Screen.TaxTypeAdd.route) { inclusive = true }
                        }
                    }) {
                        Text(stringResource(R.string.yes))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    TaxTypeScreen(
        taxType = taxTypeState,
        onTaxTypeChange = { taxTypeState = it },
        selectedBasedOn = selectedBasedOn,
        onBasedOnChange = { selectedBasedOn = it },
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
                coroutineScope.launch {
                    workTaxViewModel.insertTaxTypeWithEmployerLinks(
                        taxType,
                        employers,
                        df.getCurrentUTCTimeAsString()
                    )
                    savedTaxType = taxType
                    showNextStepDialog = true
                }
            } else {
                Toast.makeText(
                    context,
                    errorLabel + (errorMessages[errorResId] ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )
}