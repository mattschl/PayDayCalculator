package ms.mattschlenkrich.paycalculator.ui.tax

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.TaxTypes
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel

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
                    errorLabel + (errorMessages[errorResId] ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        onBackClick = { navController.popBackStack() }
    )
}