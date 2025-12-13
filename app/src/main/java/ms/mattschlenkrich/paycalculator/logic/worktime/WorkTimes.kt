package ms.mattschlenkrich.paycalculator.logic.worktime

import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import java.util.Calendar

class WorkTimes(
    mainActivity: MainActivity,
    private val employerId: Long,
    private val workDateId: Long,
    private val callingFragment: Fragment,
) {
    private val workTimeViewModel = mainActivity.workTimeViewModel
    private lateinit var workOrderList: List<WorkOrder>
    private lateinit var existingHistories: List<WorkOrderHistoryCombined>
    private lateinit var existingHistoriesWithTimes: List<WorkOrderHistoryTimeWorkedCombined>
    private val timeWorkedByDayAsCalendarPairs = ArrayList<Pair<Calendar, Calendar>>()
    private lateinit var workOrdersForDay: List<WorkOrder>
    private var totalRegHoursForDay = 0.0
    private var totalOtHoursForDay = 0.0
    private var totalDblOtHoursForDay = 0.0
    private var totalHoursForDay = 0.0
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val defaultScope = CoroutineScope(Default)

    init {
        instantiateVariables()
    }

    private fun instantiateVariables() {
        defaultScope.launch {
            getWorkOrderList()
            async { getExistingHistoriesWithTimesForDay() }.await()
            async { getExistingWorkTimesForDay() }.await()
            async { getExistingHistoriesForDay() }.await()
            async { getWorkOrdersForDay() }.await()
            
            calculateHourTotals()

        }

    }

    private fun calculateHoursPerWorkOrderHistory(workOrderId: Long): WorkOrderHistory {
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

    private fun calculateHourTotals() {
        for (timePair in timeWorkedByDayAsCalendarPairs) {
            totalHoursForDay += df.getTimeWorked(timePair.first, timePair.second)
        }
        totalRegHoursForDay = 0.0
        totalOtHoursForDay = 0.0
        totalDblOtHoursForDay = 0.0

        for (timeWorked in existingHistoriesWithTimes) {
            when (timeWorked.timeWorked.wohtTimeType) {
                TimeWorkedTypes.REG_HOURS.value -> {
                    totalRegHoursForDay += df.getTimeWorked(
                        timeWorked.timeWorked.wohtStartTime,
                        timeWorked.timeWorked.wohtEndTime
                    )
                }

                TimeWorkedTypes.OT_HOURS.value -> {
                    totalOtHoursForDay += df.getTimeWorked(
                        timeWorked.timeWorked.wohtStartTime,
                        timeWorked.timeWorked.wohtEndTime
                    )
                }

                TimeWorkedTypes.DBL_OT_HOURS.value -> {
                    totalDblOtHoursForDay += df.getTimeWorked(
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

    private fun getExistingWorkTimesForDay() {
        for (timWorked in existingHistoriesWithTimes) {
            val startTime = df.getCalendarFromString(timWorked.timeWorked.wohtStartTime)
            val endTime = df.getCalendarFromString(timWorked.timeWorked.wohtEndTime)
            timeWorkedByDayAsCalendarPairs.add(Pair(startTime, endTime))
        }
    }

    private suspend fun getWorkOrderList() {
        workOrderList = workTimeViewModel.getWorkOrders(employerId)
    }

    private suspend fun getExistingHistoriesWithTimesForDay() {
        existingHistoriesWithTimes =
            workTimeViewModel.getExistingHistoriesWithTimes(workDateId)

    }

    private suspend fun getExistingHistoriesForDay() {
        existingHistories = workTimeViewModel.getExistingHistories(workDateId)
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

    fun getTotalHoursForDay(): Double {
        return totalHoursForDay
    }

    fun getRegHoursForDay(): Double {
        return totalRegHoursForDay
    }

    fun getOtHoursForDay(): Double {
        return totalOtHoursForDay
    }

    fun getDblOtHoursForDay(): Double {
        return totalDblOtHoursForDay
    }
}