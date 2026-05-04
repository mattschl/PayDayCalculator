package ms.mattschlenkrich.paycalculator.ui.tax

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel

@Composable
fun TaxTypeUpdateRoute(
    mainViewModel: MainViewModel,
    workTaxViewModel: WorkTaxViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val errorLabel = stringResource(R.string.error_)
    val errorMessages = mapOf(
        R.string.the_tax_type_must_have_a_name to stringResource(R.string.the_tax_type_must_have_a_name),
        R.string.this_tax_type_already_exists to stringResource(R.string.this_tax_type_already_exists)
    )

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
                    errorLabel + (errorMessages[errorResId] ?: ""),
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