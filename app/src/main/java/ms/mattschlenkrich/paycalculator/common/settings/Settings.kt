package ms.mattschlenkrich.paycalculator.common.settings

data class Settings(
    val fontSize: Float = 16f,
    val payPeriodsLimit: Int = 15,
    val isDarkTheme: Boolean = false,
    val isSystemTheme: Boolean = true
)