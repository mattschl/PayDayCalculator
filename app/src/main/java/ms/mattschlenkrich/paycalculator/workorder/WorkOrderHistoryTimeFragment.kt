package ms.mattschlenkrich.paycalculator.workorder

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_TIME
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.common.WAIT_1000
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.TimeWorkedByDay
import ms.mattschlenkrich.paycalculator.data.WorkDates
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.logic.IWorkTimesFragment
import ms.mattschlenkrich.paycalculator.logic.WorkTimes
import java.util.Calendar

private const val TAG = FRAG_WORK_ORDER_HISTORY_TIME

class WorkOrderHistoryTimeFragment : Fragment(),
    IWorkTimesFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var payDayViewModel: PayDayViewModel
    private lateinit var curDateString: String
    private var startTimeState = mutableStateOf(Calendar.getInstance())
    private var endTimeState = mutableStateOf(Calendar.getInstance())
    private lateinit var curWorkOrderHistory: WorkOrderHistoryCombined
    private lateinit var workTimes: WorkTimes
    private var timeWorkedByDayData = mutableStateOf(TimeWorkedByDay())
    private var selectedTimeType = mutableIntStateOf(TimeWorkedTypes.REG_HOURS.value)

    private var infoText = mutableStateOf("")
    private var hoursSummaryText = mutableStateOf("")
    private var totalTimeText = mutableStateOf("")

    private var existingHistoriesForDay =
        mutableStateOf<List<WorkOrderHistoryTimeWorkedCombined>>(emptyList())
    private var existingHistoriesForWorkOrder =
        mutableStateOf<List<WorkOrderHistoryTimeWorkedCombined>>(emptyList())
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        payDayViewModel = mainActivity.payDayViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                val existingTimes by existingHistoriesForWorkOrder
                val startTime by startTimeState
                val endTime by endTimeState
                val timeType by selectedTimeType
                val info by infoText
                val hoursSummary by hoursSummaryText
                val totalTime by totalTimeText

                WorkOrderHistoryTimeScreen(
                    infoText = info,
                    hoursSummaryText = hoursSummary,
                    startTime = startTime,
                    endTime = endTime,
                    totalTimeText = totalTime,
                    selectedTimeType = timeType,
                    onTimeTypeChange = { selectedTimeType.intValue = it },
                    onStartTimeClick = { showStartTimePicker() },
                    onEndTimeClick = { showEndTimePicker() },
                    onEnterTimeClick = { insertTimeWorkedIfValid() },
                    onDoneClick = { chooseToSaveOrDiscard() },
                    existingTimes = existingTimes,
                    onTimeClick = {
                        mainViewModel.setWorkOrderHistoryTimeWorkedCombined(it)
                        gotoWorkOrderHistoryTimeUpdateFragment()
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
    }

    override fun populateValues() {
        lifecycleScope.launch {
            populateWorkOrderHistory()
            delay(WAIT_250)
            if (::curWorkOrderHistory.isInitialized) {
                workTimes = WorkTimes(
                    mainActivity,
                    curWorkOrderHistory.workOrder.woEmployerId,
                    curWorkOrderHistory.workDate.workDateId,
                    requireView()
                )
                populateUi()
            }
        }
    }

    private fun populateWorkOrderHistory() {
        mainViewModel.getWorkOrderHistory()?.let { history ->
            workOrderViewModel.getWorkOrderHistoryCombined(history.woHistoryId)
                .observe(viewLifecycleOwner) { historyCombined ->
                    curWorkOrderHistory = historyCombined
                    curDateString = curWorkOrderHistory.workDate.wdDate
                }
        }
    }

    override fun populateUi() {
        lifecycleScope.launch {
            workTimes.instantiateVariables()
            delay(WAIT_1000)
            populateExistingHistoriesForDay()
            populateExistingHistoriesForWorkOrder()
            populateWorkOrderInfo()
            populateTimesFromHistory()

            timeWorkedByDayData.value = workTimes.getTimeWorkedByDay()
            delay(WAIT_250)
            adjustStartTimeToLastTimeWorkedForDay()
            calculateAdjustmentsForRegAndOt(endTimeState.value)
            adjustWorkTimeTypes()
            calculateTimesToDisplay()
            updateTimesDisplayed()
        }
    }


    override fun updateUi() {
        populateUi()
    }

    private fun populateExistingHistoriesForDay() {
        existingHistoriesForDay.value = workTimes.getWorkOrderHistoryTimeWorkedList()
    }

    private fun populateExistingHistoriesForWorkOrder() {
        existingHistoriesForWorkOrder.value =
            workTimes.getWorkOrderHistoryWithTimes(curWorkOrderHistory.workOrder.workOrderId)
    }

    private fun populateWorkOrderInfo() {
        infoText.value =
            "${getString(R.string.set_time_for_wo)} ${curWorkOrderHistory.workOrder.woNumber} " +
                    "${getString(R.string.at_)} ${curWorkOrderHistory.workOrder.woAddress} " +
                    "${getString(R.string._on_)} ${df.getDisplayDate(curWorkOrderHistory.workDate.wdDate)}"
    }

    private fun populateTimesFromHistory() {
        val tempDate = curWorkOrderHistory.workDate.wdDate.split("-")
        val start = Calendar.getInstance().apply {
            set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }
        val end = start.clone() as Calendar
        startTimeState.value = start
        endTimeState.value = end
    }

    private fun adjustStartTimeToLastTimeWorkedForDay() {
        if (existingHistoriesForDay.value.isNotEmpty()) {
            val tempStartTime =
                df.splitTimeFromDateTime(existingHistoriesForDay.value.last().timeWorked.wohtEndTime)
            val newStart = startTimeState.value.clone() as Calendar
            newStart.set(Calendar.HOUR_OF_DAY, tempStartTime[0].toInt())
            newStart.set(Calendar.MINUTE, tempStartTime[1].toInt())
            newStart.set(Calendar.SECOND, 0)
            startTimeState.value = newStart
        }
    }

    private fun calculateAdjustmentsForRegAndOt(time: Calendar) {
        val timeNow: Calendar = time.clone() as Calendar
        val tempDate = curDateString.split("-")
        timeNow.set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
        if (df.getTimeWorked(startTimeState.value, endTimeState.value) < 0.0) {
            endTimeState.value = startTimeState.value.clone() as Calendar
            if (timeNow > endTimeState.value) {
                endTimeState.value = df.roundCalendarTimeUpTo15Minutes(timeNow)
            }
        }
        if (timeWorkedByDayData.value.hrsReg + df.getTimeWorked(
                startTimeState.value,
                endTimeState.value
            ) > 8.0 && selectedTimeType.intValue == TimeWorkedTypes.REG_HOURS.value
        ) {
            val timeToAdjust = 8 - timeWorkedByDayData.value.hrsReg
            endTimeState.value = df.addHoursToCalendar(startTimeState.value, timeToAdjust)
            displayMessage(getString(R.string.time_has_been_adjusted_to_8_hours))
        }
        if (timeWorkedByDayData.value.hrsOt + timeWorkedByDayData.value.hrsReg + df.getTimeWorked(
                startTimeState.value,
                endTimeState.value
            ) > 12.0 && (selectedTimeType.intValue == TimeWorkedTypes.REG_HOURS.value || selectedTimeType.intValue == TimeWorkedTypes.OT_HOURS.value)
        ) {
            val timeToAdjust =
                12 - timeWorkedByDayData.value.hrsOt - timeWorkedByDayData.value.hrsReg
            endTimeState.value = df.addHoursToCalendar(startTimeState.value, timeToAdjust)
            displayMessage(getString(R.string.time_has_been_adjusted_to_12_hours))
        }
    }

    private fun adjustWorkTimeTypes() {
        if (selectedTimeType.intValue == TimeWorkedTypes.BREAK.value && timeWorkedByDayData.value.hrsReg < 8.0) {
            selectedTimeType.intValue = TimeWorkedTypes.REG_HOURS.value
        }
        if (timeWorkedByDayData.value.hrsReg >= 8.0) {
            selectedTimeType.intValue = TimeWorkedTypes.OT_HOURS.value
        }
        if (timeWorkedByDayData.value.hrsOt + timeWorkedByDayData.value.hrsReg >= 12.0) {
            selectedTimeType.intValue = TimeWorkedTypes.DBL_OT_HOURS.value
        }
    }

    private fun calculateTimesToDisplay() {
        totalTimeText.value = "${getString(R.string.total_hours)} ${
            nf.getNumberFromDouble(
                df.getTimeWorked(
                    startTimeState.value,
                    endTimeState.value
                )
            )
        } "

        timeWorkedByDayData.value = workTimes.getTimeWorkedByDay()
        var display2 = ""
        if (timeWorkedByDayData.value.hrsReg > 0.0) display2 += getString(R.string.reg_hrs_) + nf.getNumberFromDouble(
            timeWorkedByDayData.value.hrsReg
        )
        if (timeWorkedByDayData.value.hrsOt > 0.0) {
            if (display2 != "") display2 += getString(R.string.pipe)
            display2 += getString(R.string.ot_hrs_) + nf.getNumberFromDouble(timeWorkedByDayData.value.hrsOt)
        }
        if (timeWorkedByDayData.value.hrsDblOt > 0.0) {
            if (display2 != "") display2 += getString(R.string.pipe)
            display2 += getString(R.string.dbl_ot_) + nf.getNumberFromDouble(timeWorkedByDayData.value.hrsDblOt)
        }
        if (timeWorkedByDayData.value.hrsStat > 0.0) {
            if (display2 != "") display2 += getString(R.string.pipe)
            display2 += getString(R.string.other_hours_) + nf.getNumberFromDouble(
                timeWorkedByDayData.value.hrsStat
            )
        }
        if (display2 != "") display2 = getString(R.string.time_entered_for_date) + display2

        val tempWorkOrderHistory =
            workTimes.getWorkOrderHistory(curWorkOrderHistory.workOrder.workOrderId)
        var display = ""
        if (tempWorkOrderHistory != null) {
            if (tempWorkOrderHistory.woHistoryRegHours > 0.0)
                display =
                    getString(R.string.reg_hrs_) + nf.getNumberFromDouble(tempWorkOrderHistory.woHistoryRegHours)
            if (tempWorkOrderHistory.woHistoryOtHours > 0.0) {
                if (display != "") display += getString(R.string.pipe)
                display += getString(R.string.ot_hrs_) + nf.getNumberFromDouble(
                    tempWorkOrderHistory.woHistoryOtHours
                )
            }
            if (tempWorkOrderHistory.woHistoryDblOtHours > 0.0) {
                if (display != "") display += getString(R.string.pipe)
                display += getString(R.string.dbl_ot_) + nf.getNumberFromDouble(
                    tempWorkOrderHistory.woHistoryDblOtHours
                )
            }
            if (display != "") display =
                getString(R.string.time_entered_for_work_order) + display
        }
        hoursSummaryText.value = display + "\n$display2"

        if (selectedTimeType.intValue == TimeWorkedTypes.BREAK.value) {
            selectedTimeType.intValue = TimeWorkedTypes.REG_HOURS.value
        }
        if (timeWorkedByDayData.value.hrsReg >= 8.0) {
            selectedTimeType.intValue = TimeWorkedTypes.OT_HOURS.value
        }
        if (timeWorkedByDayData.value.hrsOt + timeWorkedByDayData.value.hrsReg >= 12.0) {
            selectedTimeType.intValue = TimeWorkedTypes.DBL_OT_HOURS.value
        }
    }

    private fun updateTimesDisplayed() {
        val display = nf.getNumberFromDouble(
            df.getTimeWorked(
                startTimeState.value,
                endTimeState.value
            )
        ) + " " + getString(R.string.hours)
        totalTimeText.value = display
    }


    override fun setClickActions() {
        // Handled in Compose
    }

    private fun showStartTimePicker() {
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val tempStartTime =
                    df.roundTimeDownTo15Minutes(hourOfDay, minute)
                val newStart = startTimeState.value.clone() as Calendar
                newStart.set(Calendar.HOUR_OF_DAY, tempStartTime.first)
                newStart.set(Calendar.MINUTE, tempStartTime.second)
                newStart.set(Calendar.SECOND, 0)
                startTimeState.value = newStart
                updateTimesDisplayed()
            }
        val startTimePicker = TimePickerDialog(
            requireContext(),
            timeSetListener,
            df.get12HourIntOfHour(startTimeState.value),
            df.get12HourIntOfMinute(startTimeState.value),
            false // true for 24-hour format, false for AM/PM
        )
        startTimePicker.setTitle(getString(R.string.select_start_time))
        startTimePicker.show()
    }

    private fun showEndTimePicker() {
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                lifecycleScope.launch {
                    val tempEndTime = hourOfDay * 60 + minute
                    val tempStartTime =
                        startTimeState.value.get(Calendar.HOUR_OF_DAY) * 60 + startTimeState.value.get(
                            Calendar.MINUTE
                        )
                    val newEnd = endTimeState.value.clone() as Calendar
                    if (tempEndTime < tempStartTime) {
                        displayMessage(getString(R.string.end_time_before_start_time))
                        newEnd.set(
                            Calendar.HOUR_OF_DAY,
                            startTimeState.value.get(Calendar.HOUR_OF_DAY)
                        )
                        newEnd.set(Calendar.MINUTE, startTimeState.value.get(Calendar.MINUTE))
                        newEnd.set(Calendar.SECOND, 0)
                    } else {
                        val tempHoursAndMin = df.roundTimeUpTo15Minutes(hourOfDay, minute)
                        newEnd.set(Calendar.HOUR_OF_DAY, tempHoursAndMin.first)
                        newEnd.set(Calendar.MINUTE, tempHoursAndMin.second)
                        newEnd.set(Calendar.SECOND, 0)
                    }
                    endTimeState.value = newEnd
                    lifecycleScope.launch {
                        calculateAdjustmentsForRegAndOt(endTimeState.value)
                        delay(WAIT_250)
                        updateTimesDisplayed()
                    }
                }
            }
        val endTimePickerDialog = TimePickerDialog(
            requireContext(),
            timeSetListener,
            df.get12HourIntOfHour(endTimeState.value),
            df.get12HourIntOfMinute(endTimeState.value),
            false // true for 24-hour format, false for AM/PM
        )
        endTimePickerDialog.setTitle(getString(R.string.select_end_time))
        endTimePickerDialog.show()
    }


    private fun insertTimeWorkedIfValid(updateUi: Boolean = true): Boolean {
        val answer = validateTimeWorked()
        if (answer == ANSWER_OK) {
            val workTimeType = selectedTimeType.intValue
            try {
                lifecycleScope.launch {
                    val finalStart = df.roundCalendarTimeTo15Minutes(startTimeState.value)
                    val finalEnd = df.roundCalendarTimeTo15Minutes(endTimeState.value)
                    startTimeState.value = finalStart
                    endTimeState.value = finalEnd
                    insertTimeWorked(workTimeType)
                    if (updateUi) {
                        delay(WAIT_250)
                        updateWorkOrderHistoryInDb(curWorkOrderHistory.workOrderHistory)
                        calculateWorkDateHoursAndUpdateDb(curWorkOrderHistory.workDate)
                        delay(WAIT_250)
                        updateUi()
                    }
                }
                return true
            } catch (e: SQLiteConstraintException) {
                displayMessage(getString(R.string.error_) + e.message)
                Log.d(TAG, e.message.toString())
                return false
            }
        } else {
            displayMessage("${getString(R.string.error_)} $answer")
            return false
        }
    }

    private fun validateTimeWorked(): String {
        if (selectedTimeType.intValue == TimeWorkedTypes.REG_HOURS.value) {
            if (timeWorkedByDayData.value.hrsReg + df.getTimeWorked(
                    startTimeState.value,
                    endTimeState.value
                ) > 8.0
            ) {
                return getString(R.string.this_will_exceed_8_hours)
            }
        }
        if (selectedTimeType.intValue == TimeWorkedTypes.OT_HOURS.value) {
            if (timeWorkedByDayData.value.hrsReg + timeWorkedByDayData.value.hrsOt + df.getTimeWorked(
                    startTimeState.value,
                    endTimeState.value
                ) > 12.0
            ) {
                return getString(R.string.this_will_exceed_12_hours)
            }
        }
        if (selectedTimeType.intValue == TimeWorkedTypes.DBL_OT_HOURS.value) {
            if (timeWorkedByDayData.value.hrsReg + timeWorkedByDayData.value.hrsOt + timeWorkedByDayData.value.hrsDblOt + df.getTimeWorked(
                    startTimeState.value,
                    endTimeState.value
                ) > 18.0
            ) {
                return getString(R.string.this_will_exceed_24_hours)
            }
        }
        if (endTimeState.value.timeInMillis < startTimeState.value.timeInMillis) {
            return getString(R.string.end_time_before_start_time)
        }
        if (endTimeState.value.get(Calendar.HOUR_OF_DAY) == startTimeState.value.get(Calendar.HOUR_OF_DAY) &&
            endTimeState.value.get(Calendar.MINUTE) == startTimeState.value.get(Calendar.MINUTE)
        ) {
            return getString(R.string.end_time_same_as_start_time)
        }
        return ANSWER_OK
    }

    private fun insertTimeWorked(workTimeType: Int) {
        workOrderViewModel.insertTimeWorked(
            WorkOrderHistoryTimeWorked(
                nf.generateRandomIdAsLong(),
                curWorkOrderHistory.workOrderHistory.woHistoryId,
                curWorkOrderHistory.workDate.workDateId,
                df.getDateFromCalendarAsString(startTimeState.value),
                df.getDateFromCalendarAsString(endTimeState.value),
                workTimeType,
                false,
                df.getCurrentTimeAsString()
            )
        )
    }

    private fun chooseToSaveOrDiscard() {
        if (df.getTimeWorked(startTimeState.value, endTimeState.value) > 0.0) {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.unsaved_time))
                .setMessage(getString(R.string.would_you_like_to_save_the_time_entered))
                .setPositiveButton(getString(R.string.enter_time)) { _, _ ->
                    lifecycleScope.launch {
                        insertTimeWorkedIfValid(false)
                        delay(WAIT_250)
                        gotoWorkOrderHistoryUpdateFragment()
                    }
                }
                .setNeutralButton(getString(R.string.go_back), null)
                .setNegativeButton(getString(R.string.no)) { _, _ ->
                    gotoWorkOrderHistoryUpdateFragment()
                }
                .show()
        } else {
            gotoWorkOrderHistoryUpdateFragment()
        }
    }

    private fun calculateWorkDateHoursAndUpdateDb(workDate: WorkDates) {
        var hrsReg = timeWorkedByDayData.value.hrsReg
        var hrsOt = timeWorkedByDayData.value.hrsOt
        var hrsDblOt = timeWorkedByDayData.value.hrsDblOt
        if (selectedTimeType.intValue == TimeWorkedTypes.REG_HOURS.value) {
            hrsReg += df.getTimeWorked(startTimeState.value, endTimeState.value)
        }
        if (selectedTimeType.intValue == TimeWorkedTypes.OT_HOURS.value) {
            hrsOt += df.getTimeWorked(startTimeState.value, endTimeState.value)
        }
        if (selectedTimeType.intValue == TimeWorkedTypes.DBL_OT_HOURS.value) {
            hrsDblOt += df.getTimeWorked(startTimeState.value, endTimeState.value)
        }
        updateWorkDateInDb(
            hrsReg,
            hrsOt,
            hrsDblOt,
            workDate
        )
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun gotoCallingFragment() {
        if (mainViewModel.getCallingFragment() != null) {
            val frag = mainViewModel.getCallingFragment()!!
            if (frag.contains(FRAG_WORK_ORDER_HISTORY_UPDATE)) {
                gotoWorkOrderHistoryUpdateFragment()
            }
        }
    }

    private fun gotoWorkOrderHistoryUpdateFragment() {
        requireView().findNavController().navigate(
            WorkOrderHistoryTimeFragmentDirections.actionWorkOrderHistoryTimeToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun gotoWorkOrderHistoryTimeUpdateFragment() {
        mainViewModel.addCallingFragment(TAG)
        findNavController().navigate(
            WorkOrderHistoryTimeFragmentDirections.actionWorkOrderHistoryTimeToWorkOrderHistoryTimeUpdateFragment()
        )
    }
}