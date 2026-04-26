package ms.mattschlenkrich.paycalculator.ui.extras

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel

@Composable
fun ExtraRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
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
        onDeleteExtraDefinition = { definition ->
            workExtraViewModel.deleteWorkExtraDefinition(
                definition.definition.workExtraDefId,
                DateFunctions()
                    .getCurrentUTCTimeAsString()
            )
        }
    )
}