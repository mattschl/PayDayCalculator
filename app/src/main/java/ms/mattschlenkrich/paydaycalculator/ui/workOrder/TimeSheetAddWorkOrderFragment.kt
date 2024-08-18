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
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTimeSheetAddWorkOrderBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.workOrder.TempTimeSheetWorkOrderInfo
import ms.mattschlenkrich.paydaycalculator.model.workOrder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

private const val TAG = "TimeSheetAddWorkOrder"

class TimeSheetAddWorkOrderFragment : Fragment(R.layout.fragment_time_sheet_add_work_order) {

    private var _binding: FragmentTimeSheetAddWorkOrderBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private lateinit var workOrderList: ArrayList<String>
    private lateinit var workDateObject: WorkDates
    private lateinit var curEmployer: Employers

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSheetAddWorkOrderBinding.inflate(
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
    }

    private fun setInfoValues() {
        if (mainActivity.mainViewModel.getTempTimeSheetWorkOrderInfo() != null) {

        } else if (mainActivity.mainViewModel.getWorkDateObject() != null) {
            workDateObject = mainActivity.mainViewModel.getWorkDateObject()!!
            binding.apply {
                lblDate.text = df.getDisplayDate(workDateObject.wdDate)
                if (mainActivity.mainViewModel.getEmployer() != null) {
                    curEmployer = mainActivity.mainViewModel.getEmployer()!!
                    spEmployers.visibility = View.INVISIBLE
                    tvEmployers.visibility = View.VISIBLE
                    tvEmployers.text = curEmployer.employerName
                }
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
                newList.add(it.workOrderId.toString())
            }
        }
        return newList
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                validateWorkOrderNumber()
            }
        }
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
        binding.apply {
            mainActivity.mainViewModel.setTempTimeSheetWorkOrderInfo(
                TempTimeSheetWorkOrderInfo(
                    0L,
                    lblDate.text.toString(),
                    etRegHours.text.toString().toDouble(),
                    etOtHours.text.toString().toDouble(),
                    etDblOtHours.text.toString().toDouble()
                )
            )
        }
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            TimeSheetAddWorkOrderFragmentDirections
                .actionTimeSheetAddWorkOrderFragmentToWorkOrderAddFragment()
        )
    }

    private fun prepareToSave() {
        val answer = validateEntry()
        if (answer != ANSWER_OK) {
            saveEntry()
        } else {
            Toast.makeText(
                mView.context,
                answer, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveEntry() {
        mainActivity.workOrderViewModel.insertWorkOrderHistory(
            getCurHistory()
        )
        gotoCallingFragment()
    }

    private fun getCurHistory(): WorkOrderHistory {
        binding.apply {
            return WorkOrderHistory(
                nf.generateRandomIdAsLong(),
                acWorkOrder.text.toString(),
                lblDate.text.toString(),
                etRegHours.text.toString().toDouble(),
                etOtHours.text.toString().toDouble(),
                etDblOtHours.text.toString().toDouble()
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
            TimeSheetAddWorkOrderFragmentDirections
                .actionTimeSheetAddWorkOrderFragmentToWorkDateUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}