package ms.mattschlenkrich.paydaycalculator.ui.workOrder

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderHistoryBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.workOrder.TempWorkOrderInfo
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

private const val TAG = "WorkOrderHistoryUpdate"

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
    private lateinit var curHistory: WorkOrderHistory

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
            acWorkOrder.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        mainActivity.workOrderViewModel.getWorkOrder(
                            acWorkOrder.text.toString()
                        ).observe(viewLifecycleOwner) { workOrder ->
                            val disp = workOrder.woAddress +
                                    " | " + workOrder.woDescription
                            binding.tvDescription.text = disp
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        ///not needed
                    }
                }
        }
    }

    private fun setExistingValues() {
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
//        if (mainActivity.mainViewModel.getTempWorkOrderInfo() != null) {
//            binding.apply {
//                val tempWorkOrder =
//                    mainActivity.mainViewModel.getTempWorkOrderInfo()!!
//                acWorkOrder.setText(tempWorkOrder.tempID)
//                etRegHours.setText(
//                    nf.getNumberFromDouble(tempWorkOrder.woHistoryRegHours)
//                )
//                etOtHours.setText(
//                    nf.getNumberFromDouble(tempWorkOrder.woHistoryOtHours)
//                )
//                etDblOtHours.setText(
//                    nf.getNumberFromDouble(tempWorkOrder.woHistoryDblOtHours)
//                )
//                etNote.setText(
//                    tempWorkOrder.woHistoryNote
//                )
//            }
//        }
        if (mainActivity.mainViewModel.getWorkOrderHistory() != null) {
            val historyId = mainActivity.mainViewModel.getWorkOrderHistory()!!.woHistoryId
            mainActivity.workOrderViewModel.getWorkOrderHistory(historyId)
                .observe(viewLifecycleOwner) { history ->
                    curHistory = history
                    binding.apply {
                        acWorkOrder.setText(history.woHistoryWorkOrderId)
                        acWorkOrder.isEnabled = false
                        setOnWorkOrderSelected(history.woHistoryWorkOrderId)
                        etRegHours.setText(
                            nf.getNumberFromDouble(history.woHistoryRegHours)
                        )
                        etOtHours.setText(
                            nf.getNumberFromDouble(history.woHistoryOtHours)
                        )
                        etDblOtHours.setText(
                            nf.getNumberFromDouble(history.woHistoryDblOtHours)
                        )
                        etNote.setText(history.woHistoryNote)
                    }
                }
        }
    }

    private fun setOnWorkOrderSelected(workOrderId: String) {
        mainActivity.mainViewModel.setWorkOrderId(workOrderId)
        setTempWorkOrderInfo()
        gotoWorkOrderUpdateFragment()
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
                newList.add(it.workOrderId)
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
                TempWorkOrderInfo(
                    if (acWorkOrder.text.isNullOrBlank())
                        "00000" else acWorkOrder.text.toString(),
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
            curHistory.woHistoryId,
            history.woHistoryWorkOrderId,
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
                nf.generateRandomIdAsLong(),
                acWorkOrder.text.toString(),
                workDateObject.workDateId,
                etRegHours.text.toString().toDouble(),
                etOtHours.text.toString().toDouble(),
                etDblOtHours.text.toString().toDouble(),
                if (etNote.text.isNullOrBlank()) null
                else etNote.text.toString(),
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