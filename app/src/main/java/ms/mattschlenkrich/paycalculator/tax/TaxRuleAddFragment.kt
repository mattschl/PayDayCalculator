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
import ms.mattschlenkrich.paycalculator.data.WorkTaxRules

class TaxRuleAddFragment : Fragment() {

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
                TaxRuleAddScreen()
            }
        }
    }

    @Composable
    fun TaxRuleAddScreen() {
        val taxType = mainViewModel.getTaxTypeString() ?: ""
        val taxLevel = mainViewModel.getTaxLevel().toString()
        val effectiveDate = mainViewModel.getEffectiveDateString() ?: ""

        var percentage by remember { mutableStateOf("") }
        var hasExemption by remember { mutableStateOf(false) }
        var exemptionAmount by remember { mutableStateOf("") }
        var hasUpperLimit by remember { mutableStateOf(false) }
        var upperLimit by remember { mutableStateOf("") }

        TaxRuleScreen(
            title = getString(R.string.add_tax_rule),
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
                val message = validateTaxRule(
                    percentage,
                    hasExemption,
                    exemptionAmount,
                    hasUpperLimit,
                    upperLimit
                )
                if (message == ANSWER_OK) {
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
                        df.getCurrentTimeAsString()
                    )
                    mainActivity.workTaxViewModel.insertTaxRule(taxRule)
                    gotoCallingFragment()
                } else {
                    displayError(getString(R.string.error_) + message)
                }
            },
            onBackClick = { gotoCallingFragment() }
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

    private fun gotoCallingFragment() {
        findNavController().navigate(
            TaxRuleAddFragmentDirections.actionTaxRuleAddFragmentToTaxRulesFragment()
        )
    }
}