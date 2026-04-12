package ms.mattschlenkrich.paycalculator.tax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_EMPLOYER_UPDATE
import ms.mattschlenkrich.paycalculator.common.FRAG_TAX_RULES
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.TaxTypes
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel

class TaxTypeAddFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var workTaxViewModel: WorkTaxViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var mainViewModel: MainViewModel

    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        workTaxViewModel = mainActivity.workTaxViewModel
        employerViewModel = mainActivity.employerViewModel
        mainViewModel = mainActivity.mainViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                androidx.compose.material3.MaterialTheme {
                    TaxTypeAddScreen()
                }
            }
        }
    }

    @Composable
    fun TaxTypeAddScreen() {
        var taxTypeState by remember { mutableStateOf("") }
        var selectedBasedOn by remember { mutableIntStateOf(0) }
        var showNextStepDialog by remember { mutableStateOf(false) }
        var savedTaxType by remember { mutableStateOf<TaxTypes?>(null) }

        val taxTypeList by workTaxViewModel.getTaxTypes().observeAsState(emptyList())
        val employers by employerViewModel.getEmployers().observeAsState(emptyList())

        if (showNextStepDialog && savedTaxType != null) {
            AlertDialog(
                onDismissRequest = { /* Prevent dismiss */ },
                title = { Text(stringResource(R.string.choose_next_steps_for) + savedTaxType!!.taxType) },
                text = { Text(stringResource(R.string.the_tax_type_has_been_added_but_there_are_no_rules_yet_)) },
                confirmButton = {
                    TextButton(onClick = {
                        mainViewModel.setTaxType(savedTaxType)
                        mainViewModel.setTaxTypeString(savedTaxType!!.taxType)
                        gotoTaxRulesFragment()
                    }) {
                        Text(stringResource(R.string.yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        mainViewModel.setTaxType(savedTaxType)
                        mainViewModel.setTaxTypeString(savedTaxType!!.taxType)
                        gotoCallingFragment()
                    }) {
                        Text(stringResource(R.string.no))
                    }
                }
            )
        }

        TaxTypeScreen(
            taxType = taxTypeState,
            onTaxTypeChange = { taxTypeState = it },
            selectedBasedOn = selectedBasedOn,
            onBasedOnChange = { selectedBasedOn = it },
            title = getString(R.string.add_a_new_tax_type),
            onSaveClick = {
                val message = validateTaxType(taxTypeState, taxTypeList)
                if (message == ANSWER_OK) {
                    val taxType = TaxTypes(
                        nf.generateRandomIdAsLong(),
                        taxTypeState.trim(),
                        selectedBasedOn,
                        false,
                        df.getCurrentTimeAsString()
                    )
                    workTaxViewModel.insertTaxTypeWithEmployerLinks(
                        taxType,
                        employers,
                        df.getCurrentTimeAsString()
                    )
                    savedTaxType = taxType
                    showNextStepDialog = true
                } else {
                    displayError(getString(R.string.error_) + message)
                }
            },
            onBackClick = { findNavController().navigateUp() }
        )
    }

    private fun validateTaxType(name: String, list: List<TaxTypes>): String {
        if (name.isBlank()) {
            return getString(R.string.the_tax_type_must_have_a_name)
        }
        if (list.any { it.taxType.equals(name.trim(), ignoreCase = true) }) {
            return getString(R.string.this_tax_type_already_exists)
        }
        return ANSWER_OK
    }

    private fun displayError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun gotoTaxRulesFragment() {
        findNavController().navigate(
            TaxTypeAddFragmentDirections.actionTaxTypeAddFragmentToTaxRulesFragment()
        )
    }

    private fun gotoCallingFragment() {
        val callingFragment = mainViewModel.getCallingFragment()
        if (callingFragment != null) {
            if (callingFragment.contains(FRAG_EMPLOYER_UPDATE)) {
                gotoEmployerUpdateFragment()
            } else if (callingFragment.contains(FRAG_TAX_RULES)) {
                gotoTaxRulesFragment()
            } else {
                gotoTaxTypeFragment()
            }
        } else {
            gotoTaxTypeFragment()
        }
    }

    private fun gotoEmployerUpdateFragment() {
        findNavController().navigate(
            TaxTypeAddFragmentDirections.actionTaxTypeAddFragmentToEmployerUpdateFragment()
        )
    }

    private fun gotoTaxTypeFragment() {
        findNavController().navigate(
            TaxTypeAddFragmentDirections.actionTaxTypeAddFragmentToTaxTypeFragment()
        )
    }
}