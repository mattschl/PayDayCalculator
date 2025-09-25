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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderHistoryTimeBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import java.util.Calendar

class WorkOrderHistoryTimeFragment : Fragment(R.layout.fragment_work_order_history_time) {

    private var _binding: FragmentWorkOrderHistoryTimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var startTime: Calendar
    private lateinit var endTime: Calendar
    private lateinit var curDate: String
    private lateinit var curWorkOrderHistory: WorkOrderHistoryCombined
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
            populateVariables()
            populateWorkOrderInfo()
            delay(WAIT_250)
            populateTime()
        }
    }

    private fun populateVariables() {
        val tempDate = mainViewModel.getWorkDateString()
    }

    private fun populateWorkOrderInfo() {
        binding.apply {
            if (mainViewModel.getTempWorkOrderHistoryInfo() != null) {
                val tempHistory = mainViewModel.getTempWorkOrderHistoryInfo()!!
                var display =
                    getString(R.string.set_time_for_wo) + tempHistory.woHistoryWorkOrderNumber +
                            getString(R.string._on_) + tempHistory.woHistoryWorkDate
                tvInfo.text = display
                display = ""
                if (tempHistory.woHistoryRegHours > 0.0)
                    display =
                        getString(R.string.reg_hrs_) + nf.getNumberFromDouble(tempHistory.woHistoryRegHours)
                if (display.isBlank()) display += getString(R.string.pipe)
                if (tempHistory.woHistoryOtHours > 0.0)
                    display += getString(R.string.ot_hrs_) + nf.getNumberFromDouble(tempHistory.woHistoryOtHours)
                if (display.isBlank()) display += getString(R.string.pipe)
                if (tempHistory.woHistoryDblOtHours > 0.0)
                    display += getString(R.string.dbl_ot_) + nf.getNumberFromDouble(tempHistory.woHistoryDblOtHours)
                if (display.isBlank()) display = getString(R.string.no_time_entered)
                tvHours.text = display
            }
        }

    }

    private fun populateTime() {

        startTime.set(Calendar.HOUR_OF_DAY, 8)
        startTime.set(Calendar.MINUTE, 30)
        binding.apply {
            clkStartTime.text = df.get12HourDisplay(startTime)
            clkEndTime.text = df.get12HourDisplay(endTime)
        }
    }

    private fun setClickActions() {
        binding.apply {
            setStartTimeActions()
            setEndTimeAction()
            fabDone.setOnClickListener { gotoWorkOrderHistoryUpdate() }
        }
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