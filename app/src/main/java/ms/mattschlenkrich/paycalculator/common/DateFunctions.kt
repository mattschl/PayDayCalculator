package ms.mattschlenkrich.paycalculator.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.round

private const val TAG = "DateFunctions"

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

    fun get12HourDisplay(time: String): String {
        val tempTime = time.split(":")
        return if (tempTime[0].toInt() == 0) {
            "12:${tempTime[1]} AM"
        } else if (tempTime[0].toInt() < 12) {
            "${tempTime[0]}:${tempTime[1]} AM"
        } else if (tempTime[0].toInt() == 12) {
            "12:${tempTime[1]} PM"
        } else {
            "${tempTime[0].toInt() - 12}:${tempTime[1]} PM"
        }
    }

    fun get12HourIntOfHour(time: Calendar): Int {
        return SimpleDateFormat("HH", Locale.getDefault()).format(time.time).toInt()
    }

    fun get12HourIntOfMinute(time: Calendar): Int {
        return SimpleDateFormat("mm", Locale.getDefault()).format(time.time).toInt()
    }

    fun roundTimeTo15Minutes(hour: Int, minute: Int): Pair<Int, Int> {
        val roundedMinute = (round(minute.toDouble() / 15) * 15).toInt()
        if (roundedMinute == 60) {
            return Pair(hour + 1, 0)
        }
        return Pair(hour, roundedMinute)
    }

    fun roundTimeUpTo15Minutes(hour: Int, minute: Int): Pair<Int, Int> {
        val roundedMinute = (round((minute.toDouble() + 6.5) / 15) * 15).toInt()
        if (roundedMinute == 60) {
            return Pair(hour + 1, 0)
        }
        return Pair(hour, roundedMinute)
    }

    fun roundTimeDownTo15Minutes(hour: Int, minute: Int): Pair<Int, Int> {
        val roundedMinute = (round((minute.toDouble() - 6.5) / 15) * 15).toInt()
        if (roundedMinute == 60) {
            return Pair(hour + 1, 0)
        }
        return Pair(hour, roundedMinute)
    }

    fun getDateFromCalendarAsString(time: Calendar): String {
        return timeFormatter.format(time.time)
    }

    fun splitTimeFromDateTime(time: String): List<String> {
        return time.split(" ").last().split(":")
    }

    fun getTimeWorked(startTime: String, endTime: String): Double {
        val tempStart = splitTimeFromDateTime(startTime)
        val tempEnd = splitTimeFromDateTime(endTime)
        val hoursStart = tempStart[0].toDouble() * 60 + tempStart[1].toDouble()
        val hoursEnd = tempEnd[0].toDouble() * 60 + tempEnd[1].toDouble()
        return (hoursEnd - hoursStart) / 60
    }

    fun getTimeWorked(startTime: Calendar, endTime: Calendar): Double {
        val hoursStart =
            startTime.get(Calendar.HOUR_OF_DAY).toDouble() * 60 + startTime.get(Calendar.MINUTE)
                .toDouble()
        val hoursEnd =
            endTime.get(Calendar.HOUR_OF_DAY).toDouble() * 60 + endTime.get(Calendar.MINUTE)
                .toDouble()
        return (hoursEnd - hoursStart) / 60
    }

    fun getCalendarFromString(timeString: String): Calendar {
        val tempDateTime = timeString.split(" ")
        val tempDate = tempDateTime[0].split("-")
        val tempTime = tempDateTime[1].split(":")
        val cal = Calendar.getInstance()
        cal.set(tempDate[0].toInt(), tempDate[1].toInt() - 1, tempDate[2].toInt())
        cal.set(Calendar.HOUR_OF_DAY, tempTime[0].toInt())
        cal.set(Calendar.MINUTE, tempTime[1].toInt())
        cal.set(Calendar.SECOND, 0)
        return cal
    }

    fun roundCalendarTimeTo15Minutes(time: Calendar): Calendar {
        val roundedMinute = (round(time.get(Calendar.MINUTE).toDouble() / 15) * 15).toInt()
        time.set(Calendar.MINUTE, roundedMinute)
        time.set(Calendar.SECOND, 0)
        return time
    }

    fun roundCalendarTimeUpTo15Minutes(time: Calendar): Calendar {
        val roundedMinute = (round(time.get(Calendar.MINUTE).toDouble() / 15 + 6.5) * 15).toInt()
        time.set(Calendar.MINUTE, roundedMinute)
        time.set(Calendar.SECOND, 0)
        return time
    }

    fun getHourFromStringAsInt(timeString: String): Int {
        var tempTime = timeString.split(" ")
        tempTime = tempTime[1].split(":")
        return tempTime[0].toInt()
    }

    fun getMinuteFromStringAsInt(timeString: String): Int {
        var tempTime = timeString.split(" ")
        tempTime = tempTime[1].split(":")
        return tempTime[1].toInt()
    }
}