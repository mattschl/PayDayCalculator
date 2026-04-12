package ms.mattschlenkrich.paycalculator.tax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.MainViewModel

class TaxRuleUpdateFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                TaxRuleUpdateScreen()
            }
        }
    }

    @Composable
    fun TaxRuleUpdateScreen() {
        val curTaxRule = mainViewModel.getTaxRule() ?: return

        var percentage by remember { mutableStateOf(nf.getPercentStringFromDouble(curTaxRule.wtPercent)) }
        var hasExemption by remember { mutableStateOf(curTaxRule.wtHasExemption) }
        var exemptionAmount by remember { mutableStateOf(nf.displayDollars(curTaxRule.wtExemptionAmount)) }
        var hasUpperLimit by remember { mutableStateOf(curTaxRule.wtHasBracket) }
        var upperLimit by remember { mutableStateOf(nf.displayDollars(curTaxRule.wtBracketAmount)) }

        TaxRuleScreen(
            title = getString(R.string.view_or_update_tax_rule),
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
                val message = validateTaxRule(
                    percentage,
                    hasExemption,
                    exemptionAmount,
                    hasUpperLimit,
                    upperLimit
                )
                if (message == ANSWER_OK) {
                    val updatedTaxRule = curTaxRule.copy(
                        wtPercent = nf.getDoubleFromPercentString(percentage),
                        wtHasExemption = hasExemption,
                        wtExemptionAmount = nf.getDoubleFromDollars(exemptionAmount),
                        wtHasBracket = hasUpperLimit,
                        wtBracketAmount = nf.getDoubleFromDollars(upperLimit),
                        wtUpdateTime = df.getCurrentTimeAsString()
                    )
                    mainActivity.workTaxViewModel.updateTaxRule(updatedTaxRule)
                    gotoTaxRulesFragment()
                } else {
                    displayError(message)
                }
            },
            onDeleteClick = {
                val deletedTaxRule = curTaxRule.copy(
                    wtIsDeleted = true,
                    wtUpdateTime = df.getCurrentTimeAsString()
                )
                mainActivity.workTaxViewModel.updateTaxRule(deletedTaxRule)
                gotoTaxRulesFragment()
            },
            onBackClick = { gotoTaxRulesFragment() }
        )
    }

    private fun validateTaxRule(
        percentage: String, hasExemption: Boolean, exemptionAmount: String,
        hasUpperLimit: Boolean, upperLimit: String
    ): String {
        if (percentage.isBlank() || nf.getDoubleFromDollarOrPercentString(percentage) == 0.0) {
            return getString(R.string.there_should_be_a_percentage_here)
        }
        if (hasExemption && (exemptionAmount.isBlank() || nf.getDoubleFromDollarOrPercentString(
                exemptionAmount
            ) == 0.0)
        ) {
            return getString(R.string.an_exemption_is_indicated_but_no_amount_was_entered)
        }
        if (hasUpperLimit && (upperLimit.isBlank() || nf.getDoubleFromDollarOrPercentString(
                upperLimit
            ) == 0.0)
        ) {
            return getString(R.string.an_upper_limit_is_indicated_but_no_amount_was_entered)
        }
        return ANSWER_OK
    }

    private fun displayError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun gotoTaxRulesFragment() {
        findNavController().navigate(
            TaxRuleUpdateFragmentDirections.actionTaxRuleUpdateFragmentToTaxRulesFragment()
        )
    }
}