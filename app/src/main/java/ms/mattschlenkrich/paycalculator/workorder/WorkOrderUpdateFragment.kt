package ms.mattschlenkrich.paycalculator.workorder

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_TIME
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDERS
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_UPDATE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.data.Areas
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.JobSpec
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.MaterialAndQuantity
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.data.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.data.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.WorkPerformedAndQuantity

private const val TAG = FRAG_WORK_ORDER_UPDATE

class WorkOrderUpdateFragment : Fragment(), IWorkOrderUpdateFragment {

    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
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
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val defaultScope = CoroutineScope(Dispatchers.Default)

    // Compose states
    private var employerNameState by mutableStateOf("")
    private var woNumberState by mutableStateOf("")
    private var addressState by mutableStateOf("")
    private var descriptionState by mutableStateOf("")
    private var jobSpecTextState by mutableStateOf("")
    private var areaTextState by mutableStateOf("")
    private var workPerformedNoteState by mutableStateOf("")
    private var addedJobSpecsState by mutableStateOf(emptyList<WorkOrderJobSpecCombined>())
    private var historyListState by mutableStateOf(emptyList<WorkOrderHistoryWithDates>())
    private var workPerformedListState by mutableStateOf(emptyList<WorkPerformedAndQuantity>())
    private var materialsListState by mutableStateOf(emptyList<MaterialAndQuantity>())
    private var jobSpecSummaryTextState by mutableStateOf("")
    private var historySummaryTextState by mutableStateOf("")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        return ComposeView(requireContext()).apply {
            setContent {
                WorkOrderUpdateScreen(
                    employerName = employerNameState,
                    woNumber = woNumberState,
                    onWoNumberChange = { woNumberState = it },
                    address = addressState,
                    onAddressChange = { addressState = it },
                    description = descriptionState,
                    onDescriptionChange = { descriptionState = it },
                    jobSpecText = jobSpecTextState,
                    onJobSpecTextChange = { jobSpecTextState = it },
                    jobSpecSuggestions = if (::jobSpecListForAutoComplete.isInitialized) jobSpecListForAutoComplete else emptyList(),
                    onJobSpecSelected = {
                        jobSpecTextState = it.jsName
                        curJobSpec = it
                    },
                    areaText = areaTextState,
                    onAreaTextChange = { areaTextState = it },
                    areaSuggestions = if (::areaListForAutoComplete.isInitialized) areaListForAutoComplete else emptyList(),
                    onAreaSelected = {
                        areaTextState = it.areaName
                        curArea = it
                    },
                    workPerformedNote = workPerformedNoteState,
                    onWorkPerformedNoteChange = { workPerformedNoteState = it },
                    onAddJobSpecClick = { addJobSpecToWorkOrderIfValid(true) },
                    addedJobSpecs = addedJobSpecsState,
                    onJobSpecClick = { chooseOptionsForJobSpec(it) },
                    jobSpecSummaryText = jobSpecSummaryTextState,
                    historyList = historyListState,
                    onHistoryClick = { chooseOptionsForHistory(it) },
                    historySummaryText = historySummaryTextState,
                    onAddHistoryClick = { gotoWorkOrderHistoryAddFragment() },
                    workPerformedList = workPerformedListState,
                    materialsList = materialsListState,
                    onDoneClick = { prepareToUpdate() },
                    onBackClick = {
                        mainViewModel.getCallingFragment()?.let {
                            gotoCallingFragment()
                        } ?: run {
                            mainActivity.onBackPressedDispatcher.onBackPressed()
                        }
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        setInitialValues()
    }

    private fun setInitialValues() {
        populateJobSpecListForAutoComplete()
        populateAreaListForAutoComplete()
        if (mainViewModel.getEmployer() != null) {
            curEmployer = mainViewModel.getEmployer()!!
            employerNameState = curEmployer.employerName
            populateWorkOrderListForValidation()
            if (mainViewModel.getWorkOrder() != null) {
                curWorkOrder = mainViewModel.getWorkOrder()!!
                setValuesFromHistory(curWorkOrder)
            }
        }
    }

    private fun populateWorkOrderListForValidation() {
        workOrderViewModel.getWorkOrdersByEmployerId(
            curEmployer.employerId
        ).observe(viewLifecycleOwner) { list ->
            workOrderList = list
        }
    }

    private fun populateAreaListForAutoComplete() {
        workOrderViewModel.getAreasList().observe(viewLifecycleOwner) { list ->
            areaListForAutoComplete = list
        }
    }

    private fun setValuesFromHistory(workOrder: WorkOrder) {
        woNumberState = workOrder.woNumber
        addressState = workOrder.woAddress
        descriptionState = workOrder.woDescription
        populateHistory(workOrder.workOrderId)
    }

    private fun populateJobSpecListForAutoComplete() {
        workOrderViewModel.getJobSpecsAll().observe(viewLifecycleOwner) { list ->
            jobSpecListForAutoComplete = list
        }
    }

    private fun populateHistory(workOrderId: Long) {
        populateDateHistory(workOrderId)
        populateWorkPerformedHistory(workOrderId)
        populateMaterialHistory(workOrderId)
        populateJobSpecsForWorkOrder()
    }

    private fun populateWorkPerformedHistory(workOrderId: Long) {
        workOrderViewModel.getWorkOrderHistoriesByWorkOrder(workOrderId).observe(
            viewLifecycleOwner
        ) { histories ->
            val allWorkPerformed = mutableListOf<WorkPerformedAndQuantity>()
            var historiesProcessed = 0
            if (histories.isEmpty()) {
                workPerformedListState = emptyList()
                return@observe
            }

            histories.forEach { history ->
                workOrderViewModel.getWorkPerformedCombinedByWorkOrderHistory(history.history.woHistoryId)
                    .observe(viewLifecycleOwner) { wpList ->
                        wpList.forEach { wp ->
                            val existing = allWorkPerformed.find {
                                it.description == wp.workPerformed.wpDescription &&
                                        it.area == wp.area?.areaName
                            }
                            if (existing == null) {
                                allWorkPerformed.add(
                                    WorkPerformedAndQuantity(
                                        wp.workPerformed.wpDescription,
                                        wp.area?.areaName,
                                        1
                                    )
                                )
                            } else {
                                val index = allWorkPerformed.indexOf(existing)
                                allWorkPerformed[index] =
                                    existing.copy(quantity = existing.quantity + 1)
                            }
                        }
                        historiesProcessed++
                        if (historiesProcessed == histories.size) {
                            workPerformedListState = allWorkPerformed.sortedBy { it.description }
                        }
                    }
            }
        }
    }

    private fun populateDateHistory(workOrderId: Long) {
        workOrderViewModel.getWorkOrderHistoriesByWorkOrder(workOrderId).observe(
            viewLifecycleOwner
        ) { list ->
            calculateHistoryTotals(list)
            historyListState = list.sortedBy { it.workDate.wdDate }
        }
    }

    private fun populateMaterialHistory(workOrderId: Long) {
        workOrderViewModel.getMaterialsHistoryByWorkOrderId(workOrderId).observe(
            viewLifecycleOwner
        ) { list ->
            if (list.isNotEmpty()) {
                val materials = ArrayList<MaterialAndQuantity>()
                list.groupBy { it.material.mName }.forEach { (name, historyItems) ->
                    val totalQuantity =
                        historyItems.sumOf { it.workOrderHistoryMaterial.wohmQuantity }
                    materials.add(MaterialAndQuantity(name, totalQuantity))
                }
                materialsListState = materials.sortedBy { it.name }
            } else {
                materialsListState = emptyList()
            }
        }
    }

    private fun calculateHistoryTotals(historyList: List<WorkOrderHistoryWithDates>) {
        var regHours = 0.0
        var otHours = 0.0
        var dblOtHours = 0.0
        historyList.forEach {
            regHours += it.history.woHistoryRegHours
            otHours += it.history.woHistoryOtHours
            dblOtHours += it.history.woHistoryDblOtHours
        }
        var display = ""
        if (regHours != 0.0) {
            display = getString(R.string.reg_) + nf.getNumberFromDouble(regHours)
        }
        if (otHours != 0.0) {
            if (display.isNotBlank()) {
                display += " | "
            }
            display += getString(R.string.ot_) + nf.getNumberFromDouble(otHours)
        }
        if (dblOtHours != 0.0) {
            if (display.isNotBlank()) {
                display += " | "
            }
            display += getString(R.string.dbl_ot_) + nf.getNumberFromDouble(dblOtHours)
        }
        historySummaryTextState = display
    }

    private fun addJobSpecToWorkOrderIfValid(showError: Boolean) {
        mainScope.launch {
            if (jobSpecTextState.isBlank()) {
                if (showError) {
                    displayMessage(
                        getString(R.string.error_) + getString(R.string.add_a_description_first)
                    )
                }
            } else {
                val jobSpec = async {
                    return@async if (setCurrentJobSpec()) {
                        curJobSpec!!
                    } else {
                        insertJobSpecIntoDatabase()
                    }
                }
                val area = async {
                    return@async if (setCurrentArea()) {
                        curArea!!
                    } else if (areaTextState.isBlank()) {
                        null
                    } else {
                        insertAreaIntoDatabase()
                    }
                }
                val jobSpecCombinedIsValid = async {
                    validateJobSpecCombined(
                        jobSpec.await(), area.await(), showError
                    )
                }
                if (jobSpecCombinedIsValid.await()) {
                    addJobSpecToWorkOrder(
                        jobSpec.await(), area.await()
                    )
                }
            }
        }
    }

    private fun validateJobSpecCombined(
        jobSpec: JobSpec, area: Areas?, showError: Boolean
    ): Boolean {
        for (combinedJobSpec in jobSpecCombinedList) {
            if (jobSpec.jsName == combinedJobSpec.jobSpec.jsName && area?.areaName == combinedJobSpec.area?.areaName) {
                if (showError) {
                    displayMessage(getString(R.string.error_) + getString(R.string.this_job_spec_has_already_been_entered_for_this_area))
                }
                return false
            }
        }
        return true
    }

    private fun insertAreaIntoDatabase(): Areas {
        val newArea = Areas(
            nf.generateRandomIdAsLong(),
            areaTextState.trim(),
            false,
            df.getCurrentTimeAsString()
        )
        workOrderViewModel.insertArea(newArea)
        return newArea
    }


    private fun insertJobSpecIntoDatabase(): JobSpec {
        val newJobSpec = JobSpec(
            nf.generateRandomIdAsLong(),
            jobSpecTextState.trim(),
            false,
            df.getCurrentTimeAsString()
        )
        workOrderViewModel.insertJobSpec(
            newJobSpec
        )
        return newJobSpec
    }


    private fun addJobSpecToWorkOrder(jobSpec: JobSpec, area: Areas?) {
        mainScope.launch {
            val note: String? = if (workPerformedNoteState.isBlank()) {
                null
            } else {
                workPerformedNoteState.trim()
            }
            val areaId: Long? = area?.areaId
            delay(WAIT_250)
            jobSpecSequence++
            defaultScope.launch {
                workOrderViewModel.insertWorkOrderJobSpec(
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
            jobSpecTextState = ""
            workPerformedNoteState = ""
            populateJobSpecsForWorkOrder()
        }
    }

    private fun setCurrentArea(): Boolean {
        curArea = null
        if (areaTextState.isBlank()) {
            return false
        } else {
            for (area in areaListForAutoComplete) if (area.areaName == areaTextState.trim()
            ) {
                curArea = area
                return true
            }
        }
        return false
    }

    private fun populateJobSpecsForWorkOrder() {
        workOrderViewModel.getWorkOrderJobSpecs(curWorkOrder.workOrderId).observe(
            viewLifecycleOwner
        ) { jobSpecList ->
            jobSpecCombinedList = jobSpecList
            populateJobSpecSummary(jobSpecList)
            jobSpecSequence = determineSequence(jobSpecList)
            addedJobSpecsState = jobSpecList
        }
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
        jobSpecSummaryTextState = display
    }

    private fun setCurrentJobSpec(): Boolean {
        curJobSpec = null
        if (jobSpecTextState.isBlank()) {
            return false
        } else {
            for (jobSpec in jobSpecListForAutoComplete) {
                if (jobSpec.jsName == jobSpecTextState.trim()) {
                    curJobSpec = jobSpec
                    return true
                }
            }
        }
        return false
    }

    private fun prepareToUpdate() {
        mainScope.launch {
            addJobSpecToWorkOrderIfValid(false)
            delay(WAIT_500)
            val answer = validateWorkOrder()
            if (answer == ANSWER_OK) {
                updateWorkOrder()
            } else {
                displayMessage(getString(R.string.error_) + answer)
            }
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun updateWorkOrder() {
        val workOrder = getCurrentWorkOrder()
        workOrder.apply {
            mainActivity.workOrderViewModel.updateWorkOrder(
                workOrderId,
                woNumber,
                woEmployerId,
                woAddress,
                woDescription,
                false,
                df.getCurrentTimeAsString()
            )
        }
        gotoCallingFragment()
    }

    private fun getCurrentWorkOrder(): WorkOrder {
        return WorkOrder(
            curWorkOrder.workOrderId,
            woNumberState,
            curEmployer.employerId,
            addressState.trim(),
            descriptionState.trim(),
            false,
            df.getCurrentTimeAsString()
        )
    }

    private fun validateWorkOrder(): String {
        if (woNumberState.isEmpty()) {
            return getString(R.string.please_enter_a_valid_work_order_number)
        }
        for (workOrder in workOrderList) {
            if (workOrder.woNumber == woNumberState && woNumberState != curWorkOrder.woNumber) {
                return getString(R.string.this_work_order_has_been_used)
            }
        }
        if (addressState.isEmpty()) {
            return getString(R.string.please_enter_an_address)
        }
        if (descriptionState.isEmpty()) {
            return getString(R.string.please_enter_a_description)
        }
        return ANSWER_OK
    }

    private fun chooseOptionsForJobSpec(jobSpec: WorkOrderJobSpecCombined) {
        AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.choose_option_for) + "\"${jobSpec.jobSpec.jsName}\""
        ).setItems(
            arrayOf(
                getString(R.string.edit_the_job_spec_description_in_the_work_order),
                getString(R.string.remove_this_work_job_spec_description_in_the_work_order),
                getString(R.string.edit_work_description_of) + jobSpec.jobSpec.jsName,
                if (jobSpec.workOrderJobSpec.wojsAreaId != null) {
                    getString(R.string.edit_area_description_of_) + jobSpec.area?.areaName
                } else {
                    ""
                }
            )
        ) { _, pos ->
            when (pos) {
                0 -> {
                    gotoJobSpecUpdate(
                        jobSpec.workOrderJobSpec.workOrderJobSpecId
                    )
                }

                1 -> {
                    removeJobSpecFromWorkOrder(
                        jobSpec
                    )
                }

                2 -> {
                    editJobSpec(jobSpec)
                }

                3 -> {
                    editArea(jobSpec.workOrderJobSpec.wojsAreaId!!)
                }
            }
        }.setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun gotoJobSpecUpdate(workOrderJobSpecId: Long) {
        // TODO: Not yet implemented in legacy either
    }

    private fun removeJobSpecFromWorkOrder(woJobSpec: WorkOrderJobSpecCombined) {
        workOrderViewModel.deleteWorkOrderJobSpec(woJobSpec.workOrderJobSpec.workOrderJobSpecId)
    }

    private fun editJobSpec(woJobSpec: WorkOrderJobSpecCombined) {
        mainViewModel.apply {
            setJobSpec(woJobSpec.jobSpec)
            addCallingFragment(TAG)
        }
        gotoJobSpecUpdateFragment()
    }

    private fun editArea(areaId: Long) {
        mainViewModel.apply {
            setAreaId(areaId)
            addCallingFragment(TAG)
        }
        gotoAreaUpdateFragment()
    }

    private fun chooseOptionsForHistory(history: WorkOrderHistoryWithDates) {
        AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.choose_option_for) + " ${getString(R.string.work_performed_on)}" + " ${
                df.getDisplayDate(
                    history.workDate.wdDate
                )
            }"
        ).setPositiveButton(getString(R.string.open_caps)) { _, _ ->
            gotoEditWorkOrderHistory(
                history
            )
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun gotoEditWorkOrderHistory(history: WorkOrderHistoryWithDates) {
        mainViewModel.apply {
            setWorkOrderHistory(history.history)
            setWorkDateObject(history.workDate)
        }
        gotoWorkOrderHistoryFragment()
    }

    private fun gotoCallingFragment() {
        val frag = mainViewModel.getCallingFragment()!!
        if (frag.contains(FRAG_WORK_ORDER_HISTORY_ADD)) {
            gotoWorkOrderHistoryAddFragment()
        } else if (frag.contains(FRAG_WORK_ORDER_HISTORY_UPDATE)) {
            gotoWorkOrderHistoryUpdateFragment()
        } else if (frag.contains(FRAG_WORK_ORDERS)) {
            gotoWorkOrdersFragment()
        } else if (frag.contains(FRAG_WORK_DATE_TIME)) {
            gotoWorkDateTimesFragment()
        }
    }

    override fun gotoWorkOrdersFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections.actionWorkOrderUpdateFragmentToWorkOrdersFragment()
        )
    }

    override fun gotoWorkOrderHistoryUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections.actionWorkOrderUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun gotoWorkOrderHistoryAddFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections.actionWorkOrderUpdateFragmentToWorkOrderHistoryAddFragment()
        )
    }

    override fun gotoJobSpecUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections.actionWorkOrderUpdateFragmentToJobSpecUpdateFragment()
        )
    }

    override fun gotoWorkOrderJobSpecUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections.actionWorkOrderUpdateFragmentToWorkOrderJobSpecUpdateFragment()
        )
    }

    override fun gotoAreaUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections.actionWorkOrderUpdateFragmentToAreaUpdateFragment()
        )
    }

    override fun gotoWorkOrderHistoryFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections.actionWorkOrderUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    private fun gotoWorkDateTimesFragment() {
        mView.findNavController().navigate(
            WorkOrderUpdateFragmentDirections.actionWorkOrderUpdateFragmentToWorkDateTimes()
        )
    }

    override fun onStop() {
        defaultScope.cancel()
        mainScope.cancel()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}