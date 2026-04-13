package ms.mattschlenkrich.paycalculator.workorder

import android.app.AlertDialog
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
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_PERFORMED_UPDATE
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_PERFORMED_VIEW
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.WorkPerformed

class WorkPerformedUpdateFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                val workPerformedId = mainViewModel.getWorkPerformedId()
                var currentDescription by remember { mutableStateOf("") }
                var oldWorkPerformed by remember { mutableStateOf<WorkPerformed?>(null) }
                val workPerformedList by workOrderViewModel.getWorkPerformedAll()
                    .observeAsState(emptyList())

                LaunchedEffect(workPerformedId) {
                    if (workPerformedId != null) {
                        val wp = withContext(Dispatchers.IO) {
                            workOrderViewModel.getWorkPerformedSync(workPerformedId)
                        }
                        if (wp != null) {
                            oldWorkPerformed = wp
                            currentDescription = wp.wpDescription
                        }
                    }
                }

                WorkPerformedUpdateScreen(
                    currentDescription = currentDescription,
                    onDescriptionChange = { currentDescription = it },
                    onUpdateClick = {
                        val wpToUpdate = oldWorkPerformed
                        if (wpToUpdate != null) {
                            updateWorkPerformedIfValid(
                                wpToUpdate,
                                currentDescription,
                                workPerformedList,
                                true
                            )
                        }
                    },
                    onMergeClick = {
                        val wpToUpdate = oldWorkPerformed
                        if (wpToUpdate != null) {
                            updateWorkPerformedIfValid(
                                wpToUpdate,
                                currentDescription,
                                workPerformedList,
                                false
                            ) {
                                chooseMergeOptions(wpToUpdate.workPerformedId, currentDescription)
                            }
                        }
                    },
                    onCancelClick = {
                        gotoCallingFragment()
                    },
                    title = if (oldWorkPerformed != null) {
                        getString(R.string.update_work_description_) + oldWorkPerformed!!.wpDescription
                    } else {
                        getString(R.string.update_work_performed_description)
                    }
                )
            }
        }
    }

    private fun updateWorkPerformedIfValid(
        oldWorkPerformed: WorkPerformed,
        newDescription: String,
        workPerformedList: List<WorkPerformed>,
        shouldNavigateBack: Boolean,
        onSuccess: (() -> Unit)? = null
    ) {
        val answer = validateWorkPerformed(oldWorkPerformed, newDescription, workPerformedList)
        if (answer == ANSWER_OK) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    workOrderViewModel.updateWorkPerformed(
                        WorkPerformed(
                            oldWorkPerformed.workPerformedId,
                            newDescription.trim(),
                            false,
                            df.getCurrentTimeAsString()
                        )
                    )
                }
                if (onSuccess != null) {
                    onSuccess()
                } else if (shouldNavigateBack) {
                    gotoCallingFragment()
                }
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_) + answer, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun validateWorkPerformed(
        oldWorkPerformed: WorkPerformed,
        newDescription: String,
        workPerformedList: List<WorkPerformed>
    ): String {
        if (newDescription.isBlank()) {
            return getString(R.string.please_enter_a_valid_work_performed_description)
        }
        val trimmed = newDescription.trim()
        if (workPerformedList.any { it.wpDescription == trimmed && it.workPerformedId != oldWorkPerformed.workPerformedId }) {
            return getString(R.string.this_work_performed_description_already_exists)
        }
        return ANSWER_OK
    }

    private fun chooseMergeOptions(workPerformedId: Long, description: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_merge_option_for, description.trim()))
            .setItems(
                arrayOf(
                    "Make this a master description and add children",
                    "Add this to another description as a child",
                    "*Note: This will attempt to save the current Work Performed description."
                )
            ) { _, pos ->
                when (pos) {
                    0 -> setOptionsForMergeAndGotoMerge(workPerformedId, true)
                    1 -> setOptionsForMergeAndGotoMerge(workPerformedId, false)
                }
            }
            .setNeutralButton(getString(R.string.cancel), null)
            .show()
    }

    private fun setOptionsForMergeAndGotoMerge(workPerformedId: Long, isMaster: Boolean) {
        mainViewModel.setWorkPerformedId(workPerformedId)
        mainViewModel.setWorkPerformedIsMaster(isMaster)
        mainViewModel.addCallingFragment(FRAG_WORK_PERFORMED_UPDATE)
        findNavController().navigate(
            WorkPerformedUpdateFragmentDirections.actionWorkPerformedUpdateFragmentToWorkPerformedMergeFragment()
        )
    }

    private fun gotoCallingFragment() {
        mainViewModel.setWorkPerformedId(null)
        val callingFragment = mainViewModel.getCallingFragment()
        if (callingFragment != null && callingFragment.contains(FRAG_WORK_PERFORMED_VIEW)) {
            findNavController().navigate(
                WorkPerformedUpdateFragmentDirections.actionWorkPerformedUpdateFragmentToWorkPerformedViewFragment()
            )
        } else {
            findNavController().navigate(
                WorkPerformedUpdateFragmentDirections.actionWorkPerformedUpdateFragmentToWorkOrderHistoryUpdateFragment()
            )
        }
    }
}