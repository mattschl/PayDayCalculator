package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

import android.app.AlertDialog
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_TIME
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderHistoryTimeBinding
import ms.mattschlenkrich.paycalculator.logic.worktime.WorkTimes
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import java.util.Calendar

private const val TAG = FRAG_WORK_ORDER_HISTORY_TIME

class WorkOrderHistoryTimeFragmentOld : Fragment(R.layout.fragment_work_order_history_time) {

    private var _binding: FragmentWorkOrderHistoryTimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var payDayViewModel: PayDayViewModel
    private lateinit var curDateString: String
    private lateinit var startTime: Calendar
    private lateinit var endTime: Calendar
    private lateinit var curWorkOrderHistory: WorkOrderHistoryCombined
    private lateinit var workTimes: WorkTimes
    private var totalRegHours = 0.0
    private var totalOtHours = 0.0
    private var totalDblOtHours = 0.0
    private var totalRegHoursForDay = 0.0
    private var totalOtHoursForDay = 0.0
    private var totalDblOtHoursForDay = 0.0
    private val timeWorkedByDay = ArrayList<WorkOrderHistoryTimeWorkedCombined>()
    private val timeWorkedByDayAsCalendarPairs = ArrayList<Pair<Calendar, Calendar>>()
    private lateinit var existingHistoriesForDay: List<WorkOrderHistoryTimeWorkedCombined>
    private lateinit var existingHistoriesForWorkOrder: List<WorkOrderHistoryTimeWorkedCombined>
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    //    private val defaultScope = CoroutineScope(Dispatchers.Default)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderHistoryTimeBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        payDayViewModel = mainActivity.payDayViewModel
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

    fun populateValues() {
        mainScope.launch {
            val populateWorkOrderHistoryDeferred = async { populateWorkOrderHistory() }
            awaitAll(populateWorkOrderHistoryDeferred)
            delay(WAIT_250)
            val populateWorkTimesDeferred = async {
                workTimes =
                    WorkTimes(
                        mainActivity,
                        curWorkOrderHistory.workOrder.woEmployerId,
                        curWorkOrderHistory.workDate.workDateId,
                    )
            }
            awaitAll(populateWorkTimesDeferred)
            val populateExistingHistoriesDeferred = async {
                populateExistingHistoriesForDay()
                populateExistingHistoriesForWorkOrder()
                populateWorkOrderInfo()
                populateTimesFromHistory()
            }
            awaitAll(populateExistingHistoriesDeferred)
            delay(WAIT_500)
            populateUi()
        }
    }

    private fun populateExistingHistoriesForWorkOrder() {
        workOrderViewModel.getTimeWorkedForWorkOrderHistory(curWorkOrderHistory.workOrderHistory.woHistoryId)
            .observe(viewLifecycleOwner) { histories ->
                existingHistoriesForWorkOrder = histories
            }
    }

    fun populateUi() {
        mainScope.launch {
            val populateExistingHistoryCalendarPairsDeferred = async {
                populateExistingHistoryCalendarPairs()
                calculateAdjustmentsForRegAndOt(endTime)
            }
            awaitAll(populateExistingHistoryCalendarPairsDeferred)
            delay(WAIT_250)
            adjustStartTimeToLastTimeWorkedForDay()
            adjustWorkTimeTypes()
            calculateTimesToDisplay()
            updateTimesDisplayed()
//            populateExistingTimesRecycler()
        }
    }

    private fun populateWorkOrderHistory() {
        if (mainViewModel.getWorkOrderHistory() != null) {
            workOrderViewModel.getWorkOrderHistoryCombined(mainViewModel.getWorkOrderHistory()!!.woHistoryId)
                .observe(viewLifecycleOwner) { historyCombined ->
                    curWorkOrderHistory = historyCombined
                    curDateString = curWorkOrderHistory.workDate.wdDate

                }
        }
    }

    private fun populateWorkOrderInfo() {
        binding.apply {
            val display =
                "${getString(R.string.set_time_for_wo)} ${curWorkOrderHistory.workOrder.woNumber} " +
                        "${getString(R.string.at_)} ${curWorkOrderHistory.workOrder.woAddress} " +
                        "${getString(R.string._on_)} ${df.getDisplayDate(curWorkOrderHistory.workDate.wdDate)}"
            tvInfo.text = display
        }
    }

    private fun populateTimesFromHistory() {
        val tempDate = curWorkOrderHistory.workDate.wdDate.split("-")
        startTime.set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
        endTime.set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
        startTime.set(Calendar.HOUR_OF_DAY, 8)
        startTime.set(Calendar.MINUTE, 30)
        startTime.set(Calendar.SECOND, 0)
        if (endTime < startTime) {
            endTime = startTime
        }
    }

    private fun populateExistingHistoryCalendarPairs() {
        timeWorkedByDay.clear()
        timeWorkedByDayAsCalendarPairs.clear()
        for (time in existingHistoriesForDay) {
            timeWorkedByDay.add(time)
            val start = df.getCalendarFromString(time.timeWorked.wohtStartTime)
            val end = df.getCalendarFromString(time.timeWorked.wohtEndTime)
            timeWorkedByDayAsCalendarPairs.add(Pair(start, end))
        }
    }

    private fun adjustStartTimeToLastTimeWorkedForDay() {
        if (existingHistoriesForDay.isNotEmpty()) {
            val tempStartTime =
                df.splitTimeFromDateTime(existingHistoriesForDay.last().timeWorked.wohtEndTime)
            startTime.set(Calendar.HOUR_OF_DAY, tempStartTime[0].toInt())
            startTime.set(Calendar.MINUTE, tempStartTime[1].toInt())
            startTime.set(Calendar.SECOND, 0)
        }
    }

    private fun adjustWorkTimeTypes() {
        binding.apply {
            if (radBreak.isChecked && totalRegHoursForDay < 8.0) {
                radRegHours.isChecked = true
                radBreak.isChecked = false
                radOtHours.isChecked = false
                radDblOtHours.isChecked = false
            }
            if (totalRegHoursForDay >= 8.0) {
                radOtHours.isChecked = true
                radRegHours.isChecked = false
                radDblOtHours.isChecked = false
                radBreak.isChecked = false
            }
            if (totalOtHoursForDay + totalRegHoursForDay >= 12.0) {
                radDblOtHours.isChecked = true
                radOtHours.isChecked = false
                radRegHours.isChecked = false
                radBreak.isChecked = false
            }
        }
    }


    private fun calculateTimeWorkForDay() {
        if (existingHistoriesForDay.isNotEmpty()) {
            totalRegHoursForDay = 0.0
            totalOtHoursForDay = 0.0
            totalDblOtHoursForDay = 0.0
            for (time in existingHistoriesForWorkOrder) {
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

    private fun calculateTimesToDisplay() {
        calculateTimeWorkForDay()
        mainScope.launch {
            binding.apply {
                totalRegHours = 0.0
                totalOtHours = 0.0
                totalDblOtHours = 0.0
                for (time in existingHistoriesForWorkOrder) {
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
                if (radBreak.isChecked) {
                    radHourType.check(R.id.radRegHours)
                }
                if (totalRegHoursForDay >= 8.0) {
                    radHourType.check(R.id.radOtHours)
                }
                if (totalOtHoursForDay + totalRegHoursForDay >= 12.0) {
                    radHourType.check(R.id.radDblOtHours)
                }
                delay(WAIT_250)
            }
        }
    }

    private fun updateTimesDisplayed() {
        binding.apply {
//            Log.d(TAG, "displayTimesAndHours: ${df.get12HourDisplay(endTime)}")
            clkStartTime.text = df.get12HourDisplay(startTime)
            clkEndTime.text = df.get12HourDisplay(endTime)
            val display = nf.getNumberFromDouble(
                df.getTimeWorked(
                    startTime,
                    endTime
                )
            ) + " " + getString(R.string.hours)
            tvTotalTime.text = display
        }
    }


//    private fun populateExistingTimesRecycler() {
//        val workOrderHistoryTimeWorkedAdapter =
//            WorkOrderHistoryTimeWorkedAdapter(mainActivity, mView, TAG, this)
//        binding.rvTimeWorked.apply {
//            layoutManager = LinearLayoutManager(mView.context)
//            adapter = workOrderHistoryTimeWorkedAdapter
//        }
//        workOrderHistoryTimeWorkedAdapter.differ.submitList(existingHistoriesForWorkOrder)
//    }

    private fun populateExistingHistoriesForDay() {
        workOrderViewModel.getTimeWorkedPerDay(curWorkOrderHistory.workDate.workDateId)
            .observe(viewLifecycleOwner) { histories ->
                existingHistoriesForDay = histories
            }
    }

    fun setClickActions() {
        binding.apply {
            setStartTimeActions()
            setEndTimeAction()
            btnEnterTime.setOnClickListener { insertTime() }
            fabDone.setOnClickListener {
                chooseToSaveOrDiscard()
            }
        }
    }

    private fun chooseToSaveOrDiscard() {
        if (df.getTimeWorked(startTime, endTime) > 0.0) {
            AlertDialog.Builder(mView.context)
                .setTitle(getString(R.string.unsaved_time))
                .setMessage(getString(R.string.would_you_like_to_save_the_time_entered))
                .setPositiveButton(getString(R.string.enter_time)) { _, _ ->
                    mainScope.launch {
                        insertTime(false)
                        delay(WAIT_250)
                        gotoWorkOrderHistoryUpdate()
                    }
                }
                .setNeutralButton(getString(R.string.go_back), null)
                .setNegativeButton(getString(R.string.no)) { _, _ ->
                    gotoWorkOrderHistoryUpdate()
                }
                .show()
        } else {
            gotoWorkOrderHistoryUpdate()
        }
    }


    private fun setStartTimeActions() {
        binding.apply {
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    val tempStartTime =
                        df.roundTimeDownTo15Minutes(hourOfDay, minute)
                    startTime.set(Calendar.HOUR_OF_DAY, tempStartTime.first)
                    startTime.set(Calendar.MINUTE, tempStartTime.second)
                    startTime.set(Calendar.SECOND, 0)
                    calculateTimesToDisplay()
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
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    mainScope.launch {
                        val tempEndTime = hourOfDay * 60 + minute
                        val tempStartTime =
                            startTime.get(Calendar.HOUR_OF_DAY) * 60 + startTime.get(Calendar.MINUTE)
                        if (tempEndTime < tempStartTime) {
                            displayMessage(getString(R.string.end_time_before_start_time))
                            endTime.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY))
                            endTime.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE))
                            endTime.set(Calendar.SECOND, 0)
                        } else {
                            val tempHoursAndMin = df.roundTimeUpTo15Minutes(hourOfDay, minute)
                            endTime.set(Calendar.HOUR_OF_DAY, tempHoursAndMin.first)
                            endTime.set(Calendar.MINUTE, tempHoursAndMin.second)
                            endTime.set(Calendar.SECOND, 0)
                        }
                        populateUi()
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

    private fun calculateAdjustmentsForRegAndOt(time: Calendar) {
        binding.apply {
            val timeNow: Calendar = time.clone() as Calendar
            val tempDate = curDateString.split("-")
            timeNow.set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
            if (df.getTimeWorked(startTime, endTime) < 0.0) {
                endTime = startTime.clone() as Calendar
                if (timeNow > endTime) {
                    endTime = df.roundCalendarTimeUpTo15Minutes(timeNow)
                }
            }
            if (totalRegHoursForDay + df.getTimeWorked(
                    startTime,
                    endTime
                ) > 8.0 && radRegHours.isChecked
            ) {
                val timeToAdjust = 8 - totalRegHoursForDay
                endTime = df.addHoursToCalendar(startTime, timeToAdjust)
                displayMessage(getString(R.string.time_has_been_adjusted_to_8_hours))
            }
            if (totalOtHoursForDay + totalRegHoursForDay + df.getTimeWorked(
                    startTime,
                    endTime
                ) > 12.0 && radOtHours.isChecked
            ) {
                val timeToAdjust = 12 - totalOtHoursForDay - totalRegHoursForDay
                endTime = df.addHoursToCalendar(startTime, timeToAdjust)
                displayMessage(getString(R.string.time_has_been_adjusted_to_12_hours))
            }
            updateTimesDisplayed()
        }
    }


    private fun insertTime(updateUi: Boolean = true): Boolean {
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
                        if (updateUi) {
                            delay(WAIT_250)
                            updateWorkOrderHistoryInDb(curWorkOrderHistory.workOrderHistory)
                            calculateWorkDateHoursAndUpdateDb(curWorkOrderHistory.workDate)
                            delay(WAIT_250)
                            populateUi()
                        }
                    }
                    return true
                } catch (e: SQLiteConstraintException) {
                    displayMessage(getString(R.string.error_) + e.message)
                    Log.d(TAG, e.message.toString())
                    return false
                }
            }
        } else {
            displayMessage("${getString(R.string.error_)} $answer")
            return false
        }
    }

    private fun calculateWorkDateHoursAndUpdateDb(workDate: WorkDates) {
        var hrsRegByTimeEntered = 0.0
        var hrsOtByTimeEntered = 0.0
        var hrsDblOtByTimeEntered = 0.0
        for (history in existingHistoriesForDay) {
            hrsRegByTimeEntered += if (history.timeWorked.wohtTimeType == TimeWorkedTypes.REG_HOURS.value) {
                df.getTimeWorked(
                    history.timeWorked.wohtStartTime,
                    history.timeWorked.wohtEndTime
                )
            } else {
                0.0
            }
            hrsOtByTimeEntered += if (history.timeWorked.wohtTimeType == TimeWorkedTypes.OT_HOURS.value) {
                df.getTimeWorked(
                    history.timeWorked.wohtStartTime,
                    history.timeWorked.wohtEndTime
                )
            } else {
                0.0
            }
            hrsDblOtByTimeEntered += if (history.timeWorked.wohtTimeType == TimeWorkedTypes.DBL_OT_HOURS.value) {
                df.getTimeWorked(
                    history.timeWorked.wohtStartTime,
                    history.timeWorked.wohtEndTime
                )
            } else {
                0.0
            }
            updateWorkDateInDb(
                hrsRegByTimeEntered,
                hrsOtByTimeEntered,
                hrsDblOtByTimeEntered,
                workDate
            )
        }
    }

    private fun updateWorkDateInDb(
        hrsReg: Double,
        hrsOt: Double,
        hrsDblOt: Double,
        workDate: WorkDates
    ) {
        payDayViewModel.updateWorkDate(
            WorkDates(
                workDate.workDateId,
                workDate.wdPayPeriodId,
                workDate.wdEmployerId,
                workDate.wdCutoffDate,
                workDate.wdDate,
                hrsReg,
                hrsOt,
                hrsDblOt,
                workDate.wdStatHours,
                workDate.wdNote,
                false,
                df.getCurrentTimeAsString()
            )
        )
    }

    private fun updateWorkOrderHistoryInDb(workOrderHistory: WorkOrderHistory) {
        workOrderViewModel.getTimeWorkedForWorkOrderHistory(workOrderHistory.woHistoryId)
            .observe(viewLifecycleOwner) { timesList ->
                var hrsReg = 0.0
                var hrsOt = 0.0
                var hrsDblOt = 0.0
                for (time in timesList) {
                    hrsReg += if (time.timeWorked.wohtTimeType == TimeWorkedTypes.REG_HOURS.value) {
                        df.getTimeWorked(
                            time.timeWorked.wohtStartTime,
                            time.timeWorked.wohtEndTime,
                        )
                    } else {
                        0.0
                    }
                    hrsOt += if (time.timeWorked.wohtTimeType == TimeWorkedTypes.OT_HOURS.value) {
                        df.getTimeWorked(
                            time.timeWorked.wohtStartTime,
                            time.timeWorked.wohtEndTime,
                        )
                    } else {
                        0.0
                    }
                    hrsDblOt += if (time.timeWorked.wohtTimeType == TimeWorkedTypes.DBL_OT_HOURS.value) {
                        df.getTimeWorked(
                            time.timeWorked.wohtStartTime,
                            time.timeWorked.wohtEndTime,
                        )
                    } else {
                        0.0
                    }
                }
                workOrderViewModel.updateWorkOrderHistory(
                    WorkOrderHistory(
                        workOrderHistory.woHistoryId,
                        workOrderHistory.woHistoryWorkOrderId,
                        curWorkOrderHistory.workDate.workDateId,
                        hrsReg,
                        hrsOt,
                        hrsDblOt,
                        workOrderHistory.woHistoryNote,
                        false,
                        df.getCurrentTimeAsString()
                    )
                )
            }
    }

    private fun displayMessage(message: String) {
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

    fun gotoCallingFragment() {
        TODO("Not yet implemented")
    }

    fun gotoWorkOrderHistoryTimeUpdateFragment() {
        TODO("Not yet implemented")
    }

    private fun gotoWorkOrderHistoryUpdate() {
        mView.findNavController().navigate(
            WorkOrderHistoryTimeFragmentDirections.actionWorkOrderHistoryTimeToWorkOrderHistoryUpdateFragment()
        )
    }

    fun gotoWorkOrderHistoryTimeUpdate() {
        mainViewModel.addCallingFragment(TAG)
        findNavController().navigate(
            WorkOrderHistoryTimeFragmentDirections.actionWorkOrderHistoryTimeToWorkOrderHistoryTimeUpdateFragment()
        )
    }
}