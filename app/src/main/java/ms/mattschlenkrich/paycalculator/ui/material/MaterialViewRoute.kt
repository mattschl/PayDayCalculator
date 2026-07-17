package ms.mattschlenkrich.paycalculator.ui.material

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
import ms.mattschlenkrich.paycalculator.data.viewmodel.MaterialViewModel
import ms.mattschlenkrich.paycalculator.ui.material.composable.MaterialViewScreen
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel

@Composable
fun MaterialViewRoute(
    mainViewModel: MainViewModel,
    materialViewModel: MaterialViewModel,
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.observeAsState()
    val minColumnWidth = settings?.minColumnWidth ?: DEFAULT_MIN_COLUMN_WIDTH
    var searchQuery by remember { mutableStateOf("") }
    val materialList by materialViewModel.searchMaterials("%$searchQuery%")
        .observeAsState(emptyList())

    MaterialViewScreen(
        materialList = materialList,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onMaterialClick = { material ->
            mainViewModel.setMaterial(material)
            navController.navigate(Screen.MaterialUpdate.route)
        },
        minColumnWidth = minColumnWidth
    )
}