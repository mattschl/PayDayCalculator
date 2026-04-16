package ms.mattschlenkrich.paycalculator.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ms.mattschlenkrich.paycalculator.common.settings.Settings
import ms.mattschlenkrich.paycalculator.common.settings.SettingsManager

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager(application)
    private val _settings = MutableLiveData<Settings>()
    val settings: LiveData<Settings> = _settings

    init {
        _settings.value = settingsManager.loadSettings()
    }

    fun updateFontSize(size: Float) {
        val newSettings = _settings.value?.copy(fontSize = size) ?: Settings(fontSize = size)
        _settings.value = newSettings
        settingsManager.saveSettings(newSettings)
    }
}