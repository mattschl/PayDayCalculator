package ms.mattschlenkrich.billsprojectionv2.common

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale

//private const val TAG = "DateFunctions"

@Suppress("unused")
class DateFunctions {
    private val dateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA)
    private val timeFormatter = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)
    private val dateChecker = SimpleDateFormat(DATE_CHECK, Locale.CANADA)
    private val displayDateString = SimpleDateFormat(DISPLAY_DATE, Locale.CANADA)
    private val displayDateWithYear = SimpleDateFormat(DISPLAY_DATE_WITH_YEAR, Locale.CANADA)

    fun getCurrentTimeAsString(): String {
        return timeFormatter.format(Calendar.getInstance().time)
    }

    fun getCurrentDateAsString(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }

    fun convertDateToString(date: LocalDate): String {
        return dateFormat.format(date)
    }

    fun convertStringToDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    fun getDisplayDate(date: String): String {
        return displayDateString.format(
            dateChecker.parse(date)!!
        )
    }

    private fun getDisplayDateWithYear(date: String): String {
        return displayDateWithYear.format(
            dateChecker.parse(date)!!
        )
    }

    fun getDisplayDateInComingYear(date: String): String {
        var mDate = LocalDate.parse(date)
//        Log.d(TAG, "FIRST date is $mDate")
        while (mDate.toString() < getCurrentDateAsString()) {
            mDate = mDate.plusYears(1)
//            Log.d(TAG, "new date is $mDate")
        }
        return getDisplayDateWithYear(mDate.toString())
    }

    fun getDisplayDateInComingYear(date: String, count: Long): String {
        val mDate = LocalDate.parse(date)
        return getDisplayDateWithYear(mDate.plusYears(count).toString())
    }

    fun getNextMonthlyDate(startDate: String, interval: Int): String {
        var mDate = LocalDate.parse(startDate)
        while (mDate.toString() < getCurrentDateAsString()) {
            mDate = mDate.plusMonths(interval.toLong())
        }
        return getDisplayDateWithYear(mDate.toString())
    }

    fun getNextWeeklyDate(startDate: String, interval: Int): String {
        var mDate = LocalDate.parse(startDate)
        while (mDate.toString() < getCurrentDateAsString()) {
            mDate = mDate.plusWeeks(interval.toLong())
        }
        return getDisplayDateWithYear(mDate.toString())
    }

    fun getDateStringFromDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun getMonthsBetween(startDate: String, endDate: String): Int {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        return end.monthValue + end.year - start.monthValue - start.year + 1
    }

    fun getFirstOfMonth(date: String): String {
        return date.dropLast(2) + "01"
    }

    private fun getLastOfMonth(date: String): String {
        val mDate = LocalDate.parse(getFirstOfMonth(date))
        return mDate.plusMonths(1).minusDays(1).toString()
    }

    fun getFirstOfPreviousMonth(date: String): String {
        val mDate = LocalDate.parse(date)
        return getFirstOfMonth(mDate.minusMonths(1).toString())
    }

    fun getLastOfPreviousMonth(date: String): String {
        val mDate = LocalDate.parse(date).minusMonths(1)
        return getLastOfMonth(mDate.toString())
    }
}