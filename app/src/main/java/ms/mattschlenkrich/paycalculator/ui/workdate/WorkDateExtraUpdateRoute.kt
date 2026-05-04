package ms.mattschlenkrich.paycalculator.ui.workdate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.ui.extras.WorkDateExtraScreen

@Composable
fun WorkDateExtraUpdateRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val workDate = mainViewModel.getWorkDateObject() ?: return
    val initialExtra = mainViewModel.getWorkDateExtra() ?: return
    val employer = mainViewModel.getEmployer() ?: return

    val existingExtras by payDayViewModel.getWorkDateExtras(workDate.workDateId)
        .observeAsState(emptyList())

    WorkDateExtraScreen(
        initialWorkDate = workDate,
        employerName = employer.employerName,
        initialExtra = initialExtra,
        existingExtras = existingExtras,
        onUpdate = { extra ->
            payDayViewModel.updateWorkDateExtra(extra)
            navController.popBackStack()
        },
        onDelete = { extra ->
            payDayViewModel.updateWorkDateExtra(
                (extra as WorkDateExtras).copy(
                    wdeIsDeleted = true,
                    wdeUpdateTime = DateFunctions().getCurrentUTCTimeAsString()
                )
            )
            navController.popBackStack()
        },
        onCancel = { navController.popBackStack() }
    )
}