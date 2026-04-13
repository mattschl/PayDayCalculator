package ms.mattschlenkrich.paycalculator.workorder

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.WorkPerformedAndChild
import ms.mattschlenkrich.paycalculator.data.WorkPerformedMerged

class WorkPerformedMergeFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel

    private val df = DateFunctions()
    private val nf = NumberFunctions()

    private var wpParent by mutableStateOf<WorkPerformed?>(null)
    private var wpChild by mutableStateOf<WorkPerformed?>(null)
    private var parentDescription by mutableStateOf("")
    private var childDescription by mutableStateOf("")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                val workPerformedList by workOrderViewModel.getWorkPerformedAll()
                    .observeAsState(emptyList())

                val childList by if (wpParent != null) {
                    workOrderViewModel.getWorkPerformedAndChildList(wpParent!!.workPerformedId)
                        .observeAsState(emptyList())
                } else {
                    remember { mutableStateOf(emptyList()) }
                }

                // Initial selection from cache
                LaunchedEffect(Unit) {
                    val cachedId = mainViewModel.getWorkPerformedId()
                    if (cachedId != null) {
                        val wp = withContext(Dispatchers.IO) {
                            workOrderViewModel.getWorkPerformedSync(cachedId)
                        }
                        if (wp != null) {
                            if (mainViewModel.getWorkPerformedIsMaster()) {
                                chooseAsParent(wp)
                            } else {
                                chooseAsChild(wp)
                            }
                        }
                    }
                }

                WorkPerformedMergeScreen(
                    workPerformedList = workPerformedList,
                    parentDescription = parentDescription,
                    onParentDescriptionChange = { description ->
                        parentDescription = description
                        wpParent = workPerformedList.find { it.wpDescription == description }
                    },
                    onParentSelected = {
                        chooseAsParent(it)
                    },
                    childList = childList,
                    onRemoveChild = {
                        workOrderViewModel.deleteWorkPerformedMerged(it.workPerformedMerged.workPerformedMergeId)
                    },
                    childDescription = childDescription,
                    onChildDescriptionChange = { description ->
                        childDescription = description
                        wpChild = workPerformedList.find { it.wpDescription == description }
                    },
                    onChildSelected = {
                        chooseAsChild(it)
                    },
                    onMergeClick = {
                        if (parentDescription.isNotBlank()) {
                            lifecycleScope.launch {
                                val existingParent = withContext(Dispatchers.IO) {
                                    workOrderViewModel.getWorkPerformedSync(parentDescription)
                                }
                                if (existingParent == null) {
                                    chooseToAddDescriptionAndMerge(parentDescription)
                                } else {
                                    wpParent = existingParent
                                    val currentChildDesc = childDescription
                                    if (currentChildDesc.isNotBlank()) {
                                        val existingChild = withContext(Dispatchers.IO) {
                                            workOrderViewModel.getWorkPerformedSync(currentChildDesc)
                                        }
                                        if (existingChild == null) {
                                            chooseToAddChildAndMerge(currentChildDesc)
                                        } else if (existingChild.workPerformedId != existingParent.workPerformedId) {
                                            wpChild = existingChild
                                            chooseOptionsForMerge(existingParent, existingChild) {
                                                wpChild = null
                                                childDescription = ""
                                            }
                                        }
                                    } else if (wpChild != null && wpChild!!.workPerformedId != existingParent.workPerformedId) {
                                        chooseOptionsForMerge(existingParent, wpChild!!) {
                                            wpChild = null
                                            childDescription = ""
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onDoneClick = {
                        findNavController().navigate(
                            WorkPerformedMergeFragmentDirections.actionWorkPerformedMergeFragmentToWorkPerformedUpdateFragment()
                        )
                    },
                    onListItemSelected = {
                        if (wpParent == null) {
                            chooseAsParent(it)
                        } else {
                            chooseAsChild(it)
                        }
                    }
                )
            }
        }
    }

    fun chooseAsParent(workPerformed: WorkPerformed) {
        wpParent = workPerformed
        parentDescription = workPerformed.wpDescription
        mainViewModel.setWorkPerformedId(workPerformed.workPerformedId)
        mainViewModel.setWorkPerformedIsMaster(true)
    }

    fun chooseAsChild(workPerformed: WorkPerformed) {
        wpChild = workPerformed
        childDescription = workPerformed.wpDescription
    }

    fun removeWorkPerformedChild(workPerformedAndChild: WorkPerformedAndChild) {
        workOrderViewModel.deleteWorkPerformedMerged(workPerformedAndChild.workPerformedMerged.workPerformedMergeId)
    }

    private fun chooseToAddDescriptionAndMerge(newDescription: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_new_work_performed))
            .setMessage(
                getString(
                    R.string.the_description_does_not_exist_would_you_like_to_add_it,
                    newDescription
                )
            )
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                lifecycleScope.launch {
                    val existing = withContext(Dispatchers.IO) {
                        workOrderViewModel.getWorkPerformedSync(newDescription)
                    }
                    if (existing == null) {
                        val newWp = WorkPerformed(
                            nf.generateRandomIdAsLong(),
                            newDescription,
                            false,
                            df.getCurrentTimeAsString()
                        )
                        workOrderViewModel.insertWorkPerformed(newWp).join()
                        val newlyCreated = withContext(Dispatchers.IO) {
                            workOrderViewModel.getWorkPerformedSync(newDescription)
                        }
                        if (newlyCreated != null) {
                            chooseAsParent(newlyCreated)
                        }
                    } else {
                        chooseAsParent(existing)
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun chooseToAddChildAndMerge(newDescription: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_new_work_performed))
            .setMessage(
                getString(
                    R.string.the_description_does_not_exist_would_you_like_to_add_it,
                    newDescription
                )
            )
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                lifecycleScope.launch {
                    val existing = withContext(Dispatchers.IO) {
                        workOrderViewModel.getWorkPerformedSync(newDescription)
                    }
                    if (existing == null) {
                        val newWp = WorkPerformed(
                            nf.generateRandomIdAsLong(),
                            newDescription,
                            false,
                            df.getCurrentTimeAsString()
                        )
                        workOrderViewModel.insertWorkPerformed(newWp).join()
                        val newlyCreated = withContext(Dispatchers.IO) {
                            workOrderViewModel.getWorkPerformedSync(newDescription)
                        }
                        if (newlyCreated != null) {
                            chooseAsChild(newlyCreated)
                        }
                    } else {
                        chooseAsChild(existing)
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun chooseOptionsForMerge(
        parent: WorkPerformed,
        child: WorkPerformed,
        onMerged: () -> Unit
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_merge_option))
            .setMessage(getString(R.string.either_merge_and_replace_or_merge_))
            .setPositiveButton(getString(R.string.merge_and_replace)) { _, _ ->
                mergeAndReplace(parent, child, onMerged)
            }
            .setNeutralButton(getString(R.string.merge_and_keep)) { _, _ ->
                mergeAndKeep(parent, child, onMerged)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun mergeAndReplace(parent: WorkPerformed, child: WorkPerformed, onMerged: () -> Unit) {
        lifecycleScope.launch {
            try {
                workOrderViewModel.updateWorkPerformedMerged(
                    child.workPerformedId,
                    parent.workPerformedId
                )
                delay(WAIT_100)
                workOrderViewModel.deleteWorkPerformedMerged(child.workPerformedId)
                workOrderViewModel.deleteWorkPerformed(child)
                onMerged()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    private fun mergeAndKeep(parent: WorkPerformed, child: WorkPerformed, onMerged: () -> Unit) {
        workOrderViewModel.insertWorkPerformedMerged(
            WorkPerformedMerged(
                nf.generateRandomIdAsLong(),
                parent.workPerformedId,
                child.workPerformedId,
                false,
                df.getCurrentTimeAsString()
            )
        )
        onMerged()
    }
}