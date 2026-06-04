@file:Suppress("AssignedValueIsNeverRead")

package ms.mattschlenkrich.paycalculator.ui.areas

import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH

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
import ms.mattschlenkrich.paycalculator.ui.areas.composable.AreaViewScreen
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel

@Composable
fun AreaViewRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.observeAsState()
    val minColumnWidth = settings?.minColumnWidth ?: DEFAULT_MIN_COLUMN_WIDTH
    var searchQuery by remember { mutableStateOf("") }
    val areaList by if (searchQuery.isEmpty()) {
        workOrderViewModel.areasList.observeAsState(emptyList())
    } else {
        workOrderViewModel.searchAreas("%$searchQuery%").observeAsState(emptyList())
    }

    AreaViewScreen(
        areaList = areaList,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onAreaClick = { area ->
            mainViewModel.setAreaId(area.areaId)
            navController.navigate(Screen.AreaUpdate.route)
        },
        minColumnWidth = minColumnWidth
    )
}