package ms.mattschlenkrich.paycalculator.logic

import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.data.TimeWorkedByDay
import ms.mattschlenkrich.paycalculator.data.WorkDates
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryTimeWorkedCombined
import java.util.Calendar

class WorkTimesNew(
    val mainActivity: MainActivity,
    private val employerId: Long,
    private val workDateId: Long,
    mView: View,
) {
    private val workTimeViewModel = mainActivity.workTimeViewModel
    private lateinit var workOrderList: List<WorkOrder>
    private lateinit var existingHistories: List<WorkOrderHistoryCombined>
    private lateinit var existingHistoriesWithTimes: List<WorkOrderHistoryTimeWorkedCombined>
    private val timeWorkedByDayAsCalendarPairs = ArrayList<Pair<Calendar, Calendar>>()
    private lateinit var workOrdersForDay: List<WorkOrder>
    private var regHoursByTimeWorked = 0.0
    private var otHoursByTimeWorked = 0.0
    private var dblOtHoursByTimeWorked = 0.0
    private var hrsRegByDay = 0.0
    private var hrsOtByDay = 0.0
    private var hrsDblOtByDay = 0.0

    private var hrsStatByDay = 0.0
    private val df = DateFunctions()

    //    private val nf = NumberFunctions()
    private val defaultScope = CoroutineScope(Default)

    init {
        instantiateVariables()
    }

    fun instantiateVariables() {
        defaultScope.launch {
            getWorkOrderListFromDb()
            val op1 = async { getExistingHistoriesWithTimesForDay() }
            val op2 = async { getExistingHistoriesForDay() }
            awaitAll(op1, op2)
            delay(WAIT_100)
            val op3 = async { getExistingWorkTimesForDay() }
            val op4 = async { getWorkOrdersForDay() }
            awaitAll(op3, op4)

            calculateHourTotalsForDay()

        }
    }

    suspend fun getExistingHistoriesWithTimesForDay() {

    }

    suspend fun getExistingHistoriesForDay() {
        existingHistories = workTimeViewModel.getExistingHistories(workDateId)
    }

    private fun getExistingWorkTimesForDay() {
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

    suspend fun calculateHourTotalsForDay() {
        val workDate = workTimeViewModel.getWorkDate(workDateId)
        hrsRegByDay = workDate.wdRegHours
        hrsOtByDay = workDate.wdOtHours
        hrsDblOtByDay = workDate.wdDblOtHours
        hrsStatByDay = workDate.wdStatHours

        regHoursByTimeWorked = 0.0
        otHoursByTimeWorked = 0.0
        dblOtHoursByTimeWorked = 0.0

        for (timeWorked in existingHistoriesWithTimes) {
            when (timeWorked.timeWorked.wohtTimeType) {
                TimeWorkedTypes.REG_HOURS.value -> {
                    regHoursByTimeWorked += df.getTimeWorked(
                        timeWorked.timeWorked.wohtStartTime,
                        timeWorked.timeWorked.wohtEndTime
                    )
                }

                TimeWorkedTypes.OT_HOURS.value -> {
                    otHoursByTimeWorked += df.getTimeWorked(
                        timeWorked.timeWorked.wohtStartTime,
                        timeWorked.timeWorked.wohtEndTime
                    )
                }

                TimeWorkedTypes.DBL_OT_HOURS.value -> {
                    dblOtHoursByTimeWorked += df.getTimeWorked(
                        timeWorked.timeWorked.wohtStartTime,
                        timeWorked.timeWorked.wohtEndTime
                    )
                }

                else -> {
                    // Break time
                }
            }
        }
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
            df.getCurrentUTCTimeAsString()
        )
    }

    private suspend fun getWorkOrderListFromDb() {
        workOrderList = workTimeViewModel.getWorkOrders(employerId)
    }

    fun getWorkOrderList(): List<WorkOrder> {
        return workOrderList
    }

    fun getWorkOrderHistoryTimeWorkedList(): List<WorkOrderHistoryTimeWorkedCombined> {
        return existingHistoriesWithTimes

    }

    fun getTimeWorkedByDay(): TimeWorkedByDay {
        return TimeWorkedByDay(
            hrsRegByDay,
            hrsOtByDay,
            hrsDblOtByDay,
            hrsStatByDay,
            regHoursByTimeWorked,
            otHoursByTimeWorked,
            dblOtHoursByTimeWorked,
        )
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

    fun insertWorkDateTime(workOrderHistoryTimeWorked: WorkOrderHistoryTimeWorked) {
        defaultScope.launch {
            workTimeViewModel.insertWorkTime(workOrderHistoryTimeWorked)
        }

    }
}