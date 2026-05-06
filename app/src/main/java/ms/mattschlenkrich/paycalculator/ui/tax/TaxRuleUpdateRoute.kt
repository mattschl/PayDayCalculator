package ms.mattschlenkrich.paycalculator.ui.tax

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.ui.tax.composable.TaxRuleScreen

@Composable
fun TaxRuleUpdateRoute(
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
                    errorLabel + (errorMessages[errorResId] ?: ""),
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