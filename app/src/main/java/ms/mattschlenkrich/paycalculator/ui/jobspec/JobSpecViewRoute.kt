@file:Suppress("AssignedValueIsNeverRead")

package ms.mattschlenkrich.paycalculator.ui.jobspec

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
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.ui.jobspec.composable.JobSpecViewScreen
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel

@Composable
fun JobSpecViewRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.observeAsState()
    val minColumnWidth = settings?.minColumnWidth ?: DEFAULT_MIN_COLUMN_WIDTH
    var searchQuery by remember { mutableStateOf("") }
    val jobSpecList by if (searchQuery.isEmpty()) {
        workOrderViewModel.jobSpecsAll.observeAsState(emptyList())
    } else {
        workOrderViewModel.searchJobSpecs("%$searchQuery%").observeAsState(emptyList())
    }

    JobSpecViewScreen(
        jobSpecList = jobSpecList,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onJobSpecClick = { js ->
            mainViewModel.setJobSpecId(js.jobSpecId)
            navController.navigate(Screen.JobSpecUpdate.route)
        },
        minColumnWidth = minColumnWidth
    )
}