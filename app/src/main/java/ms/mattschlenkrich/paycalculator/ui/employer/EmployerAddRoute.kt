package ms.mattschlenkrich.paycalculator.ui.employer

import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PayDayFrequencies
import ms.mattschlenkrich.paycalculator.common.WorkDayOfWeek
import ms.mattschlenkrich.paycalculator.common.compose.ConfirmationBottomSheet
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTaxViewModel
import ms.mattschlenkrich.paycalculator.ui.employer.composable.EmployerScreen

@OptIn(ExperimentalMaterial3Api::class)
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
    val errorLabel = stringResource(R.string.prefix_error)
    val errorMessages = mapOf(
        R.string.the_employer_must_have_a_name to stringResource(R.string.the_employer_must_have_a_name),
        R.string.the_number_of_days_before_the_pay_day_is_required to stringResource(R.string.the_number_of_days_before_the_pay_day_is_required),
        R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day to stringResource(R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day)
    )

    var name by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf(PayDayFrequencies.BI_WEEKLY.toString()) }
    var startDate by rememberSaveable { mutableStateOf(df.getCurrentDateAsString()) }
    var dayOfWeek by rememberSaveable { mutableStateOf(WorkDayOfWeek.FRIDAY.toString()) }
    var daysBefore by rememberSaveable { mutableStateOf("6") }
    var midMonthDate by rememberSaveable { mutableStateOf("15") }
    var mainMonthDate by rememberSaveable { mutableStateOf("31") }

    var showDialog by rememberSaveable { mutableStateOf(false) }

    ConfirmationBottomSheet(
        showDialog = showDialog,
        onDismissRequest = { showDialog = false },
        title = "${stringResource(R.string.choose_next_steps_for)} $name",
        message = stringResource(R.string.would_you_like_to_go_to_the_next_step),
        dismissButtonText = stringResource(R.string.go_back),
        onConfirm = {
            val curEmployer = getCurrentEmployer(
                nf.generateRandomIdAsLong(),
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
                employerViewModel.insertEmployer(curEmployer)
                addEmployerTaxRules(curEmployer.employerId, workTaxViewModel, df)
                mainViewModel.setEmployer(curEmployer)
                navController.navigate(Screen.EmployerUpdate.route) {
                    popUpTo(Screen.EmployerAdd.route) { inclusive = true }
                }
            }
        }
    )

    EmployerScreen(
        isUpdate = false,
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
    )
}