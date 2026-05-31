package ms.mattschlenkrich.paycalculator.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = viewModel(),
    employerViewModel: EmployerViewModel
) {
    val settings by viewModel.settings.observeAsState()
    val employers by employerViewModel.getEmployers().observeAsState(emptyList())

    SettingsScreen(
        fontSize = settings?.fontSize ?: 16f,
        payPeriodsLimit = settings?.payPeriodsLimit ?: 15,
        isDarkTheme = settings?.isDarkTheme ?: false,
        isSystemTheme = settings?.isSystemTheme ?: true,
        isPasswordProtected = settings?.isPasswordProtected ?: false,
        isPasswordSet = viewModel.isPasswordSet(),
        defaultEmployerId = settings?.defaultEmployerId,
        employers = employers,
        onFontSizeChange = { viewModel.updateFontSize(it) },
        onPayPeriodsLimitChange = { viewModel.updatePayPeriodsLimit(it) },
        onIsDarkThemeChange = { viewModel.updateIsDarkTheme(it) },
        onIsSystemThemeChange = { viewModel.updateIsSystemTheme(it) },
        onIsPasswordProtectedChange = { viewModel.updateIsPasswordProtected(it) },
        onPasswordSet = { viewModel.savePassword(it) },
        onDefaultEmployerChange = { viewModel.updateDefaultEmployerId(it) }
    )
}