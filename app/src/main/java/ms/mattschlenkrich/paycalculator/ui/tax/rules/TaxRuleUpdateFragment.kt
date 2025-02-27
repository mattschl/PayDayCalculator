package ms.mattschlenkrich.paycalculator.ui.tax.rules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.tax.WorkTaxRules
import ms.mattschlenkrich.paycalculator.databinding.FragmentTaxRuleUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity


class TaxRuleUpdateFragment : Fragment(R.layout.fragment_tax_rule_update) {

    private var _binding: FragmentTaxRuleUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private var curTaxRule: WorkTaxRules? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxRuleUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.view_or_update_tax_rule)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        binding.apply {
            if (mainActivity.mainViewModel.getTaxRule() != null) {
                curTaxRule = mainActivity.mainViewModel.getTaxRule()
                tvTaxRuleType.text = curTaxRule!!.wtType
                tvTaxRuleLevel.text = String.format(curTaxRule!!.wtLevel.toString())
                tvEffectiveDate.text = curTaxRule!!.wtEffectiveDate
                etPercentage.setText(nf.getPercentStringFromDouble(curTaxRule!!.wtPercent))
                chkExemption.isChecked = curTaxRule!!.wtHasExemption
                if (chkExemption.isChecked) etExemption.visibility = View.VISIBLE
                etExemption.setText(nf.displayDollars(curTaxRule!!.wtExemptionAmount))
                chkUpperLimit.isChecked = curTaxRule!!.wtHasBracket
                if (chkUpperLimit.isChecked) etUpperLimit.visibility = View.VISIBLE
                etUpperLimit.setText(nf.displayDollars(curTaxRule!!.wtBracketAmount))
            }
        }
    }

    private fun setClickActions() {
        setMenuActions()
        setCheckBoxActions()
        binding.fabDone.setOnClickListener {
            updateTaxRuleIfValid()
        }
    }

    private fun setMenuActions() {
        mainActivity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_delete, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        deleteTaxRule()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun setCheckBoxActions() {
        binding.apply {
            chkExemption.setOnClickListener {
                if (chkExemption.isChecked) {
                    etExemption.visibility = View.VISIBLE
                } else {
                    etExemption.visibility = View.INVISIBLE
                    etExemption.setText("0.0")
                }
            }
            chkUpperLimit.setOnClickListener {
                if (chkUpperLimit.isChecked) {
                    etUpperLimit.visibility = View.VISIBLE
                } else {
                    etUpperLimit.visibility = View.INVISIBLE
                    etUpperLimit.setText("0.0")
                }
            }
        }
    }

    private fun updateTaxRuleIfValid() {
        val message = validateTaxRule()
        if (message == ANSWER_OK) {
            updateTaxRuleAndGotoCallingFragment()
        } else {
            displayError(message)
        }
    }

    private fun validateTaxRule(): String {
        binding.apply {
            if (etPercentage.text.isNullOrBlank() ||
                nf.getDoubleFromDollarOrPercentString(etPercentage.text.toString()) == 0.0
            ) {
                return getString(R.string.there_should_be_a_percentage_here)
            }
            if ((etExemption.text.isNullOrBlank() ||
                        nf.getDoubleFromDollarOrPercentString(etExemption.text.toString()) == 0.0) &&
                chkExemption.isChecked
            ) {
                return getString(R.string.an_exemption_is_indicated_but_no_amount_was_entered)
            }
            if ((etUpperLimit.text.isNullOrBlank() ||
                        nf.getDoubleFromDollarOrPercentString(etUpperLimit.text.toString()) == 0.0) &&
                chkUpperLimit.isChecked
            ) {
                return getString(R.string.an_upper_limit_is_indicated_but_no_amount_was_entered)
            }
            return ANSWER_OK
        }
    }

    private fun getCurrentTaxRule(): WorkTaxRules {
        binding.apply {
            return WorkTaxRules(
                curTaxRule!!.workTaxRuleId,
                curTaxRule!!.wtType,
                curTaxRule!!.wtLevel,
                curTaxRule!!.wtEffectiveDate,
                nf.getDoubleFromPercentString(etPercentage.text.toString()),
                chkExemption.isChecked,
                nf.getDoubleFromDollars(etExemption.text.toString()),
                chkUpperLimit.isChecked,
                nf.getDoubleFromDollars(etUpperLimit.text.toString()),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun updateTaxRuleAndGotoCallingFragment() {
        mainActivity.workTaxViewModel.updateTaxRule(
            getCurrentTaxRule()
        )
        gotoTaxRulesFragment()
    }

    private fun deleteTaxRule() {
        displayError(getString(R.string.this_cannot_be_deleted))
    }

    private fun displayError(message: String) {
        Toast.makeText(
            mView.context,
            getString(R.string.error_) + message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun gotoTaxRulesFragment() {
        mView.findNavController().navigate(
            TaxRuleUpdateFragmentDirections
                .actionTaxRuleUpdateFragmentToTaxRulesFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}