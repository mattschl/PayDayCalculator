package ms.mattschlenkrich.paycalculator.ui.workdate

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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_TIME
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.database.viewModel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkDateTimeBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import java.util.Calendar

private const val TAG = FRAG_WORK_DATE_TIME

class WorkDateTimes : Fragment(R.layout.fragment_work_date_time) {

    private var _binding: FragmentWorkDateTimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var curEmployer: Employers
    private lateinit var curDate: WorkDates
    private var curWorkOrder: WorkOrder? = null
    private lateinit var workOrderList: List<WorkOrder>
    private lateinit var existingHistories: List<WorkOrderHistoryTimeWorkedCombined>
    private lateinit var startTime: Calendar
    private lateinit var endTime: Calendar
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val defaultScope = CoroutineScope(Dispatchers.Default)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkDateTimeBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        employerViewModel = mainActivity.employerViewModel
        startTime = Calendar.getInstance()
        endTime = Calendar.getInstance()
        mainActivity.topMenuBar.title = getString(R.string.enter_work_time)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        mainScope.launch {
            populateWorkDateAndEmployer()
            populateWorkOrderListForAutoComplete()
            delay(WAIT_250)
            populateWorkOrderFromCache()
        }
    }

    private fun populateWorkOrderFromCache() {
        if (mainViewModel.getWorkOrder() != null) {
            curWorkOrder = mainViewModel.getWorkOrder()
            binding.acWorkOrder.setText(curWorkOrder!!.woNumber)
            setCurWorkOrder()
        }
    }

    private fun populateWorkOrderListForAutoComplete() {
        workOrderViewModel.getWorkOrdersByEmployerId(curEmployer.employerId)
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

    private fun populateWorkDateAndEmployer() {
        if (mainViewModel.getEmployer() != null) {
            curEmployer = mainViewModel.getEmployer()!!
        }
        if (mainViewModel.getWorkDateObject() != null) {
            curDate = mainViewModel.getWorkDateObject()!!
            workOrderViewModel.getTimeWorkedPerDay(curDate.workDateId)
                .observe(viewLifecycleOwner) { histories ->
                    existingHistories = histories

                }
        }
        populateDateInfo()
    }

    private fun populateDateInfo() {
        binding.apply {
            var display = ""
            display = "Employer: ${curEmployer.employerName}\n"
            display += "Date: ${df.getDisplayDate(curDate.wdDate)}\n"

            tvInfo.text = display
        }
    }

    private fun setClickActions() {
        binding.apply {
            acWorkOrder.setOnItemClickListener { _, _, _, _ -> setCurWorkOrder() }
            acWorkOrder.setOnLongClickListener {
                gotoWorkOrderLookup()
                true
            }
        }

    }

    private fun displayMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
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
                tvWoInfo.visibility = View.VISIBLE
            } else {
                btnWorkOrder.text = getString(R.string.create)
                tvWoInfo.visibility = View.INVISIBLE
            }
        }
    }

    private fun populateWorkOrderInfo() {
        val display = curWorkOrder!!.woAddress + " | " + curWorkOrder!!.woDescription
        binding.tvWoInfo.apply {
            text = display
            visibility = View.VISIBLE
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

    private fun gotoWorkOrderLookup() {
        mainViewModel.setWorkOrder(curWorkOrder)
        mainViewModel.addCallingFragment(TAG)
        gotoWorkOrderLookupFragment()
    }

    private fun gotoWorkOrderLookupFragment() {
        mView.findNavController().navigate(
            WorkDateTimesDirections.actionWorkDateTimesToWorkOrderLookupFragment()
        )
    }

    override fun onDestroy() {
        mainScope.cancel()
        defaultScope.cancel()
        super.onDestroy()
        _binding = null
    }

}