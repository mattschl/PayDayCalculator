package ms.mattschlenkrich.paydaycalculator.ui.extras

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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.EmployerExtraDefinitionFullAdapter
import ms.mattschlenkrich.paydaycalculator.common.FRAG_EXTRA_DEFINITIONS
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentEmployerExtraDefinitionsBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraTypes

private const val TAG = FRAG_EXTRA_DEFINITIONS

class EmployerExtraDefinitionsFragment : Fragment(R.layout.fragment_employer_extra_definitions) {

    private var _binding: FragmentEmployerExtraDefinitionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val employerList = ArrayList<Employers>()
    private val extraTypeList = ArrayList<WorkExtraTypes>()
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
        mainActivity.title = "View Extra Pay Items"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActions()
        fillEmployers()
        selectEmployer()
        selectExtraType()
        fillValues()
    }

    private fun fillValues() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.apply {
                delay(WAIT_250)
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
                delay(WAIT_250)
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
    }

    private fun selectExtraType() {
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
                        for (extra in extraTypeList) {
                            if (extra.wetName == spExtraType.selectedItem.toString()) {
                                curExtraType = extra
                                fillExtraTypeInfo()
                                fillExtrasList()
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // useless
                }
            }
        }
    }

    private fun fillExtraTypeInfo() {
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
                    var display = "Calculated ${
                        resources.getStringArray(
                            R.array.pay_per_frequencies
                        )[curExtraType!!.wetAppliesTo]
                    }"
                    tvAppliesTo.text = display
                    display = "Attaches to ${
                        resources.getStringArray(
                            R.array.pay_per_frequencies
                        )[curExtraType!!.wetAttachTo]
                    }"
                    tvAttachTo.text = display
                    display = if (curExtraType!!.wetIsCredit) {
                        getString(R.string.this_is_a_credit)
                    } else {
                        getString(R.string.this_is_a_deduction)
                    }
                    tvCredit.text = display
                    display = if (curExtraType!!.wetIsDefault) {
                        getString(R.string.is_default)
                    } else {
                        getString(R.string.manually_added)
                    }
                    tvDefault.text = display
                }
            }
        }
    }

    private fun gotoExtraTypeAdd() {
        mainActivity.mainViewModel.addCallingFragment(TAG)
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mView.findNavController().navigate(
            EmployerExtraDefinitionsFragmentDirections
                .actionEmployerExtraDefinitionsFragmentToWorkExtraTypeAddFragment()
        )
    }

    private fun selectEmployer() {
        binding.apply {
            spEmployers.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    for (employer in employerList) {
                        if (employer.employerName == spEmployers.selectedItem.toString()) {
                            curEmployer = employer
                            fillExtraTypes()
                            break
                        } else if (spEmployers.selectedItem.toString() ==
                            getString(R.string.add_new_employer)
                        ) {
                            gotoEmployerAdd()
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //not needed
                }
            }
        }
    }

    private fun fillExtraTypes() {
        if (curEmployer != null) {
            binding.apply {
                val extraAdapter = ArrayAdapter<Any>(
                    mView.context, R.layout.spinner_item_bold
                )
                mainActivity.workExtraViewModel.getExtraDefTypes(curEmployer!!.employerId)
                    .observe(viewLifecycleOwner) { extraTypes ->
                        extraTypeList.clear()
                        extraTypes.listIterator().forEach {
                            extraAdapter.add(it.wetName)
                            extraTypeList.add(it)
                        }
                        extraAdapter.add(getString(R.string.add_a_new_extra_type))
                        updateUI(employerList, extraTypeList)
                    }
                spExtraType.adapter = extraAdapter
            }
        }
    }

    private fun gotoEmployerAdd() {
        mView.findNavController().navigate(
            EmployerExtraDefinitionsFragmentDirections
                .actionEmployerExtraDefinitionsFragmentToEmployerAddFragment()
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun fillExtrasList() {
        if (curEmployer != null && curExtraType != null) {
            binding.apply {
                val extraDefinitionAdapter = EmployerExtraDefinitionFullAdapter(
                    mainActivity, mView,
                    this@EmployerExtraDefinitionsFragment,
                    null
                )
                rvExtras.apply {
                    layoutManager = StaggeredGridLayoutManager(
                        2,
                        StaggeredGridLayoutManager.VERTICAL
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
                        updateRecycler(extras)
                    }
                }
            }
        }
    }

    private fun setActions() {
        binding.apply {
            fabNew.setOnClickListener {
                gotoExtraAdd()
            }
            crdExtraInfo.setOnClickListener {
                gotoExtraTypeUpdate()
            }
        }
    }

    private fun gotoExtraTypeUpdate() {
        if (curExtraType != null) {
            mainActivity.mainViewModel.setEmployer(curEmployer)
            mainActivity.mainViewModel.setWorkExtraType(curExtraType)
            mainActivity.mainViewModel.addCallingFragment(TAG)
            mView.findNavController().navigate(
                EmployerExtraDefinitionsFragmentDirections
                    .actionEmployerExtraDefinitionsFragmentToWorkExtraTypeUpdateFragment()
            )
        }
    }

    private fun gotoExtraAdd() {
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mainActivity.mainViewModel.setWorkExtraType(curExtraType)
        mainActivity.mainViewModel.addCallingFragment(TAG)
        mView.findNavController().navigate(
            EmployerExtraDefinitionsFragmentDirections
                .actionEmployerExtraDefinitionsFragmentToEmployerExtraDefinitionsAddFragment()
        )
    }

    private fun fillEmployers() {
        val employerAdapter = ArrayAdapter<Any>(
            mView.context,
            R.layout.spinner_item_bold
        )
        mainActivity.employerViewModel.getEmployers().observe(
            viewLifecycleOwner
        ) { employers ->
            employerAdapter.clear()
            employerList.clear()
            employerAdapter.notifyDataSetChanged()
            employers.listIterator().forEach {
                employerAdapter.add(it.employerName)
                employerList.add(it)
            }
            employerAdapter.add(getString(R.string.add_new_employer))
        }
        binding.spEmployers.adapter = employerAdapter
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


    private fun updateRecycler(extras: List<Any>) {
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}