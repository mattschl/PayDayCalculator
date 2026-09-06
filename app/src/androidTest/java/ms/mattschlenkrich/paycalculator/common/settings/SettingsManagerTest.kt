package ms.mattschlenkrich.paycalculator.common.settings

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SettingsManagerTest {

    private lateinit var settingsManager: SettingsManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        // Ensure clean state by deleting the settings file if it exists
        File(context.filesDir, "settings.json").delete()
        settingsManager = SettingsManager(context)
    }

    @Test
    fun testSaveAndLoadSettings_PreservesData() {
        val originalSettings = Settings(
            isDarkTheme = true,
            fontSize = 20f,
            minColumnWidth = 400,
            isPasswordProtected = true,
            defaultLaborRate = 75.0,
            defaultMarkupRate = 25.0
        )

        settingsManager.saveSettings(originalSettings)
        val loadedSettings = settingsManager.loadSettings()

        assertEquals(originalSettings.isDarkTheme, loadedSettings.isDarkTheme)
        assertEquals(originalSettings.fontSize, loadedSettings.fontSize, 0.1f)
        assertEquals(originalSettings.minColumnWidth, loadedSettings.minColumnWidth)
        assertEquals(originalSettings.isPasswordProtected, loadedSettings.isPasswordProtected)
        assertEquals(originalSettings.defaultLaborRate, loadedSettings.defaultLaborRate, 0.01)
        assertEquals(originalSettings.defaultMarkupRate, loadedSettings.defaultMarkupRate, 0.01)
    }

    @Test
    fun testLoadSettings_ReturnsDefault_WhenFileMissing() {
        val loadedSettings = settingsManager.loadSettings()

        // Check default values from Settings class
        // (Assuming defaults: isDarkTheme=false, fontSize=16f, minColumnWidth=360)
        assertFalse(loadedSettings.isDarkTheme)
        assertEquals(16f, loadedSettings.fontSize, 0.1f)
        assertEquals(360, loadedSettings.minColumnWidth)
    }
}