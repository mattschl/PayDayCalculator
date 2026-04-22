package ms.mattschlenkrich.paycalculator.ui.extras

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel

@Composable
fun WorkExtraTypeAddRoute(
    mainViewModel: MainViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val curEmployer = mainViewModel.getEmployer()
    if (curEmployer != null) {
        val extraTypeList by workExtraViewModel.getExtraDefTypes(curEmployer.employerId)
            .observeAsState(emptyList())

        WorkExtraTypeScreen(
            initialEmployer = curEmployer,
            initialExtraType = null,
            existingExtraTypes = extraTypeList,
            onUpdate = { newExtraType: WorkExtraTypes ->
                workExtraViewModel.insertWorkExtraType(newExtraType)
                mainViewModel.setWorkExtraType(newExtraType)
                navController.navigate(Screen.Extras.route) {
                    popUpTo(Screen.WorkExtraTypeAdd.route) { inclusive = true }
                }
            },
            onDelete = {}
        )
    } else {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }
}