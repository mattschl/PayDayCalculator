package ms.mattschlenkrich.paycalculator.workdate

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_TIME
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_UPDATE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.common.WAIT_1000
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.TimeWorkedByDay
import ms.mattschlenkrich.paycalculator.data.WorkDates
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTimeViewModel
import ms.mattschlenkrich.paycalculator.logic.IWorkTimesFragment
import ms.mattschlenkrich.paycalculator.logic.WorkTimes
import java.util.Calendar

private const val TAG = FRAG_WORK_DATE_TIME

class WorkDateTimesFragment : Fragment(), IWorkTimesFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workTimeViewModel: WorkTimeViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var payDetailViewModel: PayDetailViewModel
    private lateinit var payDayViewModel: PayDayViewModel
    private lateinit var curEmployer: Employers
    private lateinit var curDate: WorkDates
    private var curWorkOrder: WorkOrder? = null
    private var workOrderList: List<WorkOrder> = emptyList()
    private var existingHistories: List<WorkOrderHistoryTimeWorkedCombined> = emptyList()
    private lateinit var workTimes: WorkTimes
    private lateinit var timeWorkedByDayData: TimeWorkedByDay
    private var startTime by mutableStateOf(Calendar.getInstance())
    private var endTime by mutableStateOf(Calendar.getInstance())
    private var workOrderNumber by mutableStateOf("")
    private var workOrderSuggestions by mutableStateOf(listOf<String>())
    private var workOrderButtonText by mutableStateOf("")
    private var workOrderInfoText by mutableStateOf("")
    private var hoursSummaryText by mutableStateOf("")
    private var infoText by mutableStateOf("")
    private var totalTimeText by mutableStateOf("")
    private var selectedTimeType by mutableIntStateOf(TimeWorkedTypes.REG_HOURS.value)
    private var existingTimes by mutableStateOf(listOf<WorkOrderHistoryTimeWorkedCombined>())

    private lateinit var timeWorkedByDay: TimeWorkedByDay
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val defaultScope = CoroutineScope(Default)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workTimeViewModel = mainActivity.workTimeViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        employerViewModel = mainActivity.employerViewModel
        payDetailViewModel = mainActivity.payDetailViewModel
        payDayViewModel = mainActivity.payDayViewModel
        startTime = Calendar.getInstance()
        endTime = Calendar.getInstance()
        mainActivity.topMenuBar.title = getString(R.string.enter_work_time)

        return ComposeView(requireContext()).apply {
            setContent {
                WorkDateTimesScreen(
                    infoText = infoText,
                    hoursSummaryText = hoursSummaryText,
                    workOrderNumber = workOrderNumber,
                    onWorkOrderNumberChange = {
                        workOrderNumber = it
                        setCurWorkOrder()
                    },
                    workOrderSuggestions = workOrderSuggestions,
                    workOrderButtonText = workOrderButtonText,
                    onWorkOrderButtonClick = {
                        if (workOrderButtonText == getString(R.string.edit)) {
                            gotoWorkOrderUpdate()
                        } else {
                            gotoWorkOrderAdd()
                        }
                    },
                    workOrderInfoText = workOrderInfoText,
                    startTime = startTime,
                    endTime = endTime,
                    totalTimeText = totalTimeText,
                    selectedTimeType = selectedTimeType,
                    onTimeTypeChange = { selectedTimeType = it },
                    onStartTimeClick = { showStartTimePicker() },
                    onEndTimeClick = { showEndTimePicker() },
                    onEnterTimeClick = { validateWorkOrderNumberAndSaveHistoryIfValid() },
                    onDoneClick = { chooseToSaveOrDiscard() },
                    existingTimes = existingTimes,
                    onTimeClick = { time ->
                        mainViewModel.setWorkOrderHistoryTimeWorkedCombined(time)
                        gotoWorkOrderHistoryTimeUpdateFragment()
                    }
                )
            }
        }
    }

    private fun showStartTimePicker() {
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val tempStartTime =
                    df.roundTimeDownTo15Minutes(hourOfDay, minute)
                val newStart = startTime.clone() as Calendar
                newStart.set(Calendar.HOUR_OF_DAY, tempStartTime.first)
                newStart.set(Calendar.MINUTE, tempStartTime.second)
                newStart.set(Calendar.SECOND, 0)
                startTime = newStart
                updateTimesDisplayed()
            }
        val startTimePicker = TimePickerDialog(
            requireContext(),
            timeSetListener,
            df.get12HourIntOfHour(startTime),
            df.get12HourIntOfMinute(startTime),
            false // true for 24-hour format, false for AM/PM
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
                        newEnd.set(Calendar.SECOND, 0)
                    } else {
                        val tempHoursAndMin = df.roundTimeUpTo15Minutes(hourOfDay, minute)
                        newEnd.set(Calendar.HOUR_OF_DAY, tempHoursAndMin.first)
                        newEnd.set(Calendar.MINUTE, tempHoursAndMin.second)
                        newEnd.set(Calendar.SECOND, 0)
                    }
                    endTime = newEnd
                    mainScope.launch {
                        calculateAdjustmentsForRegAndOt(endTime)
                        delay(WAIT_500)
                        updateTimesDisplayed()
                    }
                }
            }
        val endTimePickerDialog = TimePickerDialog(
            requireContext(),
            timeSetListener,
            df.get12HourIntOfHour(endTime),
            df.get12HourIntOfMinute(endTime),
            false // true for 24-hour format, false for AM/PM
        )
        endTimePickerDialog.setTitle(getString(R.string.select_end_time))
        endTimePickerDialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        // setClickActions() // Not needed for Compose
    }

    override fun populateValues() {
        mainScope.launch {
            val employerAndDate = async {
                populateEmployer()
                populateWorkDate()
            }
            awaitAll(employerAndDate)
            delay(WAIT_100)
            populateBasicInfo()
            populateBasics()
            delay(WAIT_1000)
            populateUi()
        }
    }

    private fun populateEmployer() {
        if (mainViewModel.getEmployer() != null) {
            curEmployer = mainViewModel.getEmployer()!!
        }
    }

    private fun populateWorkDate() {
        if (mainViewModel.getWorkDateObject() != null) {
            curDate = mainViewModel.getWorkDateObject()!!
        }
    }

    private fun populateBasicInfo() {
        var display = "Employer: ${curEmployer.employerName}\n"
        display += "Date: ${df.getDisplayDate(curDate.wdDate)}\n"
        infoText = display
    }

    private fun populateBasics() {
        mainScope.launch {
            val workTimesDeferred = instantiateWorkTimesObject()
            awaitAll(workTimesDeferred)
            delay(WAIT_250)
            val populateBasicInfoDeferred = async {
                populateTimeVariables()
                queryWorkOrderListForAutoComplete()
                timeWorkedByDay = workTimes.getTimeWorkedByDay()
                queryExistingHistories()
            }
            awaitAll(populateBasicInfoDeferred)
            delay(WAIT_500)
            val calculateAdjustmentsForRegAndOtDeferred =
                async { calculateAdjustmentsForRegAndOt(Calendar.getInstance()) }
            awaitAll(calculateAdjustmentsForRegAndOtDeferred)
            val populateWorkOrdersDeferred = async { populateWorkOrdersFromDb() }
            awaitAll(populateWorkOrdersDeferred)
        }
    }

    private fun CoroutineScope.instantiateWorkTimesObject(): Deferred<Unit> {
        val workTimesDeferred = async {
            workTimes = WorkTimes(
                mainActivity,
                curEmployer.employerId,
                curDate.workDateId,
                requireView(),
            )
            workTimes.instantiateVariables()
        }
        return workTimesDeferred
    }

    private fun populateTimeVariables() {
        val tempDate = curDate.wdDate.split("-")
        val newStart = startTime.clone() as Calendar
        val newEnd = endTime.clone() as Calendar
        newStart.set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
        newEnd.set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
        newStart.set(Calendar.HOUR_OF_DAY, 8)
        newStart.set(Calendar.MINUTE, 30)
        newStart.set(Calendar.SECOND, 0)
        startTime = newStart
        if (newEnd < newStart) {
            endTime = newStart.clone() as Calendar
        } else {
            endTime = newEnd
        }
    }

    private fun calculateAdjustmentsForRegAndOt(tempEndTime: Calendar) {
        val timeNow: Calendar = tempEndTime.clone() as Calendar
        val tempDate = curDate.wdDate.split("-")
        timeNow.set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
        val newEnd = endTime.clone() as Calendar
        if (df.getTimeWorked(startTime, newEnd) < 0.0) {
            val correctedEnd = startTime.clone() as Calendar
            if (timeNow > correctedEnd) {
                endTime = df.roundCalendarTimeUpTo15Minutes(timeNow)
            } else {
                endTime = correctedEnd
            }
        }
        if (timeWorkedByDay.hrsRegByTimeEntered + df.getTimeWorked(
                startTime,
                endTime
            ) > 8.0 && selectedTimeType == TimeWorkedTypes.REG_HOURS.value
        ) {
            val timeToAdjust = 8 - timeWorkedByDay.hrsRegByTimeEntered
            endTime = df.addHoursToCalendar(startTime, timeToAdjust)
            displayMessage(getString(R.string.time_has_been_adjusted_to_8_hours))
        }
        if (timeWorkedByDay.hrsOtByTimeEntered + timeWorkedByDay.hrsRegByTimeEntered + df.getTimeWorked(
                startTime,
                endTime
            ) > 12.0 && selectedTimeType == TimeWorkedTypes.OT_HOURS.value
        ) {
            val timeToAdjust =
                12 - (timeWorkedByDay.hrsOtByTimeEntered + timeWorkedByDay.hrsRegByTimeEntered)
            endTime = df.addHoursToCalendar(startTime, timeToAdjust)
            displayMessage(getString(R.string.time_has_been_adjusted_to_12_hours))
        }
    }

    private fun queryWorkOrderListForAutoComplete() {
        workOrderList = workTimes.getWorkOrderList()
        workOrderSuggestions = workTimes.getWorkOrderNumbers()
    }

    private fun updateWorkDateHoursInDb(
        hrsReg: Double,
        hrsOt: Double,
        hrsDblOt: Double,
        hrsStat: Double
    ) {
        payDayViewModel.updateWorkDate(
            WorkDates(
                curDate.workDateId,
                curDate.wdPayPeriodId,
                curDate.wdEmployerId,
                curDate.wdCutoffDate,
                curDate.wdDate,
                hrsReg,
                hrsOt,
                hrsDblOt,
                hrsStat,
                curDate.wdNote,
                false,
                df.getCurrentTimeAsString()
            )
        )
    }

    override fun populateUi() {
        mainScope.launch {
            workTimes.instantiateVariables()
            delay(WAIT_500)
            queryExistingHistories()
            delay(WAIT_250)
            setCorrectedTimes()
            adjustWorkTimeTypes()
            populateTimesRecycler()
            updateTimesDisplayed()
            delay(WAIT_250)
            populateHoursInDisplay()
        }
    }

    override fun updateUi() {
        mainScope.launch {
            workTimes.instantiateVariables()
            delay(WAIT_500)
            queryExistingHistories()
            delay(WAIT_250)
            setCorrectedTimes()
            adjustWorkTimeTypes()
            populateTimesRecycler()
            updateTimesDisplayed()
            delay(WAIT_250)
            populateHoursInDisplay()
        }
    }

    private fun queryExistingHistories() {
        existingHistories = workTimes.getWorkOrderHistoryTimeWorkedList()
    }

    private fun setCorrectedTimes() {
        if (existingHistories.isNotEmpty()) {
            val tempStartTime =
                df.splitTimeFromDateTime(existingHistories.last().timeWorked.wohtEndTime)
            val newStart = startTime.clone() as Calendar
            newStart.set(Calendar.HOUR_OF_DAY, tempStartTime[0].toInt())
            newStart.set(Calendar.MINUTE, tempStartTime[1].toInt())
            newStart.set(Calendar.SECOND, 0)
            startTime = newStart
        }
    }

    private fun adjustWorkTimeTypes() {
        if (selectedTimeType == TimeWorkedTypes.BREAK.value && timeWorkedByDay.hrsRegByTimeEntered < 8.0) {
            selectedTimeType = TimeWorkedTypes.REG_HOURS.value
        }
        if (timeWorkedByDay.hrsRegByTimeEntered >= 8.0) {
            selectedTimeType = TimeWorkedTypes.OT_HOURS.value
        }
        if (timeWorkedByDay.hrsOtByTimeEntered + timeWorkedByDay.hrsRegByTimeEntered >= 12.0) {
            selectedTimeType = TimeWorkedTypes.DBL_OT_HOURS.value
        }
    }

    private fun populateTimesRecycler() {
        existingTimes = existingHistories
    }

    private fun updateTimesDisplayed() {
        totalTimeText = nf.getNumberFromDouble(
            df.getTimeWorked(
                startTime,
                endTime
            )
        ) + " " + getString(R.string.hours)
    }

    private fun populateWorkOrdersFromDb() {
        if (mainViewModel.getWorkOrder() != null) {
            curWorkOrder = mainViewModel.getWorkOrder()
            workOrderNumber = curWorkOrder!!.woNumber
            setCurWorkOrder()
        }
    }

    private fun populateHoursInDisplay() {
        timeWorkedByDayData = workTimes.getTimeWorkedByDay()
        val hrsReg = timeWorkedByDayData.hrsReg
        val hrsOt = timeWorkedByDayData.hrsOt
        val hrsDblOt = timeWorkedByDayData.hrsDblOt
        val hrsStat = timeWorkedByDayData.hrsStat
        var display = ""
        display += (if (hrsReg > 0.0) "${nf.getNumberFromDouble(hrsReg)} ${getString(R.string.reg_hours)} " else "")
        display += (if (hrsOt > 0.0 && display != "") " ${getString(R.string.pipe)} " else "")
        display += (if (hrsOt > 0.0) "${nf.getNumberFromDouble(hrsOt)} ${getString(R.string.ot_hrs)} " else "")
        display += (if (hrsDblOt > 0.0 && display != "") " ${getString(R.string.pipe)} " else "")
        display += (if (hrsDblOt > 0.0) "${nf.getNumberFromDouble(hrsDblOt)} ${getString(R.string.dbl_ot_hrs)} " else "")
        display += (if (hrsStat > 0.0 && display != "") " ${getString(R.string.pipe)} " else "")
        display += (if (hrsStat > 0.0) "${nf.getNumberFromDouble(hrsStat)} ${getString(R.string.other_hours)} " else "")
        if (display != "") display = getString(R.string.time_entered) + " $display"
        hoursSummaryText = display
    }

    override fun setClickActions() {
        // Not used anymore
    }

    private fun setCurWorkOrder() {
        if (workOrderNumber.isBlank()) {
            displayMessage(
                getString(R.string.error_) + getString(R.string.please_enter_a_valid_work_order_before_adding_work_performed)
            )
        }
        if (doesWorkOrderExist()) {
            populateWorkOrderInfo()
            workOrderButtonText = getString(R.string.edit)
        } else {
            workOrderButtonText = getString(R.string.create)
            workOrderInfoText = ""
        }
    }

    private fun validateWorkOrderNumberAndSaveHistoryIfValid() {
        if (doesWorkOrderExist()) {
            insertTime()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.create_work_order_) + "$workOrderNumber?")
                .setMessage(getString(R.string.this_work_order_does_not_exist))
                .setPositiveButton(getString(R.string.yes)) { _, _ -> gotoWorkOrderAdd() }
                .setNegativeButton(getString(R.string.no), null).show()
        }
    }

    private fun doesWorkOrderExist(): Boolean {
        for (workOrder in workOrderList) {
            if (workOrderNumber == workOrder.woNumber) {
                curWorkOrder = workOrder
                return true
            }
        }
        return false
    }

    private fun insertTime(): Boolean {
        val answer = validateTimeWorked()
        if (answer == ANSWER_OK) {
            val workTimeType = selectedTimeType
            try {
                mainScope.launch {
                    val workOrderHistoryDeferred =
                        async { getOrCreateWorkOrderHistory() }
                    startTime = df.roundCalendarTimeTo15Minutes(startTime)
                    endTime = df.roundCalendarTimeTo15Minutes(endTime)
                    workOrderViewModel.insertTimeWorked(
                        WorkOrderHistoryTimeWorked(
                            nf.generateRandomIdAsLong(),
                            workOrderHistoryDeferred.await().woHistoryId,
                            curDate.workDateId,
                            df.getDateFromCalendarAsString(startTime),
                            df.getDateFromCalendarAsString(endTime),
                            workTimeType,
                            false,
                            df.getCurrentTimeAsString()
                        )
                    )
                    delay(WAIT_250)
                    workTimes.instantiateVariables()
                    delay(WAIT_500)
                    updateWorkOrderHistoryInDb(workOrderHistoryDeferred.await())
                    updateHoursInWorkDate()
                    delay(WAIT_500)
                    updateUi()
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

    private suspend fun getOrCreateWorkOrder(): WorkOrder {
        val wo: WorkOrder? = workOrderViewModel.findWorkOrder("break", curEmployer.employerId)
        if (wo != null) {
            curWorkOrder = wo
        } else {
            curWorkOrder = WorkOrder(
                nf.generateRandomIdAsLong(),
                "break",
                curEmployer.employerId,
                "van",
                "break",
                false,
                df.getCurrentTimeAsString()
            )
            workOrderViewModel.insertWorkOrder(curWorkOrder!!)
        }
        return curWorkOrder!!
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
                        curDate.workDateId,
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

    private fun updateHoursInWorkDate() {
        mainScope.launch {
            delay(WAIT_500)
            val timeWorkedByDay = workTimes.getTimeWorkedByDay()
            delay(WAIT_250)
            timeWorkedByDay.apply {
                updateWorkDateHoursInDb(
                    hrsRegByTimeEntered,
                    hrsOtByTimeEntered,
                    hrsDblOtByTimeEntered,
                    hrsStat
                )
            }
        }
    }

    private fun chooseToSaveOrDiscard() {
        if (df.getTimeWorked(startTime, endTime) > 0.0) {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.confirm_leave))
                .setMessage(getString(R.string.would_you_like_to_save_time_entered))
                .setPositiveButton(getString(R.string.enter_time)) { _, _ ->
                    mainScope.launch {
                        insertTime()
                        delay(WAIT_250)
                        gotoCallingFragment()
                    }
                }
                .setNeutralButton(getString(R.string.go_back), null)
                .setNegativeButton(getString(R.string.no)) { _, _ ->
                    gotoCallingFragment()
                }
                .show()
        } else {
            gotoCallingFragment()
        }
    }


    private suspend fun getOrCreateWorkOrderHistory(): WorkOrderHistory {
        var tempWorkOrderHistory: WorkOrderHistory? =
            when (curWorkOrder) {
                null -> {
                    if (selectedTimeType == TimeWorkedTypes.BREAK.value) {
                        curWorkOrder = getOrCreateWorkOrder()
                        workOrderViewModel.getWorkOrderHistory(
                            curWorkOrder!!.workOrderId,
                            curDate.workDateId
                        )
                    } else {
                        null
                    }
                }

                else -> {
                    workOrderViewModel.getWorkOrderHistory(
                        curWorkOrder!!.workOrderId,
                        curDate.workDateId
                    )
                }
            }

        if (tempWorkOrderHistory == null) {
            tempWorkOrderHistory = insertNewWorkOrderHistory()
        }

        return tempWorkOrderHistory!!
    }


    private fun insertNewWorkOrderHistory(): WorkOrderHistory {
        val tempWorkOrderHistory = WorkOrderHistory(
            nf.generateRandomIdAsLong(),
            curWorkOrder!!.workOrderId,
            curDate.workDateId,
            0.0,
            0.0,
            0.0,
            getString(R.string.added_automatically),
            false,
            df.getCurrentTimeAsString()
        )
        workOrderViewModel.insertWorkOrderHistory(tempWorkOrderHistory)
        return tempWorkOrderHistory
    }

    private fun validateTimeWorked(): String {
        if (workOrderNumber.isBlank() && selectedTimeType != TimeWorkedTypes.BREAK.value) {
            return getString(R.string.please_enter_a_work_order_number)
        }
        if (selectedTimeType == TimeWorkedTypes.REG_HOURS.value) {
            if (timeWorkedByDay.hrsRegByTimeEntered + df.getTimeWorked(
                    startTime,
                    endTime
                ) > 8.0
            ) {
                return getString(R.string.this_will_exceed_8_hours)
            }
        }
        if (selectedTimeType == TimeWorkedTypes.OT_HOURS.value) {
            if (timeWorkedByDay.hrsRegByTimeEntered + timeWorkedByDay.hrsOtByTimeEntered + df.getTimeWorked(
                    startTime,
                    endTime
                ) > 12.0
            ) {
                return getString(R.string.this_will_exceed_12_hours)
            }
        }
        if (selectedTimeType == TimeWorkedTypes.DBL_OT_HOURS.value) {
            if (timeWorkedByDay.hrsRegByTimeEntered + timeWorkedByDay.hrsOtByTimeEntered + timeWorkedByDay.hrsDblOtByTimeEntered + df.getTimeWorked(
                    startTime,
                    endTime
                ) > 18.0
            ) {
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
        return ANSWER_OK
    }

    private fun displayMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun populateWorkOrderInfo() {
        val display = curWorkOrder!!.woAddress + " | " + curWorkOrder!!.woDescription
        workOrderInfoText = display
    }

    override fun gotoCallingFragment() {
        if (mainViewModel.getCallingFragment() != null) {
            val frag = mainViewModel.getCallingFragment()!!
            if (frag.contains(FRAG_WORK_DATE_UPDATE)) {
                gotoWorkDateUpdateFragment()
            }
        }
    }

    private fun gotoWorkOrderAdd() {
        mainViewModel.setWorkOrderNumber(workOrderNumber)
        mainViewModel.addCallingFragment(TAG)
        gotoWorkOrderAddFragment()
    }

    private fun gotoWorkOrderUpdate() {
        mainViewModel.setWorkOrderNumber(workOrderNumber)
        mainViewModel.setWorkOrder(curWorkOrder)
        mainViewModel.addCallingFragment(TAG)
        gotoWorkOrderUpdateFragment()
    }

    private fun gotoWorkDateUpdateFragment() {
        requireView().findNavController().navigate(
            WorkDateTimesFragmentDirections.actionWorkDateTimesToWorkDateUpdateFragment()
        )
    }

    private fun gotoWorkOrderAddFragment() {
        requireView().findNavController().navigate(
            WorkDateTimesFragmentDirections.actionWorkDateTimesToWorkOrderAddFragment()
        )
    }

    private fun gotoWorkOrderUpdateFragment() {
        requireView().findNavController().navigate(
            WorkDateTimesFragmentDirections.actionWorkDateTimesToWorkOrderUpdateFragment()
        )
    }

    private fun gotoWorkOrderLookup() {
        mainViewModel.setWorkOrder(curWorkOrder)
        mainViewModel.addCallingFragment(TAG)
        gotoWorkOrderLookupFragment()
    }

    private fun gotoWorkOrderLookupFragment() {
        requireView().findNavController().navigate(
            WorkDateTimesFragmentDirections.actionWorkDateTimesToWorkOrderLookupFragment()
        )
    }

    override fun gotoWorkOrderHistoryTimeUpdateFragment() {
        mainViewModel.addCallingFragment(TAG)
        requireView().findNavController().navigate(
            WorkDateTimesFragmentDirections.actionWorkDateTimesFragmentToWorkOrderHistoryTimeUpdateFragment()
        )
    }

    override fun onDestroy() {
        mainScope.cancel()
        defaultScope.cancel()
        super.onDestroy()
    }
}