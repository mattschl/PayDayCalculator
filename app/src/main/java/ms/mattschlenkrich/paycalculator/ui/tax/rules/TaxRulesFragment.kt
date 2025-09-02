package ms.mattschlenkrich.paycalculator.ui.tax.rules

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
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_TAX_RULES
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_1000
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.tax.TaxEffectiveDates
import ms.mattschlenkrich.paycalculator.database.model.tax.WorkTaxRules
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentTaxRulesBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.tax.rules.adapter.TaxRuleAdapter

private const val TAG = FRAG_TAX_RULES

class TaxRulesFragment : Fragment(R.layout.fragment_tax_rules) {

    private var _binding: FragmentTaxRulesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workTaxViewModel: WorkTaxViewModel
    private val df = DateFunctions()
    private val cf = NumberFunctions()
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxRulesBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workTaxViewModel = mainActivity.workTaxViewModel
        mainActivity.title = getString(R.string.view_universal_tax_rules)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }


    private fun populateValues() {
        populateTaxTypes()
        populateEffectiveDates()
        mainScope.launch {
            delay(WAIT_1000)
            binding.apply {
                if (mainViewModel.getTaxTypeString() != null) {
                    for (i in 0 until spTaxType.adapter.count) {
                        if (spTaxType.getItemAtPosition(i) == mainActivity.mainViewModel.getTaxTypeString()!!) {
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
            spEffectiveDate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (spEffectiveDate.selectedItem.toString() == getString(R.string.add_new_effective_date) && spTaxType.selectedItem.toString() != getString(
                            R.string.add_a_new_tax_type
                        )
                    ) {
                        chooseNewEffectiveDate()
                    } else {
                        populateTaxRuleList()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //not needed
                }
            }
        }
    }

    private fun populateSummary() {
        binding.apply {
            workTaxViewModel.findTaxType(spTaxType.selectedItem.toString()).observe(
                viewLifecycleOwner
            ) { type ->
                val display =
                    "${getString(R.string.base_on)} " + resources.getStringArray(R.array.tax_based_on)[type.ttBasedOn]
                tvTaxSummary.text = display
            }
        }
    }

    private fun populateEffectiveDates() {
        val dateAdapter = ArrayAdapter<Any>(
            mView.context, R.layout.spinner_item_bold
        )
        workTaxViewModel.getTaxEffectiveDates().observe(viewLifecycleOwner) { dates ->
            dateAdapter.clear()
            dates.listIterator().forEach {
                dateAdapter.add(it.tdEffectiveDate)
            }
            dateAdapter.add(getString(R.string.add_new_effective_date))
        }
        binding.spEffectiveDate.adapter = dateAdapter
    }

    private fun populateTaxTypes() {
        val taxTypeAdapter = ArrayAdapter<Any>(
            mView.context, R.layout.spinner_item_bold
        )
        workTaxViewModel.getTaxTypes().observe(viewLifecycleOwner) { types ->
            taxTypeAdapter.clear()
            types.listIterator().forEach {
                taxTypeAdapter.add(it.taxType)
            }
            taxTypeAdapter.add(getString(R.string.add_a_new_tax_type))
        }
        binding.spTaxType.adapter = taxTypeAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun populateTaxRuleList() {
        mainScope.launch {
            delay(WAIT_250)
            binding.apply {
                if (spTaxType.adapter.count > 1 && spEffectiveDate.adapter.count > 1) {
                    val taxRuleAdapter = TaxRuleAdapter(
                        mainActivity, mView, this@TaxRulesFragment,
                    )
                    rvTaxRules.apply {
                        layoutManager = GridLayoutManager(
                            mView.context, 2, GridLayoutManager.VERTICAL, false
                        )
                        setHasFixedSize(true)
                        adapter = taxRuleAdapter
                    }
                    workTaxViewModel.getTaxRules(
                        spTaxType.selectedItem.toString(), spEffectiveDate.selectedItem.toString()
                    ).observe(viewLifecycleOwner) { taxRules ->
                        rvTaxRules.adapter!!.notifyDataSetChanged()
                        taxRuleAdapter.differ.submitList(taxRules)
                        updateUI(taxRules)
                    }
                } else {
                    rvTaxRules.adapter = null
                    updateUI(null)
                }
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

    private fun setClickActions() {
        selectTaxType()
        selectEffectiveDate()
        binding.apply {
            fabNew.setOnClickListener {
                gotoTaxRuleAdd()
            }
            crdSummary.setOnClickListener {
                gotoTaxTypeUpdate()
            }
        }
    }

    private fun chooseNewEffectiveDate() {
        val curDateAll = df.getCurrentDateAsString().split("-")
        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                val display = "$year-${
                    month.toString().padStart(2, '0')
                }-${
                    dayOfMonth.toString().padStart(2, '0')
                }"
                mainActivity.workTaxViewModel.insertEffectiveDate(
                    TaxEffectiveDates(
                        display, cf.generateRandomIdAsLong(), false, df.getCurrentTimeAsString()
                    )
                )
                populateTaxRuleList()
            }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
        )
        datePickerDialog.setTitle(getString(R.string.choose_a_universal_effective_date))
        datePickerDialog.show()
    }

    private fun selectTaxType() {
        binding.apply {
            spTaxType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (spTaxType.selectedItem.toString() == getString(R.string.add_a_new_tax_type)) {
                        gotoTaxTypeAdd()
                    } else {
                        populateSummary()
                        populateTaxRuleList()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //not needed
                }
            }
        }
    }

    private fun gotoTaxTypeUpdate() {
        binding.apply {
            workTaxViewModel.findTaxType(spTaxType.selectedItem.toString()).observe(
                viewLifecycleOwner
            ) { type ->
                mainViewModel.setTaxType(
                    type
                )
            }
        }
        mainViewModel.setCallingFragment(TAG)
        gotoTaxTypeUpdateFragment()
    }

    private fun gotoTaxTypeUpdateFragment() {
        mView.findNavController().navigate(
            TaxRulesFragmentDirections.actionTaxRulesFragmentToTaxTypeUpdateFragment()
        )
    }

    private fun gotoTaxRuleAdd() {
        binding.apply {
            mainViewModel.setTaxTypeString(
                spTaxType.selectedItem.toString()
            )
            mainViewModel.setEffectiveDateString(
                spEffectiveDate.selectedItem.toString()
            )
            if (rvTaxRules.adapter != null) {
                mainViewModel.setTaxLevel(
                    rvTaxRules.adapter!!.itemCount
                )
            } else {
                mainViewModel.setTaxLevel(0)
            }
            mainViewModel.addCallingFragment(TAG)
        }
        gotoTaxRuleAddFragment()
    }

    private fun gotoTaxRuleAddFragment() {
        mView.findNavController().navigate(
            TaxRulesFragmentDirections.actionTaxRulesFragmentToTaxRuleAddFragment()
        )
    }

    private fun gotoTaxTypeAdd() {
        mainViewModel.addCallingFragment(TAG)
        gotoTaxTypeAddFragment()
    }

    private fun gotoTaxTypeAddFragment() {
        mView.findNavController().navigate(
            TaxRulesFragmentDirections.actionTaxRulesFragmentToTaxTypeAddFragment()
        )
    }

    fun gotoTaxRuleUpdateFragment() {
        mView.findNavController().navigate(
            TaxRulesFragmentDirections.actionTaxRulesFragmentToTaxRuleUpdateFragment()
        )
    }

    override fun onStop() {
        mainScope.cancel()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}