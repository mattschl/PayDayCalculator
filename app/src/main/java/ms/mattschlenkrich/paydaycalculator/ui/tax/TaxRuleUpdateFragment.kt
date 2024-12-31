package ms.mattschlenkrich.paydaycalculator.ui.tax

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
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.tax.WorkTaxRules
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTaxRuleUpdateBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity


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
            updateTaxRule()
            gotoTaxRulesFragment()
        } else {
            Toast.makeText(
                mView.context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun validateTaxRule(): String {
        binding.apply {
            val errorMessage = if (etPercentage.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "There should be a percentage here!"
            } else if (etExemption.text.isNullOrBlank() &&
                chkExemption.isChecked
            ) {
                "    ERROR!!\n" +
                        "An exemption is indicated but no amount was entered!"
            } else if (etUpperLimit.text.isNullOrBlank() &&
                chkUpperLimit.isChecked
            ) {
                "    ERROR!!\n" +
                        "An upper limit is indicated but no amount was entered!"
            } else {
                ANSWER_OK
            }
            return errorMessage
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

    private fun updateTaxRule() {
        mainActivity.workTaxViewModel.updateTaxRule(
            getCurrentTaxRule()
        )
    }

    private fun deleteTaxRule() {
        Toast.makeText(
            mView.context,
            "This cannot be deleted!",
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