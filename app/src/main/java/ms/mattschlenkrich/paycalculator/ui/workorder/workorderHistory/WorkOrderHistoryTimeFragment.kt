package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_TIME
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderHistoryTimeBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory.adpater.TimeWorkedAdapter
import java.util.Calendar

private const val TAG = FRAG_WORK_ORDER_HISTORY_TIME

class WorkOrderHistoryTimeFragment : Fragment(R.layout.fragment_work_order_history_time) {

    private var _binding: FragmentWorkOrderHistoryTimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var curDateString: String
    private lateinit var startTime: Calendar
    private lateinit var endTime: Calendar
    private lateinit var curWorkOrderHistory: WorkOrderHistoryCombined
    private val timeWorkedByDay = ArrayList<WorkOrderHistoryTimeWorkedCombined>()
    private val timeWorkedByHistory = ArrayList<WorkOrderHistoryTimeWorkedCombined>()
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val defaultScope = CoroutineScope(Dispatchers.Default)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWorkOrderHistoryTimeBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        startTime = Calendar.getInstance()
        endTime = Calendar.getInstance()
        mainActivity.title = getString(R.string.enter_work_time)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateInitialValues()
        setClickActions()
    }

    private fun populateInitialValues() {
        mainScope.launch {
            populateVariablesAndValues()
        }
    }

    private fun populateVariablesAndValues() {
        if (mainViewModel.getWorkOrderHistory() != null) {
            workOrderViewModel.getWorkOrderHistoryCombined(mainViewModel.getWorkOrderHistory()!!.woHistoryId)
                .observe(viewLifecycleOwner) { historyCombined ->
                    curWorkOrderHistory = historyCombined
                    binding.apply {
                        curDateString = historyCombined.workDate.wdDate
                        populateWorkOrderInfo(historyCombined)
                        populateStartTimeTimeFromDate(historyCombined)
                        populateExistingTimesFromWorkOrderHistory(historyCombined)
                    }
                }
        }
    }

    private fun populateWorkOrderInfo(historyCombined: WorkOrderHistoryCombined) {
        binding.apply {
            var display =
                "${getString(R.string.set_time_for_wo)} ${historyCombined.workOrder.woNumber} " +
                        "${getString(R.string.at_)} ${historyCombined.workOrder.woAddress} " +
                        "${getString(R.string._on_)} ${df.getDisplayDate(historyCombined.workDate.wdDate)}"
            tvInfo.text = display
            display = ""
            if (historyCombined.workOrderHistory.woHistoryRegHours > 0.0)
                display =
                    getString(R.string.reg_hrs_) + nf.getNumberFromDouble(historyCombined.workOrderHistory.woHistoryRegHours)
            if (display != "") display += getString(R.string.pipe)
            if (historyCombined.workOrderHistory.woHistoryOtHours > 0.0)
                display += getString(R.string.ot_hrs_) + nf.getNumberFromDouble(historyCombined.workOrderHistory.woHistoryOtHours)
            if (display != "") display += getString(R.string.pipe)
            if (historyCombined.workOrderHistory.woHistoryDblOtHours > 0.0)
                display += getString(R.string.dbl_ot_) + nf.getNumberFromDouble(historyCombined.workOrderHistory.woHistoryDblOtHours)
            if (display != "") display = getString(R.string.no_time_entered)
            tvHours.text = display
        }
    }

    private fun populateStartTimeTimeFromDate(historyCombined: WorkOrderHistoryCombined) {
        startTime.set(Calendar.HOUR_OF_DAY, 8)
        startTime.set(Calendar.MINUTE, 30)
        workOrderViewModel.getTimeWorkedPerDay(historyCombined.workDate.workDateId)
            .observe(viewLifecycleOwner) { timeWorkedHistory ->
                for (time in timeWorkedHistory) {
                    timeWorkedByDay.add(time)
                }
                if (timeWorkedHistory.isNotEmpty()) {
                    val tempStartTime =
                        timeWorkedHistory.last().workDate.wdDate
                            .replace("$curDateString ", "")
                            .split(":")
                    startTime.set(Calendar.HOUR_OF_DAY, tempStartTime[0].toInt())
                    startTime.set(Calendar.MINUTE, tempStartTime[1].toInt())
                }
                binding.apply {
                    clkStartTime.text = df.get12HourDisplay(startTime)
                    clkEndTime.text = df.get12HourDisplay(endTime)
                }
            }
    }

    private fun populateExistingTimesFromWorkOrderHistory(historyCombined: WorkOrderHistoryCombined) {
        workOrderViewModel.getTimeWorkedForWorkOrderHistory(historyCombined.workOrderHistory.woHistoryId)
            .observe(viewLifecycleOwner) { timeWorkedOnHistory ->
                for (time in timeWorkedOnHistory) {
                    timeWorkedByHistory.add(time)
                }
                val timeWorkedAdapter =
                    TimeWorkedAdapter(mainActivity, mView, TAG, this)
                timeWorkedAdapter.differ.submitList(timeWorkedOnHistory)
            }
    }

    private fun setClickActions() {
        binding.apply {
            setStartTimeActions()
            setEndTimeAction()
            btnEnterTime.setOnClickListener { insertTimeIntoWorkOrderHistory() }
            fabDone.setOnClickListener { gotoWorkOrderHistoryUpdate() }
        }
    }

    private fun insertTimeIntoWorkOrderHistory() {
        // TODO: Set time
    }

    private fun setStartTimeActions() {
        binding.apply {
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
                    startTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    startTime.set(Calendar.MINUTE, minute)
                    clkStartTime.text = df.get12HourDisplay(startTime)
                }
            clkStartTime.setOnClickListener {
                TimePickerDialog(
                    mView.context,
                    timeSetListener,
                    df.get12HourIntOfHour(startTime),
                    df.get12HourIntOfMinute(startTime),
                    false // true for 24-hour format, false for AM/PM
                ).show()
            }
        }
    }

    private fun setEndTimeAction() {
        binding.apply {
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
                    endTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    endTime.set(Calendar.MINUTE, minute)
                    clkEndTime.text = df.get12HourDisplay(endTime)
                }
            clkEndTime.setOnClickListener {
                TimePickerDialog(
                    mView.context,
                    timeSetListener,
                    df.get12HourIntOfHour(endTime),
                    df.get12HourIntOfMinute(endTime),
                    false // true for 24-hour format, false for AM/PM
                ).show()
            }
        }
    }

    private fun gotoWorkOrderHistoryUpdate() {
        mView.findNavController().navigate(
            WorkOrderHistoryTimeFragmentDirections.actionWorkOrderHistoryTimeToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun onDestroy() {
        mainScope.cancel()
        defaultScope.cancel()
        super.onDestroy()
        _binding = null
    }
}