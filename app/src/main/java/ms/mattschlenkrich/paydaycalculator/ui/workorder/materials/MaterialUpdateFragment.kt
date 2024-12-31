package ms.mattschlenkrich.paydaycalculator.ui.workorder.materials

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_MATERIAL_VIEW
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.Material
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentMaterialUpdateBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class MaterialUpdateFragment
    : Fragment(R.layout.fragment_material_update) {

    private var _binding: FragmentMaterialUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity

    private val materialList = ArrayList<Material>()
    private lateinit var oldMaterial: Material
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaterialUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.update_material_description)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInitialValues()
        setClickActions()
    }

    private fun setInitialValues() {
        populateMaterialListForValidation()
        if (mainActivity.mainViewModel.getMaterial() != null) {
            oldMaterial = mainActivity.mainViewModel.getMaterial()!!
            binding.apply {
                val display = getString(R.string.update_) +
                        oldMaterial.mName
                tvTitle.text = display
                etMaterial.setText(oldMaterial.mName)
                etPrice.setText(nf.displayDollars(oldMaterial.mPrice))
                etCost.setText(nf.displayDollars(oldMaterial.mCost))
            }
        }
    }

    private fun populateMaterialListForValidation() {
        mainActivity.workOrderViewModel.getMaterialsList()
            .observe(viewLifecycleOwner) { list ->
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
        }
    }

    private fun updateMaterialIfValid() {
        val answer = validateMaterial()
        if (answer == ANSWER_OK) {
            updateMaterial()
            gotoCallingFragment()
        } else {
            Toast.makeText(
                mView.context,
                answer,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun validateMaterial(): String {
        binding.apply {
            if (etMaterial.text.isNullOrBlank()) {
                return getString(R.string.error_) +
                        getString(R.string.please_enter_a_new_name_for_this_material)
            }
            if (etCost.text.isNullOrBlank()) {
                return getString(R.string.error_) +
                        getString(R.string.there_needs_to_be_a_cost_including_zero)
            }
            if (etPrice.text.isNullOrBlank()) {
                return getString(R.string.error_) +
                        getString(R.string.there_needs_to_be_a_price_including_zero)
            }
            for (material in materialList) {
                if (material.mName == etMaterial.text.toString().trim() &&
                    etMaterial.text.toString().trim() != oldMaterial.mName
                ) {
                    return getString(R.string.error_) +
                            getString(R.string.this_material_already_exists)
                }
            }
            if (nf.getDoubleFromDollars(etCost.text.toString()) >
                nf.getDoubleFromDollars(etPrice.text.toString())
            ) {
                return getString(R.string.error_) +
                        getString(R.string.the_cost_is_greater_than_the_price)
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

    private fun updateMaterial() {
        mainActivity.workOrderViewModel.updateMaterial(
            getCurMaterial()
        )
    }

    private fun gotoCallingFragment() {
        mainActivity.mainViewModel.setMaterial(null)
        if (mainActivity.mainViewModel.getCallingFragment()!!
                .contains(FRAG_MATERIAL_VIEW)
        ) {
            gotoMaterialViewFragment()
        } else {
            gotoWorkOrderHistoryUpdateFragment()
        }
    }

    private fun gotoMaterialViewFragment() {
        mView.findNavController().navigate(
            MaterialUpdateFragmentDirections
                .actionMaterialUpdateFragmentToMaterialViewFragment()
        )
    }

    private fun gotoWorkOrderHistoryUpdateFragment() {
        mView.findNavController().navigate(
            MaterialUpdateFragmentDirections
                .actionMaterialUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}