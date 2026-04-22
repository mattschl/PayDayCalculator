package ms.mattschlenkrich.paycalculator.ui.workorder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

@Composable
fun WorkOrderViewRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    var selectedEmployer by remember { mutableStateOf(mainViewModel.getEmployer()) }
    var searchQuery by remember { mutableStateOf("") }

    val workOrders by if (selectedEmployer != null) {
        if (searchQuery.isEmpty()) {
            workOrderViewModel.getWorkOrdersByEmployerId(selectedEmployer!!.employerId)
                .observeAsState(emptyList())
        } else {
            workOrderViewModel.searchWorkOrders(selectedEmployer!!.employerId, "%$searchQuery%")
                .observeAsState(emptyList())
        }
    } else {
        remember { mutableStateOf(emptyList<WorkOrder>()) }
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
        onBackClick = { navController.popBackStack() }
    )
}