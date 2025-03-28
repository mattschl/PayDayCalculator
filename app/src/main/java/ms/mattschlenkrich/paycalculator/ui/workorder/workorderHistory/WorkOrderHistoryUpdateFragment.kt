package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

import android.app.AlertDialog
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
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
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWorkPerformedCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderHistoryBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.WorkOrderCommonFunctions
import ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.adpater.WorKOrderHistoryWorkPerformedAdapter
import ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.adpater.WorkOrderHistoryMaterialAdapter

private const val TAG = FRAG_WORK_ORDER_HISTORY_UPDATE

class WorkOrderHistoryUpdateFragment :
    Fragment(R.layout.fragment_work_order_history), IWorkOrderHistoryUpdateFragment {

    private var _binding: FragmentWorkOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var workOrderList: List<WorkOrder>
    private var workDateObject: WorkDates? = null
    private lateinit var curEmployer: Employers
    private lateinit var curHistoryDetailed: WorkOrderHistoryWithDates
    private var curWorkOrder: WorkOrder? = null
    private lateinit var workPerformedListForAutoComplete: List<WorkPerformed>
    private lateinit var existingWorkPerformedListForValidation:
            List<WorkOrderHistoryWorkPerformedCombined>
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderHistoryBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        commonFunctions = WorkOrderCommonFunctions(mainActivity)
        mainActivity.title = getString(R.string.add_time_to_work_order)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateInitialValues()
        setClickActions()
    }

    private fun populateInitialValues() {
        CoroutineScope(Dispatchers.Main).launch {
            removeFragmentReferenceFromHistory()
            populateWorkPerformedListForAutoComplete()
            populateMaterialListForAutoComplete()
            populateAreaListForAutoComplete()
            unHideMaterialAndWorkPerformed()
            populateWorkDate()
            if (workDateObject != null) {
                populateCurrentEmployer()
            }
            delay(WAIT_100)
            populateWorkOrderListForAutoComplete()
            delay(WAIT_250)
            if (commonFunctions.getWorkOrderHistory() != null) {
                populateFromHistory()
            }
            delay(WAIT_100)
            if (mainActivity.mainViewModel.getTempWorkOrderHistoryInfo() != null) {
                populateFromTempValues()
            }
        }
    }

    private fun removeFragmentReferenceFromHistory() {
        mainActivity.mainViewModel.removeCallingFragment(TAG)
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
            binding.lblDate.text =
                df.getDisplayDate(workDateObject!!.wdDate)
        }
    }

    private fun populateCurrentEmployer() {
        if (mainActivity.mainViewModel.getEmployer() != null) {
            curEmployer =
                mainActivity.mainViewModel.getEmployer()!!
            binding.tvEmployers.text =
                curEmployer.employerName
        }
    }

    private fun populateMaterialListForAutoComplete() {
        mainActivity.workOrderViewModel.getMaterialsList()
            .observe(viewLifecycleOwner) { list ->
                materialListForAutoComplete = list
                val materialListNames = ArrayList<String>()
                list.listIterator().forEach { materialListNames.add(it.mName) }
                val mAdapter = ArrayAdapter(
                    mView.context,
                    R.layout.spinner_item_normal,
                    materialListNames
                )
                binding.acMaterials.setAdapter(mAdapter)
            }
    }

    private fun populateAreaListForAutoComplete() {
        mainActivity.workOrderViewModel.getAreasList()
            .observe(viewLifecycleOwner) { list ->
                areaListForAutoComplete = list
                val areaNameList = ArrayList<String>()
                list.listIterator().forEach { areaNameList.add(it.areaName) }
                val mAdapter = ArrayAdapter(
                    mView.context,
                    R.layout.spinner_item_normal,
                    areaNameList
                )
                binding.acArea.setAdapter(mAdapter)
            }
    }

    private fun populateWorkPerformedLists() {
        mainActivity.workOrderViewModel.getWorkPerformedCombinedByWorkOrderHistory(
            curHistoryDetailed.history.woHistoryId
        ).observe(viewLifecycleOwner) { list ->
            populateWorkPerformedRecycler(list)
            existingWorkPerformedListForValidation = list
            determineWorkPerformedSequence(list)
        }
    }

    private fun populateFromHistory() {
        val historyId =
            commonFunctions.getWorkOrderHistory()!!.woHistoryId
        mainActivity.workOrderViewModel.getWorkOrderHistory(historyId)
            .observe(viewLifecycleOwner) { history ->
                curHistoryDetailed = history
                binding.apply {
                    acWorkOrder.setText(history.workOrder.woNumber)
                    mainActivity.workOrderViewModel.getWorkOrder(
                        history.workOrder.woNumber
                    ).observe(viewLifecycleOwner) { workOrder ->
                        curWorkOrder = workOrder
                        populateWorkPerformedLists()
                        populateMaterials()
                    }
                    etRegHours.setText(
                        nf.getNumberFromDouble(history.history.woHistoryRegHours)
                    )
                    etOtHours.setText(
                        nf.getNumberFromDouble(history.history.woHistoryOtHours)
                    )
                    etDblOtHours.setText(
                        nf.getNumberFromDouble(history.history.woHistoryDblOtHours)
                    )
                    etNote.setText(history.history.woHistoryNote)
//                    btnWorkOrder.visibility = View.VISIBLE
                    btnWorkOrder.text = getString(R.string.edit)
                }
                if (mainActivity.mainViewModel.getWorkOrderNumber() != null) {
                    mainActivity.workOrderViewModel.getWorkOrder(
                        curHistoryDetailed.history.woHistoryWorkOrderId
                    ).observe(
                        viewLifecycleOwner
                    ) { workOrder ->
                        curWorkOrder = workOrder
                        setCurWorkOrder()
                    }
                }
            }
    }

    private fun populateFromTempValues() {
        val tempWorkOrderInfo =
            mainActivity.mainViewModel.getTempWorkOrderHistoryInfo()!!
        mainActivity.workOrderViewModel.getWorkOrder(
            tempWorkOrderInfo.woHistoryWorkOrderNumber
        ).observe(viewLifecycleOwner) { workOrder ->
            curWorkOrder = workOrder
        }
        binding.apply {
            acWorkOrder.setText(tempWorkOrderInfo.woHistoryWorkOrderNumber)
            etRegHours.setText(
                nf.getNumberFromDouble(tempWorkOrderInfo.woHistoryRegHours)
            )
            etOtHours.setText(
                nf.getNumberFromDouble(tempWorkOrderInfo.woHistoryOtHours)
            )
            etDblOtHours.setText(
                nf.getNumberFromDouble(tempWorkOrderInfo.woHistoryDblOtHours)
            )
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
                etMaterialQty.setText(
                    nf.getNumberFromDouble((tempWorkOrderInfo.woMaterialQty))
                )
            }
        }
    }

    private fun populateWorkOrderListForAutoComplete() {
        mainActivity.workOrderViewModel.getWorkOrdersByEmployerId(
            workDateObject!!.wdEmployerId
        ).observe(viewLifecycleOwner) { list ->
            workOrderList = list
            val workOrderListForAutocomplete = ArrayList<String>()
            list.listIterator().forEach { workOrderListForAutocomplete.add(it.woNumber) }
            binding.apply {
                val woAdapter = ArrayAdapter(
                    mView.context, R.layout.spinner_item_normal,
                    workOrderListForAutocomplete
                )
                acWorkOrder.setAdapter(woAdapter)
            }
        }
    }

    private fun populateWorkPerformedListForAutoComplete() {
        mainActivity.workOrderViewModel.getWorkPerformedAll()
            .observe(viewLifecycleOwner) { list ->
                workPerformedListForAutoComplete = list
                val workPerformedDescriptions = ArrayList<String>()
                list.listIterator().forEach { workPerformedDescriptions.add(it.wpDescription) }
                val wpAdapter = ArrayAdapter(
                    mView.context,
                    R.layout.spinner_item_normal,
                    workPerformedDescriptions
                )
                binding.acWorkPerformed.setAdapter(wpAdapter)
            }
    }

    private fun setCurWorkOrder() {
        binding.apply {
            if (acWorkOrder.text.isNullOrBlank()) {
                displayMessage(
                    getString(R.string.error_) +
                            getString(R.string.please_enter_a_valid_work_order_before_adding_work_performed)
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
        val display = curWorkOrder!!.woAddress +
                " | " + curWorkOrder!!.woDescription
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
            this@WorkOrderHistoryUpdateFragment,
            TAG,
            curHistoryDetailed.history,
            mView,
        )
        binding.rvWorkPerformed.apply {
            layoutManager = LinearLayoutManager(
                mView.context
            )
            adapter = workPerformedAdapter
        }
        workPerformedAdapter.differ.submitList(
            workPerFormedActualList
        )
    }

    private fun populateMaterials() {
        mainActivity.workOrderViewModel.getMaterialsByHistory(
            curHistoryDetailed.history.woHistoryId
        ).observe(viewLifecycleOwner) { list ->
            val materialActualList =
                ArrayList<MaterialInSequence>()
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
            mainActivity,
            mView,
            this@WorkOrderHistoryUpdateFragment
        )
        binding.rvMaterials.apply {
            layoutManager = LinearLayoutManager(
                mView.context
            )
            adapter = materialAdapter
        }
        materialAdapter.differ.submitList(materialActualList)
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener { validateWorkOrderNumberAndPrepareToUpdate() }
            btnWorkOrder.setOnClickListener {
                if (btnWorkOrder.text.toString() == getString(R.string.edit)) {
                    setOnWorkOrderSelected(acWorkOrder.text.toString())
                    mainActivity.mainViewModel.setCallingFragment(TAG)
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
                CoroutineScope(Dispatchers.Default).launch { setCurWorkPerformed() }
            }
            acMaterials.setOnItemClickListener { _, _, _, _ -> setCurMaterial() }
            acArea.setOnItemClickListener { _, _, _, _ ->
                CoroutineScope(Dispatchers.Default).launch { setCurArea() }
            }
            btnAddWorkPerformed.setOnClickListener {
                addWorkPerformedToWorkOrderIfValid(true)
            }
            btnAddMaterial.setOnClickListener {
                addMaterialToWorkOrderIfValid(true)
            }
            acWorkOrder.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
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

    private fun validateWorkOrderNumberAndPrepareToUpdate() {
        if (doesWorkOrderExist()) {
            updateHistoryIfValid()
        } else {
            AlertDialog.Builder(mView.context)
                .setTitle(
                    getString(R.string.create_work_order_) +
                            "${binding.acWorkOrder.text}?"
                )
                .setMessage(
                    getString(R.string.this_work_order_does_not_exist)
                )
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    gotoWorkOrderAdd()
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }
    }

    private fun gotoWorkOrderLookup() {
        setTempWorkOrderHistoryInfo()
        mainActivity.mainViewModel.setCallingFragment(
            mainActivity.mainViewModel.getCallingFragment() +
                    ", $TAG"
        )
        gotoWorkOrderLookupFragment()
    }

    private fun gotoWorkOrderLookupFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToWorkOrderLookupFragment()
        )
    }

    private fun updateHistoryIfValid() {
        val answer = validateHistory()
        if (answer == ANSWER_OK) {
            CoroutineScope(Dispatchers.Main).launch {
                addWorkPerformedToWorkOrderIfValid(false)
                addMaterialToWorkOrderIfValid(false)
                delay(WAIT_500)
                updateHistory()
            }
        } else {
            displayMessage(
                getString(R.string.error_) + answer
            )
        }
    }

    private fun updateHistory() {
        try {
            mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(null)
            val history = getCurHistory()
            mainActivity.workOrderViewModel.updateWorkOrderHistory(
                history.woHistoryId,
                curWorkOrder!!.workOrderId,
                history.woHistoryWorkDateId,
                history.woHistoryRegHours,
                history.woHistoryOtHours,
                history.woHistoryDblOtHours,
                history.woHistoryNote,
                false,
                history.woHistoryUpdateTime
            )
            gotoCallingFragment()
        } catch (e: SQLiteConstraintException) {
            AlertDialog.Builder(mView.context)
                .setTitle(getString(R.string.something_went_wrong))
                .setMessage(
                    getString(R.string.an_unknown_error_occurred_error_was) +
                            e.toString()
                )
                .setNeutralButton(getString(R.string.ok), null)
                .show()
        }
    }

    private fun setOnWorkOrderSelected(woNumber: String) {
        mainActivity.mainViewModel.setWorkOrderNumber(woNumber)
        mainActivity.workOrderViewModel.getWorkOrder(woNumber).observe(
            viewLifecycleOwner
        ) { workOrder ->
            curWorkOrder = workOrder
            mainActivity.mainViewModel.setWorkOrder(curWorkOrder)
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
                if (acMaterials.text.toString() ==
                    material.mName && !acMaterials.text.isNullOrBlank()
                ) {
                    curMaterial = material
                    return true
                }
            }
        }
        return false
    }

    private fun addMaterialToWorkOrderIfValid(showError: Boolean) {
        if (binding.acMaterials.text.isNullOrBlank()
        ) {
            if (showError) {
                displayMessage(
                    getString(R.string.error_) +
                            getString(R.string.please_enter_a_valid_material_description_to_add_it)
                )
            }
        } else if (setCurMaterial()) {
            addMaterialToHistory(curMaterial!!)
        } else if (!binding.acMaterials.text.isNullOrBlank()) {
            CoroutineScope(Dispatchers.Main).launch {
                val material = insertNewMaterialIntoDatabase()
                delay(WAIT_250)
                addMaterialToHistory(material)
            }
        }
    }

    private fun addMaterialToHistory(material: Material) {
        binding.apply {
            materialSequence++
            try {
                mainActivity.workOrderViewModel.insertWorkOrderHistoryMaterial(
                    WorkOrderHistoryMaterial(
                        nf.generateRandomIdAsLong(),
                        curHistoryDetailed.history.woHistoryId,
                        material.materialId,
                        if (etMaterialQty.text.isNullOrBlank()) 1.0 else
                            etMaterialQty.text.toString().trim().toDouble(),
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
                    .setTitle(getString(R.string.something_went_wrong))
                    .setMessage(
                        getString(R.string.check_to_see_if_this_material_has_already_been_added_) +
                                " " + e.toString()
                    )
                    .setNeutralButton(getString(R.string.ok), null)
                    .show()
            }
        }
    }

    private fun insertNewMaterialIntoDatabase(): Material {
        val material =
            Material(
                nf.generateRandomIdAsLong(),
                binding.acMaterials.text.toString().trim(),
                0.0,
                0.0,
                false,
                df.getCurrentTimeAsString()
            )
        mainActivity.workOrderViewModel.insertMaterial(
            material
        )
        return material
    }

    private fun addWorkPerformedToWorkOrderIfValid(showError: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.apply {
                if (acWorkPerformed.text.isNullOrBlank()) {
                    if (showError) {
                        displayMessage(
                            getString(R.string.error_) +
                                    getString(R.string.please_enter_a_valid_description_of_work_performed_to_add_it)
                        )
                    }
                } else {
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
                            insertAreaIntoDatabase(
                                acArea.text.toString().trim()
                            )
                        }
                    }
                    val workPerformedCombinedIsValid = async {
                        validateWorkPerformedCombined(
                            workPerformed.await(), area.await(), showError
                        )
                    }
                    if (workPerformedCombinedIsValid.await()) {
                        addWorkPerformedToHistory(
                            workPerformed.await(), area.await()
                        )
                    }
                }
            }
        }
    }

    private fun validateWorkPerformedCombined(
        workPerformed: WorkPerformed, area: Areas?, showError: Boolean
    ): Boolean {
        for (combinedWorkPerformed in existingWorkPerformedListForValidation) {
            if (workPerformed.wpDescription == combinedWorkPerformed.workPerformed.wpDescription &&
                area?.areaName == combinedWorkPerformed.area?.areaName
            ) {
                if (showError) {
                    displayMessage(
                        getString(R.string.error_) +
                                getString(R.string.this_work_description_and_area_is_already_used)
                    )
                }
                return false
            }
        }
        return true
    }

    private fun displayMessage(message: String) {
        Toast.makeText(
            mView.context,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

//    private fun validateWorkPerformed(): String {
//        if (!setCurWorkPerformed()) {
//            return getString(R.string.please_enter_a_valid_description_of_work_performed_to_add_it)
//        }
//        binding.apply {
//            val curWorkPerformed = acWorkPerformed.text.toString().trim()
//            val currArea = if (acArea.text.isNullOrBlank()) {
//                null
//            } else {
//                acArea.text.toString().trim()
//            }
//            for (workPerformed in existingWorkPerformedListForValidation) {
//                if (workPerformed.workPerformed.wpDescription == curWorkPerformed &&
//                    workPerformed.area?.areaName == currArea
//                ) {
//                    return getString(R.string.this_work_description_and_area_is_already_used)
//                }
//            }
//        }
//        return ANSWER_OK
//    }

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
                mainActivity.workOrderViewModel.insertWorkOrderHistoryWorkPerformed(
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
                    .setTitle(getString(R.string.something_went_wrong))
                    .setMessage(
                        getString(R.string.check_to_see_if_this_work_was_already_entered_) +
                                " " + e.toString()
                    )
                    .setNeutralButton(getString(R.string.ok), null)
                    .show()
            }
            resetWorkPerformedValues()
        }
    }

    private suspend fun resetWorkPerformedValues() =
        withContext(Dispatchers.Main) {
            curWorkPerformed = null
            binding.apply {
                acWorkPerformed.text.clear()
                etWorkPerformedNote.text.clear()
            }
        }

    private fun insertAreaIntoDatabase(areaName: String): Areas {
        val newArea = Areas(
            nf.generateRandomIdAsLong(),
            areaName,
            false,
            df.getCurrentTimeAsString()
        )
        mainActivity.workOrderViewModel.insertArea(newArea)
        return newArea
    }

    private fun insertNewWorkPerformedIntoDatabase(): WorkPerformed? {
        try {
            val workPerformed =
                WorkPerformed(
                    nf.generateRandomIdAsLong(),
                    binding.acWorkPerformed.text.toString(),
                    false,
                    df.getCurrentTimeAsString()
                )
            mainActivity.workOrderViewModel.insertWorkPerformed(workPerformed)
            return workPerformed
        } catch (e: SQLiteConstraintException) {
            return null
        }
    }

    private fun setCurWorkPerformed(): Boolean {
        binding.apply {
            if (!acWorkPerformed.text.isNullOrBlank()) {
                for (workPerformed in workPerformedListForAutoComplete) {
                    if (acWorkPerformed.text.toString().trim() ==
                        workPerformed.wpDescription
                    ) {
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
                if (acArea.text.toString() ==
                    area.areaName &&
                    !acArea.text.isNullOrBlank()
                ) {
                    curArea = area
                    return true
                }
            }
        }
        return false
    }

    override fun setTempWorkOrderHistoryInfo() {
        binding.apply {
            mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(
                TempWorkOrderHistoryInfo(
                    if (acWorkOrder.text.isNullOrBlank())
                        "0" else acWorkOrder.text.toString().trim(),
                    lblDate.text.toString(),
                    if (etRegHours.text.isNullOrBlank())
                        0.0 else etRegHours.text.toString().toDouble(),
                    if (etOtHours.text.isNullOrBlank())
                        0.0 else etOtHours.text.toString().toDouble(),
                    if (etDblOtHours.text.isNullOrBlank())
                        0.0 else etDblOtHours.text.toString().toDouble(),
                    if (etNote.text.isNullOrBlank())
                        "" else etNote.text.toString(),
                    if (acWorkPerformed.text.isNullOrBlank())
                        "" else acWorkPerformed.text.toString().trim(),
                    if (acArea.text.isNullOrBlank())
                        "" else acArea.text.toString().trim(),
                    if (etWorkPerformedNote.text.isNullOrBlank())
                        "" else etWorkPerformedNote.text.toString().trim(),
                    if (etMaterialQty.text.isNullOrBlank())
                        0.0 else etMaterialQty.text.toString().trim().toDouble(),
                    if (acMaterials.text.isNullOrBlank())
                        "" else acMaterials.text.toString().trim()
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
                if (etRegHours.text.isNullOrBlank())
                    0.0 else etRegHours.text.toString().toDouble(),
                if (etOtHours.text.isNullOrBlank())
                    0.0 else etOtHours.text.toString().toDouble(),
                if (etDblOtHours.text.isNullOrBlank())
                    0.0 else etDblOtHours.text.toString().toDouble(),
                if (etNote.text.isNullOrBlank())
                    null else etNote.text.toString(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun validateHistory(): String {
        binding.apply {
            convertNumberStringsToDoubles()
            if (etRegHours.text.toString().toDouble() == 0.0
                && etOtHours.text.toString().toDouble() == 0.0
                && etDblOtHours.text.toString().toDouble() == 0.0
            ) {
                return getString(R.string.there_was_no_time_entered_please_enter_some_time)
            }
        }
        return ANSWER_OK
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
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToWorkDateUpdateFragment()
        )
    }

    private fun gotoWorkOrderUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToWorkOrderUpdateFragment()
        )
    }

    private fun gotoWorkOrderAdd() {
        setTempWorkOrderHistoryInfo()
        mainActivity.mainViewModel.setCallingFragment(TAG)
        gotoWorkOrderAddFragment()
    }

    private fun gotoWorkOrderAddFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToWorkOrderAddFragment()
        )
    }

    fun gotoWorkOrderHistoryWorkPerformedUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToWorkOrderHistoryWorkPerformedUpdateFragment()
        )
    }

    fun gotoAreaUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToAreaUpdateFragment()
        )
    }

    fun gotoWorkPerformedUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToWorkPerformedUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}