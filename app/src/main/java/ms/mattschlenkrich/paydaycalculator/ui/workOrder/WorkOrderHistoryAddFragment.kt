package ms.mattschlenkrich.paydaycalculator.ui.workOrder

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkPerformed
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderHistoryBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

private const val TAG = FRAG_WORK_ORDER_HISTORY_ADD

class WorkOrderHistoryAddFragment : Fragment(R.layout.fragment_work_order_history) {

    private var _binding: FragmentWorkOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private lateinit var workOrderListForAutoComplete:
            ArrayList<String>
    private lateinit var workDateObject: WorkDates
    private lateinit var curEmployer: Employers
    private var curWorkOrder: WorkOrder? = null
    private lateinit var workPerformedListForAutoComplete:
            ArrayList<WorkPerformed>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderHistoryBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
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
            if (!acWorkOrder.text.isNullOrBlank()) {
                mainActivity.workOrderViewModel.getWorkOrder(
                    acWorkOrder.text.toString()
                ).observe(viewLifecycleOwner) { workOrder ->
                    curWorkOrder = workOrder
                }
            } else if (mainActivity.mainViewModel.getWorkOrder() != null) {
                curWorkOrder =
                    mainActivity.mainViewModel.getWorkOrder()!!
            }
            populateWorkOrderInfo()
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
        populateWorkDateInfo()
        populateTempWorkOrderInfo()
        populateWorkOrderListInAutoComplete()
        populateWorkPerformedListInAutoComplete()
    }

    private fun populateWorkPerformedListInAutoComplete() {
        workPerformedListForAutoComplete =
            getWorkPerformedListForAutoComplete()
        binding.apply {
            val wpAdapter = ArrayAdapter(
                mView.context,
                R.layout.spinner_item_normal,
                workPerformedListForAutoComplete
            )
            acWorkPerformed.setAdapter(wpAdapter)
        }
    }

    private fun getWorkPerformedListForAutoComplete():
            ArrayList<WorkPerformed> {
        val newList = ArrayList<WorkPerformed>()
        mainActivity.workOrderViewModel.getWorkPerformedAll()
            .observe(viewLifecycleOwner) { list ->
                list.listIterator().forEach {
                    newList.add(it)
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
                    acWorkOrder.setText(mainActivity.mainViewModel.getWorkOrderNumber()!!)
                } else {
                    acWorkOrder.setText(tempWorkOrderHistory.woHistoryWorkOrderNumber)
                }
                etRegHours.setText(
                    nf.getNumberFromDouble(tempWorkOrderHistory.woHistoryRegHours)
                )
                etOtHours.setText(
                    nf.getNumberFromDouble(tempWorkOrderHistory.woHistoryOtHours)
                )
                etDblOtHours.setText(
                    nf.getNumberFromDouble(tempWorkOrderHistory.woHistoryDblOtHours)
                )
                etNote.setText(tempWorkOrderHistory.woHistoryNote)
                setCurWorkOrder()
            }
        }
    }

    private fun populateWorkDateInfo() {
        if (mainActivity.mainViewModel.getWorkDateObject() != null) {
            workDateObject = mainActivity.mainViewModel.getWorkDateObject()!!
            binding.apply {
                lblDate.text = df.getDisplayDate(workDateObject.wdDate)
                if (mainActivity.mainViewModel.getEmployer() != null) {
                    curEmployer = mainActivity.mainViewModel.getEmployer()!!
                    tvEmployers.text = curEmployer.employerName
                }
            }
        }
    }

    private fun populateWorkOrderListInAutoComplete() {
        workOrderListForAutoComplete = getWorkOrderList()
        binding.apply {
            val woAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_normal, workOrderListForAutoComplete
            )
            acWorkOrder.setAdapter(woAdapter)
        }
    }

    private fun getWorkOrderList(): ArrayList<String> {
        val newList = ArrayList<String>()
        mainActivity.workOrderViewModel.getWorkOrdersByEmployerId(
            workDateObject.wdEmployerId
        ).observe(viewLifecycleOwner) { list ->
            list.listIterator().forEach {
                newList.add(it.woNumber)
            }
        }
        return newList
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
                saveCurrentHistoryIfValidAndGotoUpdateFragment()
            }
            btnAddWorkPerformed.setOnClickListener {
                saveCurrentHistoryIfValidAndGotoUpdateFragment()
            }
        }
    }

    private fun saveCurrentHistoryIfValidAndGotoUpdateFragment() {
        binding.apply {
            if (acWorkOrder.text.isNullOrBlank()) {
                Toast.makeText(
                    mView.context,
                    "Please enter a valid work order number before adding work performed",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                saveHistoryIfValid(true)
            }
        }
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

    private fun validateWorkOrderNumber() {
        if (checkIfWorOrderExists()) {
            saveHistoryIfValid(false)
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

    private fun checkIfWorOrderExists(): Boolean {
        for (workOrder in workOrderListForAutoComplete) {
            if (binding.acWorkOrder.text.toString() == workOrder) {
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
                        null else etNote.text.toString()
                )
            )
        }
    }

    private fun saveHistoryIfValid(gotoUpdate: Boolean) {
        val answer = validateEntry()
        if (answer == ANSWER_OK) {
            saveHistory(gotoUpdate)
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveHistory(gotoUpdate: Boolean) {
        val workOrderHistory = getCurHistory()
        mainActivity.workOrderViewModel.insertWorkOrderHistory(
            workOrderHistory
        )
        if (gotoUpdate) {
            mainActivity.mainViewModel.setWorkOrderHistory(
                workOrderHistory
            )
            gotoWorkOrderHistoryUpdateFragment()
        } else {
            mainActivity.mainViewModel.setTempWorkOrderHistoryInfo(null)
            gotoCallingFragment()
        }
    }

    private fun gotoWorkOrderHistoryUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections
                .actionWorkOrderHistoryAddFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    private fun getCurHistory(): WorkOrderHistory {
        binding.apply {
            val workOrderId = curWorkOrder!!.workOrderId
            return WorkOrderHistory(
                nf.generateRandomIdAsLong(),
                workOrderId,
                workDateObject.workDateId,
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

    private fun validateEntry(): String {
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
            if (curWorkOrder == null) {
                return "There is no work order selected"
            }
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
            WorkOrderHistoryAddFragmentDirections
                .actionWorkOrderHistoryAddFragmentToWorkDateUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}