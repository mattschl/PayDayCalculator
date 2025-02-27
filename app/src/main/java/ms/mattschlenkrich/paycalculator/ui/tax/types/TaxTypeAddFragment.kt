package ms.mattschlenkrich.paycalculator.ui.tax.types

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_EMPLOYER_UPDATE
import ms.mattschlenkrich.paycalculator.common.FRAG_TAX_RULES
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.database.model.tax.TaxTypes
import ms.mattschlenkrich.paycalculator.databinding.FragmentTaxTypeAddBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class TaxTypeAddFragment : Fragment(R.layout.fragment_tax_type_add) {

    private var _binding: FragmentTaxTypeAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private lateinit var taxTypeList: List<TaxTypes>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxTypeAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.add_a_new_tax_type)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populateTaxTypeListForValidation()
        populateSpinner()
    }

    private fun populateTaxTypeListForValidation() {
        mainActivity.workTaxViewModel.getTaxTypes().observe(
            viewLifecycleOwner
        ) { list ->
            taxTypeList = list
        }
    }

    private fun populateSpinner() {
        binding.apply {
            val basedOnAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_bold,
                resources.getStringArray(R.array.tax_based_on)
            )
            basedOnAdapter.setDropDownViewResource(R.layout.spinner_item_bold)
            spBasedOn.adapter = basedOnAdapter
        }
    }

    private fun setClickActions() {
        setMenuActions()
    }

    private fun setMenuActions() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        saveTaxTypeIfValidAndAttachToEmployers()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun saveTaxTypeIfValidAndAttachToEmployers() {
        val message = validateTaxType()
        if (message == ANSWER_OK) {
            val taxType = getCurrentTaxType()
            saveTaxType(taxType)
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_500)
                attachTaxTypeToEmployers(taxType)
                delay(WAIT_500)
                chooseNextStep(taxType)
            }
        } else {
            displayError(message)
        }
    }

    private fun displayError(message: String) {
        Toast.makeText(
            mView.context,
            getString(R.string.error_) + message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun validateTaxType(): String {
        binding.apply {
            if (etTaxType.text.isNullOrBlank()) {
                return getString(R.string.the_tax_type_must_have_a_name)
            }
            if (taxTypeList.isNotEmpty()) {
                for (taxType in taxTypeList) {
                    if (taxType.taxType == etTaxType.text.toString().trim()) {
                        return getString(R.string.this_tax_type_already_exists)
                    }
                }
            }
            return ANSWER_OK
        }
    }

    private fun getCurrentTaxType(): TaxTypes {
        binding.apply {
            return TaxTypes(
                nf.generateRandomIdAsLong(),
                etTaxType.text.toString(),
                spBasedOn.selectedItemPosition,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun saveTaxType(taxType: TaxTypes) {
        mainActivity.workTaxViewModel.insertTaxType(
            taxType
        )
    }

    private fun chooseNextStep(taxType: TaxTypes) {
        AlertDialog.Builder(mView.context)
            .setTitle(
                getString(R.string.choose_next_steps_for) +
                        taxType.taxType
            )
            .setMessage(
                getString(R.string.the_tax_type_has_been_added_but_there_are_no_rules_yet_)
            )
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                mainActivity.mainViewModel.setTaxType(taxType)
                mainActivity.mainViewModel.setTaxTypeString(taxType.taxType)
                gotoTaxRulesFragment()
            }
            .setNegativeButton(getString(R.string.no)) { _, _ ->
                mainActivity.mainViewModel.setTaxType(taxType)
                mainActivity.mainViewModel.setTaxTypeString(taxType.taxType)
                gotoCallingFragment()
            }
            .show()
    }

    private fun attachTaxTypeToEmployers(taxType: TaxTypes) {
        mainActivity.employerViewModel.getEmployers().observe(
            viewLifecycleOwner
        ) { employers ->
            employers.forEach {
                mainActivity.workTaxViewModel.insertEmployerTaxType(
                    EmployerTaxTypes(
                        etrEmployerId = it.employerId,
                        etrTaxType = taxType.taxType,
                        etrInclude = false,
                        etrIsDeleted = false,
                        etrUpdateTime = df.getCurrentTimeAsString()
                    )
                )
            }
        }
    }

    private fun gotoTaxRulesFragment() {
        mView.findNavController().navigate(
            TaxTypeAddFragmentDirections
                .actionTaxTypeAddFragmentToTaxRulesFragment()
        )
    }

    private fun gotoCallingFragment() {
        val callingFragment = mainActivity.mainViewModel.getCallingFragment()
        if (callingFragment != null) {
            if (callingFragment.contains(FRAG_EMPLOYER_UPDATE)) {
                gotoEmployerUpdateFragment()
            } else if (callingFragment.contains(FRAG_TAX_RULES)) {
                gotoTaxRulesFragment()
            } else {
                gotoTaxTypeFragment()
            }
        }
    }

    private fun gotoEmployerUpdateFragment() {
        mView.findNavController().navigate(
            TaxTypeAddFragmentDirections
                .actionTaxTypeAddFragmentToEmployerUpdateFragment()
        )
    }

    private fun gotoTaxTypeFragment() {
        mView.findNavController().navigate(
            TaxTypeAddFragmentDirections
                .actionTaxTypeAddFragmentToTaxTypeFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }
}