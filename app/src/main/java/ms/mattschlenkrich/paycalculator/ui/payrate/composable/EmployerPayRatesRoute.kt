package ms.mattschlenkrich.paycalculator.ui.payrate.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel

@Composable
fun EmployerPayRatesRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    navController: NavController
) {
    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    var selectedEmployer by remember { mutableStateOf(mainViewModel.getEmployer()) }

    val payRates by if (selectedEmployer != null) {
        employerViewModel.getEmployerPayRates(selectedEmployer!!.employerId)
            .observeAsState(emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    EmployerPayRatesScreen(
        employers = employers,
        selectedEmployer = selectedEmployer,
        onEmployerSelected = {
            selectedEmployer = it
            mainViewModel.setEmployer(it)
        },
        payRates = payRates,
        onAddPayRate = { employer ->
            mainViewModel.setEmployer(employer)
            navController.navigate(Screen.EmployerPayRateAdd.route)
        },
        onUpdatePayRate = { wage, employer ->
            mainViewModel.setPayRate(wage)
            mainViewModel.setEmployer(employer)
            navController.navigate(Screen.EmployerPayRateUpdate.route)
        },
        onAddEmployer = {
            navController.navigate(Screen.EmployerAdd.route)
        },
    )
}