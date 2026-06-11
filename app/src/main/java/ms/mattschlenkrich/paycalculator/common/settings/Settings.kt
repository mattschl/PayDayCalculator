package ms.mattschlenkrich.paycalculator.common.settings

import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH

data class Settings(
    val fontSize: Float = 16f,
    val payPeriodsLimit: Int = 15,
    val isDarkTheme: Boolean = false,
    val isSystemTheme: Boolean = true,
    val isPasswordProtected: Boolean = false,
    val defaultEmployerId: Long? = null,
    val minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH
)