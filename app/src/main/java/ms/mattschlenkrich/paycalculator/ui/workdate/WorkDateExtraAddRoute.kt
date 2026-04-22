package ms.mattschlenkrich.paycalculator.ui.workdate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.ui.extras.WorkDateExtraScreen

@Composable
fun WorkDateExtraAddRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val workDate = mainViewModel.getWorkDateObject() ?: return
    val employer = mainViewModel.getEmployer() ?: return

    val existingExtras by payDayViewModel.getWorkDateExtras(workDate.workDateId)
        .observeAsState(emptyList())

    WorkDateExtraScreen(
        initialWorkDate = workDate,
        employerName = employer.employerName,
        initialExtra = null,
        existingExtras = existingExtras,
        onUpdate = { extra ->
            payDayViewModel.insertWorkDateExtra(extra)
            navController.popBackStack()
        },
        onDelete = {},
        onCancel = { navController.popBackStack() }
    )
}