package ms.mattschlenkrich.paycalculator.ui.workdate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.ui.extras.composable.WorkDateExtraScreen

@Composable
fun WorkDateExtraRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    navController: NavController,
    isUpdate: Boolean
) {
    val workDate = mainViewModel.getWorkDateObject() ?: return
    val initialExtra = if (isUpdate) mainViewModel.getWorkDateExtra() else null
    if (isUpdate && initialExtra == null) return
    val employer = mainViewModel.getEmployer() ?: return

    val existingExtras by payDayViewModel.getWorkDateExtras(workDate.workDateId)
        .observeAsState(emptyList())

    val coroutineScope = rememberCoroutineScope()

    WorkDateExtraScreen(
        initialWorkDate = workDate,
        employerName = employer.employerName,
        initialExtra = initialExtra,
        existingExtras = existingExtras,
        onUpdate = { extra ->
            coroutineScope.launch {
                if (isUpdate) {
                    payDayViewModel.updateWorkDateExtra(extra)
                } else {
                    payDayViewModel.insertWorkDateExtra(extra)
                }
                navController.popBackStack()
            }
        },
        onDelete = { extra ->
            coroutineScope.launch {
                payDayViewModel.updateWorkDateExtra(
                    extra.copy(
                        wdeIsDeleted = true,
                        wdeUpdateTime = DateFunctions().getCurrentUTCTimeAsString()
                    )
                )
                navController.popBackStack()
            }
        },
        onCancel = { navController.popBackStack() }
    )
}