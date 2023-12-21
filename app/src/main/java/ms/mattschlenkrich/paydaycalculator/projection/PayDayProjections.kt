package ms.mattschlenkrich.paydaycalculator.projection

import ms.mattschlenkrich.paydaycalculator.common.DAY_FRIDAY
import ms.mattschlenkrich.paydaycalculator.common.DAY_MONDAY
import ms.mattschlenkrich.paydaycalculator.common.DAY_SATURDAY
import ms.mattschlenkrich.paydaycalculator.common.DAY_SUNDAY
import ms.mattschlenkrich.paydaycalculator.common.DAY_THURSDAY
import ms.mattschlenkrich.paydaycalculator.common.DAY_TUESDAY
import ms.mattschlenkrich.paydaycalculator.common.DAY_WEDNESDAY
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.INTERVAL_BI_WEEKLY
import ms.mattschlenkrich.paydaycalculator.common.INTERVAL_WEEKLY
import java.time.DayOfWeek
import java.time.LocalDate

class PayDayProjections {
    val df = DateFunctions()
    fun ProjectPayDays(
        startDate: String, endDate: String, frequency: String, dayOfWeek: String
    )
            : ArrayList<String> {
        val dates = when (frequency) {
            INTERVAL_WEEKLY -> {
                projectWeekly(startDate, endDate, dayOfWeek)
            }

            INTERVAL_BI_WEEKLY -> {
                projectBiWeekly(startDate, endDate, dayOfWeek)
            }

            else -> {
                return ArrayList()
            }
        }

        return dates
    }

    private fun projectWeekly(
        startDate: String, endDate: String, dayOfWeek: String
    ): ArrayList<String> {
        val dates = ArrayList<String>()
        var workingDate = LocalDate.parse(startDate)
        workingDate = fixDateToDay(workingDate, dayOfWeek)
        val curDate = LocalDate.parse(df.getCurrentDateAsString())
        val lastDate = LocalDate.parse(endDate)
        while (workingDate < lastDate) {
            if (workingDate >= curDate) {
                dates.add(workingDate.toString())
            }
            workingDate = workingDate.plusWeeks(1)
        }

        return dates
    }

    private fun projectBiWeekly(
        startDate: String, endDate: String, dayOfWeek: String
    ): ArrayList<String> {
        val dates = ArrayList<String>()
        var workingDate = LocalDate.parse(startDate)
        workingDate = fixDateToDay(workingDate, dayOfWeek)
        val curDate = LocalDate.parse(df.getCurrentDateAsString())
        val lastDate = LocalDate.parse(endDate)
        while (workingDate < lastDate) {
            if (workingDate >= curDate) {
                dates.add(workingDate.toString())
            }
            workingDate = workingDate.plusWeeks(2)
        }

        return dates
    }

    private fun fixDateToDay(workingDate: LocalDate, dayOfWeek: String): LocalDate {
        val dayMinus = when (workingDate.dayOfWeek) {
            DayOfWeek.MONDAY -> {
                1L
            }

            DayOfWeek.TUESDAY -> {
                2L
            }

            DayOfWeek.WEDNESDAY -> {
                3L
            }

            DayOfWeek.THURSDAY -> {
                4L
            }

            DayOfWeek.FRIDAY -> {
                5L
            }

            DayOfWeek.SATURDAY -> {
                6L
            }

            DayOfWeek.SUNDAY -> {
                0L
            }

            else -> {
                0L
            }
        }
        val dayAdd = when (dayOfWeek) {
            DAY_MONDAY -> {
                1L
            }

            DAY_TUESDAY -> {
                2L
            }

            DAY_WEDNESDAY -> {
                3L
            }

            DAY_THURSDAY -> {
                4L
            }

            DAY_FRIDAY -> {
                5L
            }

            DAY_SATURDAY -> {
                6L
            }

            DAY_SUNDAY -> {
                0L
            }

            else -> {
                0L
            }
        }
        return workingDate.minusDays(dayMinus).plusDays(dayAdd)
    }
}