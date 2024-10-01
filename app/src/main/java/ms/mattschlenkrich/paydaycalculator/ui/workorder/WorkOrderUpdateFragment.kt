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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.workorders.WorkOrderHistoryAdapter
import ms.mattschlenkrich.paydaycalculator.adapter.workorders.WorkOrderJobSpecAdapter
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ODER_HISTORY_UPDATE
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ORDERS
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_100
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.JobSpec
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderJobSpec
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderAddBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class WorkOrderUpdateFragment : Fragment(R.layout.fragment_work_order_add) {

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
        mainActivity.title = getString(R.string.update_work_order)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDefaultValues()
        setClickActions()
    }

    private fun populateHistory(workOrderId: Long) {
        mainActivity.workOrderViewModel.getWorkOrderHistoriesById(
            workOrderId
        ).observe(viewLifecycleOwner) { historyList ->
            calculateTotals(historyList)
            val workOrderHistoryAdapter =
                WorkOrderHistoryAdapter(
                    mainActivity,
                    mView,
                    historyList
                )
            binding.rvHistory.apply {
                layoutManager = LinearLayoutManager(mView.context)
                adapter = workOrderHistoryAdapter
            }
        }
    }

    private fun calculateTotals(historyList: List<WorkOrderHistoryWithDates>) {
        var regHours = 0.0
        var otHours = 0.0
        var dblOtHours = 0.0
        historyList.listIterator().forEach {
            regHours += it.history.woHistoryRegHours
            otHours += it.history.woHistoryOtHours
            dblOtHours += it.history.woHistoryDblOtHours
        }
        var display = ""
        if (regHours != 0.0) {
            display = "Reg: " +
                    nf.getNumberFromDouble(
                        regHours
                    )
        }
        if (otHours != 0.0) {
            if (display.isNotBlank()) {
                display += " | "
            }
            display += "Ot: " +
                    nf.getNumberFromDouble(
                        otHours
                    )
        }
        if (dblOtHours != 0.0) {
            if (display.isNotBlank()) {
                display += " | "
            }
            display += "Ot: " +
                    nf.getNumberFromDouble(
                        dblOtHours
                    )
        }
        binding.tvWorkOrderSummary.text = display
    }

    private fun setDefaultValues() {
        if (mainActivity.mainViewModel.getEmployer() != null) {
            curEmployer = mainActivity.mainViewModel.getEmployer()!!
            binding.apply {
                spEmployers.visibility = View.INVISIBLE
                tvEmployer.visibility = View.VISIBLE
                tvEmployer.text = curEmployer.employerName
            }
            getWorkOrderListForValidation()
        }
        if (mainActivity.mainViewModel.getWorkOrder() != null) {
            curWorkOrder =
                mainActivity.mainViewModel.getWorkOrder()!!
            setValuesFromHistory(curWorkOrder)
            populateJobSpecsForWorkOrder()
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

    private fun setValuesFromHistory(workOrder: WorkOrder) {
        binding.apply {
            etWorkOrderNumber.setText(workOrder.woNumber)
            etAddress.setText(workOrder.woAddress)
            etDescription.setText(workOrder.woDescription)
        }

        populateHistory(workOrder.workOrderId)
    }

    private fun getWorkOrderListForValidation() {
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

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                prepareToUpdate()
            }
            acJobSpec.setOnItemClickListener { _, _, _, _ ->
                setCurrentJoSpec()
            }
            btnAddJobSpec.setOnClickListener {
                addSpecToWorkOrderIfValid()
            }
        }
    }

    private fun addSpecToWorkOrderIfValid() {
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
                saveJobSpecAndAddToWorkOrder()
            } else {
                Toast.makeText(
                    mView.context,
                    "Add a description first",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun saveJobSpecAndAddToWorkOrder() {
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
            curJobSpec = saveJobSpec()
            delay(WAIT_100)
            addJobSpecToWorkOrder()
        }
    }

    private fun saveJobSpec(): JobSpec {
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
        populateJobSpecsForWorkOrder()
        binding.acJobSpec.text = null
    }

    private fun populateJobSpecsForWorkOrder() {
        mainActivity.workOrderViewModel.getWorkOrderJobSpecs(
            curWorkOrder.workOrderId
        ).observe(viewLifecycleOwner) { jobSpecList ->
            populateJobSpecSummary(jobSpecList)
            jobSpecSequence = determineSequence(jobSpecList)
            populateJobSpecRecycler(jobSpecList)
        }
    }

    private fun populateJobSpecRecycler(jobSpecList: List<WorkOrderJobSpecCombined>) {
        val workOrderJobSpecAdapter =
            WorkOrderJobSpecAdapter(
                mainActivity,
                mView
            )
        binding.rvJobSpecs.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            adapter = workOrderJobSpecAdapter
        }
        workOrderJobSpecAdapter.differ.submitList(jobSpecList)
    }

    private fun determineSequence(jobSpecList: List<WorkOrderJobSpecCombined>): Int {
        var seq = 0
        for (jobSpec in jobSpecList) {
            if (jobSpec.WorkOrderJobSpec.wojsSequence > seq) {
                seq = jobSpec.WorkOrderJobSpec.wojsSequence
            }
        }
        return seq
    }

    private fun populateJobSpecSummary(jobSpecList: List<WorkOrderJobSpecCombined>) {
        var display = getString(R.string.job_specs)
        var seq = 1
        for (jobSpec in jobSpecList.listIterator()) {
            display +=
                " ${seq}. " +
                        jobSpec.jobSpec.jsName
            seq++
        }
        binding.tvJobSpecsCombined.text = display
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

    private fun prepareToUpdate() {
        val answer = validateWorkOrder()
        if (answer == ANSWER_OK) {
            updateWorkOrder()
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateWorkOrder() {
        val workOrder = getCurrentWorkOrder()
        mainActivity.workOrderViewModel.updateWorkOrder(
            workOrder.workOrderId,
            workOrder.woNumber,
            workOrder.woEmployerId,
            workOrder.woAddress,
            workOrder.woDescription,
            false,
            df.getCurrentTimeAsString()
        )
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        when (mainActivity.mainViewModel.getCallingFragment()) {

            FRAG_WORK_ORDER_HISTORY_ADD -> {
                gotoWorkOrderHistoryAddFragment()
            }

            FRAG_WORK_ODER_HISTORY_UPDATE -> {
                gotoWorkOrderHistoryUpdateFragment()
            }

            FRAG_WORK_ORDERS -> {
                gotoWorkOrdersFragment()
            }
        }
    }

    private fun gotoWorkOrdersFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToWorkOrdersFragment()
        )
    }

    private fun gotoWorkOrderHistoryUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    private fun gotoWorkOrderHistoryAddFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToWorkOrderHistoryAddFragment()
        )
    }

    private fun getCurrentWorkOrder(): WorkOrder {
        binding.apply {
            return WorkOrder(
                curWorkOrder.workOrderId,
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
                        getWorkOrderListForValidation()
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
                return getString(R.string.please_enter_a_valid_work_order_number)
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