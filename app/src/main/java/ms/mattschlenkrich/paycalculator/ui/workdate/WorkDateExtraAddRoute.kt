package ms.mattschlenkrich.paycalculator.ui.workdate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.ui.extras.composable.WorkDateExtraScreen

@Composable
fun WorkDateExtraAddRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    navController: NavController
) {
    val workDate = mainViewModel.getWorkDateObject() ?: return
    val employer = mainViewModel.getEmployer() ?: return

    val existingExtras by payDayViewModel.getWorkDateExtras(workDate.workDateId)
        .observeAsState(emptyList())

    val coroutineScope = rememberCoroutineScope()

    WorkDateExtraScreen(
        initialWorkDate = workDate,
        employerName = employer.employerName,
        initialExtra = null,
        existingExtras = existingExtras,
        onUpdate = { extra ->
            coroutineScope.launch {
                payDayViewModel.insertWorkDateExtra(extra)
                navController.popBackStack()
            }
        },
        onDelete = {},
        onCancel = { navController.popBackStack() }
    )
}