package ms.mattschlenkrich.paycalculator.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DateFunctionsTest {

    private val df = DateFunctions()

    @Test
    fun getTimeDisplay_returnsCorrectTimeString() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 45)
        }
        val result = df.getTimeDisplay(cal)
        assertEquals("14:30:45", result)
    }

    @Test
    fun getCalendarFromTime_returnsCorrectCalendar() {
        val time = "08:45"
        val cal = df.getCalendarFromTime(time)
        assertEquals(8, cal[Calendar.HOUR_OF_DAY])
        assertEquals(45, cal[Calendar.MINUTE])
        assertEquals(0, cal[Calendar.SECOND])
    }

    @Test
    fun getCalendarFromDateTime_returnsCorrectCalendar() {
        val dateTime = "2026-08-04 22:15:00"
        val cal = df.getCalendarFromDateTime(dateTime)
        assertEquals(2026, cal[Calendar.YEAR])
        assertEquals(7, cal[Calendar.MONTH]) // August is 7
        assertEquals(4, cal[Calendar.DAY_OF_MONTH])
        assertEquals(22, cal[Calendar.HOUR_OF_DAY])
        assertEquals(15, cal[Calendar.MINUTE])
    }

    @Test
    fun getDisplayDate_returnsFormattedDate() {
        val date = "2026-08-04"
        val result = df.getDisplayDate(date)
        // Format is "EEE dd LLL" -> e.g. "Tue 04 Aug" (depending on Locale)
        // Locale.CANADA is used in DateFunctions
        assertTrue(result.contains("04"))
        assertTrue(result.contains("Aug") || result.contains("août"))
    }

    @Test
    fun get12HourDisplay_fromCalendar_returnsCorrectString() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 13)
            set(Calendar.MINUTE, 5)
        }
        val result = df.get12HourDisplay(cal)
        // Depending on system locale default for AM/PM, but let's check general structure
        assertTrue(result.contains("1:05"))
        assertTrue(result.contains("PM"))
    }

    @Test
    fun get12HourDisplay_fromString_returnsCorrectString() {
        assertEquals("12:00 AM", df.get12HourDisplay("00:00"))
        assertEquals("8:30 AM", df.get12HourDisplay("08:30"))
        assertEquals("12:15 PM", df.get12HourDisplay("12:15"))
        assertEquals("4:45 PM", df.get12HourDisplay("16:45"))
    }

    @Test
    fun get12HourDisplay_fromFullDateTimeString_returnsCorrectString() {
        assertEquals("8:30 AM", df.get12HourDisplay("2026-08-04 08:30:00"))
    }

    @Test
    fun getTimeWorked_simpleDay_returnsCorrectHours() {
        val start = "2026-08-04 08:00:00"
        val end = "2026-08-04 16:30:00"
        assertEquals(8.5, df.getTimeWorked(start, end), 0.01)
    }

    @Test
    fun getTimeWorked_crossMidnight_returnsCorrectHours() {
        val start = "2026-08-04 22:00:00"
        val end = "2026-08-05 06:00:00"
        assertEquals(8.0, df.getTimeWorked(start, end), 0.01)
    }

    @Test
    fun getTimeWorked_calendar_crossMidnight_returnsCorrectHours() {
        val start = df.getCalendarFromTime("22:00")
        val end = df.getCalendarFromTime("06:00")
        end.add(Calendar.DAY_OF_YEAR, 1) // Simulate UI adjustment for cross-midnight
        assertEquals(8.0, df.getTimeWorked(start, end), 0.01)
    }

    @Test
    fun roundCalendarTimeUpTo15Minutes_roundsCorrectly() {
        val cal = df.getCalendarFromTime("08:07")
        val rounded = df.roundCalendarTimeUpTo15Minutes(cal)
        assertEquals(15, rounded[Calendar.MINUTE])

        val cal2 = df.getCalendarFromTime("08:46")
        val rounded2 = df.roundCalendarTimeUpTo15Minutes(cal2)
        assertEquals(9, rounded2[Calendar.HOUR_OF_DAY])
        assertEquals(0, rounded2[Calendar.MINUTE])
    }

    @Test
    fun roundCalendarTimeDownTo15Minutes_roundsCorrectly() {
        val cal = df.getCalendarFromTime("08:14")
        val rounded = df.roundCalendarTimeDownTo15Minutes(cal)
        assertEquals(0, rounded[Calendar.MINUTE])

        val cal2 = df.getCalendarFromTime("08:29")
        val rounded2 = df.roundCalendarTimeDownTo15Minutes(cal2)
        assertEquals(15, rounded2[Calendar.MINUTE])
    }

    @Test
    fun addHoursToCalendar_addsCorrectly() {
        val cal = df.getCalendarFromTime("08:00")
        val result = df.addHoursToCalendar(cal, 1.5)
        assertEquals(9, result[Calendar.HOUR_OF_DAY])
        assertEquals(30, result[Calendar.MINUTE])
    }

    @Test
    fun getNextDate_returnsNextDay() {
        assertEquals("2026-08-05", df.getNextDate("2026-08-04"))
        assertEquals("2027-01-01", df.getNextDate("2026-12-31"))
    }

    @Test
    fun getDateTimeStringFromDate_returnsUtcString() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.AUGUST, 4, 20, 0, 0)
        }
        val result = df.getDateTimeStringFromDate(cal.time)
        assertEquals("2026-08-04 20:00:00", result)
    }

    @Test
    fun convertUtcToLocalDisplay_correctlyTranslatesUtcToLocalTime() {
        val utcTimeStr = "2026-08-04 20:00:00"
        val display = df.convertUtcToLocalDisplay(utcTimeStr)

        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.AUGUST, 4, 20, 0, 0)
        }
        val expected = SimpleDateFormat.getDateTimeInstance(
            SimpleDateFormat.MEDIUM,
            SimpleDateFormat.SHORT,
            Locale.getDefault()
        ).format(utcCal.time)

        assertEquals(expected, display)
    }
}