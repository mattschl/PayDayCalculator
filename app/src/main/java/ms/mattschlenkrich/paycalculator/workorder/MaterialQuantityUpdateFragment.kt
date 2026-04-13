package ms.mattschlenkrich.paycalculator.workorder

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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

class MaterialQuantityUpdateFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel

        val material = mainViewModel.getMaterialInSequence() ?: return View(requireContext())
        val date = df.getDisplayDate(
            mainViewModel.getWorkDateObject()!!.wdDate
        )
        val details = getString(R.string.update_quantity_of) +
                nf.getNumberFromDouble(material.mQty) +
                getString(R.string._for_) + material.mName +
                getString(R.string._on_) + date

        return ComposeView(requireContext()).apply {
            setContent {
                var quantity by remember { mutableStateOf(nf.getNumberFromDouble(material.mQty)) }

                MaterialQuantityUpdateScreen(
                    details = details,
                    quantity = quantity,
                    onQuantityChange = { quantity = it },
                    onDoneClick = {
                        val validation = validateQuantity(quantity)
                        if (validation == ANSWER_OK) {
                            val newQty = quantity.trim().toDouble()
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
                            mainViewModel.setMaterialInSequence(null)
                            findNavController().navigate(
                                MaterialQuantityUpdateFragmentDirections.actionMaterialQuantityUpdateFragmentToWorkOrderHistoryUpdateFragment()
                            )
                        } else {
                            Toast.makeText(requireContext(), validation, Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }
    }

    private fun validateQuantity(quantity: String): String {
        if (quantity.isBlank()) {
            return getString(R.string.please_enter_a_new_quantity)
        }
        return ANSWER_OK
    }
}