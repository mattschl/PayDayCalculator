package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

import android.app.TimePickerDialog
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderHistoryTimeBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import java.util.Calendar


private const val TAG = "WorkOrderHistoryTimeUpdate"

class WorkOrderHistoryTimeUpdateFragment :
    Fragment(R.layout.fragment_work_order_history_time) {
    private var _binding: FragmentWorkOrderHistoryTimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var startTime: Calendar
    private lateinit var endTime: Calendar
    private lateinit var context: Context
    private lateinit var curWorkOrderHistory: WorkOrderHistoryCombined

    //    private var totalRegHours = 0.0
//    private var totalOtHours = 0.0
//    private var totalDblOtHours = 0.0
    private var totalRegHoursForDay = 0.0
    private var totalOtHoursForDay = 0.0
    private var totalDblOtHoursForDay = 0.0
    private val timeWorkedByDay = ArrayList<WorkOrderHistoryTimeWorkedCombined>()
    private val timeWorkedByDayAsCalendarPairs = ArrayList<Pair<Calendar, Calendar>>()

    //    private val timeWorkedByHistory = ArrayList<WorkOrderHistoryTimeWorkedCombined>()
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    //    private val defaultScope = CoroutineScope(Dispatchers.Default)
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private lateinit var workOrderHistoryTimeWorkedCombined: WorkOrderHistoryTimeWorkedCombined

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
        context = mView.context
//        startTime = Calendar.getInstance()
//        endTime = Calendar.getInstance()
        mainActivity.topMenuBar.title = getString(R.string.update_work_time)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateVariablesAndValues()
        setClickActions()
    }

    private fun populateVariablesAndValues() {
        workOrderHistoryTimeWorkedCombined = mainViewModel.getWorkOrderHistoryTimeWorkedCombined()!!
        curWorkOrderHistory = workOrderHistoryTimeWorkedCombined.workOrderHistory
        populateWorkOrderInfo()
        populateTimes()
        setDatesToCorrectTimes(curWorkOrderHistory)
        displayTimesAndHours()
    }

    private fun populateTimes() {
        startTime =
            df.getCalendarFromString(workOrderHistoryTimeWorkedCombined.timeWorked.wohtStartTime)
        Log.d(TAG, "populateTimes: ${df.getDateFromCalendarAsString(startTime)}")
        endTime =
            df.getCalendarFromString(workOrderHistoryTimeWorkedCombined.timeWorked.wohtEndTime)
    }

    private fun displayUpdatedTimes() {
        binding.apply {
            val display =
                "${context.getString(R.string.total_time)} ${df.getTimeWorked(startTime, endTime)}"
            tvTotalTime.text = display
            clkStartTime.text = df.get12HourDisplay(startTime)
            clkEndTime.text = df.get12HourDisplay(endTime)
        }
    }

    private fun displayTimesAndHours() {
        binding.apply {
            calculateTimeWorkForDay()
            var display =
                "${context.getString(R.string.original_time)} ${df.get12HourDisplay(startTime)} " +
                        "${context.getString(R.string._to_)} ${df.get12HourDisplay(endTime)} "

            when (workOrderHistoryTimeWorkedCombined.timeWorked.wohtTimeType) {
                1 -> {
                    display += " ${context.getString(R.string.reg_hrs_)} ${
                        nf.getNumberFromDouble(
                            df.getTimeWorked(
                                startTime,
                                endTime
                            )
                        )
                    }"
                    radHourType.check(R.id.radRegHours)
                }

                2 -> {
                    display += " ${context.getString(R.string.ot_hrs_)} ${
                        nf.getNumberFromDouble(
                            df.getTimeWorked(
                                startTime,
                                endTime
                            )
                        )
                    }"
                    radHourType.check(R.id.radOtHours)
                }

                3 -> {
                    display += " ${context.getString(R.string.dbl_ot_)} ${
                        nf.getNumberFromDouble(
                            df.getTimeWorked(
                                startTime,
                                endTime
                            )
                        )
                    }"
                    radHourType.check(R.id.radDblOtHours)
                }

                else -> {
                    display += " Break Time"
                    radHourType.check(R.id.radBreak)
                }
            }
            tvHours.text = display
            clkStartTime.text = df.get12HourDisplay(startTime)
            clkEndTime.text = df.get12HourDisplay(endTime)
            display =
                "${context.getString(R.string.total_time)} ${df.getTimeWorked(startTime, endTime)}"
            tvTotalTime.text = display
            btnEnterTime.visibility = View.INVISIBLE
        }
    }

    private fun populateWorkOrderInfo() {
        binding.apply {
            val display =
                "${getString(R.string.set_time_for_wo)} ${workOrderHistoryTimeWorkedCombined.workOrderHistory.workOrder.woNumber} " +
                        "${getString(R.string.at_)} ${workOrderHistoryTimeWorkedCombined.workOrderHistory.workOrder.woAddress} " +
                        "${getString(R.string._on_)} ${
                            df.getDisplayDate(
                                workOrderHistoryTimeWorkedCombined.workOrderHistory.workDate.wdDate
                            )
                        }"
            tvInfo.text = display
        }
    }

    private fun setDatesToCorrectTimes(historyCombined: WorkOrderHistoryCombined) {
        workOrderViewModel.getTimeWorkedPerDay(historyCombined.workDate.workDateId)
            .observe(viewLifecycleOwner) { timeWorkedHistory ->
                timeWorkedByDay.clear()
                timeWorkedByDayAsCalendarPairs.clear()
                for (time in timeWorkedHistory) {
                    timeWorkedByDay.add(time)
                    val start = df.getCalendarFromString(time.timeWorked.wohtStartTime)
                    val end = df.getCalendarFromString(time.timeWorked.wohtEndTime)
                    timeWorkedByDayAsCalendarPairs.add(Pair(start, end))
                }
                displayTimesAndHours()
            }
    }

    private fun calculateTimeWorkForDay() {
        if (timeWorkedByDay.isNotEmpty()) {
            totalRegHoursForDay = 0.0
            totalOtHoursForDay = 0.0
            totalDblOtHoursForDay = 0.0
            for (time in timeWorkedByDay) {
                when (time.timeWorked.wohtTimeType) {
                    1 -> totalRegHoursForDay += df.getTimeWorked(
                        time.timeWorked.wohtStartTime,
                        time.timeWorked.wohtEndTime
                    )

                    2 -> totalOtHoursForDay += df.getTimeWorked(
                        time.timeWorked.wohtStartTime,
                        time.timeWorked.wohtEndTime
                    )

                    3 -> totalDblOtHoursForDay += df.getTimeWorked(
                        time.timeWorked.wohtStartTime,
                        time.timeWorked.wohtEndTime
                    )

                    else -> {}
                }
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            setStartTimeActions()
            setEndTimeAction()
            fabDone.setOnClickListener { updateTimesInDatabase() }
        }
    }

    private fun setStartTimeActions() {
        binding.apply {
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
                    val tempStartTime =
                        df.roundTimeTo15Minutes(hourOfDay, minute)
                    startTime.set(Calendar.HOUR_OF_DAY, tempStartTime.first)
                    startTime.set(Calendar.MINUTE, tempStartTime.second)
                    startTime.set(Calendar.SECOND, 0)
                    displayUpdatedTimes()
                }
            clkStartTime.setOnClickListener {
                val startTimePicker = TimePickerDialog(
                    mView.context,
                    timeSetListener,
                    df.get12HourIntOfHour(startTime),
                    df.get12HourIntOfMinute(startTime),
                    false // true for 24-hour format, false for AM/PM
                )
                startTimePicker.setTitle(getString(R.string.select_start_time))
                startTimePicker.show()
            }
        }
    }

    private fun setEndTimeAction() {
        binding.apply {
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
                    mainScope.launch {
                        val tempEndTime = hourOfDay * 60 + minute
                        val tempStartTime =
                            startTime.get(Calendar.HOUR_OF_DAY) * 60 + startTime.get(Calendar.MINUTE)
                        if (tempEndTime < tempStartTime) {
                            dispayMessage(getString(R.string.end_time_before_start_time))
                            endTime.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY))
                            endTime.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE))
                            endTime.set(Calendar.SECOND, 0)
                        } else {
                            val tempHoursAndMin = df.roundTimeUpTo15Minutes(hourOfDay, minute)
                            endTime.set(Calendar.HOUR_OF_DAY, tempHoursAndMin.first)
                            endTime.set(Calendar.MINUTE, tempHoursAndMin.second)
                            endTime.set(Calendar.SECOND, 0)
                        }
                        var tempTimeWorked =
                            (tempEndTime.toDouble() - tempStartTime.toDouble()) / 60
                        if (radRegHours.isChecked) {
                            if ((tempTimeWorked + totalRegHoursForDay) > 8.0) {
                                tempTimeWorked = 8 - totalRegHoursForDay
                                val tempEndTimeCombined = timeWorkedByDay.last()
                                var tempHour =
                                    df.getHourFromStringAsInt(tempEndTimeCombined.timeWorked.wohtEndTime)
                                var tempMinute =
                                    df.getMinuteFromStringAsInt(tempEndTimeCombined.timeWorked.wohtEndTime)
                                tempHour += tempTimeWorked.toInt()

                                tempMinute += ((tempTimeWorked - tempTimeWorked.toInt()) * 60).toInt()
                                val tempHoursAndMinutes =
                                    df.roundTimeTo15Minutes(tempHour, tempMinute)
                                endTime.set(Calendar.HOUR_OF_DAY, tempHoursAndMinutes.first)
                                endTime.set(Calendar.MINUTE, tempHoursAndMinutes.second)
                                endTime.set(Calendar.SECOND, 0)
//                                binding.clkEndTime.text = df.get12HourDisplay(endTime)
                                dispayMessage(getString(R.string.time_has_been_adjusted_to_8_hours))
//                                radHourType.check(R.id.radOtHours)
                            }
                        }
                        if (radOtHours.isChecked) {
                            if ((tempTimeWorked + totalOtHoursForDay + totalOtHoursForDay) > 12.0) {
                                tempTimeWorked = 12 - totalOtHoursForDay - totalRegHoursForDay
                                val tempEndTimeCombined = timeWorkedByDay.last()
                                var tempHour =
                                    df.getHourFromStringAsInt(tempEndTimeCombined.timeWorked.wohtEndTime)
                                var tempMinute =
                                    df.getMinuteFromStringAsInt(tempEndTimeCombined.timeWorked.wohtEndTime)
                                tempHour += tempTimeWorked.toInt()
                                tempMinute += ((tempTimeWorked - tempTimeWorked.toInt()) * 60).toInt()
                                val tempHoursAndMinutes =
                                    df.roundTimeTo15Minutes(tempHour, tempMinute)
                                endTime.set(Calendar.HOUR_OF_DAY, tempHoursAndMinutes.first)
                                endTime.set(Calendar.MINUTE, tempHoursAndMinutes.second)
                                endTime.set(Calendar.SECOND, 0)
                                dispayMessage(getString(R.string.time_has_been_adjusted_to_12_hours))
                            }
                        }
                        displayTimesAndHours()
                    }
                }
            clkEndTime.setOnClickListener {
                val endTimePickerDialog = TimePickerDialog(
                    mView.context,
                    timeSetListener,
                    df.get12HourIntOfHour(endTime),
                    df.get12HourIntOfMinute(endTime),
                    false // true for 24-hour format, false for AM/PM
                )
                endTimePickerDialog.setTitle(getString(R.string.select_end_time))
                endTimePickerDialog.show()
            }
        }
    }

    private fun updateTimesInDatabase() {
        val answer = validateTimeWorked()
        if (answer == ANSWER_OK) {
            binding.apply {
                val workTimeType =
                    if (radRegHours.isChecked) {
                        1
                    } else if (radOtHours.isChecked) {
                        2
                    } else if (radDblOtHours.isChecked) {
                        3
                    } else {
                        0
                    }
                try {
                    mainScope.launch {
                        workOrderViewModel.updateTimeWorked(
                            WorkOrderHistoryTimeWorked(
                                workOrderHistoryTimeWorkedCombined.timeWorked.woHistoryTimeWorkedId,
                                workOrderHistoryTimeWorkedCombined.timeWorked.wohtHistoryId,
                                workOrderHistoryTimeWorkedCombined.timeWorked.wohtDateId,
                                df.getDateFromCalendarAsString(startTime),
                                df.getDateFromCalendarAsString(endTime),
                                workTimeType,
                                false,
                                df.getCurrentTimeAsString()
                            )
                        )
                        gotoCallingFragment()
                    }
                } catch (e: SQLiteConstraintException) {
                    dispayMessage(getString(R.string.error_) + e.message)
                    Log.d(TAG, e.message.toString())
                }
            }
        }
    }

    private fun dispayMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun validateTimeWorked(): String {
        if (endTime.timeInMillis < startTime.timeInMillis) {
            return getString(R.string.end_time_before_start_time)
        }
        if (endTime.timeInMillis == startTime.timeInMillis) {
            return getString(R.string.end_time_same_as_start_time)
        }
        return ANSWER_OK
    }

    private fun gotoCallingFragment() {
        mainViewModel.setWorkOrderHistoryTimeWorkedCombined(null)
        findNavController().navigate(
            WorkOrderHistoryTimeUpdateFragmentDirections.actionWorkOrderHistoryTimeUpdateFragmentToWorkOrderHistoryTime()
        )
    }
}