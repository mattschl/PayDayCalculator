package ms.mattschlenkrich.paycalculator.ui.workperformed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.ui.workperformed.composable.WorkPerformedViewScreen

@Composable
fun WorkPerformedViewRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.observeAsState()
    val minColumnWidth = settings?.minColumnWidth ?: DEFAULT_MIN_COLUMN_WIDTH
    var searchQuery by remember { mutableStateOf("") }
    val workPerformedList by if (searchQuery.isEmpty()) {
        workOrderViewModel.workPerformedAll.observeAsState(emptyList())
    } else {
        workOrderViewModel.searchFromWorkPerformed("%$searchQuery%").observeAsState(emptyList())
    }

    WorkPerformedViewScreen(
        workPerformedList = workPerformedList,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onWorkPerformedClick = { wp ->
            mainViewModel.setWorkPerformedId(wp.workPerformedId)
            navController.navigate(Screen.WorkPerformedUpdate.route)
        },
        minColumnWidth = minColumnWidth
    )
}