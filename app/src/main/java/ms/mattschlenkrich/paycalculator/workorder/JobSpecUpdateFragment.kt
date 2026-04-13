package ms.mattschlenkrich.paycalculator.workorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import ms.mattschlenkrich.paycalculator.common.FRAG_JOB_SPEC_VIEW
import ms.mattschlenkrich.paycalculator.data.JobSpec
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

class JobSpecUpdateFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                val oldJobSpec = mainViewModel.getJobSpec()
                var jobSpecName by remember { mutableStateOf(oldJobSpec?.jsName ?: "") }
                val jobSpecList by workOrderViewModel.getJobSpecsAll().observeAsState(emptyList())

                JobSpecUpdateScreen(
                    title = if (oldJobSpec != null) {
                        getString(R.string.update_) + oldJobSpec.jsName
                    } else {
                        getString(R.string.update_job_spec)
                    },
                    jobSpecName = jobSpecName,
                    onJobSpecNameChange = { jobSpecName = it },
                    onUpdateClick = {
                        if (oldJobSpec != null) {
                            updateJobSpecIfValid(oldJobSpec, jobSpecName, jobSpecList)
                        }
                    },
                    onCancelClick = {
                        gotoCallingFragment()
                    },
                    onMergeClick = {
                        if (oldJobSpec != null) {
                            // Merge logic is similar to WorkPerformed, but not yet fully implemented in legacy
                            // For now, let's keep it consistent with WorkPerformed pattern if possible,
                            // or show a toast if not supported yet.
                            displayMessage("Merge not yet implemented for Job Specs")
                        }
                    }
                )
            }
        }
    }

    private fun updateJobSpecIfValid(
        oldJobSpec: JobSpec,
        newName: String,
        jobSpecList: List<JobSpec>
    ) {
        val answer = validateJobSpec(oldJobSpec, newName, jobSpecList)
        if (answer == ANSWER_OK) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    workOrderViewModel.updateJobSpec(
                        JobSpec(
                            oldJobSpec.jobSpecId,
                            newName.trim(),
                            false,
                            df.getCurrentTimeAsString()
                        )
                    )
                }
                gotoCallingFragment()
            }
        } else {
            displayMessage(getString(R.string.error_) + answer)
        }
    }

    private fun validateJobSpec(
        oldJobSpec: JobSpec,
        newName: String,
        jobSpecList: List<JobSpec>
    ): String {
        if (newName.isBlank()) {
            return getString(R.string.please_enter_a_valid_job_spec)
        }
        val trimmed = newName.trim()
        if (jobSpecList.any { it.jsName == trimmed && it.jobSpecId != oldJobSpec.jobSpecId }) {
            return getString(R.string.this_job_spec_already_exists)
        }
        return ANSWER_OK
    }

    private fun displayMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun gotoCallingFragment() {
        mainViewModel.setJobSpec(null)
        val callingFragment = mainViewModel.getCallingFragment()
        if (callingFragment != null && callingFragment.contains(FRAG_JOB_SPEC_VIEW)) {
            findNavController().navigate(
                JobSpecUpdateFragmentDirections.actionJobSpecUpdateFragmentToJobSpecViewFragment()
            )
        } else {
            findNavController().navigate(
                JobSpecUpdateFragmentDirections.actionJobSpecUpdateFragmentToWorkOrderUpdateFragment()
            )
        }
    }
}