package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

import android.app.AlertDialog
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderHistoryBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.WorkOrderCommonFunctions

private const val TAG = FRAG_WORK_ORDER_HISTORY_ADD

class WorkOrderHistoryAddFragment : Fragment(R.layout.fragment_work_order_history) {

    private var _binding: FragmentWorkOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private lateinit var workOrderList: List<WorkOrder>
    private var workDateObject: WorkDates? = null
    private var curEmployer: Employers? = null
    private var curWorkOrder: WorkOrder? = null
    private lateinit var curHistory: WorkOrderHistory
    private lateinit var commonFunctions: WorkOrderCommonFunctions

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
        mainActivity.title = getString(R.string.add_time_to_work_order)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }


    private fun populateValues() {
        mainScope.launch {
            hideUnusedElements()
            populateWorkDate()
            if (workDateObject != null) {
                populateCurrentEmployer()
            }
            delay(WAIT_100)
            populateWorkOrderLists()
            delay(WAIT_250)
            populateTempWorkOrderInfo()
        }
    }

    private fun hideUnusedElements() {
        binding.apply {
            clTimeEntered.visibility = View.GONE
            crdMaterials.visibility = View.GONE
            crdWorkPerformed.visibility = View.GONE
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
            binding.tvEmployers.text = curEmployer!!.employerName
        }
    }

    private fun populateTempWorkOrderInfo() {
        if (mainViewModel.getTempWorkOrderHistoryInfo() != null) {
            val tempWorkOrderHistory = mainViewModel.getTempWorkOrderHistoryInfo()!!
            binding.apply {
                if (mainViewModel.getWorkOrderNumber() != null) {
                    acWorkOrder.setText(
                        mainViewModel.getWorkOrderNumber()!!
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
                if (tempWorkOrderHistory.woWorkPerformed.isNotEmpty()) {
                    acWorkOrder.setText(tempWorkOrderHistory.woWorkPerformed)
                }
                if (tempWorkOrderHistory.woArea.isNotEmpty()) {
                    acArea.setText(tempWorkOrderHistory.woArea)
                }
                if (tempWorkOrderHistory.woWorkPerformedNote.isNotEmpty()) {
                    etWorkPerformedNote.setText(tempWorkOrderHistory.woWorkPerformedNote)
                }
                if (tempWorkOrderHistory.woMaterial.isNotEmpty()) {
                    acMaterials.setText(tempWorkOrderHistory.woMaterial)
                }
                if (tempWorkOrderHistory.woMaterialQty != 0.0) {
                    etMaterialQty.setText(
                        nf.getNumberFromDouble((tempWorkOrderHistory.woMaterialQty))
                    )
                }
                setCurWorkOrder()
                mainViewModel.setTempWorkOrderHistoryInfo(null)
            }
        }
    }

    private fun populateWorkOrderLists() {
        if (workDateObject != null) {
            workOrderViewModel.getWorkOrdersByEmployerId(workDateObject!!.wdEmployerId).observe(
                viewLifecycleOwner
            ) { list ->
                val workOrderListForAutocomplete = ArrayList<String>()
                workOrderList = list
                list.listIterator().forEach {
                    workOrderListForAutocomplete.add(it.woNumber)
                }
                binding.apply {
                    val woAdapter = ArrayAdapter(
                        mView.context, R.layout.spinner_item_normal, workOrderListForAutocomplete
                    )
                    acWorkOrder.setAdapter(woAdapter)
                }
            }
        }
    }

    private fun populateWorkOrderInfo() {
        binding.apply {
            if (curWorkOrder != null) {
                val display = curWorkOrder!!.woAddress + " - " + curWorkOrder!!.woNumber
                tvDescription.text = display
                tvDescription.visibility = View.VISIBLE
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                validateWorkOrderNumberAndSaveHistoryIfValid()
            }
            btnWorkOrder.setOnClickListener {
                if (btnWorkOrder.text.toString() == getString(R.string.edit)) {
                    gotoWorkOrderUpdate()
                } else {
                    gotoWorkOrderAdd()
                }
            }
            acWorkOrder.setOnItemClickListener { _, _, _, _ ->
                setCurWorkOrder()
            }
            acWorkOrder.setOnLongClickListener {
                gotoWorkOrderLookup()
                true
            }
            acWorkOrder.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
//                    null
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    setCurWorkOrder()
                }

                override fun afterTextChanged(s: Editable?) {
                    setCurWorkOrder()
                }

            })
        }
    }

    private fun validateWorkOrderNumberAndSaveHistoryIfValid() {
        if (doesWorkOrderExist()) {
            chooseToGotoUpdate()
        } else {
            AlertDialog.Builder(mView.context)
                .setTitle(getString(R.string.create_work_order_) + "${binding.acWorkOrder.text}?")
                .setMessage(getString(R.string.this_work_order_does_not_exist))
                .setPositiveButton(getString(R.string.yes)) { _, _ -> gotoWorkOrderAdd() }
                .setNegativeButton(getString(R.string.no), null).show()
        }
    }

    private fun validateWorkOrderHistory(): String {
        binding.apply {
//            if (curWorkOrder == null && acWorkOrder.text.isNullOrBlank()) {
//                return getString(R.string.there_is_no_work_order_selected)
//            }
            convertNumberStringsToDoubles()
        }
        return ANSWER_OK
    }

    private fun convertNumberStringsToDoubles() {
        binding.apply {
            etRegHours.setText(
                if (etRegHours.text.isNullOrBlank()) {
                    getString(R.string.zero_double)
                } else {
                    etRegHours.text.toString().trim().toDouble().toString()
                }
            )
            etOtHours.setText(
                if (etOtHours.text.isNullOrBlank()) {
                    getString(R.string.zero_double)
                } else {
                    etOtHours.text.toString().trim().toDouble().toString()
                }
            )
            etDblOtHours.setText(
                if (etDblOtHours.text.isNullOrBlank()) {
                    getString(R.string.zero_double)
                } else {
                    etDblOtHours.text.toString().trim().toDouble().toString()
                }
            )
        }
    }

    private fun saveHistoryIfValid(gotoUpdate: Boolean) {
        val answer = validateWorkOrderHistory()
        if (answer == ANSWER_OK) {
            curHistory = getCurHistory()
            saveHistory(gotoUpdate)
        } else {
            displayMessage(getString(R.string.error_) + answer)
        }
    }

    private fun saveHistory(gotoUpdate: Boolean) {
        workOrderViewModel.insertWorkOrderHistory(curHistory)
        if (gotoUpdate) {
            mainViewModel.setWorkOrderHistory(curHistory)
            gotoWorkOrderHistoryUpdate()
        } else {
            mainViewModel.setTempWorkOrderHistoryInfo(null)
            gotoCallingFragment()
        }
    }

    private fun getCurHistory(): WorkOrderHistory {
        binding.apply {
            setCurWorkOrder()
            val workOrderId = curWorkOrder!!.workOrderId
            return WorkOrderHistory(
                nf.generateRandomIdAsLong(),
                workOrderId,
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

    private fun chooseToGotoUpdate() {
        AlertDialog.Builder(mView.context).setTitle(getString(R.string.choose_the_next_step))
            .setMessage(getString(R.string.would_you_like_to_add_work_performed_or_materials_to_this_history_))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> saveHistoryIfValid(true) }
            .setNegativeButton(getString(R.string.no)) { _, _ -> saveHistoryIfValid(false) }.show()
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
            } else {
                btnWorkOrder.text = getString(R.string.create)
                tvDescription.visibility = View.INVISIBLE
            }
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun setTempWorkOrderHistory() {
        binding.apply {
            mainViewModel.setTempWorkOrderHistoryInfo(
                TempWorkOrderHistoryInfo(
                    curHistory.woHistoryId,
                    if (acWorkOrder.text.isNullOrBlank()) "000" else acWorkOrder.text.toString(),
                    lblDate.text.toString(),
                    if (etRegHours.text.isNullOrBlank()) 0.0 else etRegHours.text.toString().trim()
                        .toDouble(),
                    if (etOtHours.text.isNullOrBlank()) 0.0 else etOtHours.text.toString().trim()
                        .toDouble(),
                    if (etDblOtHours.text.isNullOrBlank()) 0.0 else etDblOtHours.text.toString()
                        .trim().toDouble(),
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

    private fun gotoWorkOrderLookup() {
        setTempWorkOrderHistory()
        mainViewModel.addCallingFragment(TAG)
        gotoWorkOrderLookupFragment()
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

    private fun gotoWorkOrderLookupFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections.actionWorkOrderHistoryAddFragmentToWorkOrderLookupFragment()
        )
    }

    private fun gotoWorkOrderAdd() {
        setTempWorkOrderHistory()
        mainViewModel.setCallingFragment(TAG)
        gotoWorkOrderAddFragment()
    }

    private fun gotoWorkOrderAddFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections.actionWorkOrderHistoryAddFragmentToWorkOrderAddFragment()
        )
    }

    private fun gotoCallingFragment() {
        gotoWorkDateUpdateFragment()
    }

    private fun gotoWorkDateUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections.actionWorkOrderHistoryAddFragmentToWorkDateUpdateFragment()
        )
    }

    private fun gotoWorkOrderHistoryUpdate() {
        setTempWorkOrderHistory()
        gotoWorkOrderHistoryUpdateFragment()
    }

    private fun gotoWorkOrderHistoryUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections.actionWorkOrderHistoryAddFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    private fun gotoWorkOrderUpdate() {
        mainViewModel.setWorkOrderNumber(binding.acWorkOrder.text.toString())
        setTempWorkOrderHistory()
        mainActivity.mainViewModel.setCallingFragment(TAG)
        gotoWorkOrderUpdateFragment()
    }

    private fun gotoWorkOrderUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections.actionWorkOrderHistoryAddFragmentToWorkOrderUpdateFragment()
        )
    }

    override fun onDestroy() {
        mainScope.cancel()
        super.onDestroy()
        _binding = null
    }
}