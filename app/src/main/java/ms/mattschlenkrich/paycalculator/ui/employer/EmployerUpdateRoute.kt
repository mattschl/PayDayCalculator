package ms.mattschlenkrich.paycalculator.ui.employer

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
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

    val currentEmployer = mainViewModel.getEmployer()
    if (currentEmployer != null) {
        var name by remember { mutableStateOf(currentEmployer.employerName) }
        var frequency by remember { mutableStateOf(currentEmployer.payFrequency) }
        var startDate by remember { mutableStateOf(currentEmployer.startDate) }
        var dayOfWeek by remember { mutableStateOf(currentEmployer.dayOfWeek) }
        var daysBefore by remember { mutableStateOf(currentEmployer.cutoffDaysBefore.toString()) }
        var midMonthDate by remember { mutableStateOf(currentEmployer.midMonthlyDate.toString()) }
        var mainMonthDate by remember { mutableStateOf(currentEmployer.mainMonthlyDate.toString()) }

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
                val curDateAll = startDate.split("-")
                DatePickerDialog(
                    context, { _, year, monthOfYear, dayOfMonth ->
                        val month = monthOfYear + 1
                        startDate =
                            "$year-${month.toString().padStart(2, '0')}-${
                                dayOfMonth.toString().padStart(2, '0')
                            }"
                    },
                    curDateAll[0].toInt(),
                    curDateAll[1].toInt() - 1,
                    curDateAll[2].toInt()
                ).show()
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
                navController.navigate(Screen.Extras.route)
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
                    employerViewModel.updateEmployer(updatedEmployer)
                    navController.popBackStack()
                } else {
                    Toast.makeText(
                        context,
                        errorLabel + (errorMessages[errorResId] ?: ""),
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onDeleteClick = {
                val deletedEmployer = currentEmployer.copy(
                    employerIsDeleted = true,
                    employerUpdateTime = df.getCurrentUTCTimeAsString()
                )
                employerViewModel.updateEmployer(deletedEmployer)
                navController.popBackStack()
            },
        )
    } else {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }
}