package ms.mattschlenkrich.paycalculator.ui.sync

import ms.mattschlenkrich.paycalculator.common.DateFunctions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class SyncWarningTest {

    private val df = DateFunctions()

    @Test
    fun testGetDaysDifferenceFromUtc_nullOrBlank_returnsMaxLong() {
        assertEquals(Long.MAX_VALUE, df.getDaysDifferenceFromUtc(null))
        assertEquals(Long.MAX_VALUE, df.getDaysDifferenceFromUtc(""))
        assertEquals(Long.MAX_VALUE, df.getDaysDifferenceFromUtc("   "))
    }

    @Test
    fun testGetDaysDifferenceFromUtc_recentDate_returnsZeroDays() {
        val nowUtc = df.getCurrentUTCTimeAsString()
        assertEquals(0L, df.getDaysDifferenceFromUtc(nowUtc))
    }

    @Test
    fun testGetDaysDifferenceFromUtc_4DaysAgo_returnsFourDays() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            add(Calendar.DAY_OF_YEAR, -4)
        }
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val fourDaysAgoStr = formatter.format(cal.time)

        val diffDays = df.getDaysDifferenceFromUtc(fourDaysAgoStr)
        assertTrue(diffDays >= 3L)
    }

    @Test
    fun testSyncWarningInfoDataModel() {
        val info = SyncWarningInfo(
            type = SyncWarningType.STALE_SYNC,
            daysSinceLastSync = 5,
            thresholdDays = 3
        )
        assertEquals(SyncWarningType.STALE_SYNC, info.type)
        assertEquals(5L, info.daysSinceLastSync)
        assertEquals(3, info.thresholdDays)
    }
}