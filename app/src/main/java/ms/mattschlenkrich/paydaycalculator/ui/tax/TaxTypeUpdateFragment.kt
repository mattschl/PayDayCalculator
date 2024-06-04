package ms.mattschlenkrich.paydaycalculator.ui.tax

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
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTaxTypeUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class TaxTypeUpdateFragment : Fragment(R.layout.fragment_tax_type_update) {

    private var _binding: FragmentTaxTypeUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()

    //    private val cf = NumberFunctions()
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
        populateSpinner()
        getTaxTypeListForValidation()
        setMenuActions()
        setClickActions()
        populateValues()
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

    private fun populateValues() {
        if (mainActivity.mainViewModel.getTaxType() != null) {
            curTaxType = mainActivity.mainViewModel.getTaxType()!!
            binding.apply {
                etTaxType.setText(curTaxType.taxType)
                spBasedOn.setSelection(curTaxType.ttBasedOn)
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updateWorkTaxTypeIfValid()
            }
        }
    }

    private fun gotoTaxTypesFragment() {
        mainActivity.mainViewModel.setTaxType(null)
        mView.findNavController().navigate(
            TaxTypeUpdateFragmentDirections
                .actionTaxTypeUpdateFragmentToTaxTypeFragment()
        )
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
        gotoTaxTypesFragment()
    }

    private fun updateWorkTaxTypeIfValid() {
        val message = validateTaxType()
        if (message == ANSWER_OK) {
            mainActivity.workTaxViewModel.updateWorkTaxType(
                getCurrentTaxType()
            )
            gotoTaxTypesFragment()
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
                curTaxType.taxTypeId, etTaxType.text.toString(),
                spBasedOn.selectedItemPosition,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun validateTaxType(): String {
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
            } else if (nameFound && etTaxType.text.toString() != curTaxType.taxType) {
                "    ERROR!!\n" +
                        "This tax type already exists!"
            } else {
                ANSWER_OK
            }
            return errorMessage
        }
    }

    private fun getTaxTypeListForValidation() {
        mainActivity.workTaxViewModel.getTaxTypes().observe(
            viewLifecycleOwner
        ) { taxTypes ->
            taxTypeList.clear()
            taxTypes.listIterator().forEach {
                taxTypeList.add(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}