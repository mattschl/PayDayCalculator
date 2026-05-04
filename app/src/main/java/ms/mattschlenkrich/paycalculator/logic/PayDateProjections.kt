package ms.mattschlenkrich.paycalculator.logic

import ms.mattschlenkrich.paycalculator.common.DAY_FRIDAY
import ms.mattschlenkrich.paycalculator.common.DAY_MONDAY
import ms.mattschlenkrich.paycalculator.common.DAY_SATURDAY
import ms.mattschlenkrich.paycalculator.common.DAY_SUNDAY
import ms.mattschlenkrich.paycalculator.common.DAY_THURSDAY
import ms.mattschlenkrich.paycalculator.common.DAY_TUESDAY
import ms.mattschlenkrich.paycalculator.common.DAY_WEDNESDAY
import ms.mattschlenkrich.paycalculator.common.INTERVAL_BI_WEEKLY
import ms.mattschlenkrich.paycalculator.common.INTERVAL_MONTHLY
import ms.mattschlenkrich.paycalculator.common.INTERVAL_SEMI_MONTHLY
import ms.mattschlenkrich.paycalculator.common.INTERVAL_WEEKLY
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import java.time.DayOfWeek
import java.time.LocalDate

class PayDateProjections {

    fun generateNextCutOff(
        employer: Employers,
        mostRecent: String,
    ): String {
        var newDate = ""
        val projectionEnd = if (mostRecent.isNotEmpty()) {
            val mostRecentDate = LocalDate.parse(mostRecent)
            if (mostRecentDate.isAfter(LocalDate.now())) {
                mostRecentDate.plusMonths(2)
            } else {
                LocalDate.now().plusMonths(2)
            }
        } else {
            LocalDate.now().plusMonths(2)
        }
        val dates = projectAll(employer, projectionEnd.toString())

        if (mostRecent.isEmpty()) {
            return getCutOffForDate(employer, LocalDate.now().toString())
        }

        val mostRecentDate = LocalDate.parse(mostRecent)

        if (dates.isNotEmpty()) {
            for (date in dates) {
                val cutoffCandidate = date.minusDays(employer.cutoffDaysBefore.toLong()).toString()
                if (cutoffCandidate > mostRecent) {
                    newDate = cutoffCandidate
                    break
                }
            }
        } else {
            newDate = ""
        }

        return newDate
    }

    fun getCutOffForDate(
        employer: Employers,
        date: String
    ): String {
        val targetDate = LocalDate.parse(date)
        val dates = projectAll(employer, targetDate.plusMonths(1).toString())

        for (payDate in dates) {
            val cutoffDate = payDate.minusDays(employer.cutoffDaysBefore.toLong())
            if (targetDate <= cutoffDate) {
                return cutoffDate.toString()
            }
        }
        return ""
    }

    private fun projectAll(employer: Employers, endDate: String): ArrayList<LocalDate> {
        return when (employer.payFrequency) {
            INTERVAL_WEEKLY -> {
                projectWeekly(
                    employer.startDate, endDate, employer.dayOfWeek
                )
            }

            INTERVAL_BI_WEEKLY -> {
                projectBiWeekly(
                    employer.startDate, endDate, employer.dayOfWeek
                )
            }

            INTERVAL_MONTHLY -> {
                projectMonthly(
                    employer.startDate, endDate, employer.mainMonthlyDate
                )
            }

            INTERVAL_SEMI_MONTHLY -> {
                projectSemiMonthly(
                    employer.startDate, endDate, employer.midMonthlyDate, employer.mainMonthlyDate
                )
            }

            else -> {
                ArrayList()
            }
        }
    }

    private fun projectMonthly(
        startDate: String, endDate: String, mainMonthlyDate: Int
    ): ArrayList<LocalDate> {
        val dates = ArrayList<LocalDate>()
        var workingDate = LocalDate.parse(startDate)
        val lastDate = LocalDate.parse(endDate)

        while (workingDate < lastDate) {
            val mainDate = safeWithDayOfMonth(workingDate, mainMonthlyDate)
            if (mainDate >= workingDate && mainDate < lastDate) {
                dates.add(mainDate)
            }
            workingDate = workingDate.withDayOfMonth(1).plusMonths(1)
        }
        return dates
    }

    private fun projectSemiMonthly(
        startDate: String, endDate: String, midMonthlyDate: Int, mainMonthlyDate: Int
    ): ArrayList<LocalDate> {
        val dates = ArrayList<LocalDate>()
        var workingDate = LocalDate.parse(startDate)
        val lastDate = LocalDate.parse(endDate)

        while (workingDate < lastDate) {
            val midDate = safeWithDayOfMonth(workingDate, midMonthlyDate)
            if (midDate >= workingDate && midDate < lastDate) {
                dates.add(midDate)
            }
            val mainDate = safeWithDayOfMonth(workingDate, mainMonthlyDate)
            if (mainDate >= workingDate && mainDate < lastDate) {
                if (mainDate != midDate) {
                    dates.add(mainDate)
                }
            }
            workingDate = workingDate.withDayOfMonth(1).plusMonths(1)
        }
        dates.sort()
        return dates
    }

    private fun safeWithDayOfMonth(date: LocalDate, day: Int): LocalDate {
        return date.withDayOfMonth(
            if (day > date.lengthOfMonth())
                date.lengthOfMonth()
            else if (day < 1)
                1
            else
                day
        )
    }

    private fun projectWeekly(
        startDate: String, endDate: String, dayOfWeek: String
    ): ArrayList<LocalDate> {
        val dates = ArrayList<LocalDate>()
        var workingDate = LocalDate.parse(startDate)
        workingDate = fixDateToDay(workingDate, dayOfWeek)
        val lastDate = LocalDate.parse(endDate)
        while (workingDate < lastDate) {
            dates.add(workingDate)
            workingDate = workingDate.plusWeeks(1)
        }

        return dates
    }

    private fun projectBiWeekly(
        startDate: String, endDate: String, dayOfWeek: String
    ): ArrayList<LocalDate> {
        val dates = ArrayList<LocalDate>()
        var workingDate = LocalDate.parse(startDate)
        workingDate = fixDateToDay(workingDate, dayOfWeek)
        val lastDate = LocalDate.parse(endDate)
        while (workingDate < lastDate) {
            dates.add(workingDate)
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