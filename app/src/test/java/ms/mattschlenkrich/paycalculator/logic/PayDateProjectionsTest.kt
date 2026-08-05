package ms.mattschlenkrich.paycalculator.logic

import ms.mattschlenkrich.paycalculator.common.INTERVAL_BI_WEEKLY
import ms.mattschlenkrich.paycalculator.common.INTERVAL_MONTHLY
import ms.mattschlenkrich.paycalculator.common.INTERVAL_SEMI_MONTHLY
import ms.mattschlenkrich.paycalculator.common.INTERVAL_WEEKLY
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PayDateProjectionsTest {

    private val projections = PayDateProjections()

    @Test
    fun getCutOffForDate_weekly_fridayPay_0Cutoff() {
        val employer = createEmployer(
            payFrequency = INTERVAL_WEEKLY,
            startDate = "2024-01-01",
            dayOfWeek = "Friday",
            cutoffDaysBefore = 0
        )
        // 2024-08-01 is Thursday. Next Friday is 2024-08-02.
        assertEquals("2024-08-02", projections.getCutOffForDate(employer, "2024-08-01"))
        // 2024-08-02 is Friday. Should be today.
        assertEquals("2024-08-02", projections.getCutOffForDate(employer, "2024-08-02"))
        // 2024-08-03 is Saturday. Next Friday is 2024-08-09.
        assertEquals("2024-08-09", projections.getCutOffForDate(employer, "2024-08-03"))
    }

    @Test
    fun getCutOffForDate_biWeekly_thursdayPay_1Cutoff() {
        val employer = createEmployer(
            payFrequency = INTERVAL_BI_WEEKLY,
            startDate = "2024-01-04", // A Thursday
            dayOfWeek = "Thursday",
            cutoffDaysBefore = 1
        )
        // 2024-08-01 is Thursday. Cutoff is 2024-07-31. 
        // 2024-08-01 is AFTER cutoff 2024-07-31. Next pay is 2024-08-15. Cutoff 2024-08-14.
        assertEquals("2024-08-14", projections.getCutOffForDate(employer, "2024-08-01"))

        // 2024-08-14 is exactly the cutoff.
        assertEquals("2024-08-14", projections.getCutOffForDate(employer, "2024-08-14"))
    }

    @Test
    fun getCutOffForDate_monthly_15th_0Cutoff() {
        val employer = createEmployer(
            payFrequency = INTERVAL_MONTHLY,
            startDate = "2024-01-01",
            mainMonthlyDate = 15,
            cutoffDaysBefore = 0
        )
        // 2024-08-01 -> Next 15th is 2024-08-15
        assertEquals("2024-08-15", projections.getCutOffForDate(employer, "2024-08-01"))
        // 2024-08-16 -> Next 15th is 2024-09-15
        assertEquals("2024-09-15", projections.getCutOffForDate(employer, "2024-08-16"))
    }

    @Test
    fun getCutOffForDate_semiMonthly_15thAndLast_2Cutoff() {
        val employer = createEmployer(
            payFrequency = INTERVAL_SEMI_MONTHLY,
            startDate = "2024-01-01",
            midMonthlyDate = 15,
            mainMonthlyDate = 31,
            cutoffDaysBefore = 2
        )
        // 2024-08-01 -> Next pay 2024-08-15. Cutoff is 2024-08-13.
        assertEquals("2024-08-13", projections.getCutOffForDate(employer, "2024-08-01"))
        // 2024-08-14 -> Next pay 2024-08-31. Cutoff is 2024-08-29.
        assertEquals("2024-08-29", projections.getCutOffForDate(employer, "2024-08-14"))

        // Test end of month handling for shorter months
        // 2024-02-14 -> Next pay 2024-02-15. Cutoff is 2024-02-13. 
        // 14th is AFTER 13th. Next pay is end of Feb (29th). Cutoff 2024-02-27.
        assertEquals("2024-02-27", projections.getCutOffForDate(employer, "2024-02-14"))
    }

    @Test
    fun generateNextCutOff_returnsNextAvailable() {
        val employer = createEmployer(
            payFrequency = INTERVAL_WEEKLY,
            startDate = "2024-01-05", // Friday
            dayOfWeek = "Friday",
            cutoffDaysBefore = 0
        )
        // If most recent cutoff was 2024-08-02, next should be 2024-08-09
        assertEquals("2024-08-09", projections.generateNextCutOff(employer, "2024-08-02"))

        // If no most recent, should return cutoff for today
        // Note: this test is slightly fragile as it depends on LocalDate.now()
        val expected = projections.getCutOffForDate(employer, LocalDate.now().toString())
        assertEquals(expected, projections.generateNextCutOff(employer, ""))
    }

    private fun createEmployer(
        payFrequency: String,
        startDate: String,
        dayOfWeek: String = "Friday",
        cutoffDaysBefore: Int = 0,
        midMonthlyDate: Int = 15,
        mainMonthlyDate: Int = 31
    ) = Employers(
        employerId = 1L,
        employerName = "Test Corp",
        payFrequency = payFrequency,
        startDate = startDate,
        dayOfWeek = dayOfWeek,
        cutoffDaysBefore = cutoffDaysBefore,
        midMonthlyDate = midMonthlyDate,
        mainMonthlyDate = mainMonthlyDate,
        employerIsDeleted = false,
        employerUpdateTime = ""
    )
}