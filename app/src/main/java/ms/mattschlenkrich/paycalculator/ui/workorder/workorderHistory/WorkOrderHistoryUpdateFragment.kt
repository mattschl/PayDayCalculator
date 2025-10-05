package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

import android.app.AlertDialog
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.ExceptionUnknown
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.Areas
import ms.mattschlenkrich.paycalculator.database.model.workorder.Material
import ms.mattschlenkrich.paycalculator.database.model.workorder.MaterialInSequence
import ms.mattschlenkrich.paycalculator.database.model.workorder.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryMaterialCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWorkPerformedCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderHistoryBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.WorkOrderCommonFunctions
import ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.adpater.WorKOrderHistoryWorkPerformedAdapter
import ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.adpater.WorkOrderHistoryMaterialAdapter

private const val TAG = FRAG_WORK_ORDER_HISTORY_UPDATE

class WorkOrderHistoryUpdateFragment : Fragment(R.layout.fragment_work_order_history),
    IWorkOrderHistoryUpdateFragment {

    private var timeWorkedList: List<WorkOrderHistoryTimeWorkedCombined>? = null
    private var _binding: FragmentWorkOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var workOrderList: List<WorkOrder>
    private var workDateObject: WorkDates? = null
    private lateinit var curEmployer: Employers
    private lateinit var curHistoryDetailed: WorkOrderHistoryWithDates
    private var curWorkOrder: WorkOrder? = null
    private lateinit var workPerformedListForAutoComplete: List<WorkPerformed>
    private lateinit var existingWorkPerformedListForValidation: List<WorkOrderHistoryWorkPerformedCombined>
    private lateinit var materialListForAutoComplete: List<Material>
    private lateinit var areaListForAutoComplete: List<Areas>
    private var curWorkPerformed: WorkPerformed? = null
    private var curArea: Areas? = null
    private var workPerformedSequence = 0
    private var curMaterial: Material? = null
    private var materialSequence = 0
    private lateinit var commonFunctions: WorkOrderCommonFunctions
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val defaultScope = CoroutineScope(Dispatchers.Default)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderHistoryBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        commonFunctions = WorkOrderCommonFunctions(mainActivity)
        mainActivity.title = getString(R.string.add_work_performed_to_workorder_history)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateInitialValues()
        setClickActions()
    }

    private fun populateInitialValues() {
        mainScope.launch {
            removeFragmentReferenceFromBackStack()
            populateWorkPerformedListForAutoComplete()
            populateMaterialListForAutoComplete()
            populateAreaListForAutoComplete()
            unHideMaterialAndWorkPerformed()
            populateWorkDate()
            if (workDateObject != null) {
                populateCurrentEmployer()
            }
            delay(WAIT_250)
            populateWorkOrderListForAutoComplete()
            delay(WAIT_250)
            if (commonFunctions.getWorkOrderHistory() != null) {
                populateFromHistory()
            } else if (mainViewModel.getTempWorkOrderHistoryInfo() != null) {
                populateFromTempValues()
            }
        }
    }

    private fun removeFragmentReferenceFromBackStack() {
        mainViewModel.removeCallingFragment(TAG)
    }

    private fun unHideMaterialAndWorkPerformed() {
        binding.apply {
            crdMaterials.visibility = View.VISIBLE
            crdWorkPerformed.visibility = View.VISIBLE
        }
    }

    private fun populateWorkDate() {
        if (commonFunctions.getWorkDateObject() != null) {
            workDateObject = commonFunctions.getWorkDateObject()!!
            binding.lblDate.text = df.getDisplayDate(workDateObject!!.wdDate)
        }
    }

    private fun populateCurrentEmployer() {
        if (mainViewModel.getEmployer() != null) {
            curEmployer = mainViewModel.getEmployer()!!
            binding.tvEmployers.text = curEmployer.employerName
        }
    }

    private fun populateMaterialListForAutoComplete() {
        workOrderViewModel.getMaterialsList().observe(viewLifecycleOwner) { list ->
            materialListForAutoComplete = list
            val materialListNames = ArrayList<String>()
            list.listIterator().forEach { materialListNames.add(it.mName) }
            val mAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_normal, materialListNames
            )
            binding.acMaterials.setAdapter(mAdapter)
        }
    }

    private fun populateAreaListForAutoComplete() {
        workOrderViewModel.getAreasList().observe(viewLifecycleOwner) { list ->
            areaListForAutoComplete = list
            val areaNameList = ArrayList<String>()
            list.listIterator().forEach { areaNameList.add(it.areaName) }
            val mAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_normal, areaNameList
            )
            binding.acArea.setAdapter(mAdapter)
        }
    }

    private fun populateWorkPerformedLists() {
        workOrderViewModel.getWorkPerformedCombinedByWorkOrderHistory(
            curHistoryDetailed.history.woHistoryId
        ).observe(viewLifecycleOwner) { list ->
            val listSorted =
                list.sortedBy { workPerformedCombined -> workPerformedCombined.area?.areaName }
            populateWorkPerformedRecycler(listSorted)
            existingWorkPerformedListForValidation = list
            determineWorkPerformedSequence(list)
        }
    }

    private fun populateWorkOrderListForAutoComplete() {
        workOrderViewModel.getWorkOrdersByEmployerId(workDateObject!!.wdEmployerId)
            .observe(viewLifecycleOwner) { list ->
                workOrderList = list
                val workOrderListForAutocomplete = ArrayList<String>()
                list.listIterator().forEach { workOrderListForAutocomplete.add(it.woNumber) }
                binding.apply {
                    val woAdapter = ArrayAdapter(
                        mView.context, R.layout.spinner_item_normal, workOrderListForAutocomplete
                    )
                    acWorkOrder.setAdapter(woAdapter)
                }
            }
    }

    private fun populateWorkPerformedListForAutoComplete() {
        workOrderViewModel.getWorkPerformedAll().observe(viewLifecycleOwner) { list ->
            workPerformedListForAutoComplete = list
            val workPerformedDescriptions = ArrayList<String>()
            list.listIterator().forEach { workPerformedDescriptions.add(it.wpDescription) }
            val wpAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_normal, workPerformedDescriptions
            )
            binding.acWorkPerformed.setAdapter(wpAdapter)
        }
    }

    private fun populateFromHistory() {
        val historyId = commonFunctions.getWorkOrderHistory()!!.woHistoryId
        workOrderViewModel.getWorkOrderHistory(historyId).observe(viewLifecycleOwner) { history ->
            curHistoryDetailed = history
            binding.apply {
                acWorkOrder.setText(history.workOrder.woNumber)
                workOrderViewModel.getWorkOrder(history.workOrder.woNumber)
                    .observe(viewLifecycleOwner) { workOrder ->
                        curWorkOrder = workOrder
                        populateWorkPerformedLists()
                        populateMaterials()
                    }
                workOrderViewModel.getTimeWorkedForWorkOrderHistory(historyId)
                    .observe(viewLifecycleOwner) { list ->
                        timeWorkedList = list
                        if (list.isEmpty()) {
                            btnAddTime.text = getString(R.string.add_time)
                        } else {
                            btnAddTime.text = getString(R.string.edit_times)
                        }
                        var regHours = 0.0
                        var otHours = 0.0
                        var dblOtHours = 0.0
//                        if (mainViewModel.getTempWorkOrderHistoryInfo() != null) {
//                            val historyInfo = mainViewModel.getTempWorkOrderHistoryInfo()!!
//                            regHours = historyInfo.woHistoryRegHours
//                            otHours = historyInfo.woHistoryOtHours
//                            dblOtHours = historyInfo.woHistoryDblOtHours
//                        }
//                        if (history.history.woHistoryRegHours > regHours) {
//                            regHours = history.history.woHistoryRegHours
//                        }
//                        if (history.history.woHistoryOtHours > otHours) {
//                            otHours = history.history.woHistoryOtHours
//                        }
//                        if (history.history.woHistoryDblOtHours > dblOtHours) {
//                            dblOtHours = history.history.woHistoryDblOtHours
//                        }
                        for (timeWorked in list) {
                            when (timeWorked.timeWorked.wohtTimeType) {
                                1 -> regHours += df.getTimeWorked(
                                    timeWorked.timeWorked.wohtStartTime,
                                    timeWorked.timeWorked.wohtEndTime
                                )

                                2 -> otHours += df.getTimeWorked(
                                    timeWorked.timeWorked.wohtStartTime,
                                    timeWorked.timeWorked.wohtEndTime
                                )

                                3 -> dblOtHours += df.getTimeWorked(
                                    timeWorked.timeWorked.wohtStartTime,
                                    timeWorked.timeWorked.wohtEndTime
                                )
                            }
                        }
                        etRegHours.setText(nf.getNumberFromDouble(regHours))
                        etOtHours.setText(nf.getNumberFromDouble(otHours))
                        etDblOtHours.setText(nf.getNumberFromDouble(dblOtHours))
//                        populateTimeWorkedRecycler()
                    }
                etNote.setText(history.history.woHistoryNote)
                btnWorkOrder.text = getString(R.string.edit)
                if (mainViewModel.getWorkOrderNumber() != null) {
                    workOrderViewModel.getWorkOrder(
                        curHistoryDetailed.history.woHistoryWorkOrderId
                    ).observe(viewLifecycleOwner) { workOrder ->
                        curWorkOrder = workOrder
                        setCurWorkOrder()
                    }
                }
            }
        }
    }

//    private fun populateTimeWorkedRecycler() {
//        if (timeWorkedList != null) {
//            val timeWorkedAdapter = WorOrderHistoryTimeWorkedAdapter(mView)
//            binding.rvTimeEntered.apply {
//                layoutManager = LinearLayoutManager(mView.context)
//                adapter = timeWorkedAdapter
//            }
//            timeWorkedAdapter.differ.submitList((timeWorkedList))
//        }
//    }

    private fun populateFromTempValues() {
        val tempWorkOrderInfo = mainViewModel.getTempWorkOrderHistoryInfo()!!
        workOrderViewModel.getWorkOrder(tempWorkOrderInfo.woHistoryWorkOrderNumber)
            .observe(viewLifecycleOwner) { workOrder ->
                curWorkOrder = workOrder
            }
        workOrderViewModel.getWorkOrderHistory(tempWorkOrderInfo.woHistoryId)
            .observe(viewLifecycleOwner) { history ->
                curHistoryDetailed = history
            }
        binding.apply {
            acWorkOrder.setText(tempWorkOrderInfo.woHistoryWorkOrderNumber)
            etRegHours.setText(nf.getNumberFromDouble(tempWorkOrderInfo.woHistoryRegHours))
            etOtHours.setText(nf.getNumberFromDouble(tempWorkOrderInfo.woHistoryOtHours))
            etDblOtHours.setText(nf.getNumberFromDouble(tempWorkOrderInfo.woHistoryDblOtHours))
            etNote.setText(tempWorkOrderInfo.woHistoryNote)
            if (tempWorkOrderInfo.woWorkPerformed.isNotEmpty()) {
                acWorkOrder.setText(tempWorkOrderInfo.woWorkPerformed)
            }
            if (tempWorkOrderInfo.woArea.isNotEmpty()) {
                acArea.setText(tempWorkOrderInfo.woArea)
            }
            if (tempWorkOrderInfo.woWorkPerformedNote.isNotEmpty()) {
                etWorkPerformedNote.setText(tempWorkOrderInfo.woWorkPerformedNote)
            }
            if (tempWorkOrderInfo.woMaterial.isNotEmpty()) {
                acMaterials.setText(tempWorkOrderInfo.woMaterial)
            }
            if (tempWorkOrderInfo.woMaterialQty != 0.0) {
                etMaterialQty.setText(nf.getNumberFromDouble((tempWorkOrderInfo.woMaterialQty)))
            }
        }
    }

    private fun setCurWorkOrder() {
        binding.apply {
            if (acWorkOrder.text.isNullOrBlank()) {
                displayMessage(
                    getString(R.string.error_) + getString(R.string.please_enter_a_valid_work_order_before_adding_work_performed)
                )
            }
            if (doesWorkOrderExist()) {
                populateWorkOrderInfo()
                btnWorkOrder.text = getString(R.string.edit)
                tvDescription.visibility = View.VISIBLE
            } else {
                btnWorkOrder.text = getString(R.string.create)
                tvDescription.visibility = View.INVISIBLE
            }
        }
    }

    private fun doesWorkOrderExist(): Boolean {
        for (workOrder in workOrderList) {
            if (binding.acWorkOrder.text.toString() == workOrder.woNumber) {
                curWorkOrder = workOrder
                return true
            }
        }
        return false
    }

    private fun populateWorkOrderInfo() {
        val display = curWorkOrder!!.woAddress + " | " + curWorkOrder!!.woDescription
        binding.apply {
            tvDescription.text = display
            tvDescription.visibility = View.VISIBLE
        }
    }

    private fun populateWorkPerformedRecycler(
        workPerFormedActualList: List<WorkOrderHistoryWorkPerformedCombined>
    ) {
        val workPerformedAdapter = WorKOrderHistoryWorkPerformedAdapter(
            mainActivity,
            curHistoryDetailed.history,
            mView,
            TAG,
            this@WorkOrderHistoryUpdateFragment,
        )
        binding.rvWorkPerformed.apply {
            layoutManager = LinearLayoutManager(mView.context)
            adapter = workPerformedAdapter
        }
        workPerformedAdapter.differ.submitList(
            workPerFormedActualList
        )
    }

    private fun populateMaterials() {
        workOrderViewModel.getMaterialsByHistory(curHistoryDetailed.history.woHistoryId)
            .observe(viewLifecycleOwner) { list ->
                val materialActualList = ArrayList<MaterialInSequence>()
                var seq = 0
                list.listIterator().forEach {
                    seq++
                    materialActualList.add(
                        MaterialInSequence(
                            it.workOrderHistoryMaterial.workOrderHistoryMaterialId,
                            getCurHistory().woHistoryId,
                            it.material.materialId,
                            it.material.mName,
                            it.workOrderHistoryMaterial.wohmQuantity,
                            seq
                        )
                    )
                }
                populateMaterialRecycler(materialActualList)
                determineMaterialSequence(list)
            }
    }

    private fun populateMaterialRecycler(
        materialActualList: ArrayList<MaterialInSequence>
    ) {
        val materialAdapter = WorkOrderHistoryMaterialAdapter(
            mainActivity, mView, TAG, this@WorkOrderHistoryUpdateFragment
        )
        binding.rvMaterials.apply {
            layoutManager = LinearLayoutManager(mView.context)
            adapter = materialAdapter
        }
        materialAdapter.differ.submitList(materialActualList)
    }


    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener { validateAllInfoAndUpdate() }
            btnWorkOrder.setOnClickListener {
                if (btnWorkOrder.text.toString() == getString(R.string.edit)) {
                    setOnWorkOrderSelected(acWorkOrder.text.toString())
                    mainViewModel.setCallingFragment(TAG)
                    gotoWorkOrderUpdateFragment()
                } else {
                    gotoWorkOrderAdd()
                }
            }
            acWorkOrder.setOnItemClickListener { _, _, _, _ -> setCurWorkOrder() }
            acWorkOrder.setOnLongClickListener {
                gotoWorkOrderLookup()
                true
            }
            acWorkPerformed.setOnItemClickListener { _, _, _, _ ->
                defaultScope.launch { setCurWorkPerformed() }
            }
            acMaterials.setOnItemClickListener { _, _, _, _ -> setCurMaterial() }
            acArea.setOnItemClickListener { _, _, _, _ ->
                defaultScope.launch { setCurArea() }
            }
            btnAddWorkPerformed.setOnClickListener { addWorkPerformedToHistoryIfNotBlank() }
            btnAddMaterial.setOnClickListener { addMaterialToHistoryIfNotBlank() }
            btnAddTime.setOnClickListener { gotoWorkOrderHistoryTime() }
            acWorkOrder.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
//                    null
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    null
                }

                override fun afterTextChanged(s: Editable?) {
                    setCurWorkOrder()
                }

            })
        }
    }

    private fun addWorkPerformedToHistoryIfNotBlank() {
        if (binding.acWorkPerformed.text.isNullOrBlank()) {
            displayMessage(
                getString(R.string.error_) + getString(R.string.please_enter_a_valid_description_of_work_performed_to_add_it)
            )
        } else {
            addWorkPerformedToHistoryIfPresent()
        }
    }

    private fun addMaterialToHistoryIfNotBlank() {
        if (binding.acMaterials.text.isNullOrBlank()) {
            displayMessage(
                getString(R.string.error_) + getString(R.string.please_enter_a_valid_material_description_to_add_it)
            )
        } else {
            addMaterialToHistoryIfValid()
        }
    }

    private fun validateAllInfoAndUpdate() {
        binding.apply {
            mainScope.launch {
                convertNumberStringsToDoubles()
                val isWorkOrderValid = async { validateWorkOrderNumber() }
                val isWorkPerformedValid = async { addWorkPerformedToHistoryIfPresent() }
                val isMaterialValid = async { addMaterialToHistoryIfValid() }
                if (isWorkOrderValid.await() && isWorkPerformedValid.await() && isMaterialValid.await()) {
                    updateHistory()
                    delay(WAIT_250)
                    gotoCallingFragment()
                } else {
                    displayMessage(getString(R.string.error_) + " " + getString(R.string.something_went_wrong))
                }
            }
        }
    }

    private fun validateWorkOrderNumber(): Boolean {
        if (doesWorkOrderExist()) {
            return true
        } else {
            AlertDialog.Builder(mView.context)
                .setTitle(getString(R.string.create_work_order_) + "${binding.acWorkOrder.text}?")
                .setMessage(getString(R.string.this_work_order_does_not_exist))
                .setPositiveButton(getString(R.string.yes)) { _, _ -> gotoWorkOrderAdd() }
                .setNegativeButton(getString(R.string.no), null).show()
            return false
        }
    }

    private fun gotoWorkOrderLookup() {
        setTempWorkOrderHistoryInfo()
        mainViewModel.addCallingFragment(TAG)
        gotoWorkOrderLookupFragment()
    }

    private fun gotoWorkOrderHistoryTime() {
        setTempWorkOrderHistoryInfo()
        mainViewModel.addCallingFragment(TAG)
        gotWorkOrderHistoryTimeFragment()
    }


    private fun gotoWorkOrderLookupFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections.actionWorkOrderHistoryUpdateFragmentToWorkOrderLookupFragment()
        )
    }

    private fun updateHistory() {
        try {
            mainViewModel.setTempWorkOrderHistoryInfo(null)
            val history = getCurHistory()
            history.apply {
                workOrderViewModel.updateWorkOrderHistory(
                    woHistoryId,
                    curWorkOrder!!.workOrderId,
                    woHistoryWorkDateId,
                    woHistoryRegHours,
                    woHistoryOtHours,
                    woHistoryDblOtHours,
                    woHistoryNote,
                    false,
                    df.getCurrentTimeAsString()
                )
            }
        } catch (e: SQLiteConstraintException) {
            AlertDialog.Builder(mView.context).setTitle(getString(R.string.something_went_wrong))
                .setMessage(getString(R.string.an_unknown_error_occurred_error_was) + e.toString())
                .setNeutralButton(getString(R.string.ok), null).show()
        }
    }

    private fun setOnWorkOrderSelected(woNumber: String) {
        mainViewModel.setWorkOrderNumber(woNumber)
        workOrderViewModel.getWorkOrder(woNumber).observe(
            viewLifecycleOwner
        ) { workOrder ->
            curWorkOrder = workOrder
            mainViewModel.setWorkOrder(curWorkOrder)
            setCurWorkOrder()
        }
        setTempWorkOrderHistoryInfo()
    }

    private fun determineWorkPerformedSequence(list: List<WorkOrderHistoryWorkPerformedCombined>) {
        if (list.isNotEmpty()) {
            workPerformedSequence = list.last().workOrderHistoryWorkPerformed.wowpSequence
        }
    }

    private fun determineMaterialSequence(list: List<WorkOrderHistoryMaterialCombined>) {
        if (list.isNotEmpty()) {
            materialSequence = list.last().workOrderHistoryMaterial.wohmSequence
        }
    }

    private fun setCurMaterial(): Boolean {
        binding.apply {
            for (material in materialListForAutoComplete) {
                if (acMaterials.text.toString() == material.mName && !acMaterials.text.isNullOrBlank()) {
                    curMaterial = material
                    return true
                }
            }
        }
        return false
    }

    private fun addMaterialToHistoryIfValid(): Boolean {
        if (setCurMaterial()) {
            try {
                addMaterialToHistory(curMaterial!!)
//                Log.d(TAG, "material ${curMaterial?.mName} was added to the history ")
            } catch (e: ExceptionUnknown) {
                Log.d("ExceptionUnknown", "Exception is $e")
                return false
            }
            return true
        } else if (!binding.acMaterials.text.isNullOrBlank()) {
            try {
                mainScope.launch {
                    val material = async { insertNewMaterialIntoDatabase() }
                    addMaterialToHistory(material.await())
//                    Log.d(TAG, "material ${curMaterial?.mName} was added to the history ")
                }
            } catch (e: ExceptionUnknown) {
                Log.d("ExceptionUnknown", "Exception is $e")
                return false
            }
            return true
        }
        return true
    }

    private fun addMaterialToHistory(material: Material) {
        binding.apply {
            materialSequence++
            try {
                workOrderViewModel.insertWorkOrderHistoryMaterial(
                    WorkOrderHistoryMaterial(
                        nf.generateRandomIdAsLong(),
                        curHistoryDetailed.history.woHistoryId,
                        material.materialId,
                        if (etMaterialQty.text.isNullOrBlank()) 1.0 else etMaterialQty.text.toString()
                            .trim().toDouble(),
                        materialSequence,
                        false,
                        df.getCurrentTimeAsString()
                    )
                )
                curMaterial = null
                acMaterials.text.clear()
                etMaterialQty.text.clear()
            } catch (e: SQLiteConstraintException) {
                AlertDialog.Builder(mView.context)
                    .setTitle(getString(R.string.something_went_wrong)).setMessage(
                        getString(R.string.check_to_see_if_this_material_has_already_been_added_) + " " + e.toString()
                    ).setNeutralButton(getString(R.string.ok), null).show()
            }
        }
    }

    private fun insertNewMaterialIntoDatabase(): Material {
        val material = Material(
            nf.generateRandomIdAsLong(),
            binding.acMaterials.text.toString().trim(),
            0.0,
            0.0,
            false,
            df.getCurrentTimeAsString()
        )
        workOrderViewModel.insertMaterial(material)
        return material
    }

    private fun addWorkPerformedToHistoryIfPresent(): Boolean {
        binding.apply {
            if (!acWorkPerformed.text.isNullOrBlank()) {
                try {
                    mainScope.launch {
                        val workPerformed = async {
                            return@async if (setCurWorkPerformed()) {
                                curWorkPerformed!!
                            } else {
                                insertNewWorkPerformedIntoDatabase()!!
                            }
                        }
                        val area = async {
                            return@async if (setCurArea()) {
                                curArea!!
                            } else if (acArea.text.isNullOrBlank()) {
                                null
                            } else {
                                insertAreaIntoDatabase(acArea.text.toString().trim())
                            }
                        }
                        val combinedWorkPerformedIsUnique = async {
                            isCombinedWorkPerformedUnique(
                                workPerformed.await(),
                                area.await(),
                            )
                        }
                        if (combinedWorkPerformedIsUnique.await()) {
                            addWorkPerformedToHistory(workPerformed.await(), area.await())
//                            Log.d(
//                                TAG,
//                                "work performed: ${workPerformed.await().wpDescription} was added to the history"
//                            )
                        } else {
                            throw ExceptionUnknown("Something went wrong!")
                        }
                    }
                } catch (e: ExceptionUnknown) {
                    Log.d("ExceptionUnknown", "Exception is $e")
                    return false
                }
            }
        }
        return true
    }

    private fun isCombinedWorkPerformedUnique(
        workPerformed: WorkPerformed, area: Areas?
    ): Boolean {
        for (combinedWorkPerformed in existingWorkPerformedListForValidation) {
            if (workPerformed.wpDescription == combinedWorkPerformed.workPerformed.wpDescription && area?.areaName == combinedWorkPerformed.area?.areaName) {
                displayMessage(
                    getString(R.string.error_) + getString(R.string.this_work_description_and_area_is_already_used)
                )
                return false
            }
        }
        return true
    }

    private fun displayMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private suspend fun addWorkPerformedToHistory(
        workPerformed: WorkPerformed, area: Areas?
    ) {
        workPerformedSequence++
        binding.apply {
            val note = if (etWorkPerformedNote.text.isNullOrBlank()) {
                ""
            } else {
                etWorkPerformedNote.text.toString().trim()
            }
            try {
                workOrderViewModel.insertWorkOrderHistoryWorkPerformed(
                    WorkOrderHistoryWorkPerformed(
                        nf.generateRandomIdAsLong(),
                        curHistoryDetailed.history.woHistoryId,
                        workPerformed.workPerformedId,
                        area?.areaId,
                        note,
                        workPerformedSequence,
                        false,
                        df.getCurrentTimeAsString()
                    )
                )
            } catch (e: SQLiteConstraintException) {
                AlertDialog.Builder(mView.context)
                    .setTitle(getString(R.string.something_went_wrong)).setMessage(
                        getString(R.string.check_to_see_if_this_work_was_already_entered_) + " " + e.toString()
                    ).setNeutralButton(getString(R.string.ok), null).show()
            }
            resetWorkPerformedValues()
        }
    }

    private suspend fun resetWorkPerformedValues() = withContext(Dispatchers.Main) {
        curWorkPerformed = null
        binding.apply {
            acWorkPerformed.text.clear()
            etWorkPerformedNote.text.clear()
        }
    }

    private fun insertAreaIntoDatabase(areaName: String): Areas {
        val newArea = Areas(
            nf.generateRandomIdAsLong(), areaName, false, df.getCurrentTimeAsString()
        )
        mainActivity.workOrderViewModel.insertArea(newArea)
        return newArea
    }

    private fun insertNewWorkPerformedIntoDatabase(): WorkPerformed? {
        try {
            val workPerformed = WorkPerformed(
                nf.generateRandomIdAsLong(),
                binding.acWorkPerformed.text.toString(),
                false,
                df.getCurrentTimeAsString()
            )
            workOrderViewModel.insertWorkPerformed(workPerformed)
            return workPerformed
        } catch (e: SQLiteConstraintException) {
            Log.d(TAG, "error is ${e.toString()}")
            return null
        }
    }

    private fun setCurWorkPerformed(): Boolean {
        binding.apply {
            if (!acWorkPerformed.text.isNullOrBlank()) {
                for (workPerformed in workPerformedListForAutoComplete) {
                    if (acWorkPerformed.text.toString().trim() == workPerformed.wpDescription) {
                        curWorkPerformed = workPerformed
                        return true
                    }
                }
            }
        }
        curWorkPerformed = null
        return false
    }

    private fun setCurArea(): Boolean {
        binding.apply {
            for (area in areaListForAutoComplete) {
                if (acArea.text.toString() == area.areaName && !acArea.text.isNullOrBlank()) {
                    curArea = area
                    return true
                }
            }
        }
        return false
    }

    override fun setTempWorkOrderHistoryInfo() {
        binding.apply {
            mainViewModel.setTempWorkOrderHistoryInfo(
                TempWorkOrderHistoryInfo(
                    curHistoryDetailed.history.woHistoryId,
                    if (acWorkOrder.text.isNullOrBlank()) "0" else acWorkOrder.text.toString()
                        .trim(),
                    lblDate.text.toString(),
                    if (etRegHours.text.isNullOrBlank()) 0.0 else etRegHours.text.toString()
                        .toDouble(),
                    if (etOtHours.text.isNullOrBlank()) 0.0 else etOtHours.text.toString()
                        .toDouble(),
                    if (etDblOtHours.text.isNullOrBlank()) 0.0 else etDblOtHours.text.toString()
                        .toDouble(),
                    if (etNote.text.isNullOrBlank()) "" else etNote.text.toString(),
                    if (acWorkPerformed.text.isNullOrBlank()) "" else acWorkPerformed.text.toString()
                        .trim(),
                    if (acArea.text.isNullOrBlank()) "" else acArea.text.toString().trim(),
                    if (etWorkPerformedNote.text.isNullOrBlank()) "" else etWorkPerformedNote.text.toString()
                        .trim(),
                    if (etMaterialQty.text.isNullOrBlank()) 0.0 else etMaterialQty.text.toString()
                        .trim().toDouble(),
                    if (acMaterials.text.isNullOrBlank()) "" else acMaterials.text.toString().trim()
                )
            )
        }
    }

    private fun getCurHistory(): WorkOrderHistory {
        binding.apply {
            return WorkOrderHistory(
                curHistoryDetailed.history.woHistoryId,
                curWorkOrder!!.workOrderId,
                workDateObject!!.workDateId,
                if (etRegHours.text.isNullOrBlank()) 0.0 else etRegHours.text.toString().toDouble(),
                if (etOtHours.text.isNullOrBlank()) 0.0 else etOtHours.text.toString().toDouble(),
                if (etDblOtHours.text.isNullOrBlank()) 0.0 else etDblOtHours.text.toString()
                    .toDouble(),
                if (etNote.text.isNullOrBlank()) null else etNote.text.toString(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun convertNumberStringsToDoubles() {
        binding.apply {
            etRegHours.setText(
                if (etRegHours.text.isNullOrBlank()) {
                    getString(R.string.zero_double)
                } else {
                    etRegHours.text.toString().toDouble().toString()
                }
            )
            etOtHours.setText(
                if (etOtHours.text.isNullOrBlank()) {
                    getString(R.string.zero_double)
                } else {
                    etOtHours.text.toString().toDouble().toString()
                }
            )
            etDblOtHours.setText(
                if (etDblOtHours.text.isNullOrBlank()) {
                    getString(R.string.zero_double)
                } else {
                    etDblOtHours.text.toString().toDouble().toString()
                }
            )
        }
    }

    private fun gotoCallingFragment() {
        gotoWorkDateUpdateFragment()
    }

    private fun gotoWorkDateUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections.actionWorkOrderHistoryUpdateFragmentToWorkDateUpdateFragment()
        )
    }

    private fun gotoWorkOrderUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections.actionWorkOrderHistoryUpdateFragmentToWorkOrderUpdateFragment()
        )
    }

    private fun gotoWorkOrderAdd() {
        setTempWorkOrderHistoryInfo()
        mainViewModel.setCallingFragment(TAG)
        gotoWorkOrderAddFragment()
    }

    private fun gotoWorkOrderAddFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections.actionWorkOrderHistoryUpdateFragmentToWorkOrderAddFragment()
        )
    }

    fun gotoWorkOrderHistoryWorkPerformedUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections.actionWorkOrderHistoryUpdateFragmentToWorkOrderHistoryWorkPerformedUpdateFragment()
        )
    }

    fun gotoAreaUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections.actionWorkOrderHistoryUpdateFragmentToAreaUpdateFragment()
        )
    }

    fun gotoWorkPerformedUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections.actionWorkOrderHistoryUpdateFragmentToWorkPerformedUpdateFragment()
        )
    }

    override fun gotoMaterialUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections.actionWorkOrderHistoryUpdateFragmentToMaterialUpdateFragment()
        )
    }

    override fun gotoMaterialQuantityUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections.actionWorkOrderHistoryUpdateFragmentToMaterialQuantityUpdateFragment()
        )
    }

    override fun gotoWorkOrderHistoryMaterialUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections.actionWorkOrderHistoryUpdateFragmentToWorkOrderHistoryMaterialUpdateFragment()
        )
    }

    private fun gotWorkOrderHistoryTimeFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections.actionWorkOrderHistoryUpdateFragmentToWorkOrderHistoryTime()
        )
    }

    override fun onDestroy() {
        mainScope.cancel()
        defaultScope.cancel()
        super.onDestroy()
        _binding = null
    }
}