package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

import android.app.TimePickerDialog
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_TIME
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorked
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
    private var totalRegHours = 0.0
    private var totalOtHours = 0.0
    var totalDblOtHours = 0.0
    private var totalRegHoursForDay = 0.0
    private var totalOtHoursForDay = 0.0
    private var totalDblOtHoursForDay = 0.0
    private val timeWorkedByDay = ArrayList<WorkOrderHistoryTimeWorkedCombined>()
    private val timeWorkedByDayAsCalendarPairs = ArrayList<Pair<Calendar, Calendar>>()
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
        populateVariablesAndValues()
        setClickActions()
    }

    private fun populateVariablesAndValues() {
        if (mainViewModel.getWorkOrderHistory() != null) {
            workOrderViewModel.getWorkOrderHistoryCombined(mainViewModel.getWorkOrderHistory()!!.woHistoryId)
                .observe(viewLifecycleOwner) { historyCombined ->
                    curWorkOrderHistory = historyCombined
                    binding.apply {
                        curDateString = historyCombined.workDate.wdDate
                        populateWorkOrderInfo(historyCombined)
                        populateTimesFromHistory(historyCombined)
                        populateExistingTimesRecycler(historyCombined)
                    }
                }
        }

    }

    private fun populateWorkOrderInfo(historyCombined: WorkOrderHistoryCombined) {
        binding.apply {
            val display =
                "${getString(R.string.set_time_for_wo)} ${historyCombined.workOrder.woNumber} " +
                        "${getString(R.string.at_)} ${historyCombined.workOrder.woAddress} " +
                        "${getString(R.string._on_)} ${df.getDisplayDate(historyCombined.workDate.wdDate)}"
            tvInfo.text = display
        }
    }

    private fun populateTimesFromHistory(historyCombined: WorkOrderHistoryCombined) {
        val tempDate = historyCombined.workDate.wdDate.split("-")
        startTime.set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
        endTime.set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
        startTime.set(Calendar.HOUR_OF_DAY, 8)
        startTime.set(Calendar.MINUTE, 30)
        startTime.set(Calendar.SECOND, 0)
        setDatesToCorrectTimes(historyCombined)
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
                if (timeWorkedHistory.isNotEmpty()) {
                    val tempStartTime =
                        df.splitTimeFromDateTime(timeWorkedHistory.last().timeWorked.wohtEndTime)
                    startTime.set(Calendar.HOUR_OF_DAY, tempStartTime[0].toInt())
                    startTime.set(Calendar.MINUTE, tempStartTime[1].toInt())
                    startTime.set(Calendar.SECOND, 0)
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

    private fun displayTimesAndHours() {
        calculateTimeWorkForDay()
        mainScope.launch {
            binding.apply {
                calculateTimeWorkForDay()
                totalRegHours = 0.0
                totalOtHours = 0.0
                totalDblOtHours = 0.0
                for (time in timeWorkedByHistory) {
                    when (time.timeWorked.wohtTimeType) {
                        1 -> totalRegHours += df.getTimeWorked(
                            time.timeWorked.wohtStartTime,
                            time.timeWorked.wohtEndTime
                        )

                        2 -> totalOtHours += df.getTimeWorked(
                            time.timeWorked.wohtStartTime,
                            time.timeWorked.wohtEndTime
                        )

                        3 -> totalDblOtHours += df.getTimeWorked(
                            time.timeWorked.wohtStartTime,
                            time.timeWorked.wohtEndTime
                        )
                    }
                }
                var display = "${getString(R.string.total_hours)} ${
                    nf.getNumberFromDouble(
                        df.getTimeWorked(
                            startTime,
                            endTime
                        )
                    )
                } "
                tvTotalTime.text = display
                display = ""
                if (totalRegHours > 0.0)
                    display =
                        getString(R.string.reg_hrs_) + nf.getNumberFromDouble(totalRegHours)
                if (totalOtHours > 0.0) {
                    if (display != "") display += getString(R.string.pipe)
                    display += getString(R.string.ot_hrs_) + nf.getNumberFromDouble(totalOtHours)
                }
                if (totalDblOtHours > 0.0) {
                    if (display != "") display += getString(R.string.pipe)
                    display += getString(R.string.dbl_ot_) + nf.getNumberFromDouble(totalDblOtHours)
                }
                if (display == "") display = getString(R.string.no_time_entered)
                tvHours.text = display
                if (totalRegHoursForDay >= 8.0) {
                    radHourType.check(R.id.radOtHours)
                }
                if (totalOtHoursForDay + totalRegHoursForDay >= 12.0) {
                    radHourType.check(R.id.radDblOtHours)
                }
                delay(WAIT_250)
                updateTimeDisplayed()
            }
        }
    }

    private fun updateTimeDisplayed() {
        binding.apply {
//            Log.d(TAG, "displayTimesAndHours: ${df.get12HourDisplay(endTime)}")
            clkStartTime.text = df.get12HourDisplay(startTime)
            clkEndTime.text = df.get12HourDisplay(endTime)
        }
    }


    private fun populateExistingTimesRecycler(historyCombined: WorkOrderHistoryCombined) {
        workOrderViewModel.getTimeWorkedForWorkOrderHistory(historyCombined.workOrderHistory.woHistoryId)
            .observe(viewLifecycleOwner) { timeWorkedOnHistory ->
                timeWorkedByHistory.clear()
                for (time in timeWorkedOnHistory) {
                    timeWorkedByHistory.add(time)
                }
                val timeWorkedAdapter =
                    TimeWorkedAdapter(mainActivity, mView, TAG, this)
                binding.rvTimeWorked.apply {
                    layoutManager = LinearLayoutManager(mView.context)
                    adapter = timeWorkedAdapter
                }
                timeWorkedAdapter.differ.submitList(timeWorkedOnHistory)
            }
    }

    private fun setClickActions() {
        binding.apply {
            setStartTimeActions()
            setEndTimeAction()
            btnEnterTime.setOnClickListener { insertTime() }
            fabDone.setOnClickListener {
                gotoWorkOrderHistoryUpdate()
            }
        }
    }


    private fun setStartTimeActions() {
        binding.apply {
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
                    val tempStartTime =
                        df.roundTimeDownTo15Minutes(hourOfDay, minute)
                    startTime.set(Calendar.HOUR_OF_DAY, tempStartTime.first)
                    startTime.set(Calendar.MINUTE, tempStartTime.second)
                    startTime.set(Calendar.SECOND, 0)
                    displayTimesAndHours()
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
                            showMessage(getString(R.string.end_time_before_start_time))
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
                                showMessage(getString(R.string.time_has_been_adjusted_to_8_hours))
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
                                showMessage(getString(R.string.time_has_been_adjusted_to_12_hours))
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


    private fun insertTime(): Boolean {
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
                        startTime = df.roundCalendarTimeTo15Minutes(startTime)
//                        Log.d(TAG, "insertTime: End time is ${df.get12HourDisplay(endTime)}")
                        endTime = df.roundCalendarTimeTo15Minutes(endTime)
                        workOrderViewModel.insertTimeWorked(
                            WorkOrderHistoryTimeWorked(
                                nf.generateRandomIdAsLong(),
                                curWorkOrderHistory.workOrderHistory.woHistoryId,
                                curWorkOrderHistory.workDate.workDateId,
                                df.getDateFromCalendarAsString(startTime),
                                df.getDateFromCalendarAsString(endTime),
                                workTimeType,
                                false,
                                df.getCurrentTimeAsString()
                            )
                        )
                        delay(WAIT_250)
                        setDatesToCorrectTimes(curWorkOrderHistory)
                    }
                    return true
                } catch (e: SQLiteConstraintException) {
                    showMessage(getString(R.string.error_) + e.message)
                    Log.d(TAG, e.message.toString())
                    return false
                }
            }
        } else {
            showMessage("${getString(R.string.error_)} $answer")
            return false
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun validateTimeWorked(): String {
        binding.apply {
            if (radRegHours.isChecked) {
                if (totalRegHoursForDay + df.getTimeWorked(startTime, endTime) > 8.0) {
                    return getString(R.string.this_will_exceed_8_hours)
                }
            }
            if (radOtHours.isChecked) {
                if (totalOtHoursForDay + df.getTimeWorked(startTime, endTime) > 12.0) {
                    return getString(R.string.this_will_exceed_12_hours)
                }
            }
            if (radDblOtHours.isChecked) {
                if (totalDblOtHoursForDay + df.getTimeWorked(startTime, endTime) > 18.0) {
                    return getString(R.string.this_will_exceed_24_hours)
                }
            }
            if (endTime.timeInMillis < startTime.timeInMillis) {
                return getString(R.string.end_time_before_start_time)
            }
            if (endTime.get(Calendar.HOUR_OF_DAY) == startTime.get(Calendar.HOUR_OF_DAY) &&
                endTime.get(Calendar.MINUTE) == startTime.get(Calendar.MINUTE)
            ) {
                return getString(R.string.end_time_same_as_start_time)
            }
        }
        return ANSWER_OK
    }

    private fun gotoWorkOrderHistoryUpdate() {
        mView.findNavController().navigate(
            WorkOrderHistoryTimeFragmentDirections.actionWorkOrderHistoryTimeToWorkOrderHistoryUpdateFragment()
        )
    }

    fun gotoWorkOrderHistoryTimeUpdate() {
        findNavController().navigate(
            WorkOrderHistoryTimeFragmentDirections.actionWorkOrderHistoryTimeToWorkOrderHistoryTimeUpdateFragment()
        )
    }

    override fun onDestroy() {
        mainScope.cancel()
        defaultScope.cancel()
        super.onDestroy()
        _binding = null
    }
}
