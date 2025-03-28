package ms.mattschlenkrich.paycalculator.ui.workorder.workorder

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
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDERS
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_UPDATE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.workorder.Areas
import ms.mattschlenkrich.paycalculator.database.model.workorder.JobSpec
import ms.mattschlenkrich.paycalculator.database.model.workorder.MaterialAndQuantity
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.adapter.MaterialCountAdapter
import ms.mattschlenkrich.paycalculator.ui.workorder.workorder.adapter.WorkOrderJobSpecAdapter
import ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.adpater.WorkOrderHistoryAdapter

private const val TAG = FRAG_WORK_ORDER_UPDATE

class WorkOrderUpdateFragment : Fragment(R.layout.fragment_work_order),
    IWorkOrderUpdateFragment {

    private var _binding: FragmentWorkOrderBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var workOrderList: List<WorkOrder>
    private lateinit var curEmployer: Employers
    private lateinit var curWorkOrder: WorkOrder
    private var curJobSpec: JobSpec? = null
    private lateinit var jobSpecListForAutoComplete: List<JobSpec>
    private var curArea: Areas? = null
    private lateinit var areaListForAutoComplete: List<Areas>
    private lateinit var jobSpecCombinedList: List<WorkOrderJobSpecCombined>
    private var jobSpecSequence = 0
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.update_work_order)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInitialValues()
        setClickActions()
    }

    private fun setInitialValues() {
        populateJobSpecListForAutoComplete()
        populateAreaListForAutoComplete()
        unHideJobSpecsAndHistory()
        if (mainActivity.mainViewModel.getEmployer() != null) {
            curEmployer = mainActivity.mainViewModel.getEmployer()!!
            binding.apply {
                spEmployers.visibility = View.INVISIBLE
                tvEmployer.visibility = View.VISIBLE
                tvEmployer.text = curEmployer.employerName
            }
            if (mainActivity.mainViewModel.getWorkOrder() != null) {
                curWorkOrder = mainActivity.mainViewModel.getWorkOrder()!!
                setValuesFromHistory(curWorkOrder)
            } else {
                populateWorkOrderListForValidation()
            }
        }
    }

    private fun unHideJobSpecsAndHistory() {
        binding.apply {
            crdJobSpecs.visibility = View.VISIBLE
            crdHistory.visibility = View.VISIBLE
        }
    }

    private fun populateWorkOrderListForValidation() {
        mainActivity.workOrderViewModel.getWorkOrdersByEmployerId(
            curEmployer.employerId
        ).observe(viewLifecycleOwner) { list ->
            workOrderList = list
        }
    }

    private fun populateAreaListForAutoComplete() {
        mainActivity.workOrderViewModel.getAreasList()
            .observe(viewLifecycleOwner) { list ->
                areaListForAutoComplete = list
                val areaNames = ArrayList<String>()
                list.listIterator().forEach {
                    areaNames.add(it.areaName)
                }
                populateAreasInAutoComplete(areaNames)
            }
    }

    private fun populateAreasInAutoComplete(areaNames: ArrayList<String>) {
        val areaAdapter = ArrayAdapter(
            mView.context,
            R.layout.spinner_item_normal,
            areaNames
        )
        binding.acArea.setAdapter(areaAdapter)
    }

    private fun setValuesFromHistory(workOrder: WorkOrder) {
        binding.apply {
            etWorkOrderNumber.setText(workOrder.woNumber)
            etAddress.setText(workOrder.woAddress)
            etDescription.setText(workOrder.woDescription)
        }
        populateHistory(workOrder.workOrderId)
    }

    private fun populateJobSpecListForAutoComplete() {
        mainActivity.workOrderViewModel.getJobSpecsAll()
            .observe(viewLifecycleOwner) { list ->
                val jobSpecNameList = ArrayList<String>()
                list.listIterator().forEach {
                    jobSpecListForAutoComplete = list
                    jobSpecNameList.add(it.jsName)
                }
                populateJobSpecsInAutoComplete(jobSpecNameList)
            }
    }

    private fun populateJobSpecsInAutoComplete(jobSpecNameList: ArrayList<String>) {
        binding.apply {
            val jsAdapter =
                ArrayAdapter(
                    mView.context,
                    R.layout.spinner_item_normal,
                    jobSpecNameList
                )
            acJobSpec.setAdapter(jsAdapter)
        }
    }

    private fun populateHistory(workOrderId: Long) {
        populateDateHistory(workOrderId)
        populateMaterialHistory(workOrderId)
        populateJobSpecsForWorkOrder()
    }

    private fun populateDateHistory(workOrderId: Long) {
        mainActivity.workOrderViewModel.getWorkOrderHistoriesByWorkOrder(
            workOrderId
        ).observe(viewLifecycleOwner) { list ->
            calculateHistoryTotals(list)
            val histories = list.sortedBy { it.workDate.wdDate }
            val workOrderHistoryAdapter =
                WorkOrderHistoryAdapter(
                    mainActivity,
                    mView,
                    histories,
                    this@WorkOrderUpdateFragment
                )
            binding.rvHistory.apply {
                layoutManager = LinearLayoutManager(mView.context)
                adapter = workOrderHistoryAdapter
            }
            hideUnHideHistory(list)
        }
    }

    private fun hideUnHideHistory(list: List<WorkOrderHistoryWithDates>) {
        if (list.isEmpty()) {
            binding.crdHistory.visibility = View.GONE
        } else {
            binding.crdHistory.visibility = View.VISIBLE
        }
    }

    private fun populateMaterialHistory(workOrderId: Long) {
        mainActivity.workOrderViewModel.getMaterialsHistoryByWorkOrderId(
            workOrderId
        ).observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                val materials = ArrayList<MaterialAndQuantity>()
                var curMaterial = ""
                var counter = list.size
                var curMaterialAndQuantity: MaterialAndQuantity? = null
                list.listIterator().forEach {
                    counter--
                    if (curMaterial == "") {
                        curMaterial = it.material.mName
                        curMaterialAndQuantity =
                            MaterialAndQuantity(
                                curMaterial,
                                it.workOrderHistoryMaterial.wohmQuantity
                            )
                        if (counter == 0) materials.add(curMaterialAndQuantity!!)
                    } else if (curMaterial != it.material.mName) {
                        materials.add(curMaterialAndQuantity!!)
                        curMaterial = it.material.mName
                        curMaterialAndQuantity =
                            MaterialAndQuantity(
                                curMaterial,
                                it.workOrderHistoryMaterial.wohmQuantity
                            )
                    } else {
                        curMaterialAndQuantity!!.quantity +=
                            it.workOrderHistoryMaterial.wohmQuantity
                        if (counter == 0) materials.add(curMaterialAndQuantity!!)
                    }
                }
                val materialsAndCount = materials.sortedBy { it.name }
                val materialAdapter =
                    MaterialCountAdapter(
                        materialsAndCount,
                        mView
                    )
                binding.rvMaterials.apply {
//                    layoutManager = LinearLayoutManager(mView.context)
                    layoutManager = StaggeredGridLayoutManager(
                        2,
                        StaggeredGridLayoutManager.VERTICAL
                    )
                    setHasFixedSize(true)
                    adapter = materialAdapter
                }
            }
        }
    }

    private fun calculateHistoryTotals(historyList: List<WorkOrderHistoryWithDates>) {
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
            display = getString(R.string.reg_hrs_) + nf.getNumberFromDouble(regHours)
        }
        if (otHours != 0.0) {
            if (display.isNotBlank()) {
                display += " | "
            }
            display += getString(R.string.ot_hrs_) + nf.getNumberFromDouble(otHours)
        }
        if (dblOtHours != 0.0) {
            if (display.isNotBlank()) {
                display += " | "
            }
            display += getString(R.string.dblot_hrs_) + nf.getNumberFromDouble(dblOtHours)
        }
        binding.tvWorkOrderSummary.text = display
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener { prepareToUpdate() }
            acJobSpec.setOnItemClickListener { _, _, _, _ ->
                setCurrentJobSpec()
            }
            btnAddJobSpec.setOnClickListener {
                addJobSpecToWorkOrderIfValid()
            }
//            spEmployers.setOnItemClickListener { _, _, _, _ ->
//                onSelectEmployer()
//            }
        }
    }

    private fun addJobSpecToWorkOrderIfValid() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.apply {
                if (acJobSpec.text.isNullOrBlank()) {
                    displayMessage(
                        getString(R.string.error_) +
                                getString(R.string.add_a_description_first)
                    )
                } else {
                    val jobSpec = async {
                        if (setCurrentJobSpec()) {
                            curJobSpec!!
                        } else {
                            saveJobSpecToDatabase()
                        }
                    }
                    val area = async {
                        if (setCurrentArea()) {
                            curArea!!
                        } else if (acArea.text.isNullOrBlank()) {
                            null
                        } else {
                            saveAreaToDatabase()
                        }
                    }
                    val jobSpecCombinedIsValid = async {
                        validateJobSpecCombined(jobSpec.await(), area.await())
                    }
                    if (jobSpecCombinedIsValid.await()) {
                        addJobSpecToWorkOrder(jobSpec.await(), area.await())
                    }
                }
            }
        }
    }

    private fun validateJobSpecCombined(jobSpec: JobSpec, area: Areas?): Boolean {
        for (combinedJobSpec in jobSpecCombinedList) {
            if (jobSpec.jsName == combinedJobSpec.jobSpec.jsName &&
                area?.areaName == combinedJobSpec.area?.areaName
            ) {
                displayMessage(
                    getString(R.string.error_) +
                            getString(R.string.this_job_spec_has_already_been_entered_for_this_area)
                )
                return false
            }
        }
        return true
    }

    private fun saveAreaToDatabase(): Areas {
        val newArea = Areas(
            nf.generateRandomIdAsLong(),
            binding.acArea.text.toString().trim(),
            false,
            df.getCurrentTimeAsString()
        )
        mainActivity.workOrderViewModel.insertArea(newArea)
        return newArea
    }


    private fun saveJobSpecToDatabase(): JobSpec {
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

    private fun addJobSpecToWorkOrder(jobSpec: JobSpec, area: Areas?) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.apply {
                val note: String? = if (etWorkPerformedNote.text.isNullOrBlank()) {
                    null
                } else {
                    etWorkPerformedNote.text.toString().trim()
                }
                val areaId: Long? = area?.areaId
                delay(WAIT_250)
                jobSpecSequence++
                CoroutineScope(Dispatchers.Default).launch {
                    mainActivity.workOrderViewModel.insertWorkOrderJobSpec(
                        WorkOrderJobSpec(
                            nf.generateRandomIdAsLong(),
                            curWorkOrder.workOrderId,
                            jobSpec.jobSpecId,
                            areaId,
                            note,
                            jobSpecSequence,
                            false,
                            df.getCurrentTimeAsString()
                        )
                    )
                }
                delay(WAIT_250)
                curJobSpec = null
                curArea = null
                acJobSpec.text.clear()
                etWorkPerformedNote.text.clear()
                populateJobSpecsForWorkOrder()
            }
        }
    }

    private fun setCurrentArea(): Boolean {
        curArea = null
        binding.apply {
            if (acArea.text.isNullOrBlank()) {
                return false
            } else {
                for (area in areaListForAutoComplete)
                    if (area.areaName == acArea.text.toString().trim()
                    ) {
                        curArea = area
                        return true
                    }
            }
        }
        return false
    }

    private fun populateJobSpecsForWorkOrder() {
        mainActivity.workOrderViewModel.getWorkOrderJobSpecs(
            curWorkOrder.workOrderId
        ).observe(viewLifecycleOwner) { jobSpecList ->
            jobSpecCombinedList = jobSpecList
            populateJobSpecSummary(jobSpecList)
            jobSpecSequence = determineSequence(jobSpecList)
            populateJobSpecRecycler(jobSpecList)
        }
    }

    private fun populateJobSpecRecycler(jobSpecList: List<WorkOrderJobSpecCombined>) {
        val workOrderJobSpecAdapter = WorkOrderJobSpecAdapter(
            mainActivity,
            this@WorkOrderUpdateFragment,
            curWorkOrder,
            TAG,
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
            if (jobSpec.workOrderJobSpec.wojsSequence > seq) {
                seq = jobSpec.workOrderJobSpec.wojsSequence
            }
        }
        return seq
    }

    private fun populateJobSpecSummary(jobSpecList: List<WorkOrderJobSpecCombined>) {
        var display = getString(R.string.job_specs)
        var seq = 1
        for (jobSpec in jobSpecList.listIterator()) {
            display += " ${seq}) " + jobSpec.jobSpec.jsName
            display += if (jobSpec.workOrderJobSpec.wojsAreaId != null) {
                " in " + jobSpec.area!!.areaName
            } else {
                ""
            }
            seq++
        }
        binding.tvJobSpecsCombined.text = display
    }

    private fun setCurrentJobSpec(): Boolean {
        curJobSpec = null
        binding.apply {
            if (acJobSpec.text.isNullOrBlank()) {
                return false
            } else {
                for (jobSpec in jobSpecListForAutoComplete) {
                    if (jobSpec.jsName == binding.acJobSpec.text.toString().trim()
                    ) {
                        curJobSpec = jobSpec
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun prepareToUpdate() {
        val answer = validateWorkOrder()
        if (answer == ANSWER_OK) {
            updateWorkOrder()
        } else {
            displayMessage(getString(R.string.error_) + answer)
        }
    }

    private fun displayMessage(mess: String) {
        Toast.makeText(
            mView.context,
            mess,
            Toast.LENGTH_LONG
        ).show()
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
                        populateWorkOrderListForValidation()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //not needed
                    }
                }
        }
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

    private fun validateWorkOrder(): String {
        binding.apply {
            if (etWorkOrderNumber.text.isEmpty()) {
                return getString(R.string.please_enter_a_valid_work_order_number)
            }
            for (workOrder in workOrderList) {
                if (workOrder.woNumber ==
                    etWorkOrderNumber.text.toString() &&
                    etWorkOrderNumber.text.toString() !=
                    curWorkOrder.woNumber
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

    private fun gotoCallingFragment() {
        when (mainActivity.mainViewModel.getCallingFragment()) {

            FRAG_WORK_ORDER_HISTORY_ADD -> {
                gotoWorkOrderHistoryAddFragment()
            }

            FRAG_WORK_ORDER_HISTORY_UPDATE -> {
                gotoWorkOrderHistoryUpdateFragment()
            }

            FRAG_WORK_ORDERS -> {
                gotoWorkOrdersFragment()
            }
        }
    }

    override fun gotoWorkOrdersFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToWorkOrdersFragment()
        )
    }

    override fun gotoWorkOrderHistoryUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun gotoWorkOrderHistoryAddFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToWorkOrderHistoryAddFragment()
        )
    }

    override fun gotoJobSpecUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToJobSpecUpdateFragment()
        )
    }

    override fun gotoWorkOrderJobSpecUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToWorkOrderJobSpecUpdateFragment()
        )
    }

    override fun gotoAreaUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToAreaUpdateFragment()
        )
    }

    override fun gotoWorkOrderHistoryFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections
                .actionWorkOrderUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}