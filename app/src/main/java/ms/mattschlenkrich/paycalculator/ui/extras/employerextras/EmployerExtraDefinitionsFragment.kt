package ms.mattschlenkrich.paycalculator.ui.extras.employerextras

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.FRAG_EXTRA_DEFINITIONS
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.databinding.FragmentEmployerExtraDefinitionsBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.extras.adapter.EmployerExtraDefinitionFullAdapter

private const val TAG = FRAG_EXTRA_DEFINITIONS

class EmployerExtraDefinitionsFragment : Fragment(R.layout.fragment_employer_extra_definitions),
    IEmployerExtraDefinitionsFragment {

    private var _binding: FragmentEmployerExtraDefinitionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var employerList: List<Employers>
    private lateinit var extraTypeList: List<WorkExtraTypes>
    private var curEmployer: Employers? = null
    private var curExtraType: WorkExtraTypes? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerExtraDefinitionsBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.view_extra_pay_items)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populateEmployersSpinner()
        mainActivity.mainViewModel.removeCallingFragment(TAG)
        CoroutineScope(Dispatchers.Main).launch {
            binding.apply {
                delay(WAIT_250)
                setSelectionToEmployerFoundInList()
                delay(WAIT_250)
                setSelectionToExtraTypeFoundInList()
            }
        }
    }

    private fun populateExtraTypeInfo() {
        if (curExtraType != null) {
            binding.apply {
                if (curExtraType!!.wetIsDeleted) {
                    tvAppliesTo.text = getString(R.string.deleted)
                    tvAppliesTo.setTextColor(Color.RED)
                    tvAttachTo.visibility = View.GONE
                    tvDefault.visibility = View.GONE
                    tvCredit.visibility = View.GONE
                } else {
                    tvAppliesTo.setTextColor(Color.BLACK)
                    tvAttachTo.visibility = View.VISIBLE
                    tvDefault.visibility = View.VISIBLE
                    tvCredit.visibility = View.VISIBLE
                    var display = getString(R.string.calculated) +
                            resources.getStringArray(
                                R.array.applies_to_frequencies
                            )[curExtraType!!.wetAppliesTo]
                    tvAppliesTo.text = display
                    display = getString(R.string.attaches_to) +
                            resources.getStringArray(
                                R.array.attach_to_frequencies
                            )[curExtraType!!.wetAttachTo]
                    tvAttachTo.text = display
                    display = getString(R.string.this_is_a) +
                            if (curExtraType!!.wetIsCredit) {
                                getString(R.string.credit)
                            } else {
                                getString(R.string.deduction)
                            }
                    tvCredit.text = display
                    display = getString(R.string.applied) +
                            if (curExtraType!!.wetIsDefault) {
                                getString(R.string.by_default)
                            } else {
                                getString(R.string.manually)
                            }
                    tvDefault.text = display
                }
            }
        }
    }

    private fun populateExtraTypeSpinner() {
        if (curEmployer != null) {
            binding.apply {
                val extraAdapter = ArrayAdapter<Any>(
                    mView.context, R.layout.spinner_item_bold
                )
                mainActivity.workExtraViewModel.getExtraDefTypes(curEmployer!!.employerId)
                    .observe(viewLifecycleOwner) { extraTypes ->
                        extraTypeList = extraTypes
                        extraTypes.listIterator().forEach {
                            extraAdapter.add(it.wetName)
                        }
                        extraAdapter.add(getString(R.string.add_a_new_extra_type))
                        updateUI(employerList, extraTypeList)
                    }
                spExtraType.adapter = extraAdapter
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun populateExtrasList() {
        if (curEmployer != null && curExtraType != null) {
            binding.apply {
                val extraDefinitionAdapter = EmployerExtraDefinitionFullAdapter(
                    mainActivity, mView,
                    this@EmployerExtraDefinitionsFragment,
                    null
                )
                rvExtras.apply {
                    layoutManager = GridLayoutManager(
                        mView.context,
                        2,
                        GridLayoutManager.VERTICAL,
                        false
                    )
                    setHasFixedSize(true)
                    adapter = extraDefinitionAdapter
                }
                activity.let {
                    mainActivity.workExtraViewModel.getActiveExtraDefinitionsFull(
                        curEmployer!!.employerId, curExtraType!!.workExtraTypeId
                    ).observe(
                        viewLifecycleOwner
                    ) { extras ->
                        extraDefinitionAdapter.notifyDataSetChanged()
                        extraDefinitionAdapter.differ.submitList(extras)
                        updateExtrasUI(extras)
                    }
                }
            }
        }
    }

    private fun populateEmployersSpinner() {
        val employerAdapter = ArrayAdapter<Any>(
            mView.context,
            R.layout.spinner_item_bold
        )
        mainActivity.employerViewModel.getEmployers().observe(
            viewLifecycleOwner
        ) { employers ->
            employerAdapter.clear()
            employerList = employers
            employerAdapter.notifyDataSetChanged()
            employers.listIterator().forEach {
                employerAdapter.add(it.employerName)
            }
            employerAdapter.add(getString(R.string.add_new_employer))
        }
        binding.spEmployers.adapter = employerAdapter
    }

    private fun setClickActions() {
        onSelectEmployer()
        onSelectExtraType()
        binding.apply {
            fabNew.setOnClickListener {
                gotoExtraAddFragment()
            }
            crdExtraInfo.setOnClickListener {
                gotoExtraTypeUpdateFragment()
            }
        }
    }

    private fun onSelectEmployer() {
        binding.apply {
            spEmployers.onItemSelectedListener =
                object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        findEmployerInListAndPopulateExtraTypes()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
    }

    private fun setSelectionToEmployerFoundInList() {
        binding.apply {
            if (mainActivity.mainViewModel.getEmployer() != null) {
                val employerName =
                    mainActivity.mainViewModel.getEmployer()!!.employerName
                for (i in 0 until spEmployers.adapter.count) {
                    if (spEmployers.getItemAtPosition(i) == employerName) {
                        spEmployers.setSelection(i)
                        break
                    }
                }
            }
        }
    }

    private fun findEmployerInListAndPopulateExtraTypes() {
        binding.apply {
            for (employer in employerList) {
                if (employer.employerName == spEmployers.selectedItem.toString()) {
                    curEmployer = employer
                    populateExtraTypeSpinner()
                    break
                } else if (spEmployers.selectedItem.toString() ==
                    getString(R.string.add_new_employer)
                ) {
                    gotoEmployerAddFragment()
                }
            }
        }
    }

    private fun onSelectExtraType() {
        binding.apply {
            spExtraType.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (spExtraType.selectedItem.toString() ==
                        getString(R.string.add_a_new_extra_type)
                    ) {
                        gotoExtraTypeAdd()
                    } else {
                        findExtraTypeInListAndPopulateDetails()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // useless
                }
            }
        }
    }

    private fun setSelectionToExtraTypeFoundInList() {
        binding.apply {
            if (mainActivity.mainViewModel.getWorkExtraType() != null) {
                val extraType =
                    mainActivity.mainViewModel.getWorkExtraType()!!.wetName
                for (i in 0 until spExtraType.adapter.count) {
                    if (spExtraType.getItemAtPosition(i) == extraType) {
                        spExtraType.setSelection(i)
                        break
                    }
                }
            }
        }
    }

    private fun findExtraTypeInListAndPopulateDetails() {
        for (extra in extraTypeList) {
            if (extra.wetName == binding.spExtraType.selectedItem.toString()) {
                curExtraType = extra
                populateExtraTypeInfo()
                populateExtrasList()
                break
            }
        }
    }

    private fun updateUI(employers: List<Employers>, extraList: List<WorkExtraTypes>) {
        binding.apply {
            if (employers.isNotEmpty() && extraList.isNotEmpty()) {
                fabNew.isEnabled = spEmployers.getItemAtPosition(0)
                    .toString() != getString(R.string.no_employers_add_an_employer_through_the_employer_tab)
            }
            if (extraList.isEmpty()) {
                rvExtras.visibility = View.GONE
                crdNoInfo.visibility = View.VISIBLE
            } else {
                rvExtras.visibility = View.VISIBLE
                crdNoInfo.visibility = View.GONE
            }
        }
    }

    private fun updateExtrasUI(extras: List<Any>) {
        binding.apply {
            if (extras.isEmpty()) {
                rvExtras.visibility = View.GONE
                crdNoInfo.visibility = View.VISIBLE
            } else {
                rvExtras.visibility = View.VISIBLE
                crdNoInfo.visibility = View.GONE
            }
        }
    }

    private fun gotoExtraTypeUpdateFragment() {
        if (curExtraType != null) {
            mainActivity.mainViewModel.setEmployer(curEmployer)
            mainActivity.mainViewModel.setWorkExtraType(curExtraType)
            mainActivity.mainViewModel.addCallingFragment(TAG)
            gotoFragmentToWorkExtraTypeUpdateFragment()
        }
    }

    private fun gotoFragmentToWorkExtraTypeUpdateFragment() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionsFragmentDirections
                .actionEmployerExtraDefinitionsFragmentToWorkExtraTypeUpdateFragment()
        )
    }

    private fun gotoExtraAddFragment() {
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mainActivity.mainViewModel.setWorkExtraType(curExtraType)
        mainActivity.mainViewModel.addCallingFragment(TAG)
        gotoEmployerExtraDefinitionsAddFragment()
    }

    private fun gotoEmployerExtraDefinitionsAddFragment() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionsFragmentDirections
                .actionEmployerExtraDefinitionsFragmentToEmployerExtraDefinitionsAddFragment()
        )
    }

    private fun gotoEmployerAddFragment() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionsFragmentDirections
                .actionEmployerExtraDefinitionsFragmentToEmployerAddFragment()
        )
    }

    private fun gotoExtraTypeAdd() {
        mainActivity.mainViewModel.addCallingFragment(TAG)
        mainActivity.mainViewModel.setEmployer(curEmployer)
        gotoWorkExtraTypeAddFragment()
    }

    private fun gotoWorkExtraTypeAddFragment() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionsFragmentDirections
                .actionEmployerExtraDefinitionsFragmentToWorkExtraTypeAddFragment()
        )
    }

    override fun gotoEmployerExtraDefinitionUpdateFragment() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionsFragmentDirections
                .actionEmployerExtraDefinitionsFragmentToEmployerExtraDefinitionUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}