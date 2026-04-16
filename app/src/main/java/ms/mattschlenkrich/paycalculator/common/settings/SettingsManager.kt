package ms.mattschlenkrich.paycalculator.common.settings

import android.content.Context
import com.google.gson.Gson
import java.io.File

class SettingsManager(private val context: Context) {
    private val gson = Gson()
    private val settingsFile = File(context.filesDir, "settings.json")

    fun saveSettings(settings: Settings) {
        try {
            settingsFile.writeText(gson.toJson(settings))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSettings(): Settings {
        return try {
            if (settingsFile.exists()) {
                gson.fromJson(settingsFile.readText(), Settings::class.java)
            } else {
                Settings()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Settings()
        }
    }
}