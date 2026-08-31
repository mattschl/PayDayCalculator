package ms.mattschlenkrich.paycalculator.ui.tax

import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.ConfirmationBottomSheet
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
    val errorLabel = stringResource(R.string.prefix_error)
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

    ConfirmationBottomSheet(
        showDialog = showNextStepDialog && savedTaxType != null,
        onDismissRequest = { /* Prevent dismiss? No, let's allow it for consistency */ },
        title = "${stringResource(R.string.choose_next_steps_for)} ${savedTaxType?.taxType}",
        message = stringResource(R.string.the_tax_type_has_been_added_but_there_are_no_rules_yet_),
        onConfirm = {
            mainViewModel.setTaxType(savedTaxType)
            mainViewModel.setTaxTypeString(savedTaxType!!.taxType)
            navController.navigate(Screen.TaxRuleAdd.route) {
                popUpTo(Screen.TaxTypeAdd.route) { inclusive = true }
            }
        },
        content = {
            /* If they choose 'No' they just go back to the list */
        }
    )

    // Overriding the confirm/dismiss logic to match original intent
    if (showNextStepDialog && savedTaxType != null) {
        ConfirmationBottomSheet(
            showDialog = showNextStepDialog,
            onDismissRequest = {
                mainViewModel.setTaxType(savedTaxType)
                mainViewModel.setTaxTypeString(savedTaxType!!.taxType)
                navController.popBackStack()
            },
            title = "${stringResource(R.string.choose_next_steps_for)} ${savedTaxType?.taxType}",
            message = stringResource(R.string.the_tax_type_has_been_added_but_there_are_no_rules_yet_),
            confirmButtonText = stringResource(R.string.label_yes),
            dismissButtonText = stringResource(R.string.label_no),
            onConfirm = {
                mainViewModel.setTaxType(savedTaxType)
                mainViewModel.setTaxTypeString(savedTaxType!!.taxType)
                navController.navigate(Screen.TaxRuleAdd.route) {
                    popUpTo(Screen.TaxTypeAdd.route) { inclusive = true }
                }
            }
        )
    } else {
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
}