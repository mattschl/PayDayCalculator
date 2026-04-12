package ms.mattschlenkrich.paycalculator.employer

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel

class EmployerUpdateFragmentNew : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var workTaxViewModel: WorkTaxViewModel
    private lateinit var workExtraViewModel: WorkExtraViewModel
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        employerViewModel = mainActivity.employerViewModel
        workTaxViewModel = mainActivity.workTaxViewModel
        workExtraViewModel = mainActivity.workExtraViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
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
                                    requireContext(), { _, year, monthOfYear, dayOfMonth ->
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
                                viewLifecycleOwner.lifecycleScope.launch {
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
                                        mainMonthDate
                                    )
                                )
                                findNavController().navigate(EmployerUpdateFragmentNewDirections.actionEmployerUpdateFragmentToTaxTypeAddFragment())
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
                                        mainMonthDate
                                    )
                                )
                                mainViewModel.setWorkExtraType(extra)
                                findNavController().navigate(EmployerUpdateFragmentNewDirections.actionEmployerUpdateFragmentToEmployerExtraDefinitionUpdateFragment())
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
                                        mainMonthDate
                                    )
                                )
                                findNavController().navigate(EmployerUpdateFragmentNewDirections.actionEmployerUpdateFragmentToEmployerExtraDefinitionsAddFragment())
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
                                        mainMonthDate
                                    )
                                )
                                findNavController().navigate(EmployerUpdateFragmentNewDirections.actionEmployerUpdateFragmentToEmployerPayRatesFragment())
                            },
                            onSaveClick = {
                                val errorMessage =
                                    validateEmployer(name, daysBefore, frequency, midMonthDate)
                                if (errorMessage == ANSWER_OK) {
                                    val updatedEmployer = getCurrentEmployer(
                                        currentEmployer.employerId,
                                        name,
                                        frequency,
                                        startDate,
                                        dayOfWeek,
                                        daysBefore,
                                        midMonthDate,
                                        mainMonthDate
                                    )
                                    employerViewModel.updateEmployer(updatedEmployer)
                                    findNavController().popBackStack()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.error_) + errorMessage,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onDeleteClick = {
                                val deletedEmployer = currentEmployer.copy(
                                    employerIsDeleted = true,
                                    employerUpdateTime = df.getCurrentTimeAsString()
                                )
                                employerViewModel.updateEmployer(deletedEmployer)
                                findNavController().navigate(EmployerUpdateFragmentNewDirections.actionEmployerUpdateFragmentToEmployerFragment())
                            },
                            onBackClick = { findNavController().popBackStack() }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            findNavController().popBackStack()
                        }
                    }
                }
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
        mainMonthDate: String
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
    ): String {
        if (name.isBlank()) {
            return getString(R.string.the_employer_must_have_a_name)
        }
        if (daysBefore.isBlank()) {
            return getString(R.string.the_number_of_days_before_the_pay_day_is_required)
        }
        if (frequency == ms.mattschlenkrich.paycalculator.common.INTERVAL_SEMI_MONTHLY && midMonthDate.isBlank()) {
            return getString(R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day)
        }
        return ANSWER_OK
    }
}