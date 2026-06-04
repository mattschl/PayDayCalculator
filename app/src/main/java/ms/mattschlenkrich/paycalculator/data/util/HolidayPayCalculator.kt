package ms.mattschlenkrich.paycalculator.data.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_DATES
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import java.time.DayOfWeek
import java.time.LocalDate

class HolidayPayCalculator(
    private val payDayViewModel: PayDayViewModel,
    private val employerId: Long,
    private val holidayDate: String,
) {
    private val defaultScope = Dispatchers.Default

    suspend fun calculateStatHours(): Double = withContext(defaultScope) {
        val firstDate = LocalDate.parse(holidayDate).minusDays(31).toString()
        val lastDate = LocalDate.parse(holidayDate).minusDays(1).toString()
        val workDateList = getWorkDates(employerId, firstDate, lastDate)
        val totalHours = calculateHoursTotal(workDateList)
        val totalWorkDays = calculateDaysOfWork(holidayDate)
        val daysActuallyWorked = calculateDaysActuallyWorked(workDateList)
        val statHours = if (daysActuallyWorked < 15) 0.0 else totalHours / totalWorkDays
        Log.d(
            TABLE_WORK_DATES,
            "total hours: $totalHours total work days: $totalWorkDays " +
                    "days actually worked: $daysActuallyWorked stat hours: $statHours"
        )
        statHours
    }

    private fun getWorkDates(
        employerId: Long, firstDate: String, lastDate: String
    ): List<WorkDates> {
        return payDayViewModel.getWorkDatesByDateRange(
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

    private fun calculateDaysActuallyWorked(workDateList: List<WorkDates>): Int {
        var dayCount = 0
        for (workDate in workDateList) {
            if ((workDate.wdRegHours > 0.0) || (workDate.wdStatHours > 0.0)) {
                dayCount++
            }
        }
        return dayCount
    }

    private fun calculateDaysOfWork(startingDate: String): Int {
        var dayCount = 0
        for (i in 31 downTo 1) {
            val date = LocalDate.parse(startingDate).minusDays(i.toLong())
            if (date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY) {
                dayCount++
            }
        }
        return dayCount
    }
}