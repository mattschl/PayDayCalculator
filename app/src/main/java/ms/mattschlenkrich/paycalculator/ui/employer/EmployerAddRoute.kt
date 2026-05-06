package ms.mattschlenkrich.paycalculator.ui.employer

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.ui.employer.composable.EmployerScreen

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
    val errorLabel = stringResource(R.string.error_)
    val errorMessages = mapOf(
        R.string.the_employer_must_have_a_name to stringResource(R.string.the_employer_must_have_a_name),
        R.string.the_number_of_days_before_the_pay_day_is_required to stringResource(R.string.the_number_of_days_before_the_pay_day_is_required),
        R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day to stringResource(R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day)
    )

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
                            df.getCurrentUTCTimeAsString()
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
            DatePickerDialog(
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
            Toast.makeText(
                context,
                R.string.you_cannot_add_taxes_until_the_employer_is_saved,
                Toast.LENGTH_SHORT
            ).show()
        },
        extras = emptyList(),
        onExtraClick = { },
        onAddExtraClick = {
            Toast.makeText(
                context,
                R.string.you_cannot_add_any_extra_credits_or_deductions_until_the_employer_is_saved,
                Toast.LENGTH_SHORT
            ).show()
        },
        onViewWagesClick = { },
        onSaveClick = {
            val errorResId =
                validateEmployer(name, daysBefore, frequency, midMonthDate)
            if (errorResId == null) {
                showDialog = true
            } else {
                Toast.makeText(
                    context,
                    errorLabel + (errorMessages[errorResId] ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        onDeleteClick = { },
    )
}