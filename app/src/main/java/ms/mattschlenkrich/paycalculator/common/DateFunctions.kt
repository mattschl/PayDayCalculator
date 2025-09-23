package ms.mattschlenkrich.paycalculator.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

//private const val TAG = "DateFunctions"

class DateFunctions {
    private val dateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA)
    private val timeFormatter = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)
    private val dateChecker = SimpleDateFormat(DATE_CHECK, Locale.CANADA)
    private val displayDateString = SimpleDateFormat(DISPLAY_DATE, Locale.CANADA)

    fun getCurrentTimeAsString(): String {
        return timeFormatter.format(Calendar.getInstance().time)
    }

    fun getCurrentDateAsString(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }

    fun getDisplayDate(date: String): String {
        return displayDateString.format(
            dateChecker.parse(date)!!
        )
    }

    fun get12HourDisplay(time: Calendar): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(time.time)
    }

    fun get12HourIntOfHour(time: Calendar): Int {
        return SimpleDateFormat("HH", Locale.getDefault()).format(time.time).toInt()
    }

    fun get12HourIntOfMinute(time: Calendar): Int {
        return SimpleDateFormat("mm", Locale.getDefault()).format(time.time).toInt()
    }
}