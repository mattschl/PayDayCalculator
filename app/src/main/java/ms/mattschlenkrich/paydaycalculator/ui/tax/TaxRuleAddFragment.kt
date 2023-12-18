package ms.mattschlenkrich.paydaycalculator.ui.tax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTaxRuleAddBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules

class TaxRuleAddFragment : Fragment(R.layout.fragment_tax_rule_add) {

    private var _binding: FragmentTaxRuleAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val cf = CommonFunctions()
    private val taxRuleList = ArrayList<WorkTaxRules>()
    private lateinit var effectiveDate: String

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
        getTaxRuleList()
        fillMenu()
        effectiveDate = df.getFirstDayOfYear(df.getCurrentDateAsString())
    }

    private fun getTaxRuleList() {
        mainActivity.workTaxViewModel.getTaxRules().observe(
            viewLifecycleOwner
        ) { taxRules ->
            taxRuleList.clear()
            taxRules.listIterator().forEach {
                taxRuleList.add(it)
            }
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
//                mainActivity.workTaxViewModel.insertTaxRule(
//                    WorkTaxRules(
//                        workTaxRuleId = cf.generateId(),
//                        wtName = etTaxRuleName.text.toString(),
//                        wtType = spTaxType.selectedItem.toString(),
//                        wtPercent = cf.getDoubleFromPercent(etPercentage.text.toString()),
//                        wtHasExemption = chkExemption.isChecked,
//                        wtExemptionAmount = if (chkExemption.isChecked)
//                            cf.getDoubleFromDollars(etExemption.text.toString()) else 0.0,
//                        wtHasBracket = chkUpperLimit.isChecked,
//                        wtBracketAmount = if (chkUpperLimit.isChecked)
//                            cf.getDoubleFromDollars(etUpperLimit.text.toString()) else 0.0,
//                        wtEffectiveDate = etEffectiveDate.text.toString(),
//                        wtIsDeleted = false,
//                        wtUpdateTime = df.getCurrentTimeAsString()
//                    )
//                )
            }
        }
    }

    private fun checkTaxRule(): String {
        binding.apply {
            val errorMessage = if (etEffectiveDate.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "There has to be an effective date!"
            } else if (etPercentage.text.isNullOrBlank()) {
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