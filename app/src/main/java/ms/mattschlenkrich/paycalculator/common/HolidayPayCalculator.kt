package ms.mattschlenkrich.paycalculator.common

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import java.time.DayOfWeek
import java.time.LocalDate

class HolidayPayCalculator(
    private val mainActivity: MainActivity,
    private val employerId: Long,
    private val holidayDate: String
) {
    private val defaultScope = Dispatchers.Default
    private var statHours = 0.0

    init {
        CoroutineScope(defaultScope).launch {
            val firstDate = LocalDate.parse(holidayDate).minusDays(31).toString()
            val lastDate = LocalDate.parse(holidayDate).minusDays(1).toString()
            val workDaeListAsync = async { getWorkDates(employerId, firstDate, lastDate) }
            val totalHoursAsync = async { calculateHoursTotal(workDaeListAsync.await()) }
            val totalWorkDaysAsync = async { calculateDaysOfWork(holidayDate) }
            statHours = totalHoursAsync.await() / totalWorkDaysAsync.await()
            Log.d(
                TABLE_WORK_DATES,
                "total hours: ${totalHoursAsync.await()} " + "total work days: ${totalWorkDaysAsync.await()}"
            )
        }
    }

    fun getStatHours(): Double {
        return statHours
    }

    private fun getWorkDates(
        employerId: Long, firstDate: String, lastDate: String
    ): List<WorkDates> {
        return mainActivity.payDayViewModel.getWorkDatesByDateRange(
            employerId, firstDate, lastDate
        )
    }

    private fun calculateHoursTotal(workDateList: List<WorkDates>): Double {
        var totalHours = 0.0
        for (workDate in workDateList) {
            totalHours += workDate.wdRegHours
            totalHours += workDate.wdOtHours * 1.5
            totalHours += workDate.wdDblOtHours * 2
        }
        return totalHours
    }

    private fun calculateDaysOfWork(startingDate: String): Int {
        var dayCount = 0
        for (i in 31 downTo 1) {
            if (LocalDate.parse(startingDate)
                    .minusDays(i.toLong()).dayOfWeek != DayOfWeek.SATURDAY && LocalDate.parse(
                    startingDate
                ).minusDays(i.toLong()).dayOfWeek != DayOfWeek.SUNDAY
            ) {
                dayCount++
            }
        }
        return dayCount
    }
}