package ms.mattschlenkrich.paycalculator.ui.tax

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.WorkTaxRules
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.ui.tax.composable.TaxRuleScreen

@Composable
fun TaxRuleRoute(
    mainViewModel: MainViewModel,
    workTaxViewModel: WorkTaxViewModel,
    navController: NavController,
    isUpdate: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val errorLabel = stringResource(R.string.prefix_error)
    val errorMessages = mapOf(
        R.string.there_should_be_a_percentage_here to stringResource(R.string.there_should_be_a_percentage_here),
        R.string.an_exemption_is_indicated_but_no_amount_was_entered to stringResource(R.string.an_exemption_is_indicated_but_no_amount_was_entered),
        R.string.an_upper_limit_is_indicated_but_no_amount_was_entered to stringResource(R.string.an_upper_limit_is_indicated_but_no_amount_was_entered)
    )

    val curTaxRule = if (isUpdate) mainViewModel.getTaxRule() else null
    if (isUpdate && curTaxRule == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val taxType = curTaxRule?.wtType ?: mainViewModel.getTaxTypeString() ?: ""
    val taxLevel = curTaxRule?.wtLevel?.toString() ?: mainViewModel.getTaxLevel().toString()
    val effectiveDate = curTaxRule?.wtEffectiveDate ?: mainViewModel.getEffectiveDateString() ?: ""

    var percentage by remember {
        mutableStateOf(
            if (isUpdate) nf.getPercentStringFromDouble(curTaxRule!!.wtPercent) else ""
        )
    }
    var hasExemption by remember {
        mutableStateOf(
            if (isUpdate) curTaxRule!!.wtHasExemption else false
        )
    }
    var exemptionAmount by remember {
        mutableStateOf(
            if (isUpdate) nf.displayDollars(curTaxRule!!.wtExemptionAmount) else ""
        )
    }
    var hasUpperLimit by remember {
        mutableStateOf(
            if (isUpdate) curTaxRule!!.wtHasBracket else false
        )
    }
    var upperLimit by remember {
        mutableStateOf(
            if (isUpdate) nf.displayDollars(curTaxRule!!.wtBracketAmount) else ""
        )
    }

    TaxRuleScreen(
        title = if (isUpdate) stringResource(R.string.view_or_update_tax_rule)
        else stringResource(R.string.add_tax_rule),
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
                val taxRule = if (isUpdate) {
                    curTaxRule!!.copy(
                        wtPercent = nf.getDoubleFromPercentString(percentage),
                        wtHasExemption = hasExemption,
                        wtExemptionAmount = nf.getDoubleFromDollars(exemptionAmount),
                        wtHasBracket = hasUpperLimit,
                        wtBracketAmount = nf.getDoubleFromDollars(upperLimit),
                        wtUpdateTime = df.getCurrentUTCTimeAsString()
                    )
                } else {
                    WorkTaxRules(
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
                }
                coroutineScope.launch {
                    if (isUpdate) {
                        workTaxViewModel.updateTaxRule(taxRule)
                    } else {
                        workTaxViewModel.insertTaxRule(taxRule)
                    }
                    navController.popBackStack()
                }
            } else {
                Toast.makeText(
                    context,
                    errorLabel + (errorMessages[errorResId] ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        onDeleteClick = if (isUpdate) {
            {
                coroutineScope.launch {
                    workTaxViewModel.updateTaxRule(
                        curTaxRule!!.copy(
                            wtIsDeleted = true,
                            wtUpdateTime = df.getCurrentUTCTimeAsString()
                        )
                    )
                    navController.popBackStack()
                }
            }
        } else null,
        onBackClick = { navController.popBackStack() }
    )
}