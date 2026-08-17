package ms.mattschlenkrich.paycalculator.ui.extras

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefTypeAndEmployer
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.ui.extras.composable.EmployerExtraDefinitionScreen

@Composable
fun EmployerExtraDefinitionRoute(
    mainViewModel: MainViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController,
    isUpdate: Boolean
) {
    val df = DateFunctions()
    val initialDefinitionFull = if (isUpdate) {
        mainViewModel.getExtraDefinitionFull()
    } else {
        val curEmployer = mainViewModel.getEmployer()
        val curExtraType = mainViewModel.getWorkExtraType()
        if (curEmployer != null && curExtraType != null) {
            ExtraDefTypeAndEmployer(
                definition = WorkExtrasDefinitions(
                    workExtraDefId = 0L,
                    weEmployerId = curEmployer.employerId,
                    weExtraTypeId = curExtraType.workExtraTypeId,
                    weValue = 0.0,
                    weIsFixed = true,
                    weEffectiveDate = df.getCurrentDateAsString(),
                    weIsDeleted = false,
                    weUpdateTime = df.getCurrentUTCTimeAsString()
                ),
                employer = curEmployer,
                extraType = curExtraType
            )
        } else null
    }

    EmployerExtraDefinitionScreen(
        initialDefinitionFull = initialDefinitionFull,
        onUpdate = { definition ->
            if (isUpdate) {
                workExtraViewModel.updateWorkExtraDefinition(definition)
            } else {
                workExtraViewModel.insertWorkExtraDefinition(definition)
            }
            navController.popBackStack()
        },
        onDelete = { definition ->
            workExtraViewModel.deleteWorkExtraDefinition(
                definition.workExtraDefId,
                df.getCurrentUTCTimeAsString()
            )
            navController.popBackStack()
        },
        onCancel = { navController.popBackStack() }
    )
}