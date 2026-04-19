package ms.mattschlenkrich.paycalculator.payrate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PayRateBasedOn
import ms.mattschlenkrich.paycalculator.data.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.MainViewModel

@Composable
fun EmployerPayRatesRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    navController: androidx.navigation.NavController
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
            navController.navigate(ms.mattschlenkrich.paycalculator.Screen.EmployerPayRateAdd.route)
        },
        onUpdatePayRate = { wage, employer ->
            mainViewModel.setPayRate(wage)
            mainViewModel.setEmployer(employer)
            navController.navigate(ms.mattschlenkrich.paycalculator.Screen.EmployerPayRateUpdate.route)
        },
        onAddEmployer = {
            navController.navigate(ms.mattschlenkrich.paycalculator.Screen.EmployerAdd.route)
        },
    )
}

@Composable
fun EmployerPayRateAddRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    navController: androidx.navigation.NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }

    val employer = mainViewModel.getEmployer() ?: return

    var effectiveDate by remember { mutableStateOf(df.getCurrentDateAsString()) }
    var wage by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf(PayRateBasedOn.HOURLY) }

    PayRateScreen(
        title = stringResource(R.string.add_a_pay_rate),
        effectiveDate = df.getDisplayDate(effectiveDate),
        onEffectiveDateClick = {
            val curDateAll = effectiveDate.split("-")
            android.app.DatePickerDialog(
                context, { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    effectiveDate = "$year-${
                        month.toString().padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
            ).show()
        },
        wage = wage,
        onWageChange = { wage = it },
        selectedFrequency = selectedFrequency,
        onFrequencySelected = { selectedFrequency = it },
        onSaveClick = {
            if (wage.isBlank() || nf.getDoubleFromDollars(wage) == 0.0) {
                android.widget.Toast.makeText(
                    context, R.string.there_has_to_be_a_wage_to_save,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                employerViewModel.insertPayRate(
                    EmployerPayRates(
                        nf.generateRandomIdAsLong(),
                        employer.employerId,
                        effectiveDate,
                        selectedFrequency.ordinal,
                        nf.getDoubleFromDollars(wage),
                        false,
                        df.getCurrentUTCTimeAsString()
                    )
                )
                navController.popBackStack()
            }
        },
    )
}

@Composable
fun EmployerPayRateUpdateRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    navController: androidx.navigation.NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }

    val payRate = mainViewModel.getPayRate() ?: return
    val employer = mainViewModel.getEmployer() ?: return

    var effectiveDate by remember { mutableStateOf(payRate.eprEffectiveDate) }
    var wage by remember { mutableStateOf(nf.displayDollars(payRate.eprPayRate)) }
    var selectedFrequency by remember {
        mutableStateOf(PayRateBasedOn.entries[payRate.eprPerPeriod])
    }

    PayRateScreen(
        title = stringResource(R.string.update),
        effectiveDate = df.getDisplayDate(effectiveDate),
        onEffectiveDateClick = {
            val curDateAll = effectiveDate.split("-")
            android.app.DatePickerDialog(
                context, { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    effectiveDate = "$year-${
                        month.toString().padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
            ).show()
        },
        wage = wage,
        onWageChange = { wage = it },
        selectedFrequency = selectedFrequency,
        onFrequencySelected = { selectedFrequency = it },
        onSaveClick = {
            if (wage.isBlank() || nf.getDoubleFromDollars(wage) == 0.0) {
                android.widget.Toast.makeText(
                    context, R.string.there_has_to_be_a_wage_to_save,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                employerViewModel.updatePayRate(
                    payRate.copy(
                        eprEffectiveDate = effectiveDate,
                        eprPayRate = nf.getDoubleFromDollars(wage),
                        eprPerPeriod = selectedFrequency.ordinal,
                        eprUpdateTime = df.getCurrentUTCTimeAsString()
                    )
                )
                navController.popBackStack()
            }
        },
        onDeleteClick = {
            employerViewModel.updatePayRate(
                payRate.copy(
                    eprIsDeleted = true,
                    eprUpdateTime = df.getCurrentUTCTimeAsString()
                )
            )
            navController.popBackStack()
        },
    )
}