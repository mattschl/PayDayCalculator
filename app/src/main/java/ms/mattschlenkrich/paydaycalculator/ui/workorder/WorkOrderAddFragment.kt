package ms.mattschlenkrich.paydaycalculator.ui.workorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_100
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.JobSpec
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderJobSpec
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderAddBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class WorkOrderAddFragment : Fragment(R.layout.fragment_work_order_add) {

    private var _binding: FragmentWorkOrderAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    private val df = DateFunctions()

    private val nf = NumberFunctions()
    private val workOrderList = ArrayList<WorkOrder>()
    private lateinit var curEmployer: Employers
    private lateinit var curWorkOrder: WorkOrder
    private var curJobSpec: JobSpec? = null
    private val jobSpecList = ArrayList<JobSpec>()
    private var jobSpecSequence = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.add_new_work_order)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDefaultValues()
        setClickActions()
    }

    private fun setDefaultValues() {
        binding.apply {
            if (mainActivity.mainViewModel.getEmployer() != null) {
                curEmployer = mainActivity.mainViewModel.getEmployer()!!
                spEmployers.visibility = View.INVISIBLE
                tvEmployer.visibility = View.VISIBLE
                tvEmployer.text = curEmployer.employerName
                getWorkOrderList()
            } else {
                spEmployers.visibility = View.VISIBLE
                tvEmployer.visibility = View.INVISIBLE
                populateEmployers()
            }
            if (mainActivity.mainViewModel.getTempWorkOrderHistoryInfo() != null) {
                setValuesFromHistory()
//                mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(null)
            }
            crdHistory.visibility = View.INVISIBLE
        }
        fillJobSpecListForAutoComplete()
    }

    private fun fillJobSpecListForAutoComplete() {
        mainActivity.workOrderViewModel.getJobSpecsAll()
            .observe(viewLifecycleOwner) { list ->
                val jobSpecNameList = ArrayList<String>()
                list.listIterator().forEach {
                    jobSpecNameList.add(it.jsName)
                    jobSpecList.add(it)
                }
                populateJobSpecsForAutoComplete(jobSpecNameList)
            }
    }

    private fun populateJobSpecsForAutoComplete(jobSpecNameList: ArrayList<String>) {
        binding.apply {
            val jsAdapter = ArrayAdapter(
                mView.context,
                R.layout.spinner_item_normal,
                jobSpecNameList
            )
            acJobSpec.setAdapter(jsAdapter)
        }
    }

    private fun getWorkOrderList() {
        workOrderList.clear()
        mainActivity.workOrderViewModel.getWorkOrdersByEmployerId(
            curEmployer.employerId
        ).observe(
            viewLifecycleOwner
        ) { list ->
            list.listIterator().forEach {
                workOrderList.add(it)
            }
        }
    }

    private fun populateEmployers() {
        mainActivity.employerViewModel.getEmployers().observe(
            viewLifecycleOwner
        ) { list ->
            binding.apply {
                val employerAdapter = ArrayAdapter<Any>(
                    mView.context,
                    R.layout.spinner_item_bold
                )
                list.listIterator().forEach {
                    employerAdapter.add(it.employerName)
                }
                spEmployers.adapter = employerAdapter
            }
        }
    }

    private fun setValuesFromHistory() {
        val tempWorkOrder = mainActivity.mainViewModel.getTempWorkOrderHistoryInfo()!!
        binding.apply {
            etWorkOrderNumber.setText(tempWorkOrder.woHistoryWorkOrderNumber)
        }
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                prepareToSave(false, false)
            }
            onSelectEmployer()
            acJobSpec.setOnItemClickListener { _, _, _, _ ->
                setCurrentJoSpec()
            }
            btnAddJobSpec.setOnClickListener {
                prepareToSave(true, true)
            }
        }
    }

    private fun setCurrentJoSpec(): Boolean {
        for (jobSpec in jobSpecList) {
            if (jobSpec.jsName == binding.acJobSpec.text.toString().trim()) {
                curJobSpec = jobSpec
                return true
            }
        }
        curJobSpec = null
        return false
    }

    private fun prepareToSave(gotoUpdate: Boolean, addJobSpecToWorkOrder: Boolean) {
        val answer = validateWorkOrder()
        if (answer == ANSWER_OK) {
            saveWorkOrder(gotoUpdate, addJobSpecToWorkOrder)
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveWorkOrder(gotoUpdate: Boolean, addJobSpecToWorkOrder: Boolean) {
        curWorkOrder = getCurrentWorkOrder()
        mainActivity.workOrderViewModel.insertWorkOrder(curWorkOrder)
        mainActivity.mainViewModel.setWorkOrder(curWorkOrder)
        mainActivity.mainViewModel.setWorkOrderNumber(
            curWorkOrder.woNumber
        )
        if (addJobSpecToWorkOrder) {
            addJobSpecToWorkOrderIfValid()
        }
        if (gotoUpdate) {
            gotoWorkOrderUpdateFragment()
        } else {
            gotoCallingFragment()
        }
    }

    private fun addJobSpecToWorkOrderIfValid() {
        binding.apply {
            if (acJobSpec.text.isNullOrBlank()) {
                Toast.makeText(
                    mView.context,
                    "Add a description first",
                    Toast.LENGTH_LONG
                ).show()
            } else if (curJobSpec != null) {
                addJobSpecToWorkOrder()
            } else if (acJobSpec.text.isNotBlank()) {
                addJobSpecToWorkOrderOrAddToDatabaseFirst()
            } else {
                Toast.makeText(
                    mView.context,
                    "Add a description first",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun addJobSpecToWorkOrderOrAddToDatabaseFirst() {
        for (jobSpec in jobSpecList) {
            if (jobSpec.jsName ==
                binding.acJobSpec.text.toString().trim()
            ) {
                curJobSpec = jobSpec
                addJobSpecToWorkOrder()
                break
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            curJobSpec = addJobSpecToDatabase()
            delay(WAIT_100)
            addJobSpecToWorkOrder()
        }
    }

    private fun addJobSpecToDatabase(): JobSpec {
        val newJobSpec =
            JobSpec(
                nf.generateRandomIdAsLong(),
                binding.acJobSpec.text.toString().trim(),
                false,
                df.getCurrentTimeAsString()
            )
        mainActivity.workOrderViewModel.insertJobSpec(
            newJobSpec
        )
        return newJobSpec
    }

    private fun addJobSpecToWorkOrder() {
        jobSpecSequence++
        mainActivity.workOrderViewModel.insertWorkOrderJobSpec(
            WorkOrderJobSpec(
                nf.generateRandomIdAsLong(),
                curWorkOrder.workOrderId,
                curJobSpec!!.jobSpecId,
                jobSpecSequence,
                false,
                df.getCurrentTimeAsString()
            )
        )
//        populateJobSpecsForWorkOrder()
//        binding.acJobSpec.text = null
    }

    private fun gotoWorkOrderUpdateFragment() {
        Toast.makeText(
            mView.context,
            "Work order has been added automatically before adding work specs.",
            Toast.LENGTH_LONG
        ).show()
        mView.findNavController().navigate(
            WorkOrderAddFragmentDirections
                .actionWorkOrderAddFragmentToWorkOrderUpdateFragment()
        )
    }

    private fun gotoCallingFragment() {
        gotoTimeSheetAddWorkOrderFragment()
    }

    private fun gotoTimeSheetAddWorkOrderFragment() {
        mView.findNavController().navigate(
            WorkOrderAddFragmentDirections
                .actionWorkOrderAddFragmentToWorkOrderHistoryAddFragment()
        )
    }

    private fun getCurrentWorkOrder(): WorkOrder {
        binding.apply {
            return WorkOrder(
                nf.generateRandomIdAsLong(),
                etWorkOrderNumber.text.toString(),
                curEmployer.employerId,
                etAddress.text.toString().trim(),
                etDescription.text.toString().trim(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun onSelectEmployer() {
        binding.apply {
            spEmployers.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        CoroutineScope(Dispatchers.IO).launch {
                            curEmployer = mainActivity.employerViewModel.findEmployer(
                                spEmployers.selectedItem.toString()
                            )
                        }
                        getWorkOrderList()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
    }

    private fun validateWorkOrder(): String {
        binding.apply {
            if (etWorkOrderNumber.text.isEmpty()) {
                return getString(R.string.please_enter_a_work_order_number)
            }
            for (workOrder in workOrderList) {
                if (workOrder.woNumber ==
                    etWorkOrderNumber.text.toString()
                ) {
                    return getString(R.string.this_work_order_has_been_used)
                }
            }
            if (etAddress.text.isEmpty()) {
                return getString(R.string.please_enter_an_address)
            }
            if (etDescription.text.isEmpty()) {
                return getString(R.string.please_enter_a_description)
            }
        }
        return ANSWER_OK
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}