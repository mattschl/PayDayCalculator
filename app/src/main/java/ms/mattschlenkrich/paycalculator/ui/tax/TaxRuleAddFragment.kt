package ms.mattschlenkrich.paycalculator.ui.tax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_TAX_RULES
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.tax.WorkTaxRules
import ms.mattschlenkrich.paycalculator.databinding.FragmentTaxRuleAddBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class TaxRuleAddFragment : Fragment(R.layout.fragment_tax_rule_add) {

    private var _binding: FragmentTaxRuleAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxRuleAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.add_tax_rule)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        binding.apply {
            tvTaxRuleType.text = mainActivity.mainViewModel.getTaxTypeString()
            tvEffectiveDate.text = mainActivity.mainViewModel.getEffectiveDateString()
            tvTaxRuleLevel.text = mainActivity.mainViewModel.getTaxLevel().toString()
        }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            chkExemption.setOnClickListener {
                if (chkExemption.isChecked) {
                    etExemption.visibility = View.VISIBLE
                } else {
                    etExemption.visibility = View.INVISIBLE
                    etExemption.setText(getString(R.string.zero_double))
                }
            }
            chkUpperLimit.setOnClickListener {
                if (chkUpperLimit.isChecked) {
                    etUpperLimit.visibility = View.VISIBLE
                } else {
                    etUpperLimit.visibility = View.INVISIBLE
                    etUpperLimit.setText(getString(R.string.zero_double))
                }
            }
        }
    }

    private fun setMenuActions() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        saveTaxRuleIfValid()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun saveTaxRuleIfValid() {
        binding.apply {
            val message = validateTaxRule()
            if (message == ANSWER_OK) {
                saveTaxRuleAndGotoCallingFragment()
            } else {
                displayError(message)
            }
        }
    }

    private fun displayError(message: String) {
        Toast.makeText(
            mView.context,
            getString(R.string.error_) + message,
            Toast.LENGTH_LONG
        ).show()
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
                nf.generateRandomIdAsLong(),
                tvTaxRuleType.text.toString(),
                tvTaxRuleLevel.text.toString().toInt(),
                tvEffectiveDate.text.toString(),
                nf.getDoubleFromPercentString(etPercentage.text.toString()),
                chkExemption.isChecked,
                if (chkExemption.isChecked)
                    nf.getDoubleFromDollars(etExemption.text.toString()) else 0.0,
                chkUpperLimit.isChecked,
                if (chkUpperLimit.isChecked)
                    nf.getDoubleFromDollars(etUpperLimit.text.toString()) else 0.0,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun saveTaxRuleAndGotoCallingFragment() {
        mainActivity.workTaxViewModel.insertTaxRule(
            getCurrentTaxRule()
        )
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        val callingFragment = mainActivity.mainViewModel.getCallingFragment()
        if (!callingFragment.isNullOrBlank()) {
            if (callingFragment.contains(FRAG_TAX_RULES)) {
                gotoTaxRulesFragment()
            }
        } else {
            gotoTaxRulesFragment()
        }
    }

    private fun gotoTaxRulesFragment() {
        mView.findNavController().navigate(
            TaxRuleAddFragmentDirections
                .actionTaxRuleAddFragmentToTaxRulesFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}