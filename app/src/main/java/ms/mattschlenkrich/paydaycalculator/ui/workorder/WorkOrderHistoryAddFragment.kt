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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.Material
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkPerformed
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderHistoryBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

private const val TAG = FRAG_WORK_ORDER_HISTORY_ADD

class WorkOrderHistoryAddFragment :
    Fragment(R.layout.fragment_work_order_history) {

    private var _binding: FragmentWorkOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val workOrderList =
        ArrayList<WorkOrder>()
    private val workOrderListForAutocomplete =
        ArrayList<String>()
    private var workDateObject: WorkDates? = null
    private var curEmployer: Employers? = null
    private var curWorkOrder: WorkOrder? = null
    private lateinit var workPerformedListForAutoComplete:
            ArrayList<WorkPerformed>
    private var curWorkPerformed: WorkPerformed? = null
    private var workPerformedSequence = 0
    private var materialListForAutoComplete =
        ArrayList<Material>()
    private var curMaterial: Material? = null
    private var materialSequence = 0
    private lateinit var curHistory: WorkOrderHistory
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
        populateValues()
        setClickActions()
    }

    private fun setCurWorkOrder() {
        binding.apply {
            if (acWorkOrder.text.isNullOrBlank()) {
                Toast.makeText(
                    mView.context,
                    "Please enter a valid work order before adding work performed",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (curWorkOrder == null) {
                    mainActivity.workOrderViewModel.getWorkOrder(
                        acWorkOrder.text.toString()
                    ).observe(viewLifecycleOwner) { workOrder ->
                        curWorkOrder = workOrder
                        populateWorkOrderInfo()
                    }
                }// else {
//                    validateWorkOrderNumber()
//                }
            }
        }
    }

    private fun populateWorkOrderInfo() {
        binding.apply {
            if (curWorkOrder != null) {
                val display =
                    curWorkOrder!!.woAddress + " - " +
                            curWorkOrder!!.woNumber
                tvDescription.text = display
                tvDescription.visibility = View.VISIBLE
                btnEditWorkOrder.visibility = View.VISIBLE
            }
        }
    }

    private fun populateValues() {
        populateWorkDate()
        if (workDateObject != null) {
            populateCurrentEmployer()
        }
        populateTempWorkOrderInfo()
        populateWorkOrderListInAutoComplete()
        populateWorkPerformedListInAutoComplete()
        populateMaterialListForAutocomplete()
    }

    private fun populateMaterialListForAutocomplete() {
        val materialList =
            getMaterialListForAutoComplete()
        val mAdapter = ArrayAdapter(
            mView.context,
            R.layout.spinner_item_normal,
            materialList
        )
        binding.acMaterials.setAdapter(mAdapter)
    }

    private fun getMaterialListForAutoComplete(): ArrayList<String> {
        val materialNameList = ArrayList<String>()
        mainActivity.workOrderViewModel.getMaterialsList()
            .observe(viewLifecycleOwner) { list ->
                materialListForAutoComplete.clear()
                materialNameList.clear()
                list.listIterator().forEach {
                    materialNameList.add(it.mName)
                    materialListForAutoComplete.add(it)
                }
            }
        return materialNameList
    }

    private fun populateWorkPerformedListInAutoComplete() {
        val workPerformedDescriptions =
            getWorkPerformedListForAutoComplete()
        val wpAdapter = ArrayAdapter(
            mView.context,
            R.layout.spinner_item_normal,
            workPerformedDescriptions
        )
        binding.acWorkPerformed.setAdapter(wpAdapter)

    }

    private fun getWorkPerformedListForAutoComplete():
            ArrayList<String> {
        val newList = ArrayList<String>()
        mainActivity.workOrderViewModel.getWorkPerformedAll()
            .observe(viewLifecycleOwner) { list ->
                list.listIterator().forEach {
                    workPerformedListForAutoComplete = list as ArrayList
                    newList.add(it.wpDescription)
                }
            }
        return newList
    }

    private fun populateTempWorkOrderInfo() {
        if (mainActivity.mainViewModel.getTempWorkOrderHistoryInfo() != null) {
            binding.apply {
                val tempWorkOrderHistory =
                    mainActivity.mainViewModel.getTempWorkOrderHistoryInfo()!!
                if (mainActivity.mainViewModel.getWorkOrderNumber() != null) {
                    acWorkOrder.setText(
                        mainActivity.mainViewModel.getWorkOrderNumber()!!
                    )
                } else {
                    acWorkOrder.setText(
                        tempWorkOrderHistory.woHistoryWorkOrderNumber
                    )
                }
                etRegHours.setText(
                    nf.getNumberFromDouble(
                        tempWorkOrderHistory.woHistoryRegHours
                    )
                )
                etOtHours.setText(
                    nf.getNumberFromDouble(
                        tempWorkOrderHistory.woHistoryOtHours
                    )
                )
                etDblOtHours.setText(
                    nf.getNumberFromDouble(
                        tempWorkOrderHistory.woHistoryDblOtHours
                    )
                )
                etNote.setText(
                    tempWorkOrderHistory.woHistoryNote
                )
                acWorkPerformed.apply {
                    isEnabled = false
                    setText(
                        tempWorkOrderHistory.woWorkPerformed
                    )
                    isEnabled = true
                }
                setCurWorkOrder()
            }
        }
    }

    private fun populateWorkDate() {
        workDateObject = commonFunctions.getWorkDateObject()
        if (workDateObject != null) {
            binding.lblDate.text = df.getDisplayDate(workDateObject!!.wdDate)
        }
    }

    private fun populateCurrentEmployer() {
        curEmployer = commonFunctions.getEmployer()
        if (curEmployer != null) {
            binding.tvEmployers.text =
                curEmployer!!.employerName
        }
    }

    private fun populateWorkOrderListInAutoComplete() {
        getWorkOrderLists()
        binding.apply {
            val woAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_normal, workOrderListForAutocomplete
            )
            acWorkOrder.setAdapter(woAdapter)
        }
    }

    private fun getWorkOrderLists() {
        if (workDateObject != null) {
            mainActivity.workOrderViewModel.getWorkOrdersByEmployerId(
                workDateObject!!.wdEmployerId
            ).observe(viewLifecycleOwner) { list ->
                workOrderList.clear()
                workOrderListForAutocomplete.clear()
                list.listIterator().forEach {
                    workOrderList.add(it)
                    workOrderListForAutocomplete.add(it.woNumber)
                }
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                validateWorkOrderNumber()
            }
            btnEditWorkOrder.setOnClickListener {
                gotoWorkOrderUpdateFragment()
            }
            acWorkOrder.setOnItemClickListener { _, _, _, _ ->
                setCurWorkOrder()
            }
            acWorkPerformed.setOnClickListener {
                gotoWorkOrderAddOrUpdateFragment(addWorkPerformed = false, addMaterial = false)
            }
            btnAddWorkPerformed.setOnClickListener {
                gotoWorkOrderAddOrUpdateFragment(addWorkPerformed = true, addMaterial = false)
            }
            acMaterials.setOnClickListener {
                gotoWorkOrderAddOrUpdateFragment(addWorkPerformed = false, addMaterial = false)
            }
            btnAddMaterial.setOnClickListener {
                gotoWorkOrderAddOrUpdateFragment(addWorkPerformed = false, addMaterial = true)
            }
        }
    }

    private fun gotoWorkOrderAddOrUpdateFragment(addWorkPerformed: Boolean, addMaterial: Boolean) {
        if (doesWorkOrderExist()) {
            saveCurrentHistoryIfValidAndGotoUpdateFragment(addWorkPerformed, addMaterial)
        } else {
            validateWorkOrderNumber()
        }
    }

    private fun saveCurrentHistoryIfValidAndGotoUpdateFragment(
        addWorkPerformed: Boolean,
        addMaterial: Boolean
    ) {
        binding.apply {
            if (acWorkOrder.text.isNullOrBlank()) {
                Toast.makeText(
                    mView.context,
                    "Please enter a valid work order number before adding work performed",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                saveHistoryIfValid(true)
                if (addWorkPerformed) {
                    saveWorkPerformedIfValidAndAddToWorkOrder()
                }
                if (addMaterial) {
                    saveMaterialIfValidAndAddToWorkOrder()
                }
            }
        }
    }

    private fun saveMaterialIfValidAndAddToWorkOrder() {
        if (binding.acMaterials.text.isNullOrBlank()) {
            Toast.makeText(
                mView.context,
                "Please add a valid material to add it.",
                Toast.LENGTH_LONG
            ).show()
        } else if (setCurMaterial()) {
            addMaterialToWorkOrder(curMaterial!!)
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                curMaterial = insertMaterial()
                delay(WAIT_250)
                addMaterialToWorkOrder(curMaterial!!)
            }
        }
    }

    private fun insertMaterial(): Material {
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

    private fun addMaterialToWorkOrder(material: Material) {
        materialSequence++
        mainActivity.workOrderViewModel.insertWorkOrderHistoryMaterial(
            WorkOrderHistoryMaterial(
                nf.generateRandomIdAsLong(),
                curHistory.woHistoryId,
                material.materialId,
                if (binding.etMaterialQty.text.isNullOrBlank()) 1.0
                else binding.acMaterials.text.toString().toDouble(),
                materialSequence,
                false,
                df.getCurrentTimeAsString()
            )
        )
    }

    private fun setCurMaterial(): Boolean {
        for (material in materialListForAutoComplete) {
            if (binding.acMaterials.text.toString().trim() ==
                material.mName
            ) {
                curMaterial = material
                return true
            }
        }
        return false
    }

    private fun saveWorkPerformedIfValidAndAddToWorkOrder() {
        binding.apply {
            if (acWorkPerformed.text.isNullOrBlank()) {
                Toast.makeText(
                    mView.context,
                    "Please add a valid description of work performed to add it.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (setCurWorkPerformed()) {
                addWorkPerformedToWorkOrder(curWorkPerformed!!)
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    val workPerformed = insertNewWorkPerformed()
                    delay(WAIT_250)
                    addWorkPerformedToWorkOrder(workPerformed)
                }
            }
        }
    }

    private fun insertNewWorkPerformed(): WorkPerformed {
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

    private fun addWorkPerformedToWorkOrder(workPerformed: WorkPerformed) {
        workPerformedSequence++
        mainActivity.workOrderViewModel.insertWorkOrderHistoryWorkPerformed(
            WorkOrderHistoryWorkPerformed(
                nf.generateRandomIdAsLong(),
                curHistory.woHistoryId,
                workPerformed.workPerformedId,
                workPerformedSequence,
                false,
                df.getCurrentTimeAsString()
            )
        )
        curWorkPerformed = null
        binding.acWorkPerformed.text.clear()
    }

    private fun setCurWorkPerformed(): Boolean {
        binding.apply {
            for (workPerformed in workPerformedListForAutoComplete) {
                if (acWorkPerformed.text.toString() ==
                    workPerformed.wpDescription
                ) {
                    curWorkPerformed = workPerformed
                    return true
                }
            }
        }
        return false
    }

    private fun validateWorkOrderNumber() {
        if (doesWorkOrderExist()) {
            saveHistoryIfValid(false)
        } else {
            AlertDialog.Builder(mView.context)
                .setTitle(
                    "Create Work Order: " +
                            "${binding.acWorkOrder.text}?"
                )
                .setMessage(
                    "This Work Order does not exist. " +
                            "It must be created before continuing. " +
                            "Would you like to create it now?"
                )
                .setPositiveButton("Yes") { _, _ ->
                    gotoWorkOrderAddFragment()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun doesWorkOrderExist(): Boolean {
        for (workOrder in workOrderList) {
            if (binding.acWorkOrder.text.toString() == workOrder.woNumber) {
                setCurWorkOrder()
                return true
            }
        }
        return false
    }

    private fun gotoWorkOrderAddFragment() {
        setTempWorkOrderHistory()
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections
                .actionWorkOrderHistoryAddFragmentToWorkOrderAddFragment()
        )
    }

    private fun setTempWorkOrderHistory() {
        binding.apply {
            mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(
                TempWorkOrderHistoryInfo(
                    if (acWorkOrder.text.isNullOrBlank())
                        "00" else acWorkOrder.text.toString(),
                    lblDate.text.toString(),
                    if (etRegHours.text.isNullOrBlank())
                        0.0 else etRegHours.text.toString().trim().toDouble(),
                    if (etOtHours.text.isNullOrBlank())
                        0.0 else etOtHours.text.toString().trim().toDouble(),
                    if (etDblOtHours.text.isNullOrBlank())
                        0.0 else etDblOtHours.text.toString().trim().toDouble(),
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

    private fun saveHistoryIfValid(gotoUpdate: Boolean) {
        val answer = validateWorkOrderHistory()
        if (answer == ANSWER_OK) {
            curHistory = getCurHistory()
            saveHistory(gotoUpdate)
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveHistory(gotoUpdate: Boolean) {
        mainActivity.workOrderViewModel.insertWorkOrderHistory(
            curHistory
        )
        if (gotoUpdate) {
            mainActivity.mainViewModel.setWorkOrderHistory(
                curHistory
            )

            gotoWorkOrderHistoryUpdateFragment()
        } else {
            mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(null)
            gotoCallingFragment()
        }
    }

    private fun getCurHistory(): WorkOrderHistory {
        binding.apply {
            val workOrderId = curWorkOrder!!.workOrderId
            return WorkOrderHistory(
                nf.generateRandomIdAsLong(),
                workOrderId,
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

    private fun validateWorkOrderHistory(): String {
        binding.apply {
            if (curWorkOrder == null &&
                acWorkOrder.text.isNullOrBlank()
            ) {
                return "There is no work order selected"
            }
            convertNumbersToDoubles()
//            if (etRegHours.text.toString().toDouble() == 0.0
//                && etOtHours.text.toString().toDouble() == 0.0
//                && etDblOtHours.text.toString().toDouble() == 0.0
//            ) {
//                return "There was no time entered. Please enter some time."
//            }
        }
        return ANSWER_OK
    }

    private fun convertNumbersToDoubles() {
        binding.apply {
            etRegHours.setText(
                if (etRegHours.text.isNullOrBlank()) {
                    "0.0"
                } else {
                    etRegHours.text.toString().trim().toDouble().toString()
                }
            )
            etOtHours.setText(
                if (etOtHours.text.isNullOrBlank()) {
                    "0.0"
                } else {
                    etOtHours.text.toString().trim().toDouble().toString()
                }
            )
            etDblOtHours.setText(
                if (etDblOtHours.text.isNullOrBlank()) {
                    "0.0"
                } else {
                    etDblOtHours.text.toString().trim().toDouble().toString()
                }
            )
        }
    }

    private fun gotoCallingFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections
                .actionWorkOrderHistoryAddFragmentToWorkDateUpdateFragment()
        )
    }

    private fun gotoWorkOrderHistoryUpdateFragment() {
        setTempWorkOrderHistory()
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections
                .actionWorkOrderHistoryAddFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    private fun gotoWorkOrderUpdateFragment() {
        mainActivity.mainViewModel.setWorkOrderNumber(
            binding.acWorkOrder.text.toString()
        )
        setTempWorkOrderHistory()
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections
                .actionWorkOrderHistoryAddFragmentToWorkOrderUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}