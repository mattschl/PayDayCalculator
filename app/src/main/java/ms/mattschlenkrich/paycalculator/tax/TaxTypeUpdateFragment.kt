package ms.mattschlenkrich.paycalculator.tax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_TAX_RULES
import ms.mattschlenkrich.paycalculator.common.FRAG_TAX_TYPE
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.TaxTypes
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel

class TaxTypeUpdateFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workTaxViewModel: WorkTaxViewModel
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workTaxViewModel = mainActivity.workTaxViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                TaxTypeUpdateScreen()
            }
        }
    }

    @Composable
    fun TaxTypeUpdateScreen() {
        val curTaxType = mainViewModel.getTaxType() ?: return

        var taxTypeState by remember { mutableStateOf(curTaxType.taxType) }
        var selectedBasedOn by remember { mutableIntStateOf(curTaxType.ttBasedOn) }

        val taxTypeList by workTaxViewModel.getTaxTypes().observeAsState(emptyList())

        TaxTypeScreen(
            taxType = taxTypeState,
            onTaxTypeChange = { taxTypeState = it },
            selectedBasedOn = selectedBasedOn,
            onBasedOnChange = { selectedBasedOn = it },
            title = getString(R.string.update_tax_type),
            onSaveClick = {
                val message = validateTaxType(taxTypeState, curTaxType, taxTypeList)
                if (message == ANSWER_OK) {
                    val updatedTaxType = curTaxType.copy(
                        taxType = taxTypeState,
                        ttBasedOn = selectedBasedOn,
                        ttUpdateTime = df.getCurrentTimeAsString()
                    )
                    workTaxViewModel.updateWorkTaxType(updatedTaxType)
                    gotoTaxTypes()
                } else {
                    displayError(getString(R.string.error_) + message)
                }
            },
            onDeleteClick = {
                val deletedTaxType = curTaxType.copy(
                    ttIsDeleted = true,
                    ttUpdateTime = df.getCurrentTimeAsString()
                )
                workTaxViewModel.updateWorkTaxType(deletedTaxType)
                gotoTaxTypes()
            },
            onBackClick = { findNavController().navigateUp() }
        )
    }

    private fun validateTaxType(name: String, cur: TaxTypes, list: List<TaxTypes>): String {
        if (name.isBlank()) {
            return getString(R.string.the_tax_type_must_have_a_name)
        }
        if (list.any { it.taxType == name.trim() && it.taxType != cur.taxType }) {
            return getString(R.string.this_tax_type_already_exists)
        }
        return ANSWER_OK
    }

    private fun displayError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun gotoTaxTypes() {
        mainViewModel.setTaxType(null)
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        val callingFragment = mainViewModel.getCallingFragment() ?: return
        if (callingFragment.contains(FRAG_TAX_TYPE)) {
            gotoTaxTypesFragment()
        } else if (callingFragment.contains(FRAG_TAX_RULES)) {
            gotoTaxRulesFragment()
        }
    }

    private fun gotoTaxRulesFragment() {
        findNavController().navigate(
            TaxTypeUpdateFragmentDirections.actionTaxTypeUpdateFragmentToTaxRulesFragment()
        )
    }

    private fun gotoTaxTypesFragment() {
        findNavController().navigate(
            TaxTypeUpdateFragmentDirections.actionTaxTypeUpdateFragmentToTaxTypeFragment()
        )
    }
}