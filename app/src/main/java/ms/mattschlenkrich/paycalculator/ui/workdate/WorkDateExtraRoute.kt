package ms.mattschlenkrich.paycalculator.ui.workdate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.ui.extras.composable.WorkDateExtraScreen

@Composable
fun WorkDateExtraRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    navController: NavController,
    isUpdate: Boolean,
    isCredit: Boolean = false,
    workExtraViewModel: WorkExtraViewModel = viewModel()
) {
    val workDate = mainViewModel.getWorkDateObject() ?: return
    val initialExtra = if (isUpdate) mainViewModel.getWorkDateExtra() else null
    if (isUpdate && initialExtra == null) return
    val employer = mainViewModel.getEmployer() ?: return

    val existingExtras by payDayViewModel.getWorkDateExtras(workDate.workDateId)
        .observeAsState(emptyList())

    val payPeriod = mainViewModel.getPayPeriod()
    val existingPayPeriodExtras by payDayViewModel.getPayPeriodExtras(payPeriod?.payPeriodId ?: 0L)
        .observeAsState(emptyList())

    val defaultExtras by workExtraViewModel.getExtraTypesAndDefByDaily(
        employer.employerId, workDate.wdCutoffDate
    ).observeAsState(emptyList())

    val coroutineScope = rememberCoroutineScope()

    WorkDateExtraScreen(
        initialWorkDate = workDate,
        employerName = employer.employerName,
        initialExtra = initialExtra,
        existingExtras = existingExtras,
        existingPayPeriodExtras = existingPayPeriodExtras,
        defaultExtras = defaultExtras,
        mainViewModel = mainViewModel,
        initialIsCredit = if (isUpdate) initialExtra!!.wdeIsCredit else isCredit,
        onUpdate = { extra ->
            coroutineScope.launch {
                if (isUpdate && extra.workDateExtraId != 0L) {
                    payDayViewModel.updateWorkDateExtra(extra)
                } else {
                    val newExtra = if (extra.workDateExtraId == 0L) {
                        extra.copy(
                            workDateExtraId = ms.mattschlenkrich.paycalculator.common.NumberFunctions()
                                .generateRandomIdAsLong()
                        )
                    } else extra
                    payDayViewModel.insertWorkDateExtra(newExtra)
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
        onCancel = { navController.popBackStack() },
        onValueCalcClick = { initialVal ->
            mainViewModel.setCalculatorInput(initialVal)
            navController.navigate(Screen.Calculator.route)
        }
    )
}