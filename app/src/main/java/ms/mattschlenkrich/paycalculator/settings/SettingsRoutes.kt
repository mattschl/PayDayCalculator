package ms.mattschlenkrich.paycalculator.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ms.mattschlenkrich.paycalculator.data.MainViewModel

@Composable
fun SettingsRoute(
    mainViewModel: MainViewModel,
    navController: NavHostController,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.observeAsState()

    SettingsScreen(
        fontSize = settings?.fontSize ?: 16f,
        onFontSizeChange = { viewModel.updateFontSize(it) }
    )
}