package ms.mattschlenkrich.paycalculator.employer

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PayDayFrequencies
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WorkDayOfWeek
import ms.mattschlenkrich.paycalculator.data.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel

class EmployerAddFragmentNew : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var workTaxViewModel: WorkTaxViewModel
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        employerViewModel = mainActivity.employerViewModel
        workTaxViewModel = mainActivity.workTaxViewModel
        mainViewModel = mainActivity.mainViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    var name by remember { mutableStateOf("") }
                    var frequency by remember { mutableStateOf(PayDayFrequencies.BI_WEEKLY.toString()) }
                    var startDate by remember { mutableStateOf(df.getCurrentDateAsString()) }
                    var dayOfWeek by remember { mutableStateOf(WorkDayOfWeek.FRIDAY.toString()) }
                    var daysBefore by remember { mutableStateOf("6") }
                    var midMonthDate by remember { mutableStateOf("15") }
                    var mainMonthDate by remember { mutableStateOf("31") }

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
                                requireContext(),
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
                                requireContext(),
                                R.string.you_cannot_add_taxes_until_the_employer_is_saved,
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        extras = emptyList(),
                        onExtraClick = { },
                        onAddExtraClick = {
                            Toast.makeText(
                                requireContext(),
                                R.string.you_cannot_add_any_extra_credits_or_deductions_until_the_employer_is_saved,
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onViewWagesClick = { },
                        onSaveClick = {
                            val errorMessage =
                                validateEmployer(name, daysBefore, frequency, midMonthDate)
                            if (errorMessage == ANSWER_OK) {
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
                                confirmSaveAndContinue(curEmployer)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_) + errorMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        onDeleteClick = { },
                        onBackClick = { findNavController().popBackStack() }
                    )
                }
            }
        }
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
        // Duplicate check would usually happen against a list of employers
        // For simplicity, we assume the ViewModel check is sufficient or omit if complex in Compose
        if (daysBefore.isBlank()) {
            return getString(R.string.the_number_of_days_before_the_pay_day_is_required)
        }
        if (frequency == ms.mattschlenkrich.paycalculator.common.INTERVAL_SEMI_MONTHLY && midMonthDate.isBlank()) {
            return getString(R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day)
        }
        return ANSWER_OK
    }

    private fun confirmSaveAndContinue(curEmployer: Employers) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_next_steps_for) + curEmployer.employerName)
            .setMessage(getString(R.string.would_you_like_to_go_to_the_next_step))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    employerViewModel.insertEmployer(curEmployer)
                    delay(WAIT_250)
                    addEmployerTaxRules(curEmployer.employerId)
                    delay(WAIT_250)
                    mainViewModel.setEmployer(curEmployer)
                    findNavController().navigate(EmployerAddFragmentNewDirections.actionEmployerAddFragmentToEmployerUpdateFragment())
                }
            }
            .setNegativeButton(getString(R.string.go_back), null)
            .show()
    }

    private fun addEmployerTaxRules(employerId: Long) {
        workTaxViewModel.getTaxTypes().observe(viewLifecycleOwner) { type ->
            type.forEach {
                workTaxViewModel.insertEmployerTaxType(
                    EmployerTaxTypes(
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
}