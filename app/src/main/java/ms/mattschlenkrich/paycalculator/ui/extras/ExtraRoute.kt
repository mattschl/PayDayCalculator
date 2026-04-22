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
        employerViewModel = employerViewModel,
        workExtraViewModel = workExtraViewModel,
        initialEmployer = mainViewModel.getEmployer(),
        initialExtraType = mainViewModel.getWorkExtraType(),
        onAddExtraDefinition = { employer, extraType ->
            mainViewModel.setEmployer(employer)
            mainViewModel.setWorkExtraType(extraType)
            navController.navigate(Screen.EmployerExtraDefinitionsAdd.route)
        },
        onUpdateExtraDefinition = { definition ->
            mainViewModel.setEmployer(definition.employer)
            mainViewModel.setExtraDefinitionFull(definition)
            navController.navigate(Screen.EmployerExtraDefinitionUpdate.route)
        },
        onUpdateExtraType = { employer, extraType ->
            mainViewModel.setEmployer(employer)
            mainViewModel.setWorkExtraType(extraType)
            navController.navigate(Screen.WorkExtraTypeUpdate.route)
        },
        onAddNewEmployer = {
            navController.navigate(Screen.EmployerAdd.route)
        },
        onAddNewExtraType = { employer ->
            mainViewModel.setEmployer(employer)
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