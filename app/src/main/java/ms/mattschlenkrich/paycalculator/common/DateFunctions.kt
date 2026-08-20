package ms.mattschlenkrich.paycalculator.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.round

//private const val TAG = "DateFunctions"

class DateFunctions {
    private val dateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA)
    private val timeFormatter = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)
    private val dateChecker = SimpleDateFormat(DATE_CHECK, Locale.CANADA)
    private val displayDateString = SimpleDateFormat(DISPLAY_DATE, Locale.CANADA)

    fun getCurrentUTCTimeAsString(): String {
        val formatter = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Calendar.getInstance().time)
    }

    fun getCurrentDateAsString(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }

    fun getTimeDisplay(calendar: Calendar): String {
        return timeFormatter.format(calendar.time).split(" ")[1]
    }

    fun getDateTimeDisplay(calendar: Calendar): String {
        return timeFormatter.format(calendar.time)
    }

    fun getCalendarFromTime(time: String): Calendar {
        val cal = Calendar.getInstance()
        val tempTime = time.split(":")
        cal.set(Calendar.HOUR_OF_DAY, tempTime[0].toInt())
        cal.set(Calendar.MINUTE, tempTime[1].toInt())
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    fun getCalendarFromDateTime(dateTime: String): Calendar {
        val cal = Calendar.getInstance()
        try {
            cal.time = timeFormatter.parse(dateTime)!!
        } catch (_: Exception) {
        }
        return cal
    }

    fun getCalendarFromDateAndTime(date: String, time: String): Calendar {
        val cal = Calendar.getInstance()
        try {
            cal.time = timeFormatter.parse("$date $time:00")!!
        } catch (_: Exception) {
            try {
                cal.time = dateChecker.parse(date)!!
                val tempTime = time.split(":")
                cal.set(Calendar.HOUR_OF_DAY, tempTime[0].toInt())
                cal.set(Calendar.MINUTE, tempTime[1].toInt())
            } catch (_: Exception) {
            }
        }
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

//    fun getDateTimeFromDateAndTime(date: String, time: String): String {
//        return "$date $time"
//    }

    fun getDisplayDate(date: String): String {
        if (date.isBlank()) return ""
        return try {
            displayDateString.format(
                dateChecker.parse(date)!!,
            )
        } catch (_: Exception) {
            ""
        }
    }

    fun get12HourDisplay(time: Calendar): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(time.time)
    }

    fun get12HourDisplay(time: String): String {
        val tempTime =
            if (time.contains(" ")) time.split(" ").last().split(":") else time.split(":")
        val hour = tempTime[0].toInt()
        val minute = tempTime[1]
        return if (hour == 0) {
            "12:$minute AM"
        } else if (hour < 12) {
            "$hour:$minute AM"
        } else if (hour == 12) {
            "12:$minute PM"
        } else {
            "${hour - 12}:$minute PM"
        }
    }

    fun splitTimeFromDateTime(time: String): List<String> {
        return time.split(" ").last().split(":")
    }

    fun getTimeWorked(startTime: String, endTime: String): Double {
        return try {
            val start = timeFormatter.parse(startTime)!!
            val end = timeFormatter.parse(endTime)!!
            var diff = (end.time - start.time).toDouble() / (1000.0 * 60.0 * 60.0)
            if (diff < 0 && (startTime.substring(0, 10) == endTime.substring(0, 10))) {
                diff += 24.0
            }
            // Round to 4 decimal places to avoid floating point inaccuracies during sum
            round(diff * 10000.0) / 10000.0
        } catch (_: Exception) {
            val tempStart = splitTimeFromDateTime(startTime)
            val tempEnd = splitTimeFromDateTime(endTime)
            val hoursStart = (tempStart[0].toDouble() * 60) + tempStart[1].toDouble()
            val hoursEnd = (tempEnd[0].toDouble() * 60) + tempEnd[1].toDouble()
            var diff = (hoursEnd - hoursStart) / 60.0
            if (diff < 0) {
                diff += 24.0
            }
            diff
        }
    }

    fun getTimeWorked(startTime: Calendar, endTime: Calendar): Double {
        return (endTime.timeInMillis - startTime.timeInMillis).toDouble() / (1000.0 * 60.0 * 60.0)
    }

//    fun roundCalendarTimeTo15Minutes(time: Calendar): Calendar {
//        val tempTime = time.clone() as Calendar
//        val minutes = tempTime.get(Calendar.MINUTE)
//        val divided = minutes / 15.0
//        val roundedInt = round(divided)
//        val roundedMinute = (roundedInt * 15).toInt()
//        tempTime.set(Calendar.MINUTE, roundedMinute)
//        tempTime.set(Calendar.SECOND, 0)
//        return tempTime
//    }

    fun roundCalendarTimeUpTo15Minutes(time: Calendar): Calendar {
        val tempTime = time.clone() as Calendar
        val minutes = tempTime.get(Calendar.MINUTE)
        val roundedMinute = ((minutes + 14) / 15) * 15
        if (roundedMinute == 60) {
            tempTime.add(Calendar.HOUR_OF_DAY, 1)
            tempTime.set(Calendar.MINUTE, 0)
        } else {
            tempTime.set(Calendar.MINUTE, roundedMinute)
        }
        tempTime.set(Calendar.SECOND, 0)
        tempTime.set(Calendar.MILLISECOND, 0)
        return tempTime
    }

    fun roundCalendarTimeDownTo15Minutes(time: Calendar): Calendar {
        val tempTime = time.clone() as Calendar
        val minutes = tempTime.get(Calendar.MINUTE)
        val roundedMinute = (minutes / 15) * 15
        tempTime.set(Calendar.MINUTE, roundedMinute)
        tempTime.set(Calendar.SECOND, 0)
        tempTime.set(Calendar.MILLISECOND, 0)
        return tempTime
    }

    fun addHoursToCalendar(time: Calendar, hours: Double): Calendar {
        val tempTime = time.clone() as Calendar
        tempTime.add(Calendar.MINUTE, (hours * 60).toInt())
        tempTime.set(Calendar.SECOND, 0)
        tempTime.set(Calendar.MILLISECOND, 0)
        return tempTime
    }

    fun showDatePicker(
        context: android.content.Context,
        initialDate: String,
        onDateSelected: (String) -> Unit
    ) {
        val curDateAll = initialDate.split("-")
        val datePickerDialog = android.app.DatePickerDialog(
            context, { _, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                val display = "$year-${
                    month.toString().padStart(2, '0')
                }-${
                    dayOfMonth.toString().padStart(2, '0')
                }"
                onDateSelected(display)
            }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
        )
        datePickerDialog.show()
    }

    fun showTimePicker(
        context: android.content.Context,
        initialTime: Calendar,
        onTimeSelected: (Calendar) -> Unit
    ) {
        val timePickerDialog = android.app.TimePickerDialog(
            context, { _, h, m ->
                val newTime = (initialTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                }
                onTimeSelected(newTime)
            }, initialTime.get(Calendar.HOUR_OF_DAY), initialTime.get(Calendar.MINUTE), false
        )
        timePickerDialog.show()
    }

    fun getNextDate(date: String): String {
        return try {
            val cal = Calendar.getInstance()
            cal.time = dateChecker.parse(date)!!
            cal.add(Calendar.DAY_OF_YEAR, 1)
            dateFormat.format(cal.time)
        } catch (_: Exception) {
            date
        }
    }
}