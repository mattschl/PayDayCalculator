package ms.mattschlenkrich.paydaycalculator.ui.tax

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
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_TAX_RULES
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTaxRuleAddBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules

class TaxRuleAddFragment : Fragment(R.layout.fragment_tax_rule_add) {

    private var _binding: FragmentTaxRuleAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val cf = CommonFunctions()

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
        fillMenu()
        fillValues()
        setCheckBoxActions()
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

    private fun fillValues() {
        binding.apply {
            tvTaxRuleType.text = mainActivity.mainViewModel.getTaxTypeString()
            tvEffectiveDate.text = mainActivity.mainViewModel.getEffectiveDateString()
            tvTaxRuleLevel.text = mainActivity.mainViewModel.getTaxLevel().toString()
        }
    }

    private fun fillMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        saveTaxRule()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun saveTaxRule() {
        binding.apply {
            val message = checkTaxRule()
            if (message == ANSWER_OK) {
                mainActivity.workTaxViewModel.insertTaxRule(
                    WorkTaxRules(
                        cf.generateId(),
                        tvTaxRuleType.text.toString(),
                        tvTaxRuleLevel.text.toString().toInt(),
                        wtEffectiveDate = tvEffectiveDate.text.toString(),
                        cf.getDoubleFromPercent(etPercentage.text.toString()),
                        chkExemption.isChecked,
                        if (chkExemption.isChecked)
                            cf.getDoubleFromDollars(etExemption.text.toString()) else 0.0,
                        chkUpperLimit.isChecked,
                        if (chkUpperLimit.isChecked)
                            cf.getDoubleFromDollars(etUpperLimit.text.toString()) else 0.0,
                        false,
                        df.getCurrentTimeAsString()
                    )
                )
                gotoCallingFragment()
            } else {
                Toast.makeText(
                    mView.context,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun gotoCallingFragment() {
        val callingFragment = mainActivity.mainViewModel.getCallingFragment()
        if (!callingFragment.isNullOrBlank()) {
            if (callingFragment.contains(FRAG_TAX_RULES)) {
                gotoTaxRules()
            }
        } else {
            gotoTaxRules()
        }

    }

    private fun gotoTaxRules() {
        mView.findNavController().navigate(
            TaxRuleAddFragmentDirections
                .actionTaxRuleAddFragmentToTaxRulesFragment()
        )
    }

    private fun checkTaxRule(): String {
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}