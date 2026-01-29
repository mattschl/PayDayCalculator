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
import kotlinx.coroutines.awaitAll
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
import ms.mattschlenkrich.paycalculator.common.WAIT_1000
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.TimeWorkedByDay
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.database.viewModel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkTimeViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkDateTimeBinding
import ms.mattschlenkrich.paycalculator.logic.worktime.IWorkTimesFragment
import ms.mattschlenkrich.paycalculator.logic.worktime.WorkTimes
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.workdate.adapter.WorkDateTimesAdapter
import java.util.Calendar

private const val TAG = FRAG_WORK_DATE_TIME

class WorkDateTimesFragment : Fragment(R.layout.fragment_work_date_time), IWorkTimesFragment {

    private var _binding: FragmentWorkDateTimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
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
    private lateinit var workOrderList: List<WorkOrder>
    private lateinit var existingHistories: List<WorkOrderHistoryTimeWorkedCombined>
    private lateinit var workTimes: WorkTimes
    private lateinit var timeWorkedByDayData: TimeWorkedByDay
    private lateinit var startTime: Calendar
    private lateinit var endTime: Calendar
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
        _binding = FragmentWorkDateTimeBinding.inflate(inflater, container, false)
        mView = binding.root
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
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
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
        binding.apply {
            var display = "Employer: ${curEmployer.employerName}\n"
            display += "Date: ${df.getDisplayDate(curDate.wdDate)}\n"
            tvInfo.text = display
        }
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
                mView,
            )
            workTimes.instantiateVariables()
        }
        return workTimesDeferred
    }

    private fun populateTimeVariables() {
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

    private fun calculateAdjustmentsForRegAndOt(tempEndTime: Calendar) {
        binding.apply {
            val timeNow: Calendar = tempEndTime.clone() as Calendar
            val tempDate = curDate.wdDate.split("-")
            timeNow.set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
            if (df.getTimeWorked(startTime, endTime) < 0.0) {
                endTime = startTime.clone() as Calendar
                if (timeNow > endTime) {
                    endTime = df.roundCalendarTimeUpTo15Minutes(timeNow)
                }
            }
            if (timeWorkedByDay.hrsRegByTimeEntered + df.getTimeWorked(
                    startTime,
                    endTime
                ) > 8.0 && radRegHours.isChecked
            ) {
                val timeToAdjust = 8 - timeWorkedByDay.hrsRegByTimeEntered
                endTime = df.addHoursToCalendar(startTime, timeToAdjust)
                displayMessage(getString(R.string.time_has_been_adjusted_to_8_hours))
            }
            if (timeWorkedByDay.hrsOtByTimeEntered + timeWorkedByDay.hrsRegByTimeEntered + df.getTimeWorked(
                    startTime,
                    endTime
                ) > 12.0 && radOtHours.isChecked
            ) {
                val timeToAdjust =
                    12 - (timeWorkedByDay.hrsOtByTimeEntered + timeWorkedByDay.hrsRegByTimeEntered)
                endTime = df.addHoursToCalendar(startTime, timeToAdjust)
                displayMessage(getString(R.string.time_has_been_adjusted_to_12_hours))
            }
        }
    }

    private fun queryWorkOrderListForAutoComplete() {
        workOrderList = workTimes.getWorkOrderList()
        val woAdapter = ArrayAdapter(
            mView.context, R.layout.spinner_item_normal, workTimes.getWorkOrderNumbers()
        )
        binding.acWorkOrder.setAdapter(woAdapter)
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

    private fun queryExistingHistories() {
        existingHistories = workTimes.getWorkOrderHistoryTimeWorkedList()
    }

    private fun setCorrectedTimes() {
        if (existingHistories.isNotEmpty()) {
            val tempStartTime =
                df.splitTimeFromDateTime(existingHistories.last().timeWorked.wohtEndTime)
            startTime.set(Calendar.HOUR_OF_DAY, tempStartTime[0].toInt())
            startTime.set(Calendar.MINUTE, tempStartTime[1].toInt())
            startTime.set(Calendar.SECOND, 0)
        }
    }

    private fun adjustWorkTimeTypes() {
        binding.apply {
            if (radBreak.isChecked && timeWorkedByDay.hrsRegByTimeEntered < 8.0) {
                radRegHours.isChecked = true
                radBreak.isChecked = false
                radOtHours.isChecked = false
                radDblOtHours.isChecked = false
            }
            if (timeWorkedByDay.hrsRegByTimeEntered >= 8.0) {
                radOtHours.isChecked = true
                radRegHours.isChecked = false
                radDblOtHours.isChecked = false
                radBreak.isChecked = false
            }
            if (timeWorkedByDay.hrsOtByTimeEntered + timeWorkedByDay.hrsRegByTimeEntered >= 12.0) {
                radDblOtHours.isChecked = true
                radOtHours.isChecked = false
                radRegHours.isChecked = false
                radBreak.isChecked = false
            }
        }
    }

    private fun populateTimesRecycler() {
        val workDateTimesAdapter = WorkDateTimesAdapter(
            mainActivity, mView, this@WorkDateTimesFragment
        )
        binding.rvTimeWorked.apply {
            layoutManager = LinearLayoutManager(mView.context)
            adapter = workDateTimesAdapter
        }
        workDateTimesAdapter.differ.submitList(existingHistories)
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

    private fun populateWorkOrdersFromDb() {
        if (mainViewModel.getWorkOrder() != null) {
            curWorkOrder = mainViewModel.getWorkOrder()
            binding.acWorkOrder.setText(curWorkOrder!!.woNumber)
            setCurWorkOrder()
        }
    }

    private fun populateHoursInDisplay() {
        timeWorkedByDayData = workTimes.getTimeWorkedByDay()
//        Log.d(TAG, "timeWorkedByDayData: $timeWorkedByDayData")
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
        display += (if (hrsStat > 0.0) "${nf.getNumberFromDouble(hrsStat)} ${getString(R.string.stat_hours)} " else "")
        if (display != "") display = getString(R.string.time_entered) + " $display"
        binding.tvHours.text = display
    }

    override fun setClickActions() {
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
                validateWorkOrderNumberAndSaveHistoryIfValid()
            }
            fabDone.setOnClickListener {
                chooseToSaveOrDiscard()
            }
        }
        setStartTimeActions()
        setEndTimeActions()
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

    private fun validateWorkOrderNumberAndSaveHistoryIfValid() {
        if (doesWorkOrderExist()) {
//            displayMessage(getString(R.string.work_order_has_been_added_to_date))
            insertTime()
        } else {
            AlertDialog.Builder(mView.context)
                .setTitle(getString(R.string.create_work_order_) + "${binding.acWorkOrder.text}?")
                .setMessage(getString(R.string.this_work_order_does_not_exist))
                .setPositiveButton(getString(R.string.yes)) { _, _ -> gotoWorkOrderAdd() }
                .setNegativeButton(getString(R.string.no), null).show()
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

    private fun insertTime(): Boolean {
        val answer = validateTimeWorked()
        if (answer == ANSWER_OK) {
            binding.apply {
                btnEnterTime.isEnabled = false
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
                        endTime = df.roundCalendarTimeTo15Minutes(endTime)
//                        Log.d(TAG, "insertTime: $startTime")
//                        Log.d(TAG, "insertTime: $endTime")
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
                        populateUi()
                    }
                    btnEnterTime.isEnabled = true
                    return true
                } catch (e: SQLiteConstraintException) {
                    displayMessage(getString(R.string.error_) + e.message)
                    Log.d(TAG, e.message.toString())
                    btnEnterTime.isEnabled = true
                    return false
                }
            }
        } else {
            displayMessage("${getString(R.string.error_)} $answer")
            return false
        }
    }

    private suspend fun getOrCreateWorkOrder(): WorkOrder {
        var wo: WorkOrder? = null
        defaultScope.launch {
            wo = workOrderViewModel.findWorkOrder("break", curEmployer.employerId)
        }
        delay(WAIT_250)
        try {
            curWorkOrder = wo!!
        } catch (e: Exception) {
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
            Log.d(TAG, "getOrCreateWorkOrder: $e")
        }
        delay(WAIT_250)
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

    private fun setStartTimeActions() {
        binding.apply {
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
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
                        mainScope.launch {
                            calculateAdjustmentsForRegAndOt(endTime)
                            delay(WAIT_500)
                            populateUi()
                        }
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

    private fun chooseToSaveOrDiscard() {
        if (df.getTimeWorked(startTime, endTime) > 0.0) {
            AlertDialog.Builder(mView.context)
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
        var tempWorkOrderHistoryDeferred: Deferred<WorkOrderHistory?>
        var tempWorkOrderHistory: WorkOrderHistory? = null
        defaultScope.launch {
            tempWorkOrderHistoryDeferred =
                async {
                    when (curWorkOrder) {
                        null if binding.radBreak.isChecked -> {
                            curWorkOrder = getOrCreateWorkOrder()
                            workOrderViewModel.getWorkOrderHistory(
                                curWorkOrder!!.workOrderId,
                                curDate.workDateId
                            )
                        }

                        null -> {
                            return@async null
                        }

                        else -> {
                            workOrderViewModel.getWorkOrderHistory(
                                curWorkOrder!!.workOrderId,
                                curDate.workDateId
                            )
                        }
                    }
                }

            tempWorkOrderHistory = if (tempWorkOrderHistoryDeferred.await() == null) {
                insertNewWorkOrderHistory()
            } else {
                tempWorkOrderHistoryDeferred.await()
            }
        }

        delay(WAIT_500)
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
//        Log.d(TAG, "insertNewWorkOrderHistory: $tempWorkOrderHistory")
        workOrderViewModel.insertWorkOrderHistory(tempWorkOrderHistory)
        return tempWorkOrderHistory
    }

    private fun validateTimeWorked(): String {
        binding.apply {
            if (acWorkOrder.text.isNullOrBlank() && !radBreak.isChecked) {
                return getString(R.string.please_enter_a_work_order_number)
            }
            if (radRegHours.isChecked) {
                if (timeWorkedByDay.hrsRegByTimeEntered + df.getTimeWorked(
                        startTime,
                        endTime
                    ) > 8.0
                ) {
                    return getString(R.string.this_will_exceed_8_hours)
                }
            }
            if (radOtHours.isChecked) {
                if (timeWorkedByDay.hrsRegByTimeEntered + timeWorkedByDay.hrsOtByTimeEntered + df.getTimeWorked(
                        startTime,
                        endTime
                    ) > 12.0
                ) {
                    return getString(R.string.this_will_exceed_12_hours)
                }
            }
            if (radDblOtHours.isChecked) {
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
        }
        return ANSWER_OK
    }

    private fun displayMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun populateWorkOrderInfo() {
        val display = curWorkOrder!!.woAddress + " | " + curWorkOrder!!.woDescription
        binding.tvWoInfo.apply {
            text = display
            visibility = View.VISIBLE
        }
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
            WorkDateTimesFragmentDirections.actionWorkDateTimesToWorkDateUpdateFragment()
        )
    }

    private fun gotoWorkOrderAddFragment() {
        mView.findNavController().navigate(
            WorkDateTimesFragmentDirections.actionWorkDateTimesToWorkOrderAddFragment()
        )
    }

    private fun gotoWorkOrderUpdateFragment() {
        mView.findNavController().navigate(
            WorkDateTimesFragmentDirections.actionWorkDateTimesToWorkOrderUpdateFragment()
        )
    }

    private fun gotoWorkOrderLookup() {
        mainViewModel.setWorkOrder(curWorkOrder)
        mainViewModel.addCallingFragment(TAG)
        gotoWorkOrderLookupFragment()
    }

    private fun gotoWorkOrderLookupFragment() {
        mView.findNavController().navigate(
            WorkDateTimesFragmentDirections.actionWorkDateTimesToWorkOrderLookupFragment()
        )
    }

    override fun gotoWorkOrderHistoryTimeUpdateFragment() {
        mainViewModel.addCallingFragment(TAG)
        mView.findNavController().navigate(
            WorkDateTimesFragmentDirections.actionWorkDateTimesFragmentToWorkOrderHistoryTimeUpdateFragment()
        )
    }

    override fun onDestroy() {
        mainScope.cancel()
        defaultScope.cancel()
        super.onDestroy()
        _binding = null
    }
}