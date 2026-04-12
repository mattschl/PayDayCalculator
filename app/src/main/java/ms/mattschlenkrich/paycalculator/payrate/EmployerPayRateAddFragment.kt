package ms.mattschlenkrich.paycalculator.payrate

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.FRAG_EMPLOYER_UPDATE
import ms.mattschlenkrich.paycalculator.common.FRAG_PAY_RATES
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PayRateBasedOn
import ms.mattschlenkrich.paycalculator.common.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.data.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import java.time.LocalDate

class EmployerPayRateAddFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private val df = DateFunctions()
    private val cf = NumberFunctions()

    private var effectiveDate by mutableStateOf(LocalDate.now().minusMonths(1).toString())
    private var wage by mutableStateOf("")
    private var selectedFrequency by mutableStateOf(PayRateBasedOn.entries[0])

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        employerViewModel = mainActivity.employerViewModel
        mainActivity.topMenuBar.title = getString(R.string.add_a_pay_rate)

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    EmployerPayRateAddContent()
                }
            }
        }
    }

    @Composable
    fun EmployerPayRateAddContent() {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { savePayRate() },
                    containerColor = colorResource(id = R.color.dark_green),
                    contentColor = Color.White
                ) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = stringResource(id = R.string.save)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(
                        horizontal = SCREEN_PADDING_HORIZONTAL,
                        vertical = SCREEN_PADDING_VERTICAL
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker() }
                        .padding(vertical = ELEMENT_SPACING),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.effective_date),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = effectiveDate,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                DecimalOutlinedTextField(
                    value = wage,
                    onValueChange = { wage = it },
                    label = { Text(stringResource(id = R.string.pay_rate)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                SimpleDropdownField(
                    label = stringResource(id = R.string.rate_applies_to),
                    items = PayRateBasedOn.entries,
                    selectedItem = selectedFrequency,
                    onItemSelected = { selectedFrequency = it },
                    itemToString = { it.type },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = ELEMENT_SPACING)
                )
            }
        }
    }

    private fun showDatePicker() {
        val curDateAll = effectiveDate.split("-")
        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                effectiveDate = "$year-${
                    month.toString().padStart(2, '0')
                }-${
                    dayOfMonth.toString().padStart(2, '0')
                }"
            }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
        )
        datePickerDialog.setTitle(
            getString(R.string.choose_when_this_wage_goes_into_effect)
        )
        datePickerDialog.show()
    }

    private fun savePayRate() {
        val curEmployer = mainViewModel.getEmployer()!!
        val message = validatePayRate()
        if (message == ANSWER_OK) {
            val curWage = EmployerPayRates(
                cf.generateRandomIdAsLong(),
                curEmployer.employerId,
                effectiveDate,
                selectedFrequency.ordinal,
                cf.getDoubleFromDollars(wage),
                false,
                df.getCurrentTimeAsString()
            )
            employerViewModel.insertPayRate(curWage)
            gotoCallingFragment()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_) + message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun validatePayRate(): String {
        return if (wage.isBlank()) {
            getString(R.string.there_has_to_be_a_wage_to_save)
        } else {
            ANSWER_OK
        }
    }

    private fun gotoCallingFragment() {
        val callingFragment = mainViewModel.getCallingFragment() ?: ""
        if (callingFragment.contains(FRAG_PAY_RATES)) {
            requireView().findNavController().navigate(
                EmployerPayRateAddFragmentDirections.actionEmployerPayRateAddFragmentToEmployerPayRatesFragment()
            )
        } else if (callingFragment.contains(FRAG_EMPLOYER_UPDATE)) {
            requireView().findNavController().navigate(
                EmployerPayRateAddFragmentDirections.actionEmployerPayRateAddFragmentToEmployerUpdateFragment()
            )
        } else {
            requireView().findNavController().popBackStack()
        }
    }
}