package ms.mattschlenkrich.paycalculator.ui.extras

import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.ui.extras.composable.EmployerExtraDefinitionsScreen
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel

@Composable
fun ExtraRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.observeAsState()
    val minColumnWidth = settings?.minColumnWidth ?: DEFAULT_MIN_COLUMN_WIDTH

    EmployerExtraDefinitionsScreen(
        mainViewModel = mainViewModel,
        employerViewModel = employerViewModel,
        workExtraViewModel = workExtraViewModel,
        onAddExtraDefinition = { _, _ ->
            navController.navigate(Screen.EmployerExtraDefinitionsAdd.route)
        },
        onUpdateExtraDefinition = { definition ->
            mainViewModel.setEmployer(definition.employer)
            mainViewModel.setExtraDefinitionFull(definition)
            navController.navigate(Screen.EmployerExtraDefinitionUpdate.route)
        },
        onUpdateExtraType = { _, _ ->
            navController.navigate(Screen.WorkExtraTypeUpdate.route)
        },
        onAddNewEmployer = {
            navController.navigate(Screen.EmployerAdd.route)
        },
        onAddNewExtraType = { _ ->
            navController.navigate(Screen.WorkExtraTypeAdd.route)
        },
        minColumnWidth = minColumnWidth
    )
}