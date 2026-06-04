package ms.mattschlenkrich.paycalculator.ui.extras

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.ui.extras.composable.PayPeriodExtraScreen

@Composable
fun PayPeriodExtraAddRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val payPeriod = mainViewModel.getPayPeriod() ?: return
    val employer = mainViewModel.getEmployer() ?: return

    val existingPayPeriodExtras by payDayViewModel.getPayPeriodExtras(payPeriod.payPeriodId)
        .observeAsState(emptyList())
    val existingWorkDateExtras by payDayViewModel.getWorkDateExtrasPerPay(
        employer.employerId, payPeriod.ppCutoffDate
    ).observeAsState(emptyList())
    val defaultExtras by workExtraViewModel.getExtraTypesAndDefByDaily(
        employer.employerId, payPeriod.ppCutoffDate
    ).observeAsState(emptyList())

    val coroutineScope = rememberCoroutineScope()

    PayPeriodExtraScreen(
        curPayPeriod = payPeriod,
        employerName = employer.employerName,
        initialExtra = null,
        existingPayPeriodExtras = existingPayPeriodExtras,
        existingWorkDateExtras = existingWorkDateExtras,
        defaultExtras = defaultExtras,
        onUpdate = { extra ->
            coroutineScope.launch {
                payDayViewModel.insertPayPeriodExtra(extra)
                navController.popBackStack()
            }
        },
        onDelete = {},
        onCancel = { navController.popBackStack() }
    )
}