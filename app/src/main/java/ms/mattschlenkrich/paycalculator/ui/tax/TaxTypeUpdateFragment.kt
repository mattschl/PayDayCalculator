package ms.mattschlenkrich.paycalculator.ui.tax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_TAX_RULES
import ms.mattschlenkrich.paycalculator.common.FRAG_TAX_TYPE
import ms.mattschlenkrich.paycalculator.database.model.tax.TaxTypes
import ms.mattschlenkrich.paycalculator.databinding.FragmentTaxTypeUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class TaxTypeUpdateFragment : Fragment(R.layout.fragment_tax_type_update) {

    private var _binding: FragmentTaxTypeUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()

    private val taxTypeList = ArrayList<TaxTypes>()
    private lateinit var curTaxType: TaxTypes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxTypeUpdateBinding.inflate(inflater, container, false)
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.update_tax_type)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        populateSpinner()
        populateTaxTypeListForValidation()
        if (mainActivity.mainViewModel.getTaxType() != null) {
            curTaxType = mainActivity.mainViewModel.getTaxType()!!
            binding.apply {
                etTaxType.setText(curTaxType.taxType)
                spBasedOn.setSelection(curTaxType.ttBasedOn)
            }
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

    private fun populateTaxTypeListForValidation() {
        mainActivity.workTaxViewModel.getTaxTypes().observe(
            viewLifecycleOwner
        ) { taxTypes ->
            taxTypeList.clear()
            taxTypes.listIterator().forEach {
                taxTypeList.add(it)
            }
        }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            fabDone.setOnClickListener {
                updateWorkTaxTypeIfValid()
            }
        }
    }

    private fun setMenuActions() {
        mainActivity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_delete, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        deleteTaxType()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun deleteTaxType() {
        mainActivity.workTaxViewModel.updateWorkTaxType(
            TaxTypes(
                curTaxType.taxTypeId,
                curTaxType.taxType,
                curTaxType.ttBasedOn,
                true,
                df.getCurrentTimeAsString()
            )
        )
        gotoTaxTypes()
    }

    private fun updateWorkTaxTypeIfValid() {
        val message = validateTaxType()
        if (message == ANSWER_OK) {
            updateWorkTaxType()
            gotoTaxTypes()
        } else {
            Toast.makeText(
                mView.context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun validateTaxType(): String {
        binding.apply {
            if (etTaxType.text.isNullOrBlank()) {
                getString(R.string.error_) +
                        getString(R.string.the_tax_type_must_have_a_name)
            }
            if (taxTypeList.isNotEmpty()) {
                for (taxType in taxTypeList) {
                    if (taxType.taxType == etTaxType.text.toString() &&
                        taxType.taxType != curTaxType.taxType
                    ) {
                        return getString(R.string.error_) +
                                getString(R.string.this_tax_type_already_exists)
                    }

                }
            }
            return ANSWER_OK
        }
    }

    private fun getCurrentTaxType(): TaxTypes {
        binding.apply {
            return TaxTypes(
                curTaxType.taxTypeId, etTaxType.text.toString(),
                spBasedOn.selectedItemPosition,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun updateWorkTaxType() {
        mainActivity.workTaxViewModel.updateWorkTaxType(
            getCurrentTaxType()
        )
    }

    private fun gotoTaxTypes() {
        mainActivity.mainViewModel.setTaxType(null)
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        if (mainActivity.mainViewModel.getCallingFragment()!!.contains(FRAG_TAX_TYPE)) {
            gotoTaxTypesFragment()
        }
        if (mainActivity.mainViewModel.getCallingFragment()!!.contains(FRAG_TAX_RULES)) {
            gotoTaxRulesFragment()
        }
    }

    private fun gotoTaxRulesFragment() {
        mView.findNavController().navigate(
            TaxTypeUpdateFragmentDirections
                .actionTaxTypeUpdateFragmentToTaxRulesFragment()
        )
    }

    private fun gotoTaxTypesFragment() {
        mView.findNavController().navigate(
            TaxTypeUpdateFragmentDirections
                .actionTaxTypeUpdateFragmentToTaxTypeFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}