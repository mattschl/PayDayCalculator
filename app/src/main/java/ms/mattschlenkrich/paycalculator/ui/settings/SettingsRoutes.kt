package ms.mattschlenkrich.paycalculator.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.ui.settings.composable.SettingsScreen

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
        minColumnWidth = settings?.minColumnWidth ?: DEFAULT_MIN_COLUMN_WIDTH,
        regularStartTime = settings?.regularStartTime ?: "08:30",
        regularEndTime = settings?.regularEndTime ?: "17:00",
        regularDays = settings?.regularDays ?: listOf(2, 3, 4, 5, 6),
        employers = employers,
        onFontSizeChange = { viewModel.updateFontSize(it) },
        onPayPeriodsLimitChange = { viewModel.updatePayPeriodsLimit(it) },
        onIsDarkThemeChange = { viewModel.updateIsDarkTheme(it) },
        onIsSystemThemeChange = { viewModel.updateIsSystemTheme(it) },
        onIsPasswordProtectedChange = { viewModel.updateIsPasswordProtected(it) },
        onPasswordSet = { viewModel.savePassword(it) },
        onPasswordVerify = { viewModel.verifyPassword(it) },
        onDefaultEmployerChange = { viewModel.updateDefaultEmployerId(it) },
        onMinColumnWidthChange = { viewModel.updateMinColumnWidth(it) },
        onRegularStartTimeChange = { viewModel.updateRegularStartTime(it) },
        onRegularEndTimeChange = { viewModel.updateRegularEndTime(it) },
        onRegularDaysChange = { viewModel.updateRegularDays(it) },
        defaultLaborRate = settings?.defaultLaborRate ?: 0.0,
        defaultMarkupRate = settings?.defaultMarkupRate ?: 0.0,
        onDefaultLaborRateChange = { viewModel.updateDefaultLaborRate(it) },
        onDefaultMarkupRateChange = { viewModel.updateDefaultMarkupRate(it) }
    )
}