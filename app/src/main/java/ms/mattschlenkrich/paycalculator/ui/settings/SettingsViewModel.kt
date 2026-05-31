package ms.mattschlenkrich.paycalculator.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ms.mattschlenkrich.paycalculator.common.security.AuthResult
import ms.mattschlenkrich.paycalculator.common.security.SecurityManager
import ms.mattschlenkrich.paycalculator.common.settings.Settings
import ms.mattschlenkrich.paycalculator.common.settings.SettingsManager

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager(application)
    private val securityManager = SecurityManager(application)
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

    fun updatePayPeriodsLimit(limit: Int) {
        val newSettings = _settings.value?.copy(payPeriodsLimit = limit) ?: Settings(payPeriodsLimit = limit)
        _settings.value = newSettings
        settingsManager.saveSettings(newSettings)
    }

    fun updateIsDarkTheme(isDark: Boolean) {
        val newSettings = _settings.value?.copy(isDarkTheme = isDark, isSystemTheme = false)
            ?: Settings(isDarkTheme = isDark, isSystemTheme = false)
        _settings.value = newSettings
        settingsManager.saveSettings(newSettings)
    }

    fun updateIsSystemTheme(isSystem: Boolean) {
        val newSettings =
            _settings.value?.copy(isSystemTheme = isSystem) ?: Settings(isSystemTheme = isSystem)
        _settings.value = newSettings
        settingsManager.saveSettings(newSettings)
    }

    fun updateIsPasswordProtected(isProtected: Boolean) {
        val newSettings = _settings.value?.copy(isPasswordProtected = isProtected)
            ?: Settings(isPasswordProtected = isProtected)
        _settings.value = newSettings
        settingsManager.saveSettings(newSettings)
        if (!isProtected) {
            securityManager.clearPassword()
        }
    }

    fun savePassword(password: String) {
        securityManager.savePassword(password)
    }

    fun verifyPassword(password: String): AuthResult {
        return securityManager.verifyPassword(password)
    }

    fun isPasswordSet(): Boolean {
        return securityManager.isPasswordSet()
    }

    fun updateDefaultEmployerId(id: Long?) {
        val newSettings =
            _settings.value?.copy(defaultEmployerId = id) ?: Settings(defaultEmployerId = id)
        _settings.value = newSettings
        settingsManager.saveSettings(newSettings)
    }
}