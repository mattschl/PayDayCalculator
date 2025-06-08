package ms.mattschlenkrich.paycalculator.ui.workorder.materials

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.database.model.workorder.MaterialInSequence
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentMaterialQuantityUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class MaterialQuantityUpdateFragment : Fragment(R.layout.fragment_material_quantity_update) {

    var _binding: FragmentMaterialQuantityUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private lateinit var material: MaterialInSequence

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaterialQuantityUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
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
            mainViewModel.getWorkDateObject()!!.wdDate
        )
        material = mainViewModel.getMaterialInSequence()!!
        val display =
            getString(R.string.update_quantity_of) + nf.getNumberFromDouble(material.mQty) + getString(
                R.string._for_
            ) + material.mName + getString(R.string._on_) + date
        binding.apply {
            tvDetails.text = display
            etNewQuantity.setText(nf.getNumberFromDouble(material.mQty))
        }
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                updateMaterialQuantityIfValid()
            }
        }
    }

    private fun updateMaterialQuantityIfValid() {
        val answer = validateQuantity()
        if (answer == ANSWER_OK) {
            val newQty = binding.etNewQuantity.text.toString().trim().toDouble()
            if (newQty != material.mQty) {
                workOrderViewModel.updateWorkOrderHistoryMaterial(
                    WorkOrderHistoryMaterial(
                        material.workOrderHistoryMaterialId,
                        material.workOrderHistoryId,
                        material.materialId,
                        newQty,
                        material.mSequence,
                        false,
                        df.getCurrentTimeAsString()
                    )
                )
            }
            gotoWorkOrderHistoryUpdate()
        } else {
            showMessage(getString(R.string.error_) + answer)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun validateQuantity(): String {
        if (binding.etNewQuantity.text.isNullOrBlank()) {
            return getString(R.string.please_enter_a_new_quantity)
        }
        return ANSWER_OK
    }

    private fun gotoWorkOrderHistoryUpdate() {
        mainViewModel.setMaterialInSequence(null)
        gotoWorkOrderHistoryUpdateFragment()
    }

    private fun gotoWorkOrderHistoryUpdateFragment() {
        mView.findNavController().navigate(
            MaterialQuantityUpdateFragmentDirections.actionMaterialQuantityUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}