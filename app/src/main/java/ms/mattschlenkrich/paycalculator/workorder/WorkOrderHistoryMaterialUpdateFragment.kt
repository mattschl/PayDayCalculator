package ms.mattschlenkrich.paycalculator.workorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.Material
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

class WorkOrderHistoryMaterialUpdateFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: ms.mattschlenkrich.paycalculator.data.MainViewModel
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

        val materialInSequence =
            mainViewModel.getMaterialInSequence() ?: return View(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {
                val materialList by workOrderViewModel.getMaterialsList()
                    .observeAsState(emptyList())
                val materialSuggestions = materialList.map { it.mName }

                var originalCombined by remember {
                    mutableStateOf<ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryMaterialCombined?>(
                        null
                    )
                }
                var originalHistoryWithDates by remember {
                    mutableStateOf<ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWithDates?>(
                        null
                    )
                }

                LaunchedEffect(materialInSequence) {
                    withContext(Dispatchers.IO) {
                        originalCombined = workOrderViewModel.getWorkOrderHistoryMaterialCombined(
                            materialInSequence.workOrderHistoryMaterialId
                        )
                        originalHistoryWithDates =
                            workOrderViewModel.getWorkOrderHistoryWithDateById(
                                materialInSequence.workOrderHistoryId
                            )
                    }
                }

                var materialName by remember { mutableStateOf(materialInSequence.mName) }
                var quantity by remember { mutableStateOf(nf.getNumberFromDouble(materialInSequence.mQty)) }

                val info = originalHistoryWithDates?.let {
                    getString(R.string.update_material_used_on) +
                            "${it.workDate.wdDate}\n" +
                            getString(R.string.for_work_order) +
                            "${it.workOrder.woNumber} @ ${it.workOrder.woAddress} \n ${it.workOrder.woDescription}"
                } ?: ""

                WorkOrderHistoryMaterialUpdateScreen(
                    info = info,
                    materialName = materialName,
                    onMaterialNameChange = { materialName = it },
                    materialSuggestions = materialSuggestions,
                    quantity = quantity,
                    onQuantityChange = { quantity = it },
                    originalMaterialLabel = getString(R.string.original_material__) + materialInSequence.mName,
                    originalQuantityLabel = getString(R.string.original_quantity__) + nf.getNumberFromDouble(
                        materialInSequence.mQty
                    ),
                    onDoneClick = {
                        lifecycleScope.launch {
                            val result =
                                handleDone(materialName, quantity, materialList, originalCombined)
                            if (result == ANSWER_OK) {
                                mainViewModel.setMaterialInSequence(null)
                                findNavController().navigate(
                                    WorkOrderHistoryMaterialUpdateFragmentDirections.actionWorkOrderHistoryMaterialUpdateFragmentToWorkOrderHistoryUpdateFragment()
                                )
                            } else if (result.isNotEmpty()) {
                                Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            }
        }
    }

    private suspend fun handleDone(
        materialName: String,
        quantityStr: String,
        materialList: List<Material>,
        originalCombined: ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryMaterialCombined?
    ): String {
        if (originalCombined == null) return ""

        if (materialName.trim() == originalCombined.material.mName &&
            quantityStr.toDoubleOrNull() == originalCombined.workOrderHistoryMaterial.wohmQuantity
        ) {
            return ANSWER_OK
        }

        if (materialName.isBlank()) {
            return getString(R.string.choose_a_valid_material)
        }

        val existingMaterialsInHistory = withContext(Dispatchers.IO) {
            workOrderViewModel.getMaterialsFromHistoryId(originalCombined.workOrderHistoryMaterial.wohmHistoryId)
        }

        for (m in existingMaterialsInHistory) {
            if (m.mName == materialName.trim() && m.mName != originalCombined.material.mName) {
                return getString(R.string.this_material_has_already_been_used_for_this_work_order_history)
            }
        }

        val targetMaterial =
            materialList.find { it.mName == materialName.trim() } ?: withContext(Dispatchers.IO) {
                val newMaterial = Material(
                    nf.generateRandomIdAsLong(),
                    materialName.trim(),
                    0.0,
                    0.0,
                    false,
                    df.getCurrentTimeAsString()
                )
                workOrderViewModel.insertMaterial(newMaterial)
                newMaterial
            }

        withContext(Dispatchers.IO) {
            workOrderViewModel.updateWorkOrderHistoryMaterial(
                WorkOrderHistoryMaterial(
                    originalCombined.workOrderHistoryMaterial.workOrderHistoryMaterialId,
                    originalCombined.workOrderHistoryMaterial.wohmHistoryId,
                    targetMaterial.materialId,
                    quantityStr.toDoubleOrNull() ?: 1.0,
                    originalCombined.workOrderHistoryMaterial.wohmSequence,
                    false,
                    df.getCurrentTimeAsString()
                )
            )
        }

        return ANSWER_OK
    }
}