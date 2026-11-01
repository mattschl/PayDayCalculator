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

    @Test
    fun testSettings_defaultVerticalSpacingIs6() {
        val settings = Settings()
        assertEquals(6f, settings.defaultVerticalSpacing, 0.001f)
    }

    @Test
    fun testSettings_customVerticalSpacing() {
        val settings = Settings(defaultVerticalSpacing = 10f)
        assertEquals(10f, settings.defaultVerticalSpacing, 0.001f)
    }
}