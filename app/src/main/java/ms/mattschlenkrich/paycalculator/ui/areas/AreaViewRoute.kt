package ms.mattschlenkrich.paycalculator.ui.areas

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
fun AreaViewRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
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
        onBackClick = { navController.popBackStack() }
    )
}