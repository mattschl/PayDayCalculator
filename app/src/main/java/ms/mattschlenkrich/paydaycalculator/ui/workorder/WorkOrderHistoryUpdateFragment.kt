package ms.mattschlenkrich.paydaycalculator.ui.workorder

import android.app.AlertDialog
import android.os.Bundle
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ODER_HISTORY_UPDATE
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.Material
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.MaterialInSequence
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistoryMaterialCombined
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistoryWorkPerformedCombined
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkPerformed
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkPerformedInSequence
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderHistoryBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.workorder.adapter.WorKOrderHistoryWorkPerformedAdapter
import ms.mattschlenkrich.paydaycalculator.ui.workorder.adapter.WorkOrderHistoryMaterialAdapter

private const val TAG = FRAG_WORK_ODER_HISTORY_UPDATE

class WorkOrderHistoryUpdateFragment :
    Fragment(R.layout.fragment_work_order_history) {

    private var _binding: FragmentWorkOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val workOrderList = ArrayList<WorkOrder>()
    private val workOrderListForAutocomplete = ArrayList<String>()
    private var workDateObject: WorkDates? = null
    private lateinit var curEmployer: Employers
    private lateinit var curHistoryDetailed: WorkOrderHistoryWithDates
    private var curWorkOrder: WorkOrder? = null
    private var workPerformedListForAutoComplete =
        ArrayList<WorkPerformed>()
    private var curWorkPerformed: WorkPerformed? = null
    private var workPerformedSequence = 0
    private var materialListForAutoComplete =
        ArrayList<Material>()
    private var curMaterial: Material? = null
    private var materialSequence = 0
    private lateinit var commonFunctions:
            WorkOrderCommonFunctions

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderHistoryBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        commonFunctions =
            WorkOrderCommonFunctions(mainActivity)
        mainActivity.title = getString(R.string.add_time_to_work_order)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateInitialValues()
        setClickActions()
    }

    private fun populateInitialValues() {
        populateWorkPerformedListForAutoComplete()
        populateMaterialListForAutoComplete()
        unHideMaterialAndWorkPerformed()
        populateWorkDate()
        if (workDateObject != null) {
            populateCurrentEmployer()
        }
        if (commonFunctions.getWorkOrderHistory() != null) {
            populateFromHistory()
        }
        if (mainActivity.mainViewModel.getTempWorkOrderHistoryInfo() != null) {
            populateFromTempValues()
        }
        populateWorkOrderListForAutoComplete()
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
                materialListForAutoComplete.clear()
                val materialListNames = ArrayList<String>()
                list.listIterator().forEach {
                    materialListForAutoComplete.add(it)
                    materialListNames.add(it.mName)
                }
                val mAdapter = ArrayAdapter(
                    mView.context,
                    R.layout.spinner_item_normal,
                    materialListNames
                )
                binding.acMaterials.setAdapter(mAdapter)
            }
    }

    private fun populateWorkPerformed() {
        mainActivity.workOrderViewModel.getWorkPerformedCombinedByWorkOrderHistory(
            curHistoryDetailed.history.woHistoryId
        ).observe(viewLifecycleOwner) { list ->
            val workPerFormedActualList =
                ArrayList<WorkPerformedInSequence>()
            var seq = 0
            list.listIterator().forEach {
                seq++
                workPerFormedActualList.add(
                    WorkPerformedInSequence(
                        it.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId,
                        it.workPerformed.workPerformedId,
                        it.workPerformed.wpDescription,
                        seq
                    )
                )
            }
            populateWorkPerformedRecycler(workPerFormedActualList)
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
                        populateWorkPerformed()
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
        mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(null)
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
            acWorkOrder.setText(
                tempWorkOrderInfo.woHistoryWorkOrderNumber
            )
            etRegHours.setText(
                nf.getNumberFromDouble(
                    tempWorkOrderInfo.woHistoryRegHours
                )
            )
            etOtHours.setText(
                nf.getNumberFromDouble(
                    tempWorkOrderInfo.woHistoryOtHours
                )
            )
            etDblOtHours.setText(
                nf.getNumberFromDouble(
                    tempWorkOrderInfo.woHistoryDblOtHours
                )
            )
            etNote.setText(
                tempWorkOrderInfo.woHistoryNote
            )
            if (tempWorkOrderInfo.woWorkPerformed.isNotEmpty()) {
                acWorkOrder.setText(
                    tempWorkOrderInfo.woWorkPerformed
                )
            }
            if (tempWorkOrderInfo.woMaterial.isNotEmpty()) {
                acMaterials.setText(
                    tempWorkOrderInfo.woMaterial
                )
            }
            if (tempWorkOrderInfo.woMaterialQty != 0.0) {
                etMaterialQty.setText(
                    nf.getNumberFromDouble(
                        (tempWorkOrderInfo.woMaterialQty)
                    )
                )
            }
        }
    }

    private fun populateWorkOrderListForAutoComplete() {
        mainActivity.workOrderViewModel.getWorkOrdersByEmployerId(
            workDateObject!!.wdEmployerId
        ).observe(viewLifecycleOwner) { list ->
            workOrderList.clear()
            workOrderListForAutocomplete.clear()
            list.listIterator().forEach {
                workOrderList.add(it)
                workOrderListForAutocomplete.add(it.woNumber)
            }
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
                workPerformedListForAutoComplete.clear()
                val workPerformedDescriptions = ArrayList<String>()
                list.listIterator().forEach {
                    workPerformedListForAutoComplete.add(it)
                    workPerformedDescriptions.add(it.wpDescription)
                }
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
                Toast.makeText(
                    mView.context,
                    "Please enter a valid work order before adding work performed",
                    Toast.LENGTH_LONG
                ).show()
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

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                validateWorkOrderNumberAndPrepareToUpdate()
            }
            btnWorkOrder.setOnClickListener {
                setOnWorkOrderSelected(acWorkOrder.text.toString())
                mainActivity.mainViewModel.setCallingFragment(TAG)
                gotoWorkOrderUpdateFragment()
            }
            acWorkOrder.setOnItemClickListener { _, _, _, _ ->
                setCurWorkOrder()
            }
            acWorkOrder.setOnKeyListener { _, _, _ ->
                setCurWorkOrder()
                false
            }
            acWorkPerformed.setOnItemClickListener { _, _, _, _ ->
                setCurWorkPerformed()
            }
            btnAddWorkPerformed.setOnClickListener {
                saveWorkPerformedIfValidAndAddToWorkOrder()
            }
            acMaterials.setOnItemClickListener { _, _, _, _ ->
                setCurMaterial()
            }
            btnAddMaterial.setOnClickListener {
                saveMaterialIfValidAndAddToWorkOrder()
            }
        }
    }

    private fun validateWorkOrderNumberAndPrepareToUpdate() {
        if (doesWorkOrderExist()) {
            updateHistoryIfValid()
        } else {
            AlertDialog.Builder(mView.context)
                .setTitle(
                    "Create Work Order: " +
                            "${binding.acWorkOrder.text}?"
                )
                .setMessage(
                    "This Work Order does not exist." +
                            "Would you like to create a new one?"
                )
                .setPositiveButton("Yes") { _, _ ->
                    gotoWorkOrderAddFragment()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun updateHistoryIfValid() {
        val answer = validateHistory()
        if (answer == ANSWER_OK) {
            updateHistory()
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateHistory() {
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

    private fun determineWorkPerformedSequence(
        list: List<WorkOrderHistoryWorkPerformedCombined>
    ) {
        if (list.isNotEmpty()) {
            workPerformedSequence =
                list.last().workOrderHistoryWorkPerformed.wowpSequence
        }
    }

    private fun populateWorkPerformedRecycler(
        workPerFormedActualList: ArrayList<WorkPerformedInSequence>
    ) {
        val workPerformedAdapter =
            WorKOrderHistoryWorkPerformedAdapter(
                mainActivity,
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
        val materialAdapter =
            WorkOrderHistoryMaterialAdapter(
                mainActivity,
                mView
            )
        binding.rvMaterials.apply {
            layoutManager = LinearLayoutManager(
                mView.context
            )
            adapter = materialAdapter
        }
        materialAdapter.differ.submitList(
            materialActualList
        )
    }

    private fun determineMaterialSequence(
        list: List<WorkOrderHistoryMaterialCombined>
    ) {
        if (list.isNotEmpty()) {
            materialSequence =
                list.last().workOrderHistoryMaterial.wohmSequence
        }
    }

    private fun setCurMaterial(): Boolean {
        for (material in materialListForAutoComplete) {
            if (binding.acMaterials.text.toString() ==
                material.mName
            ) {
                curMaterial = material
                return true
            }
        }
        return false
    }

    private fun saveMaterialIfValidAndAddToWorkOrder() {
        if (binding.acMaterials.text.isNullOrBlank()) {
            Toast.makeText(
                mView.context,
                "Please add a valid material to add it.",
                Toast.LENGTH_LONG
            ).show()
        } else if (setCurMaterial()) {
            addMaterialToHistory(curMaterial!!)
        } else {
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
        }
    }

    private fun insertNewMaterialIntoDatabase(): Material {
        val material =
            Material(
                nf.generateRandomIdAsLong(),
                binding.acMaterials.text.toString(),
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

    private fun saveWorkPerformedIfValidAndAddToWorkOrder() {
        if (binding.acWorkPerformed.text.isNullOrBlank()) {
            Toast.makeText(
                mView.context,
                "Please add a valid description of work performed to add it.",
                Toast.LENGTH_LONG
            ).show()
        } else if (setCurWorkPerformed()) {
            addWorkPerformedToHistory(curWorkPerformed!!)
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                val workPerformed = insertNewWorkPerformedIntoDatabase()
                delay(WAIT_250)
                addWorkPerformedToHistory(workPerformed)
            }
        }
    }

    private fun addWorkPerformedToHistory(workPerformed: WorkPerformed) {
        workPerformedSequence++
        mainActivity.workOrderViewModel.insertWorkOrderHistoryWorkPerformed(
            WorkOrderHistoryWorkPerformed(
                nf.generateRandomIdAsLong(),
                curHistoryDetailed.history.woHistoryId,
                workPerformed.workPerformedId,
                workPerformedSequence,
                false,
                df.getCurrentTimeAsString()
            )
        )
        curWorkPerformed = null
        binding.acWorkPerformed.text.clear()
    }

    private fun insertNewWorkPerformedIntoDatabase(): WorkPerformed {
        val workPerformed =
            WorkPerformed(
                nf.generateRandomIdAsLong(),
                binding.acWorkPerformed.text.toString(),
                false,
                df.getCurrentTimeAsString()
            )
        mainActivity.workOrderViewModel.insertWorkPerformed(
            workPerformed
        )
        return workPerformed
    }

    private fun setCurWorkPerformed(): Boolean {
        for (workPerformed in workPerformedListForAutoComplete) {
            if (binding.acWorkPerformed.text.toString() ==
                workPerformed.wpDescription
            ) {
                curWorkPerformed = workPerformed
                return true
            }
        }
        return false
    }

    private fun setTempWorkOrderHistoryInfo() {
        binding.apply {
            mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(
                TempWorkOrderHistoryInfo(
                    if (acWorkOrder.text.isNullOrBlank())
                        "0" else acWorkOrder.text.toString(),
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
            etRegHours.setText(
                if (etRegHours.text.isNullOrBlank()) {
                    "0.0"
                } else {
                    etRegHours.text.toString().toDouble().toString()
                }
            )
            etOtHours.setText(
                if (etOtHours.text.isNullOrBlank()) {
                    "0.0"
                } else {
                    etOtHours.text.toString().toDouble().toString()
                }
            )
            etDblOtHours.setText(
                if (etDblOtHours.text.isNullOrBlank()) {
                    "0.0"
                } else {
                    etDblOtHours.text.toString().toDouble().toString()
                }
            )
            if (etRegHours.text.toString().toDouble() == 0.0
                && etOtHours.text.toString().toDouble() == 0.0
                && etDblOtHours.text.toString().toDouble() == 0.0
            ) {
                return "There was no time entered. Please enter some time."
            }
        }
        return ANSWER_OK
    }

    private fun gotoCallingFragment() {
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

    private fun gotoWorkOrderAddFragment() {
        setTempWorkOrderHistoryInfo()
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToWorkOrderAddFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}