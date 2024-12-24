package ms.mattschlenkrich.paydaycalculator.ui.workorder.materials

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.MaterialInSequence
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentMaterialQuantityUpdateBinding
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class MaterialQuantityUpdateFragment : Fragment(R.layout.fragment_material_quantity_update) {

    var _binding: FragmentMaterialQuantityUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private lateinit var material: MaterialInSequence

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaterialQuantityUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.update_quantity)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateDetails()
        setClickActions()
    }

    private fun populateDetails() {
        val date = df.getDisplayDate(
            mainActivity.mainViewModel.getWorkDateObject()!!.wdDate
        )
        material = mainActivity.mainViewModel.getMaterialInSequence()!!
        val display = "Update quantity of ${nf.getNumberFromDouble(material.mQty)} " +
                "for ${material.mName} on $date"
        binding.apply {
            tvDetails.text = display
            etNewQuantity.setText(nf.getNumberFromDouble(material.mQty))
        }
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updateMaterialQuantity()
            }
        }
    }

    private fun updateMaterialQuantity() {
        val newQty = binding.etNewQuantity.text.toString().trim().toDouble()
        if (newQty != material.mQty) {
            mainActivity.workOrderViewModel.updateWorkOrderHistoryMaterial(
                WorkOrderHistoryMaterial(
                    material.workOrderHistoryMaterialId,
                    material.workOrderHistoryId,
                    material.materialId,
                    binding.etNewQuantity.text.toString().trim().toDouble(),
                    material.mSequence,
                    false,
                    df.getCurrentTimeAsString()
                )
            )
        }
        gotoWorkOrderHistoryUpdate()
    }

    private fun gotoWorkOrderHistoryUpdate() {
        mainActivity.mainViewModel.setMaterialInSequence(null)
        mView.findNavController().navigate(
            MaterialQuantityUpdateFragmentDirections
                .actionMaterialQuantityUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}