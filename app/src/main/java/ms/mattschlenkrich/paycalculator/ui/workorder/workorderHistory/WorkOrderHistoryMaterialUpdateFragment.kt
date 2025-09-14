package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.workorder.Material
import ms.mattschlenkrich.paycalculator.database.model.workorder.MaterialInSequence
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryMaterialCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkOrderHistoryMaterialBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class WorkOrderHistoryMaterialUpdateFragment :
    Fragment(R.layout.fragment_work_order_history_material) {

    private var _binding: FragmentWorkOrderHistoryMaterialBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var materialListForAutoComplete: List<Material>
    private lateinit var existingMaterialsInHistory: List<Material>
    private var curMaterial: Material? = null
    private lateinit var materialInSequence: MaterialInSequence
    private lateinit var originalWorkOrderHistoryMaterialCombined: WorkOrderHistoryMaterialCombined
    private lateinit var originalWorkOrderHistoryWithDates: WorkOrderHistoryWithDates
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val defaultScope = CoroutineScope(Dispatchers.Default)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderHistoryMaterialBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        mainActivity.title = getString(R.string.update_material_in_history)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateInitialValues()
        setClickActions()
    }


    private fun populateInitialValues() {
        mainScope.launch {
            populateMaterialListForAutoComplete()
            getMaterialVariables()
            delay(WAIT_250)
            populateFromHistory()
        }
    }

    private suspend fun getMaterialVariables() {
        withContext(Dispatchers.Default) {
            if (mainViewModel.getMaterialInSequence() != null) {
                val materialInSequenceDefer = async {
                    mainViewModel.getMaterialInSequence()!!
                }
                materialInSequence = materialInSequenceDefer.await()
                val workOrderHistoryMaterialDefer = async {
                    workOrderViewModel.getWorkOrderHistoryMaterialCombined(
                        materialInSequenceDefer.await().workOrderHistoryMaterialId
                    )
                }
                originalWorkOrderHistoryMaterialCombined = workOrderHistoryMaterialDefer.await()
                originalWorkOrderHistoryWithDates =
                    workOrderViewModel.getWorkOrderHistoryCombinedById(
                        workOrderHistoryMaterialDefer.await().workOrderHistoryMaterial.wohmHistoryId
                    )
                existingMaterialsInHistory = workOrderViewModel.getMaterialsFromHistoryId(
                    workOrderHistoryMaterialDefer.await().workOrderHistoryMaterial.wohmHistoryId
                )
            }
        }
    }

    private fun populateMaterialListForAutoComplete() {
        workOrderViewModel.getMaterialsList().observe(viewLifecycleOwner) { list ->
            materialListForAutoComplete = list
            val materialListNames = ArrayList<String>()
            list.listIterator().forEach { materialListNames.add(it.mName) }
            val mAdapter = ArrayAdapter(
                mView.context, R.layout.spinner_item_normal, materialListNames
            )
            binding.acMaterials.setAdapter(mAdapter)
        }
    }

    private fun populateFromHistory() {
        binding.apply {
            var display = getString(R.string.original_material__) + materialInSequence.mName
            lblMaterials.text = display
            acMaterials.setText(materialInSequence.mName)
            display =
                getString(R.string.original_quantity__) + nf.getNumberFromDouble(materialInSequence.mQty)
            lblQuantity.text = display
            etQuantity.setText(nf.getNumberFromDouble(materialInSequence.mQty))
            display =
                getString(R.string.update_material_used_on) + "${originalWorkOrderHistoryWithDates.workDate.wdDate}\n" + getString(
                    R.string.for_work_order
                ) + "${originalWorkOrderHistoryWithDates.workOrder.woNumber} @ " + "${originalWorkOrderHistoryWithDates.workOrder.woAddress} \n " + originalWorkOrderHistoryWithDates.workOrder.woDescription
            tvInfo.text = display
        }
    }


    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener { updateMaterialInHistoryIfValid() }
            acMaterials.setOnItemClickListener { _, _, _, _ ->
                defaultScope.launch { setCurMaterial() }
            }
        }
    }

    private fun updateMaterialInHistoryIfValid() {
        binding.apply {
            if (etQuantity.text.isNullOrBlank()) etQuantity.setText("1")
            if (acMaterials.text.toString()
                    .trim() == originalWorkOrderHistoryMaterialCombined.material.mName && etQuantity.text.toString()
                    .toDouble() == originalWorkOrderHistoryMaterialCombined.workOrderHistoryMaterial.wohmQuantity
            ) {
                gotoWorkOrderHistoryUpdate()
            } else {
                val answer = validateMaterial()
                if (answer == ANSWER_OK) {
                    defaultScope.launch {
                        if (setCurMaterial()) {
                            updateMaterialInHistory(curMaterial!!)
                        } else {
                            val newMaterialDeferred = async { insertMaterialIntoDatabase() }
                            updateMaterialInHistory(newMaterialDeferred.await())
                        }
                        delay(WAIT_250)
                        withContext(Dispatchers.Main) { gotoCallingFragment() }
                    }
                } else {
                    displayMessage(getString(R.string.error_) + answer)
                }
            }
        }
    }

    private fun insertMaterialIntoDatabase(): Material {
        val newMaterial = Material(
            nf.generateRandomIdAsLong(),
            binding.acMaterials.text.toString().trim(),
            0.0,
            0.0,
            false,
            df.getCurrentTimeAsString()
        )
        workOrderViewModel.insertMaterial(newMaterial)
        return newMaterial
    }

    private fun validateMaterial(): String {
        binding.apply {
            if (acMaterials.text.isNullOrBlank()) {
                return getString(R.string.choose_a_valid_material)
            }
            for (material in existingMaterialsInHistory) {
                if (material.mName == acMaterials.text.toString()
                        .trim() && acMaterials.text.toString()
                        .trim() != originalWorkOrderHistoryMaterialCombined.material.mName
                ) {
                    return getString(R.string.this_material_has_already_been_used_for_this_work_order_history)
                }
            }
        }
        return ANSWER_OK
    }

    private fun displayMessage(message: String) {
        Toast.makeText(mView.context, message, Toast.LENGTH_LONG).show()
    }

    private fun updateMaterialInHistory(material: Material) {
        workOrderViewModel.updateWorkOrderHistoryMaterial(
            WorkOrderHistoryMaterial(
                originalWorkOrderHistoryMaterialCombined.workOrderHistoryMaterial.workOrderHistoryMaterialId,
                originalWorkOrderHistoryMaterialCombined.workOrderHistoryMaterial.wohmHistoryId,
                material.materialId,
                if (binding.etQuantity.text.isNullOrBlank()) 0.0
                else binding.etQuantity.text.toString().toDouble(),
                originalWorkOrderHistoryMaterialCombined.workOrderHistoryMaterial.wohmSequence,
                false,
                df.getCurrentTimeAsString()
            )
        )
    }

    private fun setCurMaterial(): Boolean {
        binding.apply {
            if (curMaterial?.mName == acMaterials.text.toString().trim()) {
                return true
            } else {
                for (material in materialListForAutoComplete) {
                    if (acMaterials.text.toString() == material.mName && !acMaterials.text.isNullOrBlank()) {
                        curMaterial = material
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun gotoCallingFragment() {
        if (!mainViewModel.getCallingFragment().isNullOrBlank()) {
            mainViewModel.setMaterialInSequence(null)
            val callingFragment = mainViewModel.getCallingFragment()!!
            if (callingFragment.contains(FRAG_WORK_ORDER_HISTORY_UPDATE)) {
                gotoWorkOrderHistoryUpdate()
            }
        }
    }

    private fun gotoWorkOrderHistoryUpdate() {
        mView.findNavController().navigate(
            WorkOrderHistoryMaterialUpdateFragmentDirections.actionWorkOrderHistoryMaterialUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun onDestroy() {
        mainScope.cancel()
        defaultScope.cancel()
        super.onDestroy()
        _binding = null
    }
}