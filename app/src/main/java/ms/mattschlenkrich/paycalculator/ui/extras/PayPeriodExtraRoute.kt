package ms.mattschlenkrich.paycalculator.ui.extras

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.ui.extras.composable.PayPeriodExtraScreen

@Composable
fun PayPeriodExtraRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController,
    isUpdate: Boolean,
    isCredit: Boolean = false,
) {
    val payPeriod = mainViewModel.getPayPeriod() ?: return
    val employer = mainViewModel.getEmployer() ?: return
    val initialExtra = if (isUpdate) mainViewModel.getPayPeriodExtra() else null
    if (isUpdate && initialExtra == null) return

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
        initialExtra = initialExtra,
        existingPayPeriodExtras = existingPayPeriodExtras,
        existingWorkDateExtras = existingWorkDateExtras,
        defaultExtras = defaultExtras,
        onUpdate = { extra ->
            coroutineScope.launch {
                if (isUpdate) {
                    payDayViewModel.updatePayPeriodExtra(extra)
                } else {
                    payDayViewModel.insertPayPeriodExtra(extra)
                }
                navController.popBackStack()
            }
        },
        onDelete = { extra ->
            coroutineScope.launch {
                payDayViewModel.updatePayPeriodExtra(
                    extra.copy(
                        ppeIsDeleted = true,
                        ppeUpdateTime = DateFunctions().getCurrentUTCTimeAsString()
                    )
                )
                navController.popBackStack()
            }
        },
        onCancel = { navController.popBackStack() },
        initialIsCredit = if (isUpdate) initialExtra!!.ppeIsCredit else isCredit
    )
}