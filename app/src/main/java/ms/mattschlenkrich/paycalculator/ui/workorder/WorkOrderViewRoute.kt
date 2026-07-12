package ms.mattschlenkrich.paycalculator.ui.workorder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel
import ms.mattschlenkrich.paycalculator.ui.workorder.composable.WorkOrderViewScreen

@Composable
fun WorkOrderViewRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.observeAsState()
    val minColumnWidth = settings?.minColumnWidth ?: DEFAULT_MIN_COLUMN_WIDTH
    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    var selectedEmployer by rememberSaveable { mutableStateOf(mainViewModel.getEmployer()) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val workOrders by if (selectedEmployer != null) {
        if (searchQuery.isEmpty()) {
            workOrderViewModel.getWorkOrdersByEmployerId(selectedEmployer!!.employerId)
                .observeAsState(emptyList())
        } else {
            workOrderViewModel.searchWorkOrders(selectedEmployer!!.employerId, "%$searchQuery%")
                .observeAsState(emptyList())
        }
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    WorkOrderViewScreen(
        employers = employers,
        selectedEmployer = selectedEmployer,
        onEmployerSelected = {
            selectedEmployer = it
            mainViewModel.setEmployer(it)
        },
        onAddNewEmployerClick = { navController.navigate(Screen.EmployerAdd.route) },
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onResetSearchClick = { searchQuery = "" },
        workOrders = workOrders,
        onWorkOrderClick = {
            mainViewModel.setWorkOrder(it)
            navController.navigate(Screen.WorkOrderUpdate.route)
        },
        onAddNewWorkOrderClick = { navController.navigate(Screen.WorkOrderAdd.route) },
        minColumnWidth = minColumnWidth
    )
}