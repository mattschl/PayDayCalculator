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
import ms.mattschlenkrich.paydaycalculator.common.FRAG_WORK_ODER_HISTORY_UPDATE
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderHistoryFull
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderHistoryBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

private const val TAG = FRAG_WORK_ODER_HISTORY_UPDATE

class WorkOrderHistoryUpdateFragment : Fragment(R.layout.fragment_work_order_history) {

    private var _binding: FragmentWorkOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private lateinit var workOrderList: ArrayList<String>
    private lateinit var workDateObject: WorkDates
    private lateinit var curEmployer: Employers
    private lateinit var curHistory: WorkOrderHistoryFull
    private lateinit var curWorkOrder: WorkOrder

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
        setExistingValues()
        populateWorkOrderList()
        setClickActions()
        onSelectWorkOrder()
    }

    private fun onSelectWorkOrder() {
        binding.apply {
            acWorkOrder.setOnItemClickListener { _, _, _, _ ->
                mainActivity.workOrderViewModel.getWorkOrder(
                    acWorkOrder.text.toString()
                ).observe(viewLifecycleOwner) { workOrder ->
                    curWorkOrder = workOrder
                    displayWorkOrderInfo(workOrder)
                }
            }
        }
    }

    private fun displayWorkOrderInfo(workOrder: WorkOrder) {
        val display = workOrder.woAddress +
                " | " + workOrder.woDescription
        binding.apply {
            tvDescription.text = display
            tvDescription.visibility = View.VISIBLE
            btnEditWorkOrder.visibility = View.VISIBLE
        }
    }

    private fun setExistingValues() {
        if (mainActivity.mainViewModel.getWorkDateObject() != null) {
            populateWorkDateValues()
        }
        if (mainActivity.mainViewModel.getWorkOrderHistory() != null) {
            val historyId = mainActivity.mainViewModel.getWorkOrderHistory()!!.woHistoryId
            mainActivity.workOrderViewModel.getWorkOrderHistory(historyId)
                .observe(viewLifecycleOwner) { history ->
                    curHistory = history
                    binding.apply {
                        acWorkOrder.setText(history.workOrder.woNumber)
                        mainActivity.workOrderViewModel.getWorkOrder(
                            history.workOrder.woNumber
                        ).observe(viewLifecycleOwner) { workOrder ->
                            curWorkOrder = workOrder
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
                        btnEditWorkOrder.visibility = View.VISIBLE
                    }
                    if (mainActivity.mainViewModel.getWorkOrderNumber() != null) {
                        mainActivity.workOrderViewModel.getWorkOrder(
                            curHistory.history.woHistoryWorkOrderId
                        ).observe(
                            viewLifecycleOwner
                        ) { workOrder ->
                            curWorkOrder = workOrder
                            displayWorkOrderInfo(workOrder)
                        }
                    }
                }
        }
        if (mainActivity.mainViewModel.getTempWorkOrderInfo() != null) {
            val tempWorkOrderInfo =
                mainActivity.mainViewModel.getTempWorkOrderInfo()!!
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
            }
        }
    }

    private fun populateWorkDateValues() {
        workDateObject = mainActivity.mainViewModel.getWorkDateObject()!!
        binding.apply {
            lblDate.text = df.getDisplayDate(workDateObject.wdDate)
            if (mainActivity.mainViewModel.getEmployer() != null) {
                curEmployer = mainActivity.mainViewModel.getEmployer()!!
                tvEmployers.text = curEmployer.employerName
            }
        }
    }

    private fun setOnWorkOrderSelected(workOrderId: String) {
        mainActivity.mainViewModel.setWorkOrderNumber(workOrderId)
        setTempWorkOrderInfo()
    }

    private fun gotoWorkOrderUpdateFragment() {
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToWorkOrderUpdateFragment()
        )
    }

    private fun populateWorkOrderList() {
        workOrderList = getWorkOrderList()
        binding.apply {
            val woAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_normal, workOrderList
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
                setOnWorkOrderSelected(acWorkOrder.text.toString())
                mainActivity.mainViewModel.setCallingFragment(TAG)
                gotoWorkOrderUpdateFragment()
            }
        }
    }

    private fun validateWorkOrderNumber() {
        if (checkIfWorOrderExists()) {
            prepareToUpdate()
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
        for (workOrder in workOrderList) {
            if (binding.acWorkOrder.text.toString() == workOrder) {
                return true
            }
        }
        return false
    }

    private fun gotoWorkOrderAddFragment() {
        setTempWorkOrderInfo()
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            WorkOrderHistoryUpdateFragmentDirections
                .actionWorkOrderHistoryUpdateFragmentToWorkOrderAddFragment()
        )
    }

    private fun setTempWorkOrderInfo() {
        binding.apply {
            mainActivity.mainViewModel.setTempWorkOrderInfo(
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
                        null else etNote.text.toString()
                )
            )
        }
    }

    private fun prepareToUpdate() {
        val answer = validateEntry()
        if (answer == ANSWER_OK) {
            updateEntry()
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateEntry() {
        mainActivity.mainViewModel.setTempWorkOrderInfo(null)
        val history = getCurHistory()
        mainActivity.workOrderViewModel.updateWorkOrderHistory(
            history.woHistoryId,
            curWorkOrder.workOrderId,
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

    private fun getCurHistory(): WorkOrderHistory {
        binding.apply {
            return WorkOrderHistory(
                curHistory.history.woHistoryId,
                curWorkOrder.workOrderId,
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}