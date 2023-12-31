package ms.mattschlenkrich.paydaycalculator.ui.tax

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.TaxRuleAdapter
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_TAX_RULES
import ms.mattschlenkrich.paydaycalculator.common.WAIT_500
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTaxRulesBinding
import ms.mattschlenkrich.paydaycalculator.model.TaxEffectiveDates
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules

const val TAG = FRAG_TAX_RULES

class TaxRulesFragment : Fragment(R.layout.fragment_tax_rules) {

    private var _binding: FragmentTaxRulesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val cf = CommonFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxRulesBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.view_universal_tax_rules)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActions()
        fillTaxTypes()
        fillEffectiveDates()
        selectTaxType()
        selectEffectiveDate()
        fillValues()
    }

    private fun fillValues() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_500)
            binding.apply {
                if (mainActivity.mainViewModel.getTaxTypeString() != null) {
                    for (i in 0 until spTaxType.adapter.count) {
                        if (spTaxType.getItemAtPosition(i) ==
                            mainActivity.mainViewModel.getTaxTypeString()!!
                        ) {
                            spTaxType.setSelection(i)
                            break
                        }
                    }
                }
            }
        }
    }

    private fun selectEffectiveDate() {
        binding.apply {
            spEffectiveDate.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (spEffectiveDate.selectedItem.toString() ==
                            getString(R.string.add_new_effective_date) &&
                            spTaxType.selectedItem.toString() !=
                            getString(R.string.add_a_new_tax_type)
                        ) {
                            getNewEffectiveDate()
                        } else {
                            fillTaxRuleList()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
    }

    private fun getNewEffectiveDate() {
        val curDateAll = df.getCurrentDateAsString()
            .split("-")
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                val display = "$year-${
                    month.toString()
                        .padStart(2, '0')
                }-${
                    dayOfMonth.toString().padStart(2, '0')
                }"
                mainActivity.workTaxViewModel.insertEffectiveDate(
                    TaxEffectiveDates(
                        display,
                        cf.generateId(),
                        false,
                        df.getCurrentTimeAsString()
                    )
                )
                fillTaxRuleList()
            },
            curDateAll[0].toInt(),
            curDateAll[1].toInt() - 1,
            curDateAll[2].toInt()
        )
        datePickerDialog.setTitle(getString(R.string.choose_a_universal_effective_date))
        datePickerDialog.show()
    }

    private fun selectTaxType() {
        binding.apply {
            spTaxType.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (spTaxType.selectedItem.toString() ==
                            getString(R.string.add_a_new_tax_type)
                        ) {
                            gotoTaxTypeAdd()
                        } else {
                            fillTaxRuleList()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
    }

    private fun fillEffectiveDates() {
        val dateAdapter = ArrayAdapter<Any>(
            mView.context,
            R.layout.spinner_item_bold
        )
        mainActivity.workTaxViewModel.getTaxEffectiveDates().observe(
            viewLifecycleOwner
        ) { dates ->
            dateAdapter.clear()
            dates.listIterator().forEach {
                dateAdapter.add(it.tdEffectiveDate)
            }
            dateAdapter.add(getString(R.string.add_new_effective_date))
        }
        binding.spEffectiveDate.adapter = dateAdapter
    }

    private fun fillTaxTypes() {
        val taxTypeAdapter = ArrayAdapter<Any>(
            mView.context,
            R.layout.spinner_item_bold
        )
        mainActivity.workTaxViewModel.getTaxTypes().observe(
            viewLifecycleOwner
        ) { types ->
            taxTypeAdapter.clear()
            types.listIterator().forEach {
                taxTypeAdapter.add(it.taxType)
            }
            taxTypeAdapter.add(getString(R.string.add_a_new_tax_type))
        }
        binding.spTaxType.adapter = taxTypeAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fillTaxRuleList() {
        binding.apply {
            if (spTaxType.adapter.count > 1 &&
                spEffectiveDate.adapter.count > 1
            ) {
                val taxRuleAdapter = TaxRuleAdapter(
                    mainActivity, mView
                )
                rvTaxRules.apply {
                    layoutManager = StaggeredGridLayoutManager(
                        2,
                        StaggeredGridLayoutManager.VERTICAL
                    )
                    setHasFixedSize(true)
                    adapter = taxRuleAdapter
                }
                activity.let {
                    mainActivity.workTaxViewModel.getTaxRules(
                        spTaxType.selectedItem.toString(),
                        spEffectiveDate.selectedItem.toString()
                    ).observe(
                        viewLifecycleOwner
                    ) { taxRules ->
                        rvTaxRules.adapter!!.notifyDataSetChanged()
                        taxRuleAdapter.differ.submitList(taxRules)
                        updateUI(taxRules)
                    }
                }
            } else {
                rvTaxRules.adapter = null
                updateUI(null)
            }
        }
    }

    private fun updateUI(workTaxRules: List<WorkTaxRules>?) {
        binding.apply {
            if (workTaxRules.isNullOrEmpty()) {
                rvTaxRules.visibility = View.GONE
                crdNoInfo.visibility = View.VISIBLE
            } else {
                rvTaxRules.visibility = View.VISIBLE
                crdNoInfo.visibility = View.GONE
            }
        }
    }

    private fun setActions() {
        binding.fabNew.setOnClickListener {
            gotoTaxRuleAdd()
        }
    }

    private fun gotoTaxRuleAdd() {
        binding.apply {
            mainActivity.mainViewModel.setTaxTypeString(
                spTaxType.selectedItem.toString()
            )
            mainActivity.mainViewModel.setEffectiveDateString(
                spEffectiveDate.selectedItem.toString()
            )
            if (rvTaxRules.adapter != null) {
                mainActivity.mainViewModel.setTaxLevel(
                    rvTaxRules.adapter!!.itemCount
                )
            } else {
                mainActivity.mainViewModel.setTaxLevel(0)
            }
        }
        mView.findNavController().navigate(
            TaxRulesFragmentDirections
                .actionTaxRulesFragmentToTaxRuleAddFragment()
        )
    }

    private fun gotoTaxTypeAdd() {
        mainActivity.mainViewModel.addCallingFragment(TAG)
        mView.findNavController().navigate(
            TaxRulesFragmentDirections
                .actionTaxRulesFragmentToTaxTypeAddFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}