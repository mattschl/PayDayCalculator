package ms.mattschlenkrich.paydaycalculator.ui.tax

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
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_EMPLOYER_UPDATE
import ms.mattschlenkrich.paydaycalculator.common.FRAG_TAX_RULES
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_500
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTaxTypeAddBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.EmployerTaxTypes
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class TaxTypeAddFragment : Fragment(R.layout.fragment_tax_type_add) {

    private var _binding: FragmentTaxTypeAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    private val df = DateFunctions()
    private val cf = NumberFunctions()
    private val taxTypeList = ArrayList<TaxTypes>()

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
        populateSpinner()
        getTaxTypeListForValidation()
        setMenuActions()
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

    private fun getTaxTypeListForValidation() {
        mainActivity.workTaxViewModel.getTaxTypes().observe(
            viewLifecycleOwner
        ) { list ->
            taxTypeList.clear()
            list.listIterator().forEach {
                taxTypeList.add(it)
            }
        }
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
        val message = checkTaxType()
        if (message == ANSWER_OK) {
            val taxType = getCurrentTaxType()
            mainActivity.workTaxViewModel.insertTaxType(
                taxType
            )
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_500)
                attachTaxTypeToEmployers(taxType)
                delay(WAIT_500)
                chooseNextStep(taxType)
            }
        } else {
            Toast.makeText(
                mView.context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun getCurrentTaxType(): TaxTypes {
        binding.apply {
            return TaxTypes(
                cf.generateRandomIdAsLong(),
                etTaxType.text.toString(),
                spBasedOn.selectedItemPosition,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun chooseNextStep(taxType: TaxTypes) {
        AlertDialog.Builder(mView.context)
            .setTitle("Choose next steps for ${taxType.taxType}")
            .setMessage(
                "The tax type has been added but there are no rules yet. " +
                        "Would you like to edit those rules now?"
            )
            .setPositiveButton("Yes") { _, _ ->
                mainActivity.mainViewModel.setTaxType(taxType)
                mainActivity.mainViewModel.setTaxTypeString(taxType.taxType)
                gotoTaxRulesFragment()
            }
            .setNegativeButton("No") { _, _ ->
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

    private fun gotoTaxRulesFragment() {
        mView.findNavController().navigate(
            TaxTypeAddFragmentDirections
                .actionTaxTypeAddFragmentToTaxRulesFragment()
        )
    }

    private fun checkTaxType(): String {
        binding.apply {
            var nameFound = false
            if (taxTypeList.isNotEmpty()) {
                for (taxType in taxTypeList) {
                    if (taxType.taxType == etTaxType.text.toString()) {
                        nameFound = true
                        break
                    }

                }
            }
            val errorMessage = if (etTaxType.text.isNullOrBlank()) {
                "    ERROR!!\n" +
                        "The tax type must have a description"
            } else if (nameFound) {
                "    ERROR!!\n" +
                        "This tax type already exists!"
            } else {
                ANSWER_OK
            }
            return errorMessage
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }
}