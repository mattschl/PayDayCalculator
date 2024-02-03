package ms.mattschlenkrich.paydaycalculator.ui.tax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTaxTypeUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.TaxTypes

class TaxTypeUpdateFragment : Fragment(R.layout.fragment_tax_type_update) {

    private var _binding: FragmentTaxTypeUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()

    //    private val cf = CommonFunctions()
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
        getTaxTypeList()
        fillMenu()
        addActions()
        fillValues()
    }

    private fun fillValues() {
        if (mainActivity.mainViewModel.getTaxType() != null) {
            curTaxType = mainActivity.mainViewModel.getTaxType()!!
            binding.apply {
                etTaxType.setText(curTaxType.taxType)
            }
        }
    }

    private fun addActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updateWorkTaxType()
            }
        }
    }

    private fun gotoCallingFragment() {
        mainActivity.mainViewModel.setTaxType(null)
        mView.findNavController().navigate(
            TaxTypeUpdateFragmentDirections
                .actionTaxTypeUpdateFragmentToTaxTypeFragment()
        )
    }

    private fun fillMenu() {
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
                true,
                df.getCurrentTimeAsString()
            )
        )
        gotoCallingFragment()
    }

    private fun updateWorkTaxType() {
        val message = checkTaxType()
        if (message == ANSWER_OK) {
            mainActivity.workTaxViewModel.updateWorkTaxType(
                TaxTypes(
                    curTaxType.taxTypeId,
                    binding.etTaxType.text.toString(),
                    false,
                    df.getCurrentTimeAsString()
                )
            )
            gotoCallingFragment()
        } else {
            Toast.makeText(
                mView.context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
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
            } else if (nameFound && etTaxType.text.toString() != curTaxType.taxType) {
                "    ERROR!!\n" +
                        "This tax type already exists!"
            } else {
                ANSWER_OK
            }
            return errorMessage
        }
    }

    private fun getTaxTypeList() {
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