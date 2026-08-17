package ms.mattschlenkrich.paycalculator.ui.payrate

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PayRateBasedOn
import ms.mattschlenkrich.paycalculator.data.entity.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.ui.payrate.composable.PayRateScreen

@Composable
fun EmployerPayRateRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    navController: NavController,
    isUpdate: Boolean
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val employer = mainViewModel.getEmployer() ?: return
    val initialPayRate = if (isUpdate) mainViewModel.getPayRate() else null
    if (isUpdate && initialPayRate == null) return

    var effectiveDate by rememberSaveable {
        mutableStateOf(initialPayRate?.eprEffectiveDate ?: df.getCurrentDateAsString())
    }
    var wage by rememberSaveable {
        mutableStateOf(if (isUpdate) nf.displayDollars(initialPayRate!!.eprPayRate) else "")
    }
    var selectedFrequency by rememberSaveable {
        mutableStateOf(
            if (isUpdate) PayRateBasedOn.entries[initialPayRate!!.eprPerPeriod]
            else PayRateBasedOn.HOURLY
        )
    }

    PayRateScreen(
        effectiveDate = df.getDisplayDate(effectiveDate),
        onEffectiveDateClick = {
            df.showDatePicker(context, effectiveDate) {
                effectiveDate = it
            }
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
                coroutineScope.launch {
                    val payRate = if (isUpdate) {
                        initialPayRate!!.copy(
                            eprEffectiveDate = effectiveDate,
                            eprPayRate = nf.getDoubleFromDollars(wage),
                            eprPerPeriod = selectedFrequency.ordinal,
                            eprUpdateTime = df.getCurrentUTCTimeAsString()
                        )
                    } else {
                        EmployerPayRates(
                            nf.generateRandomIdAsLong(),
                            employer.employerId,
                            effectiveDate,
                            selectedFrequency.ordinal,
                            nf.getDoubleFromDollars(wage),
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    }
                    if (isUpdate) {
                        employerViewModel.updatePayRate(payRate)
                    } else {
                        employerViewModel.insertPayRate(payRate)
                    }
                    navController.popBackStack()
                }
            }
        },
    )
}