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
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkOrderHistoryBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.workOrder.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

private const val TAG = FRAG_WORK_ORDER_HISTORY_ADD

class WorkOrderHistoryAddFragment : Fragment(R.layout.fragment_work_order_history) {

    private var _binding: FragmentWorkOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private lateinit var workOrderList: ArrayList<String>
    private lateinit var workDateObject: WorkDates
    private lateinit var curEmployer: Employers
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
        setInfoValues()
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
                    val disp = workOrder.woAddress +
                            " | " + workOrder.woDescription
                    binding.tvDescription.text = disp
                    binding.tvDescription.visibility = View.VISIBLE
                    binding.btnEditWorkOrder.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setInfoValues() {
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
        if (mainActivity.mainViewModel.getTempWorkOrderInfo() != null) {
            binding.apply {
                val tempWorkOrder =
                    mainActivity.mainViewModel.getTempWorkOrderInfo()!!
                acWorkOrder.setText(tempWorkOrder.woHistoryWorkOrderNumber)
                etRegHours.setText(
                    nf.getNumberFromDouble(tempWorkOrder.woHistoryRegHours)
                )
                etOtHours.setText(
                    nf.getNumberFromDouble(tempWorkOrder.woHistoryOtHours)
                )
                etDblOtHours.setText(
                    nf.getNumberFromDouble(tempWorkOrder.woHistoryDblOtHours)
                )
            }
        }
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
                gotoWorkOrderUpdateFragment()
            }
        }
    }

    private fun gotoWorkOrderUpdateFragment() {
        mainActivity.mainViewModel.setWorkOrderNumber(
            binding.acWorkOrder.text.toString()
        )
        setTempWorkOderHistory()
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections
                .actionWorkOrderHistoryAddFragmentToWorkOrderUpdateFragment()
        )
    }

    private fun validateWorkOrderNumber() {
        if (checkIfWorOrderExists()) {
            prepareToSave()
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
        setTempWorkOderHistory()
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            WorkOrderHistoryAddFragmentDirections
                .actionWorkOrderHistoryAddFragmentToWorkOrderAddFragment()
        )
    }

    private fun setTempWorkOderHistory() {
        binding.apply {
            mainActivity.mainViewModel.setTempWorkOrderInfo(
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

    private fun prepareToSave() {
        val answer = validateEntry()
        if (answer == ANSWER_OK) {
            saveEntry()
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveEntry() {
        mainActivity.mainViewModel.setTempWorkOrderInfo(null)
        mainActivity.workOrderViewModel.insertWorkOrderHistory(
            getCurHistory()
        )
        gotoCallingFragment()
    }

    private fun getCurHistory(): WorkOrderHistory {
        binding.apply {
            return WorkOrderHistory(
                nf.generateRandomIdAsLong(),
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