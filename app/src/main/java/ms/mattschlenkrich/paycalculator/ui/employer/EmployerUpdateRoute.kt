package ms.mattschlenkrich.paycalculator.ui.employer

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.ui.employer.composable.EmployerScreen

@Composable
fun EmployerUpdateRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workTaxViewModel: WorkTaxViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val coroutineScope = rememberCoroutineScope()
    val errorLabel = stringResource(R.string.error_)
    val errorMessages = mapOf(
        R.string.the_employer_must_have_a_name to stringResource(R.string.the_employer_must_have_a_name),
        R.string.the_number_of_days_before_the_pay_day_is_required to stringResource(R.string.the_number_of_days_before_the_pay_day_is_required),
        R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day to stringResource(R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day)
    )

    val initialEmployer = mainViewModel.getEmployer()
    if (initialEmployer != null) {
        val currentEmployer by employerViewModel.getEmployer(initialEmployer.employerId)
            .observeAsState(initialEmployer)

        var name by rememberSaveable(
            currentEmployer.employerName,
            currentEmployer.employerUpdateTime
        ) { mutableStateOf(currentEmployer.employerName) }
        var frequency by rememberSaveable(
            currentEmployer.payFrequency,
            currentEmployer.employerUpdateTime
        ) { mutableStateOf(currentEmployer.payFrequency) }
        var startDate by rememberSaveable(
            currentEmployer.startDate,
            currentEmployer.employerUpdateTime
        ) { mutableStateOf(currentEmployer.startDate) }
        var dayOfWeek by rememberSaveable(
            currentEmployer.dayOfWeek,
            currentEmployer.employerUpdateTime
        ) { mutableStateOf(currentEmployer.dayOfWeek) }
        var daysBefore by rememberSaveable(
            currentEmployer.cutoffDaysBefore,
            currentEmployer.employerUpdateTime
        ) { mutableStateOf(currentEmployer.cutoffDaysBefore.toString()) }
        var midMonthDate by rememberSaveable(
            currentEmployer.midMonthlyDate,
            currentEmployer.employerUpdateTime
        ) { mutableStateOf(currentEmployer.midMonthlyDate.toString()) }
        var mainMonthDate by rememberSaveable(
            currentEmployer.mainMonthlyDate,
            currentEmployer.employerUpdateTime
        ) { mutableStateOf(currentEmployer.mainMonthlyDate.toString()) }

        val taxes by workTaxViewModel.getEmployerTaxTypes(currentEmployer.employerId)
            .observeAsState(emptyList())
        val extras by workExtraViewModel.getWorkExtraTypeList(currentEmployer.employerId)
            .observeAsState(emptyList())

        EmployerScreen(
            isUpdate = true,
            name = name,
            onNameChange = { name = it },
            frequency = frequency,
            onFrequencyChange = { frequency = it },
            startDate = startDate,
            onStartDateClick = {
                df.showDatePicker(context, startDate) {
                    startDate = it
                }
            },
            dayOfWeek = dayOfWeek,
            onDayOfWeekChange = { dayOfWeek = it },
            daysBefore = daysBefore,
            onDaysBeforeChange = { daysBefore = it },
            midMonthDate = midMonthDate,
            onMidMonthDateChange = { midMonthDate = it },
            mainMonthDate = mainMonthDate,
            onMainMonthDateChange = { mainMonthDate = it },
            taxes = taxes,
            onTaxIncludeChange = { tax, include ->
                coroutineScope.launch {
                    workTaxViewModel.updateEmployerTaxType(
                        tax.copy(
                            etrInclude = include,
                            etrUpdateTime = df.getCurrentUTCTimeAsString()
                        )
                    )
                }
            },
            onAddTaxClick = {
                mainViewModel.setEmployer(
                    getCurrentEmployer(
                        currentEmployer.employerId,
                        name,
                        frequency,
                        startDate,
                        dayOfWeek,
                        daysBefore,
                        midMonthDate,
                        mainMonthDate,
                        df
                    )
                )
                navController.navigate(Screen.TaxTypeAdd.route)
            },
            extras = extras,
            onExtraClick = { extra ->
                mainViewModel.setEmployer(
                    getCurrentEmployer(
                        currentEmployer.employerId,
                        name,
                        frequency,
                        startDate,
                        dayOfWeek,
                        daysBefore,
                        midMonthDate,
                        mainMonthDate,
                        df
                    )
                )
                mainViewModel.setWorkExtraType(extra)
                mainViewModel.setSelectedTopLevelIndex(4)
                navController.popBackStack(Screen.MainPager.route, inclusive = false)
            },
            onAddExtraClick = {
                mainViewModel.setEmployer(
                    getCurrentEmployer(
                        currentEmployer.employerId,
                        name,
                        frequency,
                        startDate,
                        dayOfWeek,
                        daysBefore,
                        midMonthDate,
                        mainMonthDate,
                        df
                    )
                )
                navController.navigate(Screen.WorkExtraTypeAdd.route)
            },
            onViewWagesClick = {
                mainViewModel.setEmployer(
                    getCurrentEmployer(
                        currentEmployer.employerId,
                        name,
                        frequency,
                        startDate,
                        dayOfWeek,
                        daysBefore,
                        midMonthDate,
                        mainMonthDate,
                        df
                    )
                )
                navController.navigate(Screen.EmployerPayRates.route)
            },
            onSaveClick = {
                val errorResId =
                    validateEmployer(name, daysBefore, frequency, midMonthDate)
                if (errorResId == null) {
                    val updatedEmployer = getCurrentEmployer(
                        currentEmployer.employerId,
                        name,
                        frequency,
                        startDate,
                        dayOfWeek,
                        daysBefore,
                        midMonthDate,
                        mainMonthDate,
                        df
                    )
                    coroutineScope.launch {
                        employerViewModel.updateEmployer(updatedEmployer)
                        navController.popBackStack()
                    }
                } else {
                    Toast.makeText(
                        context,
                        errorLabel + (errorMessages[errorResId] ?: ""),
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
        )
    } else {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }
}