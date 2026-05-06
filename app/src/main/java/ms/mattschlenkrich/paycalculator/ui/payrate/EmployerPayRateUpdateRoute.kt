package ms.mattschlenkrich.paycalculator.ui.payrate

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PayRateBasedOn
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.ui.payrate.composable.PayRateScreen

@Composable
fun EmployerPayRateUpdateRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    navController: NavController
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
            DatePickerDialog(
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
                Toast.makeText(
                    context, R.string.there_has_to_be_a_wage_to_save,
                    Toast.LENGTH_SHORT
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