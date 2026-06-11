@file:Suppress("AssignedValueIsNeverRead")

package ms.mattschlenkrich.paycalculator.ui.workorder

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.StringFunctions
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.ui.workorder.composable.WorkOrderAddScreen

@Composable
fun WorkOrderAddRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val sf = remember { StringFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    val currentEmployer = mainViewModel.getEmployer()

    var selectedEmployer by remember {
        mutableStateOf(
            currentEmployer
        )
    }
    val initialWoNumber = mainViewModel.getWorkOrderNumber() ?: ""
    var woNumber by remember { mutableStateOf(initialWoNumber) }
    var address by remember { mutableStateOf("") }
    val addressSuggestions by if (selectedEmployer != null) {
        workOrderViewModel.getUniqueAddresses(selectedEmployer!!.employerId)
            .observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }
    var description by remember { mutableStateOf("") }

    var woNumberError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val errorLabel = stringResource(R.string.error_)
    val noEmployerError = stringResource(R.string.the_work_order_must_have_an_employer)
    val noNumberError = stringResource(R.string.the_work_order_must_have_a_number)
    val noAddressError = stringResource(R.string.the_work_order_must_have_an_address)
    val noDescriptionError = stringResource(R.string.the_work_order_must_have_a_description)

    WorkOrderAddScreen(
        employers = employers,
        selectedEmployer = selectedEmployer,
        onEmployerSelected = { selectedEmployer = it },
        fixedEmployerName = currentEmployer?.employerName,
        woNumber = woNumber,
        onWoNumberChange = {
            woNumber = it
            woNumberError = false
        },
        address = address,
        onAddressChange = {
            address = sf.toTitleCase(it)
            addressError = false
        },
        addressSuggestions = addressSuggestions,
        description = description,
        onDescriptionChange = {
            description = sf.capitalizeFirst(it)
            descriptionError = false
        },
        woNumberError = woNumberError,
        addressError = addressError,
        descriptionError = descriptionError,
        onDoneClick = {
            val employerId = selectedEmployer?.employerId
            if (employerId == null) {
                Toast.makeText(
                    context,
                    errorLabel + noEmployerError,
                    Toast.LENGTH_LONG
                ).show()
                return@WorkOrderAddScreen
            }
            if (woNumber.isBlank()) {
                woNumberError = true
                Toast.makeText(
                    context,
                    errorLabel + noNumberError,
                    Toast.LENGTH_LONG
                ).show()
                return@WorkOrderAddScreen
            }
            if (address.isBlank()) {
                addressError = true
                Toast.makeText(
                    context,
                    errorLabel + noAddressError,
                    Toast.LENGTH_LONG
                ).show()
                return@WorkOrderAddScreen
            }
            if (description.isBlank()) {
                descriptionError = true
                Toast.makeText(
                    context,
                    errorLabel + noDescriptionError,
                    Toast.LENGTH_LONG
                ).show()
                return@WorkOrderAddScreen
            }

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
    )
}