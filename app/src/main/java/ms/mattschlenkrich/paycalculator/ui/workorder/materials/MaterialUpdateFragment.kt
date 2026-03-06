package ms.mattschlenkrich.paycalculator.ui.workorder.materials


import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.ExceptionUnknown
import ms.mattschlenkrich.paycalculator.common.FRAG_MATERIAL_VIEW
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.workorder.Material
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentMaterialUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

private const val TAG = "MaterialUpdateFragment"

class MaterialUpdateFragment : Fragment(R.layout.fragment_material_update) {

    private var _binding: FragmentMaterialUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    private val materialList = ArrayList<Material>()
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var oldMaterial: Material
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaterialUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        mainActivity.topMenuBar.title = getString(R.string.update_material_description)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInitialValues()
        setClickActions()
    }

    private fun setInitialValues() {
        populateMaterialListForValidation()
        if (mainViewModel.getMaterial() != null) {
            oldMaterial = mainViewModel.getMaterial()!!
            binding.apply {
                val display = getString(R.string.update_) + oldMaterial.mName
                tvTitle.text = display
                etMaterial.setText(oldMaterial.mName)
                etPrice.setText(nf.displayDollars(oldMaterial.mPrice))
                etCost.setText(nf.displayDollars(oldMaterial.mCost))
            }
        }
    }

    private fun populateMaterialListForValidation() {
        workOrderViewModel.getMaterialsList().observe(viewLifecycleOwner) { list ->
            materialList.clear()
            for (material in list.listIterator()) {
                materialList.add(material)
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            btnUpdate.setOnClickListener {
                updateMaterialIfValid()
            }
            btnCancel.setOnClickListener {
                gotoCallingFragment()
            }
            btnMerge.setOnClickListener {
                chooseMergeOptions()
            }
        }
    }

    private fun chooseMergeOptions() {
        AlertDialog.Builder(mView.context)
            .setTitle(
                getString(
                    R.string.choose_merge_option_for,
                    binding.etMaterial.text.toString().trim()
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
                    0 -> {
                        setOptionsForMergeAndGotoMerge(true)
                    }

                    1 -> {
                        setOptionsForMergeAndGotoMerge(false)
                    }
                }
            }
            .setNeutralButton(getString(R.string.cancel), null)
            .show()
    }

    private fun setOptionsForMergeAndGotoMerge(isMaster: Boolean) {
        mainScope.launch {
            try {
                updateMaterialIfValid(false)
                mainViewModel.setMaterialId(oldMaterial.materialId)
                mainViewModel.setWorkPerformedIsMaster(isMaster)
                mainViewModel.addCallingFragment(TAG)
                gotoMaterialMergeFragment()
            } catch (e: ExceptionUnknown) {
                Log.d(
                    TAG,
                    "exception is ${e.toString()}"
                )
            }

        }
    }

    private fun updateMaterialIfValid(gotoCallingFragment: Boolean = true) {
        val answer = validateMaterial()
        if (answer == ANSWER_OK) {
            updateMaterial(gotoCallingFragment)
        } else {
            displayMessage(getString(R.string.error_) + answer)
        }
    }

    private fun displayMessage(answer: String) {
        Toast.makeText(mView.context, answer, Toast.LENGTH_LONG).show()
    }

    private fun validateMaterial(): String {
        binding.apply {
            if (etMaterial.text.isNullOrBlank()) {
                return getString(R.string.please_enter_a_new_name_for_this_material)
            }
            if (etCost.text.isNullOrBlank()) {
                return getString(R.string.there_needs_to_be_a_cost_including_zero)
            }
            if (etPrice.text.isNullOrBlank()) {
                return getString(R.string.there_needs_to_be_a_price_including_zero)
            }
            for (material in materialList) {
                if (material.mName == etMaterial.text.toString()
                        .trim() && etMaterial.text.toString().trim() != oldMaterial.mName
                ) {
                    return getString(R.string.this_material_already_exists)
                }
            }
            if (nf.getDoubleFromDollars(etCost.text.toString()) > nf.getDoubleFromDollars(etPrice.text.toString())) {
                return getString(R.string.the_cost_is_greater_than_the_price)
            }
        }
        return ANSWER_OK
    }

    private fun getCurMaterial(): Material {
        binding.apply {
            return Material(
                oldMaterial.materialId,
                etMaterial.text.toString().trim(),
                nf.getDoubleFromDollars(etCost.text.toString().trim()),
                nf.getDoubleFromDollars(etPrice.text.toString().trim()),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun updateMaterial(gotoCallingFragment: Boolean = true) {
        workOrderViewModel.updateMaterial(getCurMaterial())
        if (gotoCallingFragment) gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        mainViewModel.apply {
            setMaterial(null)
            if (getCallingFragment()!!.contains(FRAG_MATERIAL_VIEW)) {
                gotoMaterialViewFragment()
            } else {
                gotoWorkOrderHistoryUpdateFragment()
            }
        }
    }

    private fun gotoMaterialViewFragment() {
        mView.findNavController().navigate(
            MaterialUpdateFragmentDirections.actionMaterialUpdateFragmentToMaterialViewFragment()
        )
    }

    private fun gotoWorkOrderHistoryUpdateFragment() {
        mView.findNavController().navigate(
            MaterialUpdateFragmentDirections.actionMaterialUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    private fun gotoMaterialMergeFragment() {
        mView.findNavController().navigate(
            MaterialUpdateFragmentDirections.actionMaterialUpdateFragmentToMaterialMergeFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}