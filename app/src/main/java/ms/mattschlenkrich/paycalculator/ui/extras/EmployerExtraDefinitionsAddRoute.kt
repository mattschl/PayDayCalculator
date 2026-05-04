package ms.mattschlenkrich.paycalculator.ui.extras

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefTypeAndEmployer
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel

@Composable
fun EmployerExtraDefinitionsAddRoute(
    mainViewModel: MainViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val curEmployer = mainViewModel.getEmployer()
    val curExtraType = mainViewModel.getWorkExtraType()

    EmployerExtraDefinitionScreen(
        initialDefinitionFull = if (curEmployer != null && curExtraType != null) {
            ExtraDefTypeAndEmployer(
                definition = WorkExtrasDefinitions(
                    workExtraDefId = 0L,
                    weEmployerId = curEmployer.employerId,
                    weExtraTypeId = curExtraType.workExtraTypeId,
                    weValue = 0.0,
                    weIsFixed = true,
                    weEffectiveDate = DateFunctions()
                        .getCurrentDateAsString(),
                    weIsDeleted = false,
                    weUpdateTime = DateFunctions()
                        .getCurrentUTCTimeAsString()
                ),
                employer = curEmployer,
                extraType = curExtraType
            )
        } else null,
        onUpdate = { definition ->
            workExtraViewModel.insertWorkExtraDefinition(definition)
            navController.popBackStack()
        },
        onDelete = { /* Not applicable for Add */ },
        onCancel = { navController.popBackStack() }
    )
}