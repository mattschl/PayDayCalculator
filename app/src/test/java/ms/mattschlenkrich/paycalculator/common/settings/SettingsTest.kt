package ms.mattschlenkrich.paycalculator.common.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsTest {

    @Test
    fun testSettings_defaultJobDescriptionCharLimitIs15() {
        val settings = Settings()
        assertEquals(15, settings.jobDescriptionCharLimit)
    }

    @Test
    fun testSettings_customJobDescriptionCharLimit() {
        val settings = Settings(jobDescriptionCharLimit = 25)
        assertEquals(25, settings.jobDescriptionCharLimit)
    }
}