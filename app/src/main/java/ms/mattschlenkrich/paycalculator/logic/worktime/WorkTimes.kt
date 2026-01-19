package ms.mattschlenkrich.paycalculator.logic.worktime

import android.util.Log
import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.TimeWorkedByDay
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import java.util.Calendar

private const val TAG = "WorkTimesObject"

class WorkTimes(
    val mainActivity: MainActivity,
    private val employerId: Long,
    private val workDateId: Long,
    private val mView: View,
) {
    private val workTimeViewModel = mainActivity.workTimeViewModel
    private lateinit var workOrderList: List<WorkOrder>
    private lateinit var workOrderNumberList: List<String>
    private lateinit var existingHistories: List<WorkOrderHistoryCombined>
    private lateinit var existingHistoriesWithTimes: List<WorkOrderHistoryTimeWorkedCombined>
    private val timeWorkedByDayAsCalendarPairs = ArrayList<Pair<Calendar, Calendar>>()
    private lateinit var workOrdersForDay: List<WorkOrder>
    private var timeSummary = TimeWorkedByDay()
    private val df = DateFunctions()

    //    private val nf = NumberFunctions()
    private val defaultScope = CoroutineScope(Default)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    fun instantiateVariables() {
        mainScope.launch {
            getWorkOrderListFromDb()
            getHistoriesWithTimesForDayFromDb()
            val op2 = async { getHistoriesForDayFromDb() }
            awaitAll(op2)
            delay(WAIT_250)
            val op3 = async { getWorkTimesCalendarPairsForDay() }
            val op4 = async { getWorkOrdersForDay() }
            awaitAll(op3, op4)
            delay(WAIT_500)
            calculateHourTotalsForDay()
        }
    }

    fun getHistoriesWithTimesForDayFromDb() {
        defaultScope.launch {
            existingHistoriesWithTimes = workTimeViewModel.getExistingHistoriesWithTimes(workDateId)
        }
//        workTimeViewModel.getTimesWorkedByDate(workDateId)
//            .observe(mView.findViewTreeLifecycleOwner()!!) { timesList ->
//                existingHistoriesWithTimes = timesList
//            }
    }

    fun getHistoriesForDayFromDb() {
        defaultScope.launch {
            existingHistories = workTimeViewModel.getExistingHistories(workDateId)
        }
    }

    private fun getWorkTimesCalendarPairsForDay() {
        for (timWorked in existingHistoriesWithTimes) {
            val startTime = df.getCalendarFromString(timWorked.timeWorked.wohtStartTime)
            val endTime = df.getCalendarFromString(timWorked.timeWorked.wohtEndTime)
            timeWorkedByDayAsCalendarPairs.add(Pair(startTime, endTime))
        }
    }

    private fun getWorkOrdersForDay() {
        val historiesForDay = existingHistoriesWithTimes.groupBy {
            it.workOrderHistory.workOrder.workOrderId
        }
        val workOrderArrayList = ArrayList<WorkOrder>()
        for (history in historiesForDay) {
            workOrderArrayList.add(history.value.first().workOrderHistory.workOrder)
        }
        for (history in existingHistories) {
            workOrderArrayList.add(history.workOrder)
        }

        workOrdersForDay = workOrderArrayList.toList().groupBy {
            it.workOrderId
        }.map {
            it.value.first()
        }
    }

    suspend fun calculateHourTotalsForDay(): Boolean {
        val workDate = workTimeViewModel.getWorkDate(workDateId)

        timeSummary.hrsRegByTimeEntered = 0.0
        timeSummary.hrsOtByTimeEntered = 0.0
        timeSummary.hrsDblOtByTimeEntered = 0.0

        timeSummary.hrsRegByWorkOrderHistory = 0.0
        timeSummary.hrsOtByWorkOrderHistory = 0.0
        timeSummary.hrsDblOtByWorkOrderHistory = 0.0

        for (history in existingHistories) {
            timeSummary.hrsRegByWorkOrderHistory += history.workOrderHistory.woHistoryRegHours
            timeSummary.hrsOtByWorkOrderHistory += history.workOrderHistory.woHistoryOtHours
            timeSummary.hrsDblOtByWorkOrderHistory += history.workOrderHistory.woHistoryDblOtHours
        }

        for (timeWorked in existingHistoriesWithTimes) {
            when (timeWorked.timeWorked.wohtTimeType) {
                TimeWorkedTypes.REG_HOURS.value -> {
                    timeSummary.hrsRegByTimeEntered += df.getTimeWorked(
                        timeWorked.timeWorked.wohtStartTime,
                        timeWorked.timeWorked.wohtEndTime
                    )
                }

                TimeWorkedTypes.OT_HOURS.value -> {
                    timeSummary.hrsOtByTimeEntered += df.getTimeWorked(
                        timeWorked.timeWorked.wohtStartTime,
                        timeWorked.timeWorked.wohtEndTime
                    )
                }

                TimeWorkedTypes.DBL_OT_HOURS.value -> {
                    timeSummary.hrsDblOtByTimeEntered += df.getTimeWorked(
                        timeWorked.timeWorked.wohtStartTime,
                        timeWorked.timeWorked.wohtEndTime
                    )
                }

                else -> {
                    // Break time
                }
            }
        }
        timeSummary.hrsReg = workDate.wdRegHours
        timeSummary.hrsOt = workDate.wdOtHours
        timeSummary.hrsDblOt = workDate.wdDblOtHours
        timeSummary.hrsStat = workDate.wdStatHours
        Log.d(TAG, "time Summary $timeSummary")
        return true
    }

    fun getWorkOrderHistoryTimeWorkedList(workOrderId: Long): WorkOrderHistory {
        val histories = existingHistoriesWithTimes.filter {
            it.workOrderHistory.workOrder.workOrderId == workOrderId
        }
        var regHrs = 0.0
        var otHrs = 0.0
        var dblOtHrs = 0.0
        for (history in histories) {
            when (history.timeWorked.wohtTimeType) {
                TimeWorkedTypes.REG_HOURS.value -> {
                    regHrs += df.getTimeWorked(
                        history.timeWorked.wohtStartTime,
                        history.timeWorked.wohtEndTime
                    )
                }

                TimeWorkedTypes.OT_HOURS.value -> {
                    otHrs += df.getTimeWorked(
                        history.timeWorked.wohtStartTime,
                        history.timeWorked.wohtEndTime
                    )
                }

                TimeWorkedTypes.DBL_OT_HOURS.value -> {
                    dblOtHrs += df.getTimeWorked(
                        history.timeWorked.wohtStartTime,
                        history.timeWorked.wohtEndTime
                    )
                }

                else -> {
                    // no time
                }
            }
        }
        return WorkOrderHistory(
            histories.first().workOrderHistory.workOrderHistory.woHistoryId,
            histories.first().workOrderHistory.workOrderHistory.woHistoryWorkOrderId,
            histories.first().workOrderHistory.workOrderHistory.woHistoryWorkDateId,
            regHrs,
            otHrs,
            dblOtHrs,
            histories.first().workOrderHistory.workOrderHistory.woHistoryNote,
            false,
            df.getCurrentTimeAsString()
        )
    }

    private fun getWorkOrderListFromDb() {
        workTimeViewModel.getWorkOrderNumbers(employerId)
            .observe(mView.findViewTreeLifecycleOwner()!!) { woList ->
                workOrderList = woList
                val woNumberList = ArrayList<String>()
                for (wo in woList) {
                    woNumberList.add(wo.woNumber)
                }
                workOrderNumberList = woNumberList.toList()
            }
    }

    fun getWorkOrderList(): List<WorkOrder> {
        return workOrderList
    }

    fun getWorkOrderNumbers(): List<String> {
        return workOrderNumberList
    }


    fun getWorkOrderHistoryTimeWorkedList(): List<WorkOrderHistoryTimeWorkedCombined> {
        return existingHistoriesWithTimes

    }

    fun getTimeWorkedByDay(): TimeWorkedByDay {
        return timeSummary
    }

    fun updateWorkDate(workDate: WorkDates) {
        defaultScope.launch {
            workTimeViewModel.updateWorkDate(workDate)
        }
    }

    fun updateWorkOrderHistory(workOrderHistory: WorkOrderHistory) {
        defaultScope.launch {
            workTimeViewModel.updateWorkOrderHistory(workOrderHistory)
        }
    }
}