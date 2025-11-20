package ms.mattschlenkrich.paycalculator.ui.workorder.area

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
import ms.mattschlenkrich.paycalculator.common.FRAG_AREA_VIEW
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_UPDATE
import ms.mattschlenkrich.paycalculator.database.model.workorder.Areas
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentSingleItemUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class AreaUpdateFragment : Fragment(R.layout.fragment_single_item_update) {

    private var _binding: FragmentSingleItemUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private val df = DateFunctions()

    private lateinit var areasList: List<Areas>
    private lateinit var oldArea: Areas

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSingleItemUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        mainActivity.topMenuBar.title = getString(R.string.update_area_description)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setValues()
        setClickActions()
    }

    private fun setValues() {
        populateAreasListForValidation()
        if (mainViewModel.getAreaId() != null) {
            workOrderViewModel.getArea(mainViewModel.getAreaId()!!).observe(
                viewLifecycleOwner
            ) { area ->
                oldArea = area
                binding.apply {
                    val display = getString(R.string.update_area_description_for) + oldArea.areaName
                    tvTitle.text = display
                    etItem.setText(oldArea.areaName)
                }
            }
        }
    }

    private fun populateAreasListForValidation() {
        workOrderViewModel.getAreasList().observe(viewLifecycleOwner) { list ->
            areasList = list
        }
    }

    private fun setClickActions() {
        binding.apply {
            btnUpdate.setOnClickListener {
                updateAreaDescriptionIfValid()
            }
            btnCancel.setOnClickListener {
                gotoCallingFragment()
            }
        }
    }

    private fun updateAreaDescriptionIfValid() {
        val answer = validateArea()
        if (answer == ANSWER_OK) {
            updateArea()
        } else {
            displayMessage(getString(R.string.error_) + answer)
        }
    }

    private fun displayMessage(answer: String) {
        Toast.makeText(mView.context, answer, Toast.LENGTH_LONG).show()
    }

    private fun validateArea(): String {
        binding.apply {
            if (etItem.text.isNullOrBlank()) {
                return getString(R.string.please_enter_a_valid_description_of_the_area)
            }
            for (area in areasList) {
                if (area.areaName == etItem.text.toString().trim() && etItem.text.toString()
                        .trim() != oldArea.areaName
                ) {
                    return getString(R.string.this_area_description_already_exists)
                }
            }
        }
        return ANSWER_OK
    }

    private fun updateArea() {
        workOrderViewModel.updateArea(
            Areas(
                oldArea.areaId,
                binding.etItem.text.toString().trim(),
                false,
                df.getCurrentTimeAsString()
            )
        )
        gotoCallingFragment()
    }

    private fun gotoCallingFragment() {
        mainViewModel.setAreaId(null)
        val callingFragment = mainActivity.mainViewModel.getCallingFragment()!!
        if (callingFragment.contains(
                FRAG_AREA_VIEW
            )
        ) {
            gotoAreaViewFragment()
        } else if (callingFragment.contains(
                FRAG_WORK_ORDER_HISTORY_UPDATE
            )
        ) {
            gotoWorkOrderHistoryUpdateFragment()
        } else if (callingFragment.contains(
                FRAG_WORK_ORDER_UPDATE
            )
        ) {
            gotoWorkOrderUpdateFragment()
        }
    }

    private fun gotoAreaViewFragment() {
        mView.findNavController().navigate(
            AreaUpdateFragmentDirections.actionAreaUpdateFragmentToAreaViewFragment()
        )
    }

    private fun gotoWorkOrderHistoryUpdateFragment() {
        mView.findNavController().navigate(
            AreaUpdateFragmentDirections.actionAreaUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    private fun gotoWorkOrderUpdateFragment() {
        mView.findNavController().navigate(
            AreaUpdateFragmentDirections.actionAreaUpdateFragmentToWorkOrderUpdateFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}