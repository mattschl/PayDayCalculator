package ms.mattschlenkrich.paycalculator.workorder

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_TIME
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_TIME
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.common.WAIT_1000
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.TimeWorkedByDay
import ms.mattschlenkrich.paycalculator.data.WorkDates
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.data.WorkTimeViewModel
import ms.mattschlenkrich.paycalculator.logic.WorkTimes
import java.util.Calendar

private const val TAG = "WorkOrderHistoryTimeUpdate"

class WorkOrderHistoryTimeUpdateFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workTimeViewModel: WorkTimeViewModel
    private var startTime by mutableStateOf(Calendar.getInstance())
    private var endTime by mutableStateOf(Calendar.getInstance())
    private lateinit var curWorkOrderHistory: WorkOrderHistoryCombined

    private lateinit var timeWorkedByDay: TimeWorkedByDay
    private lateinit var workTimes: WorkTimes

    private val df = DateFunctions()
    private val nf = NumberFunctions()

    private val mainScope = CoroutineScope(Dispatchers.Main)
    private lateinit var workOrderHistoryTimeWorkedCombined: WorkOrderHistoryTimeWorkedCombined

    private var infoText by mutableStateOf("")
    private var originalTimeText by mutableStateOf("")
    private var totalTimeText by mutableStateOf("")
    private var selectedTimeType by mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workTimeViewModel = mainActivity.workTimeViewModel
        mainActivity.topMenuBar.title = getString(R.string.update_work_time)

        return ComposeView(requireContext()).apply {
            setContent {
                WorkOrderHistoryTimeUpdateScreen(
                    infoText = infoText,
                    originalTimeText = originalTimeText,
                    startTime = startTime,
                    endTime = endTime,
                    totalTimeText = totalTimeText,
                    selectedTimeType = selectedTimeType,
                    onTimeTypeChange = {
                        selectedTimeType = it
                        updateTotalTime()
                    },
                    onStartTimeClick = { showStartTimePicker() },
                    onEndTimeClick = { showEndTimePicker() },
                    onSaveClick = { updateTimesInDatabase() }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateVariablesAndValues()
    }

    private fun populateVariablesAndValues() {
        mainScope.launch {
            workOrderHistoryTimeWorkedCombined =
                mainViewModel.getWorkOrderHistoryTimeWorkedCombined()!!
            curWorkOrderHistory = workOrderHistoryTimeWorkedCombined.workOrderHistory
            workTimes = WorkTimes(
                mainActivity,
                curWorkOrderHistory.workOrder.workOrderId,
                curWorkOrderHistory.workDate.workDateId,
                requireView()
            )
            workTimes.instantiateVariables()
            delay(WAIT_500)
            populateWorkOrderInfo()
            populateTimes()
            displayTimesAndHours()
        }
    }

    private fun populateTimes() {
        startTime =
            df.getCalendarFromString(workOrderHistoryTimeWorkedCombined.timeWorked.wohtStartTime)
        endTime =
            df.getCalendarFromString(workOrderHistoryTimeWorkedCombined.timeWorked.wohtEndTime)
        selectedTimeType = workOrderHistoryTimeWorkedCombined.timeWorked.wohtTimeType
    }

    private fun displayTimesAndHours() {
        timeWorkedByDay = workTimes.getTimeWorkedByDay()
        var display =
            "${getString(R.string.original_time)} ${df.get12HourDisplay(startTime)} " +
                    "${getString(R.string._to_)} ${df.get12HourDisplay(endTime)} "

        val timeWorked = df.getTimeWorked(startTime, endTime)
        display += when (selectedTimeType) {
            TimeWorkedTypes.REG_HOURS.value ->
                " ${getString(R.string.reg_hrs_)} ${nf.getNumberFromDouble(timeWorked)}"

            TimeWorkedTypes.OT_HOURS.value ->
                " ${getString(R.string.ot_hrs_)} ${nf.getNumberFromDouble(timeWorked)}"

            TimeWorkedTypes.DBL_OT_HOURS.value ->
                " ${getString(R.string.dbl_ot_)} ${nf.getNumberFromDouble(timeWorked)}"

            else -> " Break Time"
        }
        originalTimeText = display
        updateTotalTime()
    }

    private fun updateTotalTime() {
        totalTimeText =
            "${getString(R.string.total_time)} ${df.getTimeWorked(startTime, endTime)}"
    }

    private fun populateWorkOrderInfo() {
        infoText =
            "${getString(R.string.set_time_for_wo)} ${workOrderHistoryTimeWorkedCombined.workOrderHistory.workOrder.woNumber} " +
                    "${getString(R.string.at_)} ${workOrderHistoryTimeWorkedCombined.workOrderHistory.workOrder.woAddress} " +
                    "${getString(R.string._on_)} ${
                        df.getDisplayDate(
                            workOrderHistoryTimeWorkedCombined.workOrderHistory.workDate.wdDate
                        )
                    }"
    }

    private fun showStartTimePicker() {
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val tempStartTime =
                    df.roundTimeTo15Minutes(hourOfDay, minute)
                val newStart = startTime.clone() as Calendar
                newStart.set(Calendar.HOUR_OF_DAY, tempStartTime.first)
                newStart.set(Calendar.MINUTE, tempStartTime.second)
                newStart.set(Calendar.SECOND, 0)
                startTime = newStart
                updateTotalTime()
            }
        val startTimePicker = TimePickerDialog(
            requireContext(),
            timeSetListener,
            df.get12HourIntOfHour(startTime),
            df.get12HourIntOfMinute(startTime),
            false
        )
        startTimePicker.setTitle(getString(R.string.select_start_time))
        startTimePicker.show()
    }

    private fun showEndTimePicker() {
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                mainScope.launch {
                    val tempEndTime = hourOfDay * 60 + minute
                    val tempStartTime =
                        startTime.get(Calendar.HOUR_OF_DAY) * 60 + startTime.get(Calendar.MINUTE)
                    val newEnd = endTime.clone() as Calendar
                    if (tempEndTime < tempStartTime) {
                        displayMessage(getString(R.string.end_time_before_start_time))
                        newEnd.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY))
                        newEnd.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE))
                    } else {
                        val tempHoursAndMin = df.roundTimeUpTo15Minutes(hourOfDay, minute)
                        newEnd.set(Calendar.HOUR_OF_DAY, tempHoursAndMin.first)
                        newEnd.set(Calendar.MINUTE, tempHoursAndMin.second)
                    }
                    newEnd.set(Calendar.SECOND, 0)
                    endTime = newEnd
                    updateTotalTime()
                }
            }
        val endTimePickerDialog = TimePickerDialog(
            requireContext(),
            timeSetListener,
            df.get12HourIntOfHour(endTime),
            df.get12HourIntOfMinute(endTime),
            false
        )
        endTimePickerDialog.setTitle(getString(R.string.select_end_time))
        endTimePickerDialog.show()
    }

    private fun updateTimesInDatabase() {
        val answer = validateTimeWorked()
        if (answer == ANSWER_OK) {
            try {
                mainScope.launch {
                    workTimeViewModel.updateWorkTime(
                        WorkOrderHistoryTimeWorked(
                            workOrderHistoryTimeWorkedCombined.timeWorked.woHistoryTimeWorkedId,
                            workOrderHistoryTimeWorkedCombined.timeWorked.wohtHistoryId,
                            workOrderHistoryTimeWorkedCombined.timeWorked.wohtDateId,
                            df.getDateFromCalendarAsString(startTime),
                            df.getDateFromCalendarAsString(endTime),
                            selectedTimeType,
                            false,
                            df.getCurrentTimeAsString()
                        )
                    )
                    workTimes.instantiateVariables()
                    delay(WAIT_1000)

                    workTimeViewModel.updateWorkOrderHistory(
                        workTimes.getWorkOrderHistory(
                            workOrderHistoryTimeWorkedCombined.workOrderHistory.workOrder.workOrderId
                        )!!
                    )
                    workTimeViewModel.updateWorkDate(
                        WorkDates(
                            workOrderHistoryTimeWorkedCombined.workDate.workDateId,
                            workOrderHistoryTimeWorkedCombined.workDate.wdPayPeriodId,
                            workOrderHistoryTimeWorkedCombined.workDate.wdEmployerId,
                            workOrderHistoryTimeWorkedCombined.workDate.wdCutoffDate,
                            workOrderHistoryTimeWorkedCombined.workDate.wdDate,
                            workTimes.getTimeWorkedByDay().hrsRegByTimeEntered,
                            workTimes.getTimeWorkedByDay().hrsOtByTimeEntered,
                            workTimes.getTimeWorkedByDay().hrsDblOtByTimeEntered,
                            workOrderHistoryTimeWorkedCombined.workDate.wdStatHours,
                            workOrderHistoryTimeWorkedCombined.workDate.wdNote,
                            false,
                            df.getCurrentTimeAsString()
                        )
                    )
                    gotoCallingFragment()
                }
            } catch (e: SQLiteConstraintException) {
                displayMessage(getString(R.string.error_) + e.message)
                Log.d(TAG, e.message.toString())
            }
        } else {
            displayMessage(answer)
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
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
        if (mainViewModel.getCallingFragment() != null) {
            val callingFragment = mainViewModel.getCallingFragment()!!
            if (callingFragment.contains(FRAG_WORK_ORDER_HISTORY_TIME)) {
                gotoWorkOrderHistoryTimeFragment()
            } else if (callingFragment.contains(FRAG_WORK_DATE_TIME)) {
                gotoWorkDateTimesFragment()
            }
        }
    }

    private fun gotoWorkOrderHistoryTimeFragment() {
        mainViewModel.setCallingFragment(null)
        findNavController().navigate(
            WorkOrderHistoryTimeUpdateFragmentDirections.actionWorkOrderHistoryTimeUpdateFragmentToWorkOrderHistoryTime()
        )
    }

    private fun gotoWorkDateTimesFragment() {
        mainViewModel.setCallingFragment(null)
        findNavController().navigate(
            WorkOrderHistoryTimeUpdateFragmentDirections.actionWorkOrderHistoryTimeUpdateFragmentToWorkDateTimesFragment()
        )
    }
}