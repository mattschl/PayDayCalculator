package ms.mattschlenkrich.paycalculator.workorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

class WorkOrderJobSpecUpdateFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                val workOrder = mainViewModel.getWorkOrder()
                val originalJobSpec by if (mainViewModel.getWorkOrderJobSpecId() != null) {
                    workOrderViewModel.getWorkOrderJobSpec(
                        mainViewModel.getWorkOrderJobSpecId()!!
                    ).observeAsState()
                } else {
                    remember {
                        androidx.compose.runtime.mutableStateOf(null)
                    }
                }

                val jobSpecSuggestions by workOrderViewModel.getJobSpecsAll()
                    .observeAsState(emptyList())
                val areaSuggestions by workOrderViewModel.getAreasList().observeAsState(emptyList())

                WorkOrderJobSpecUpdateScreen(
                    workOrder = workOrder,
                    originalJobSpec = originalJobSpec,
                    jobSpecSuggestions = jobSpecSuggestions,
                    areaSuggestions = areaSuggestions,
                    onUpdate = { jobSpecText, areaText, noteText ->
                        updateJobSpecInWorkOrderIfValid(
                            jobSpecText,
                            areaText,
                            noteText
                        )
                    },
                    onBack = { findNavController().navigateUp() }
                )
            }
        }
    }

    private fun updateJobSpecInWorkOrderIfValid(
        jobSpecText: String,
        areaText: String,
        noteText: String?
    ) {
        if (jobSpecText.isBlank()) {
            displayMessage(
                getString(R.string.error_) + getString(R.string.please_enter_a_valid_job_spec)
            )
            return
        }

        val originalId = mainViewModel.getWorkOrderJobSpecId()
        if (originalId == null) {
            displayMessage(getString(R.string.error_) + " Original Job Spec not found")
            return
        }

        lifecycleScope.launch {
            val jobSpec = workOrderViewModel.getOrCreateJobSpec(jobSpecText)
            val area = workOrderViewModel.getOrCreateArea(areaText)

            workOrderViewModel.updateWorkOrderJobSpec(
                originalId,
                jobSpec.jobSpecId,
                area?.areaId,
                noteText
            ) {
                gotoCallingFragment()
            }
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun gotoCallingFragment() {
        mainViewModel.setJobSpec(null)
        findNavController().navigate(
            WorkOrderJobSpecUpdateFragmentDirections
                .actionWorkOrderJobSpecUpdateFragmentToWorkOrderUpdateFragment()
        )
    }
}