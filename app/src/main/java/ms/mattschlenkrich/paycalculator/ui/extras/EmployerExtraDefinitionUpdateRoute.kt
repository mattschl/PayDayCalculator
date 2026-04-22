package ms.mattschlenkrich.paycalculator.ui.extras

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel

@Composable
fun EmployerExtraDefinitionUpdateRoute(
    mainViewModel: MainViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val definitionFull = mainViewModel.getExtraDefinitionFull()

    EmployerExtraDefinitionScreen(
        initialDefinitionFull = definitionFull,
        onUpdate = { definition ->
            workExtraViewModel.updateWorkExtraDefinition(definition)
            navController.popBackStack()
        },
        onDelete = { definition ->
            workExtraViewModel.deleteWorkExtraDefinition(
                definition.workExtraDefId,
                DateFunctions()
                    .getCurrentUTCTimeAsString()
            )
            navController.popBackStack()
        },
        onCancel = { navController.popBackStack() }
    )
}