package ms.mattschlenkrich.paycalculator.employer

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PayDayFrequencies
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WorkDayOfWeek
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel

@Composable
fun EmployerAddRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workTaxViewModel: WorkTaxViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(PayDayFrequencies.BI_WEEKLY.toString()) }
    var startDate by remember { mutableStateOf(df.getCurrentDateAsString()) }
    var dayOfWeek by remember { mutableStateOf(WorkDayOfWeek.FRIDAY.toString()) }
    var daysBefore by remember { mutableStateOf("6") }
    var midMonthDate by remember { mutableStateOf("15") }
    var mainMonthDate by remember { mutableStateOf("31") }

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.choose_next_steps_for) + " " + name) },
            text = { Text(stringResource(R.string.would_you_like_to_go_to_the_next_step)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        val curEmployer = Employers(
                            nf.generateRandomIdAsLong(),
                            name,
                            frequency,
                            startDate,
                            dayOfWeek,
                            daysBefore.toIntOrNull() ?: 0,
                            midMonthDate.toIntOrNull() ?: 0,
                            mainMonthDate.toIntOrNull() ?: 0,
                            false,
                            df.getCurrentTimeAsString()
                        )
                        coroutineScope.launch {
                            employerViewModel.insertEmployer(curEmployer)
                            delay(WAIT_250)
                            addEmployerTaxRules(curEmployer.employerId, workTaxViewModel, df)
                            delay(WAIT_250)
                            mainViewModel.setEmployer(curEmployer)
                            navController.navigate(Screen.EmployerUpdate.route) {
                                popUpTo(Screen.EmployerAdd.route) { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text(stringResource(R.string.go_back))
                }
            }
        )
    }

    EmployerScreen(
        isUpdate = false,
        name = name,
        onNameChange = { name = it },
        frequency = frequency,
        onFrequencyChange = { frequency = it },
        startDate = startDate,
        onStartDateClick = {
            val curDateAll = startDate.split("-")
            android.app.DatePickerDialog(
                context,
                { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    startDate = "$year-${
                        month.toString().padStart(2, '0')
                    }-${dayOfMonth.toString().padStart(2, '0')}"
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
        taxes = emptyList(),
        onTaxIncludeChange = { _, _ -> },
        onAddTaxClick = {
            android.widget.Toast.makeText(
                context,
                R.string.you_cannot_add_taxes_until_the_employer_is_saved,
                android.widget.Toast.LENGTH_SHORT
            ).show()
        },
        extras = emptyList(),
        onExtraClick = { },
        onAddExtraClick = {
            android.widget.Toast.makeText(
                context,
                R.string.you_cannot_add_any_extra_credits_or_deductions_until_the_employer_is_saved,
                android.widget.Toast.LENGTH_SHORT
            ).show()
        },
        onViewWagesClick = { },
        onSaveClick = {
            val errorResId =
                validateEmployer(name, daysBefore, frequency, midMonthDate)
            if (errorResId == null) {
                showDialog = true
            } else {
                android.widget.Toast.makeText(
                    context,
                    context.getString(R.string.error_) + context.getString(errorResId),
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        },
        onDeleteClick = { },
        onBackClick = { navController.popBackStack() }
    )
}

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
                android.app.DatePickerDialog(
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
                            etrUpdateTime = df.getCurrentTimeAsString()
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
                    android.widget.Toast.makeText(
                        context,
                        context.getString(R.string.error_) + context.getString(errorResId),
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            },
            onDeleteClick = {
                val deletedEmployer = currentEmployer.copy(
                    employerIsDeleted = true,
                    employerUpdateTime = df.getCurrentTimeAsString()
                )
                employerViewModel.updateEmployer(deletedEmployer)
                navController.popBackStack()
            },
            onBackClick = { navController.popBackStack() }
        )
    } else {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }
}

private fun getCurrentEmployer(
    id: Long,
    name: String,
    frequency: String,
    startDate: String,
    dayOfWeek: String,
    daysBefore: String,
    midMonthDate: String,
    mainMonthDate: String,
    df: DateFunctions
): Employers {
    return Employers(
        id,
        name,
        frequency,
        startDate,
        dayOfWeek,
        daysBefore.toIntOrNull() ?: 0,
        midMonthDate.toIntOrNull() ?: 0,
        mainMonthDate.toIntOrNull() ?: 0,
        false,
        df.getCurrentTimeAsString()
    )
}

private fun validateEmployer(
    name: String,
    daysBefore: String,
    frequency: String,
    midMonthDate: String
): Int? {
    if (name.isBlank()) {
        return R.string.the_employer_must_have_a_name
    }
    if (daysBefore.isBlank()) {
        return R.string.the_number_of_days_before_the_pay_day_is_required
    }
    if (frequency == ms.mattschlenkrich.paycalculator.common.INTERVAL_SEMI_MONTHLY && midMonthDate.isBlank()) {
        return R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day
    }
    return null
}

private fun addEmployerTaxRules(
    employerId: Long,
    workTaxViewModel: WorkTaxViewModel,
    df: DateFunctions
) {
    workTaxViewModel.getTaxTypes().observeForever { type ->
        type.forEach {
            workTaxViewModel.insertEmployerTaxType(
                ms.mattschlenkrich.paycalculator.data.EmployerTaxTypes(
                    etrEmployerId = employerId,
                    etrTaxType = it.taxType,
                    etrInclude = true,
                    etrIsDeleted = false,
                    etrUpdateTime = df.getCurrentTimeAsString()
                )
            )
        }
    }
}