package ms.mattschlenkrich.paycalculator.ui.workdate

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_TIME
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_UPDATE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.database.viewModel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkDateTimeBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workdate.adapter.WorkDateTimesAdapter
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
    private lateinit var payDetailViewModel: PayDetailViewModel

    private lateinit var payDayViewModel: PayDayViewModel
    private lateinit var curEmployer: Employers
    private lateinit var curDate: WorkDates
    private var curWorkOrder: WorkOrder? = null
    private lateinit var workOrderList: List<WorkOrder>
    private lateinit var existingHistories: List<WorkOrderHistoryTimeWorkedCombined>
    private val timeWorkedByDayAsCalendarPairs = ArrayList<Pair<Calendar, Calendar>>()
    private lateinit var startTime: Calendar
    private lateinit var endTime: Calendar
    private var totalRegHoursForDay = 0.0
    private var totalOtHoursForDay = 0.0
    private var totalDblOtHoursForDay = 0.0
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val defaultScope = CoroutineScope(Default)
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
        payDetailViewModel = mainActivity.payDetailViewModel
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

    private fun populateValues() {
        mainScope.launch {
            populateEmployer()
            populateWorkDate()
            populateBasicInfo()
            populateTimeVariables()
            populateWorkOrderListForAutoComplete()
            populateExistingHistories()
            delay(WAIT_250)
            setCorrectedTimes()
            calculateHoursAndDisplay()
            calculateAdjustmentsForRegAndOt(Calendar.getInstance())
            populateWorkOrderFromDb()
            populateTimesRecycler()
        }
    }

    private fun populateEmployer() {
        Log.d(TAG, "populateWorkDateAndEmployer: Started")
        if (mainViewModel.getEmployer() != null) {
            curEmployer = mainViewModel.getEmployer()!!
        }
    }

    private fun populateExistingHistories() {
        Log.d(TAG, "populateExistingHistories: started")
        workOrderViewModel.getTimeWorkedPerDay(curDate.workDateId)
            .observe(viewLifecycleOwner) { histories ->
                existingHistories = histories
            }
    }

    private fun populateWorkDate() {
        if (mainViewModel.getWorkDateObject() != null) {
            curDate = mainViewModel.getWorkDateObject()!!
        }
    }

    private fun populateTimeVariables() {
        Log.d(TAG, "populateTimeVariables: started")
        val tempDate = curDate.wdDate.split("-")
        startTime.set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
        endTime.set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
        startTime.set(Calendar.HOUR_OF_DAY, 8)
        startTime.set(Calendar.MINUTE, 30)
        startTime.set(Calendar.SECOND, 0)
        if (endTime < startTime) {
            endTime = startTime.clone() as Calendar
        }
    }

    private fun populateBasicInfo() {
        Log.d(TAG, "populateBasicInfo: started")
        binding.apply {
            var display = "Employer: ${curEmployer.employerName}\n"
            display += "Date: ${df.getDisplayDate(curDate.wdDate)}\n"
            tvInfo.text = display
        }
        Log.d(TAG, "populateBasicInfo: ended")
    }

    private fun calculateHoursAndDisplay() {
        Log.d(TAG, "calculateHoursAndDisplay: started")
        mainScope.launch {
            var hrsReg = 0.0
            var hrsOt = 0.0
            var hrsDblOt = 0.0
            var hrsStat = 0.0

            defaultScope.launch {
                hrsReg = payDetailViewModel.getHoursReg(curDate.workDateId)
                hrsOt = payDetailViewModel.getHoursOt(curDate.workDateId)
                hrsDblOt = payDetailViewModel.getHoursDblOt(curDate.workDateId)
                hrsStat = payDetailViewModel.getHoursStat(curDate.workDateId)
            }
            delay(WAIT_250)
            workOrderViewModel.getTimeWorkedPerDay(curDate.workDateId)
                .observe(viewLifecycleOwner) { histories ->
                    existingHistories = histories
                    var hrsRegByTimeEntered = 0.0
                    var hrsOtByTimeEntered = 0.0
                    var hrsDblOtByTimeEntered = 0.0
                    for (history in histories) {
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
                    }
                    totalRegHoursForDay = hrsRegByTimeEntered
                    totalOtHoursForDay = hrsOtByTimeEntered
                    totalDblOtHoursForDay = hrsDblOtByTimeEntered
                    if (hrsReg < hrsRegByTimeEntered || hrsOt < hrsOtByTimeEntered || hrsDblOt < hrsDblOtByTimeEntered) {
                        hrsReg = hrsRegByTimeEntered
                        hrsOt = hrsOtByTimeEntered
                        hrsDblOt = hrsDblOtByTimeEntered
                        updateWorkDateHours(
                            hrsReg,
                            hrsOt,
                            hrsDblOt,
                            hrsStat
                        )
                    }

                    populateHoursInDisplay(
                        hrsReg,
                        hrsOt,
                        hrsDblOt,
                        hrsStat,
                        hrsRegByTimeEntered,
                        hrsOtByTimeEntered,
                        hrsDblOtByTimeEntered
                    )

                }
        }
    }

    private fun populateTimesRecycler() {
        val workDateTimesAdapter = WorkDateTimesAdapter(mainActivity, mView)
        binding.rvTimeWorked.apply {
            layoutManager = LinearLayoutManager(mView.context)
            adapter = workDateTimesAdapter
        }
        workDateTimesAdapter.differ.submitList(existingHistories)
    }

    private fun setCorrectedTimes() {
        populateTimWorkedCalendarPairs()
        if (existingHistories.isNotEmpty()) {
            val tempStartTime =
                df.splitTimeFromDateTime(existingHistories.last().timeWorked.wohtEndTime)
            Log.d(TAG, "setCorrectedTimes: started ${tempStartTime[0]}:${tempStartTime[1]}")
            startTime.set(Calendar.HOUR_OF_DAY, tempStartTime[0].toInt())
            startTime.set(Calendar.MINUTE, tempStartTime[1].toInt())
            startTime.set(Calendar.SECOND, 0)
        }
    }

    private fun populateTimWorkedCalendarPairs() {
        timeWorkedByDayAsCalendarPairs.clear()
        for (time in existingHistories) {
            val start = df.getCalendarFromString(time.timeWorked.wohtStartTime)
            val end = df.getCalendarFromString(time.timeWorked.wohtEndTime)
            timeWorkedByDayAsCalendarPairs.add(Pair(start, end))
        }
    }

    private fun calculateAdjustmentsForRegAndOt(time: Calendar) {
        binding.apply {
            val timeNow = time.clone() as Calendar
            val tempDate = curDate.wdDate.split("-")
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
            if (totalOtHoursForDay + df.getTimeWorked(
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

    private fun updateTimesDisplayed() {
        binding.apply {
            clkStartTime.text = df.get12HourDisplay(startTime)
            // automatically set the end time to not exceed the next block of time
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

    private fun populateWorkOrderFromDb() {
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

    private fun updateWorkDateHours(
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

    private fun populateHoursInDisplay(
        hrsReg: Double,
        hrsOt: Double,
        hrsDblOt: Double,
        hrsStat: Double,
        hrsRegByTimeEntered: Double,
        hrsOtByTimeEntered: Double,
        hrsDblOtByTimeEntered: Double
    ) {
        var display = ""
        display += (if (hrsReg > 0.0) "${nf.getNumberFromDouble(hrsReg)} ${getString(R.string.reg_hours)} " else "")
        display += (if (hrsOt > 0.0 && display != "") " ${getString(R.string.pipe)} " else "")
        display += (if (hrsOt > 0.0) "${nf.getNumberFromDouble(hrsOt)} ${getString(R.string.ot_hrs)} " else "")
        display += (if (hrsDblOt > 0.0 && display != "") " ${getString(R.string.pipe)} " else "")
        display += (if (hrsDblOt > 0.0) "${nf.getNumberFromDouble(hrsDblOt)} ${getString(R.string.dbl_ot_hrs)} " else "")
        display += (if (hrsStat > 0.0 && display != "") " ${getString(R.string.pipe)} " else "")
        display += (if (hrsStat > 0.0) "${nf.getNumberFromDouble(hrsStat)} ${getString(R.string.stat_hours)} " else "")
        if (display != "") display = getString(R.string.time_entered_for_date) + " $display"
        var display2 = ""
        display2 += (if (hrsRegByTimeEntered > 0.0) "${nf.getNumberFromDouble(hrsRegByTimeEntered)} ${
            getString(
                R.string.reg_hours
            )
        } " else "")
        display2 += (if (hrsOtByTimeEntered > 0.0 && display2 != "") " ${getString(R.string.pipe)} " else "")
        display2 += (if (hrsOtByTimeEntered > 0.0) "${nf.getNumberFromDouble(hrsOtByTimeEntered)} ${
            getString(
                R.string.ot_hrs
            )
        } " else "")
        display2 += (if (hrsDblOtByTimeEntered > 0.0 && display2 != "") " ${getString(R.string.pipe)} " else "")
        display2 += (if (hrsDblOtByTimeEntered > 0.0) "${
            nf.getNumberFromDouble(
                hrsDblOtByTimeEntered
            )
        } ${getString(R.string.dbl_ot_hrs)}" else "")
        if (display2 != "")
            display2 = getString(R.string.time_entered_from_work_orders) + " $display2"
        display = (if (display != "") display + "\n" + display2 else display2)
        binding.tvHours.text = display

    }

    private fun setClickActions() {
        binding.apply {
            acWorkOrder.setOnItemClickListener { _, _, _, _ -> setCurWorkOrder() }
            acWorkOrder.setOnLongClickListener {
                gotoWorkOrderLookup()
                true
            }
            acWorkOrder.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
//                    null
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    setCurWorkOrder()
                }

                override fun afterTextChanged(s: Editable?) {
                    setCurWorkOrder()
                }

            })
            btnWorkOrder.setOnClickListener {
                if (btnWorkOrder.text.toString() == getString(R.string.edit)) {
                    gotoWorkOrderUpdate()
                } else {
                    gotoWorkOrderAdd()
                }
            }
            btnEnterTime.setOnClickListener {
                insertTime()
            }
            fabDone.setOnClickListener {
                gotoCallingFragment()
            }
        }
        setStartTimeActions()
        setEndTimeActions()
    }

    private fun validateWorkOrderNumberAndSaveHistoryIfValid() {
        if (doesWorkOrderExist()) {

            displayMessage(getString(R.string.work_order_has_been_added_to_date))
        } else {
            AlertDialog.Builder(mView.context)
                .setTitle(getString(R.string.create_work_order_) + "${binding.acWorkOrder.text}?")
                .setMessage(getString(R.string.this_work_order_does_not_exist))
                .setPositiveButton(getString(R.string.yes)) { _, _ -> gotoWorkOrderAdd() }
                .setNegativeButton(getString(R.string.no), null).show()
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
                    updateTimesDisplayed()
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

    private fun setEndTimeActions() {
        binding.apply {
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
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
                        calculateAdjustmentsForRegAndOt(endTime)
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
                        val workOrderHistoryDeferred =
                            async { getOrCreateWorkOrderHistory() }
                        startTime = df.roundCalendarTimeTo15Minutes(startTime)
//                        Log.d(TAG, "insertTime: End time is ${df.get12HourDisplay(endTime)}")
                        endTime = df.roundCalendarTimeTo15Minutes(endTime)
                        workOrderViewModel.insertTimeWorked(
                            WorkOrderHistoryTimeWorked(
                                nf.generateRandomIdAsLong(),
                                workOrderHistoryDeferred.await()?.woHistoryId ?: 0,
                                curDate.workDateId,
                                df.getDateFromCalendarAsString(startTime),
                                df.getDateFromCalendarAsString(endTime),
                                workTimeType,
                                false,
                                df.getCurrentTimeAsString()
                            )
                        )
                        delay(WAIT_250)
                        updateWorkOrderHistoryInDb(workOrderHistoryDeferred.await())
                        repopulateUi()
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

    private suspend fun repopulateUi() {
//        populateExistingHistories()
        setCorrectedTimes()
        populateTimesRecycler()
        populateWorkOrderInfo()
        calculateHoursAndDisplay()
        updateTimesDisplayed()
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

    private suspend fun getOrCreateWorkOrderHistory(): WorkOrderHistory {
        var tempWorkOrderHistoryDeferred: Deferred<WorkOrderHistory?>
        var tempWorkOrderHistory: WorkOrderHistory? = null
        defaultScope.launch {
            tempWorkOrderHistoryDeferred =
                async {
                    when (curWorkOrder) {
                        null if binding.radBreak.isChecked -> {
                            curWorkOrder = getOrCreateWorkOrder()
                            Log.d(
                                TAG,
                                "getOrCreateWorkOrderHistory: curWorkOrder = ${curWorkOrder?.woNumber}"
                            )
                            workOrderViewModel.getWorkOrderHistory(
                                curWorkOrder!!.workOrderId,
                                curDate.workDateId
                            )
                        }

                        null -> {
                            Log.d(TAG, "getOrCreateWorkOrderHistory: null")
                            return@async null
                        }

                        else -> {
                            Log.d(TAG, "getOrCreateWorkOrderHistory: else")

                            workOrderViewModel.getWorkOrderHistory(
                                curWorkOrder!!.workOrderId,
                                curDate.workDateId
                            )
                        }
                    }
                }

            tempWorkOrderHistory = if (tempWorkOrderHistoryDeferred.await() == null) {
                Log.d(TAG, "getOrCreateWorkOrderHistory: null")
                insertNewWorkOrderHistory()
            } else {
                Log.d(TAG, "getOrCreateWorkOrderHistory: else")
                tempWorkOrderHistoryDeferred.await()
            }
        }

        delay(WAIT_500)
        return tempWorkOrderHistory!!
    }

    private suspend fun getOrCreateWorkOrder(): WorkOrder {
        var wo: WorkOrder? = null
        defaultScope.launch {
            wo = workOrderViewModel.findWorkOrder("break", curEmployer.employerId)
        }
        delay(WAIT_100)
        try {
            Log.d(TAG, "getOrCreateWorkOrder: Work order was found $wo")
            curWorkOrder = wo!!
        } catch (e: Exception) {
            Log.d(TAG, "getOrCreateWorkOrder: error was $e")
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
            Log.d(
                TAG,
                "getOrCreateWorkOrder: ${curWorkOrder?.woNumber} & ${curWorkOrder?.workOrderId}"
            )
        }
        delay(WAIT_250)
        return curWorkOrder!!
    }


    private fun insertNewWorkOrderHistory(): WorkOrderHistory {
        Log.d(TAG, "insertNewWorkOrderHistory: ")
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
        binding.apply {
            if (acWorkOrder.text.isNullOrBlank() && !radBreak.isChecked) {
                return getString(R.string.please_enter_a_work_order_number)
            }
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

    private fun gotoCallingFragment() {
        if (mainViewModel.getCallingFragment() != null) {
            val frag = mainViewModel.getCallingFragment()!!
            if (frag.contains(FRAG_WORK_DATE_UPDATE)) {
                gotoWorkDateUpdateFragment()
            } else {
                //do nothing
            }
        }
    }


    private fun gotoWorkOrderAdd() {
        mainViewModel.setWorkOrderNumber(binding.acWorkOrder.text.toString())
        mainViewModel.addCallingFragment(TAG)
        gotoWorkOrderAddFragment()
    }

    private fun gotoWorkOrderUpdate() {
        mainViewModel.setWorkOrderNumber(binding.acWorkOrder.text.toString())
        mainViewModel.setWorkOrder(curWorkOrder)
        mainViewModel.addCallingFragment(TAG)
        gotoWorkOrderUpdateFragment()
    }

    private fun gotoWorkDateUpdateFragment() {
        mView.findNavController().navigate(
            WorkDateTimesDirections.actionWorkDateTimesToWorkDateUpdateFragment()
        )
    }

    private fun gotoWorkOrderAddFragment() {
        mView.findNavController().navigate(
            WorkDateTimesDirections.actionWorkDateTimesToWorkOrderAddFragment()
        )
    }

    private fun gotoWorkOrderUpdateFragment() {
        mView.findNavController().navigate(
            WorkDateTimesDirections.actionWorkDateTimesToWorkOrderUpdateFragment()
        )
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