package ms.mattschlenkrich.paycalculator.ui.workorder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.ui.workorder.composable.WorkOrderLookupScreen

@Composable
fun WorkOrderLookupRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val employer = mainViewModel.getEmployer()
    var searchQuery by remember { mutableStateOf("") }
    val workOrders by if (employer != null) {
        workOrderViewModel.searchWorkOrders(employer.employerId, "%$searchQuery%")
            .observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList<WorkOrder>()) }
    }

    WorkOrderLookupScreen(
        employer = employer,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        workOrders = workOrders,
        onWorkOrderSelected = { wo ->
            mainViewModel.setWorkOrder(wo)
            navController.popBackStack()
        },
        onBackClick = { navController.popBackStack() }
    )
}