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
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderHistoryTimeBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import java.util.Calendar

class WorkOrderHistoryTime : Fragment(R.layout.fragment_work_order_history_time) {

    private var _binding: FragmentWorkOrderHistoryTimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
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
            binding.apply {
                radRegHours.isSelected = true
            }
            populateWorkOrderInfo()
            delay(WAIT_250)
            populateTime()
        }
    }

    private fun populateWorkOrderInfo() {
        binding.apply {
//        TODO("Not yet implemented")
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
            setRadioButtonActions()
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

    private fun setRadioButtonActions() {
        binding.apply {
            radRegHours.setOnClickListener {
                radOtHours.isSelected = false
                radDblOt.isSelected = false
                radBreak.isSelected = false
            }
            radOtHours.setOnClickListener {
                radRegHours.isSelected = false
                radDblOt.isSelected = false
                radBreak.isSelected = false
            }
            radDblOt.setOnClickListener {
                radRegHours.isSelected = false
                radOtHours.isSelected = false
                radBreak.isSelected = false
            }
            radBreak.setOnClickListener {
                radRegHours.isSelected = false
                radOtHours.isSelected = false
                radDblOt.isSelected = false
            }
        }
    }

    private fun gotoWorkOrderHistoryUpdate() {
        mView.findNavController().navigate(
            WorkOrderHistoryTimeDirections.actionWorkOrderHistoryTimeToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun onDestroy() {
        mainScope.cancel()
        defaultScope.cancel()
        super.onDestroy()
        _binding = null
    }
}