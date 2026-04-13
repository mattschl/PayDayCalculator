package ms.mattschlenkrich.paycalculator.workorder

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_MATERIAL_VIEW
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.Material
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

private const val TAG = "MaterialUpdateFragment"

class MaterialUpdateFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private var materialList = listOf<Material>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel

        val oldMaterial = mainViewModel.getMaterial() ?: return View(requireContext())

        workOrderViewModel.getMaterialsList().observe(viewLifecycleOwner) {
            materialList = it
        }

        return ComposeView(requireContext()).apply {
            setContent {
                var name by remember { mutableStateOf(oldMaterial.mName) }
                var cost by remember { mutableStateOf(nf.displayDollars(oldMaterial.mCost)) }
                var price by remember { mutableStateOf(nf.displayDollars(oldMaterial.mPrice)) }

                MaterialUpdateScreen(
                    name = name,
                    onNameChange = { name = it },
                    cost = cost,
                    onCostChange = { cost = it },
                    price = price,
                    onPriceChange = { price = it },
                    onUpdateClick = {
                        val validation = validateMaterial(name, cost, price, oldMaterial)
                        if (validation == ANSWER_OK) {
                            updateMaterial(name, cost, price, oldMaterial)
                            gotoCallingFragment()
                        } else {
                            Toast.makeText(requireContext(), validation, Toast.LENGTH_LONG).show()
                        }
                    },
                    onMergeClick = {
                        val validation = validateMaterial(name, cost, price, oldMaterial)
                        if (validation == ANSWER_OK) {
                            chooseMergeOptions(name, cost, price, oldMaterial)
                        } else {
                            Toast.makeText(requireContext(), validation, Toast.LENGTH_LONG).show()
                        }
                    },
                    onCancelClick = { gotoCallingFragment() },
                    title = stringResource(R.string.update_) + oldMaterial.mName
                )
            }
        }
    }

    private fun validateMaterial(
        name: String,
        cost: String,
        price: String,
        oldMaterial: Material
    ): String {
        if (name.isBlank()) {
            return getString(R.string.please_enter_a_new_name_for_this_material)
        }
        if (cost.isBlank()) {
            return getString(R.string.there_needs_to_be_a_cost_including_zero)
        }
        if (price.isBlank()) {
            return getString(R.string.there_needs_to_be_a_price_including_zero)
        }
        for (material in materialList) {
            if (material.mName == name.trim() && name.trim() != oldMaterial.mName
            ) {
                return getString(R.string.this_material_already_exists)
            }
        }
        if (nf.getDoubleFromDollars(cost) > nf.getDoubleFromDollars(price)) {
            return getString(R.string.the_cost_is_greater_than_the_price)
        }
        return ANSWER_OK
    }

    private fun updateMaterial(
        name: String,
        cost: String,
        price: String,
        oldMaterial: Material
    ) {
        val material = Material(
            oldMaterial.materialId,
            name.trim(),
            nf.getDoubleFromDollars(cost.trim()),
            nf.getDoubleFromDollars(price.trim()),
            oldMaterial.mIsDeleted,
            df.getCurrentTimeAsString()
        )
        workOrderViewModel.updateMaterial(material)
        mainViewModel.setMaterial(material)
    }

    private fun chooseMergeOptions(
        name: String,
        cost: String,
        price: String,
        oldMaterial: Material
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(
                getString(
                    R.string.choose_merge_option_for,
                    name.trim()
                )
            )
            .setItems(
                arrayOf(
                    "Make this a Parent material and add children",
                    "Add this to another material as a child",
                    "*Note: This will attempt to save the current Material."
                )
            ) { _, pos ->
                when (pos) {
                    0 -> setOptionsForMergeAndGotoMerge(name, cost, price, oldMaterial, true)
                    1 -> setOptionsForMergeAndGotoMerge(name, cost, price, oldMaterial, false)
                }
            }
            .setNeutralButton(getString(R.string.cancel), null)
            .show()
    }

    private fun setOptionsForMergeAndGotoMerge(
        name: String,
        cost: String,
        price: String,
        oldMaterial: Material,
        isMaster: Boolean
    ) {
        lifecycleScope.launch {
            updateMaterial(name, cost, price, oldMaterial)
            mainViewModel.setMaterialId(oldMaterial.materialId)
            mainViewModel.setMaterialIsParent(isMaster)
            mainViewModel.addCallingFragment(TAG)
            findNavController().navigate(
                MaterialUpdateFragmentDirections.actionMaterialUpdateFragmentToMaterialMergeFragment()
            )
        }
    }

    private fun gotoCallingFragment() {
        mainViewModel.setMaterial(null)
        val callingFragment = mainViewModel.getCallingFragment()
        if (callingFragment != null && callingFragment.contains(FRAG_MATERIAL_VIEW)) {
            findNavController().navigate(
                MaterialUpdateFragmentDirections.actionMaterialUpdateFragmentToMaterialViewFragment()
            )
        } else {
            findNavController().navigate(
                MaterialUpdateFragmentDirections.actionMaterialUpdateFragmentToWorkOrderHistoryUpdateFragment()
            )
        }
    }
}