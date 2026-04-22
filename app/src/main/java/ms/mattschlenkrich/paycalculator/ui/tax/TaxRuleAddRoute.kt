package ms.mattschlenkrich.paycalculator.ui.tax

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTaxRules
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel

@Composable
fun TaxRuleAddRoute(
    mainViewModel: MainViewModel,
    workTaxViewModel: WorkTaxViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val errorLabel = stringResource(R.string.error_)
    val errorMessages = mapOf(
        R.string.there_should_be_a_percentage_here to stringResource(R.string.there_should_be_a_percentage_here),
        R.string.an_exemption_is_indicated_but_no_amount_was_entered to stringResource(R.string.an_exemption_is_indicated_but_no_amount_was_entered),
        R.string.an_upper_limit_is_indicated_but_no_amount_was_entered to stringResource(R.string.an_upper_limit_is_indicated_but_no_amount_was_entered)
    )

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
                    errorLabel + (errorMessages[errorResId] ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        onBackClick = { navController.popBackStack() }
    )
}