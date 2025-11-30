package ms.mattschlenkrich.paycalculator.logic.worktime

import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import java.util.Calendar

class WorkTimes(
    mainActivity: MainActivity,
    private val employer: Employers,
    private val workDate: WorkDates,
    private val callingFragment: Fragment,
) {
    private val workTimeViewModel = mainActivity.workTimeViewModel
    private lateinit var workOrderList: List<WorkOrder>
    private lateinit var existingHistories: List<WorkOrderHistoryCombined>
    private lateinit var existingHistoriesWithTimes: List<WorkOrderHistoryTimeWorkedCombined>
    private val timeWorkedByDayAsCalendarPairs = ArrayList<Pair<Calendar, Calendar>>()
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
            async { getExistingHistoriesWithTimes() }.await()
            async { getExistingWorkTimes() }.await()
            async { getExistingHistories() }.await()
            calculateHourTotals()

        }

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

    private fun getExistingWorkTimes() {
        for (timWorked in existingHistoriesWithTimes) {
            val startTime = df.getCalendarFromString(timWorked.timeWorked.wohtStartTime)
            val endTime = df.getCalendarFromString(timWorked.timeWorked.wohtEndTime)
            timeWorkedByDayAsCalendarPairs.add(Pair(startTime, endTime))
        }
    }

    private suspend fun getWorkOrderList() {
        workOrderList = workTimeViewModel.getWorkOrders(employer.employerId)
    }

    private suspend fun getExistingHistoriesWithTimes() {
        existingHistoriesWithTimes =
            workTimeViewModel.getExistingHistoriesWithTimes(workDate.workDateId)

    }

    private suspend fun getExistingHistories() {
        existingHistories = workTimeViewModel.getExistingHistories(workDate.workDateId)
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