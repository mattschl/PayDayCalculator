package ms.mattschlenkrich.paycalculator.ui.jobspec

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

@Composable
fun JobSpecViewRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    val jobSpecList by if (searchQuery.isEmpty()) {
        workOrderViewModel.getJobSpecsAll().observeAsState(emptyList())
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
        onBackClick = { navController.popBackStack() }
    )
}