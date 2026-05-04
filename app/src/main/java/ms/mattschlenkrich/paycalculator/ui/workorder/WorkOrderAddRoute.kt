package ms.mattschlenkrich.paycalculator.ui.workorder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel

@Composable
fun WorkOrderAddRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    val currentEmployer = mainViewModel.getEmployer()

    var selectedEmployer by remember {
        mutableStateOf<Employers?>(
            currentEmployer
        )
    }
    val initialWoNumber = mainViewModel.getWorkOrderNumber() ?: ""
    var woNumber by remember { mutableStateOf(initialWoNumber) }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    WorkOrderAddScreen(
        employers = employers,
        selectedEmployer = selectedEmployer,
        onEmployerSelected = { selectedEmployer = it },
        fixedEmployerName = currentEmployer?.employerName,
        woNumber = woNumber,
        onWoNumberChange = { woNumber = it },
        address = address,
        onAddressChange = { address = it },
        description = description,
        onDescriptionChange = { description = it },
        onDoneClick = {
            val employerId = selectedEmployer?.employerId
            if (employerId != null && woNumber.isNotBlank()) {
                coroutineScope.launch {
                    val newWo = WorkOrder(
                        nf.generateRandomIdAsLong(),
                        woNumber.trim(),
                        employerId,
                        address.trim(),
                        description.trim(),
                        false,
                        df.getCurrentUTCTimeAsString()
                    )
                    workOrderViewModel.insertWorkOrder(newWo)
                    mainViewModel.setWorkOrderNumber(null)
                    navController.popBackStack()
                }
            }
        },
        onBackClick = {
            mainViewModel.setWorkOrderNumber(null)
            navController.popBackStack()
        }
    )
}