package ms.mattschlenkrich.paycalculator.ui.extras

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel

@Composable
fun WorkExtraTypeUpdateRoute(
    mainViewModel: MainViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val curEmployer = mainViewModel.getEmployer()
    val curExtraType = mainViewModel.getWorkExtraType()
    if (curEmployer != null && curExtraType != null) {
        val extraTypeList by workExtraViewModel.getExtraDefTypes(curEmployer.employerId)
            .observeAsState(emptyList())

        WorkExtraTypeScreen(
            initialEmployer = curEmployer,
            initialExtraType = curExtraType,
            existingExtraTypes = extraTypeList,
            onUpdate = { updatedExtraType: WorkExtraTypes ->
                workExtraViewModel.updateWorkExtraType(updatedExtraType)
                mainViewModel.setWorkExtraType(updatedExtraType)
                navController.popBackStack()
            },
            onDelete = { extraTypeToDelete: WorkExtraTypes ->
                workExtraViewModel.updateWorkExtraType(
                    WorkExtraTypes(
                        extraTypeToDelete.workExtraTypeId,
                        extraTypeToDelete.wetName,
                        extraTypeToDelete.wetEmployerId,
                        extraTypeToDelete.wetAppliesTo,
                        extraTypeToDelete.wetAttachTo,
                        extraTypeToDelete.wetIsCredit,
                        extraTypeToDelete.wetIsDefault,
                        true,
                        DateFunctions().getCurrentUTCTimeAsString()
                    )
                )
                navController.popBackStack()
            }
        )
    } else {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }
}