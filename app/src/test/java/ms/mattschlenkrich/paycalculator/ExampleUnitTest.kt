package ms.mattschlenkrich.paycalculator

import ms.mattschlenkrich.paycalculator.common.PayDayFrequencies
import ms.mattschlenkrich.paycalculator.common.PayRateBasedOn
import ms.mattschlenkrich.paycalculator.common.TaxBasedOn
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Local unit tests verifying application domain constants and enums.
 */
class ExampleUnitTest {

    @Test
    fun testPayRateBasedOnEnumValues() {
        assertEquals(0, PayRateBasedOn.HOURLY.value)
        assertEquals(1, PayRateBasedOn.DAILY.value)
        assertEquals(2, PayRateBasedOn.WEEKLY.value)
        assertEquals(3, PayRateBasedOn.BI_WEEKLY.value)
        assertEquals(4, PayRateBasedOn.MONTHLY.value)
    }

    @Test
    fun testTimeWorkedTypesEnumValues() {
        assertEquals(0, TimeWorkedTypes.BREAK.value)
        assertEquals(1, TimeWorkedTypes.REG_HOURS.value)
        assertEquals(2, TimeWorkedTypes.OT_HOURS.value)
        assertEquals(3, TimeWorkedTypes.DBL_OT_HOURS.value)
    }

    @Test
    fun testTaxBasedOnEnumValues() {
        assertEquals(0, TaxBasedOn.TIME_WORKED_ONLY.value)
        assertEquals(1, TaxBasedOn.TIME_WORK_AND_STATS.value)
        assertEquals(2, TaxBasedOn.TIME_WORKED_STATS_AND_EXTRAS.value)
    }

    @Test
    fun testPayDayFrequenciesFindByString() {
        assertEquals(PayDayFrequencies.WEEKLY, PayDayFrequencies.findByString("Weekly"))
        assertEquals(PayDayFrequencies.BI_WEEKLY, PayDayFrequencies.findByString("Bi-Weekly"))
        assertEquals(PayDayFrequencies.SEMI_MONTHLY, PayDayFrequencies.findByString("Semi-Monthly"))
        assertEquals(PayDayFrequencies.MONTHLY, PayDayFrequencies.findByString("Monthly"))
        // Fallback default
        assertEquals(PayDayFrequencies.BI_WEEKLY, PayDayFrequencies.findByString("Unknown"))
    }
}