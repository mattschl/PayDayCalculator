package ms.mattschlenkrich.paycalculator.ui.extras

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.ui.extras.composable.WorkExtraTypeScreen

@Composable
fun WorkExtraTypeRoute(
    mainViewModel: MainViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController,
    isUpdate: Boolean
) {
    val curEmployer = mainViewModel.getEmployer()
    val curExtraType = if (isUpdate) mainViewModel.getWorkExtraType() else null

    if (curEmployer != null && (!isUpdate || curExtraType != null)) {
        val extraTypeList by workExtraViewModel.getExtraDefTypes(curEmployer.employerId)
            .observeAsState(emptyList())

        WorkExtraTypeScreen(
            initialEmployer = curEmployer,
            initialExtraType = curExtraType,
            existingExtraTypes = extraTypeList,
            onUpdate = { extra ->
                if (isUpdate) {
                    workExtraViewModel.updateWorkExtraType(extra)
                    mainViewModel.setWorkExtraType(extra)
                    navController.popBackStack()
                } else {
                    workExtraViewModel.insertWorkExtraType(extra)
                    mainViewModel.setWorkExtraType(extra)
                    mainViewModel.setSelectedTopLevelIndex(4)
                    navController.popBackStack(Screen.MainPager.route, inclusive = false)
                }
            },
            onDelete = { extraToDelete ->
                workExtraViewModel.updateWorkExtraType(
                    extraToDelete.copy(
                        wetIsDeleted = true,
                        wetUpdateTime = DateFunctions().getCurrentUTCTimeAsString()
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