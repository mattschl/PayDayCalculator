package ms.mattschlenkrich.paycalculator.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.observeAsState()

    SettingsScreen(
        fontSize = settings?.fontSize ?: 16f,
        payPeriodsLimit = settings?.payPeriodsLimit ?: 15,
        isDarkTheme = settings?.isDarkTheme ?: false,
        isSystemTheme = settings?.isSystemTheme ?: true,
        isPasswordProtected = settings?.isPasswordProtected ?: false,
        isPasswordSet = viewModel.isPasswordSet(),
        onFontSizeChange = { viewModel.updateFontSize(it) },
        onPayPeriodsLimitChange = { viewModel.updatePayPeriodsLimit(it) },
        onIsDarkThemeChange = { viewModel.updateIsDarkTheme(it) },
        onIsSystemThemeChange = { viewModel.updateIsSystemTheme(it) },
        onIsPasswordProtectedChange = { viewModel.updateIsPasswordProtected(it) },
        onPasswordSet = { viewModel.savePassword(it) }
    )
}