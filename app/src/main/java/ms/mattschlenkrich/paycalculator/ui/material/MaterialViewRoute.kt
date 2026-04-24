package ms.mattschlenkrich.paycalculator.ui.material

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
fun MaterialViewRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    val materialList by if (searchQuery.isEmpty()) {
        workOrderViewModel.materialsList.observeAsState(emptyList())
    } else {
        workOrderViewModel.searchMaterials("%$searchQuery%").observeAsState(emptyList())
    }

    MaterialViewScreen(
        materialList = materialList,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onMaterialClick = { material ->
            mainViewModel.setMaterial(material)
            navController.navigate(Screen.MaterialUpdate.route)
        },
        onBackClick = { navController.popBackStack() }
    )
}