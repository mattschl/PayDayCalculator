package ms.mattschlenkrich.paycalculator.workorder

import android.app.AlertDialog
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.Areas
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.WorkPerformed

private const val TAG = "WorkOrderHistoryWorkPerf"

class WorkOrderHistoryWorkPerformedUpdateFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel

    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                val originalWorkOrderHistory by if (mainViewModel.getWorkOrderHistory() != null) {
                    workOrderViewModel.getWorkOrderHistoriesById(
                        mainViewModel.getWorkOrderHistory()!!.woHistoryId
                    ).observeAsState()
                } else {
                    androidx.compose.runtime.remember {
                        androidx.compose.runtime.mutableStateOf(null)
                    }
                }

                val originalWorkPerformedHistory by if (mainViewModel.getWorkPerformedHistoryId() != null) {
                    workOrderViewModel.getWorkPerformedHistoryById(
                        mainViewModel.getWorkPerformedHistoryId()!!
                    ).observeAsState()
                } else {
                    androidx.compose.runtime.remember {
                        androidx.compose.runtime.mutableStateOf(null)
                    }
                }

                val workPerformedSuggestions by workOrderViewModel.getWorkPerformedAll()
                    .observeAsState(emptyList())
                val areaSuggestions by workOrderViewModel.getAreasList().observeAsState(emptyList())

                WorkOrderHistoryWorkPerformedUpdateScreen(
                    originalWorkOrderHistory = originalWorkOrderHistory,
                    originalWorkPerformedHistory = originalWorkPerformedHistory,
                    workPerformedSuggestions = workPerformedSuggestions,
                    areaSuggestions = areaSuggestions,
                    onUpdate = { workPerformedText, areaText, noteText ->
                        updateWorkPerformedInHistoryIfValid(
                            workPerformedText,
                            areaText,
                            noteText
                        )
                    },
                    onBack = { findNavController().navigateUp() }
                )
            }
        }
    }

    private fun updateWorkPerformedInHistoryIfValid(
        workPerformedText: String,
        areaText: String,
        noteText: String?
    ) {
        if (workPerformedText.isBlank()) {
            displayMessage(
                getString(R.string.error_) + getString(R.string.please_enter_a_valid_work_performed_description)
            )
            return
        }

        mainScope.launch {
            val workPerformed = async {
                getOrCreateWorkPerformed(workPerformedText)
            }.await()

            val area = async {
                getOrCreateArea(areaText)
            }.await()

            val historyId = mainViewModel.getWorkOrderHistory()?.woHistoryId
            if (historyId == null) {
                displayMessage(getString(R.string.error_) + " Work Order History not found")
                return@launch
            }

            val historyWorkPerformedCombinedList =
                workOrderViewModel.getWorkPerformedByWorkOrderHistorySync(historyId)

            val originalHistoryItem =
                mainViewModel.getWorkPerformedHistoryId()?.let { id ->
                    workOrderViewModel.getWorkPerformedHistoryByIdSync(id)
                }

            val isUnique =
                historyWorkPerformedCombinedList.none { combined: ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWorkPerformedCombined ->
                    combined.workPerformed.workPerformedId == workPerformed?.workPerformedId &&
                            combined.area?.areaId == area?.areaId &&
                            combined.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId !=
                            originalHistoryItem?.workOrderHistoryWorkPerformed?.workOrderHistoryWorkPerformedId
                }

            if (isUnique && workPerformed != null) {
                originalHistoryItem?.let { original ->
                    updateWorkHistory(
                        original,
                        workPerformed.workPerformedId,
                        area?.areaId,
                        noteText
                    )
                    gotoCallingFragment()
                }
            } else if (workPerformed == null) {
                displayMessage(getString(R.string.error_) + " Could not create Work Performed")
            } else {
                displayMessage(
                    getString(R.string.error_) + getString(R.string.this_work_performed_and_area_combination_is_already_in_this_work_history)
                )
            }
        }
    }

    private suspend fun getOrCreateWorkPerformed(name: String): WorkPerformed? {
        val existing = workOrderViewModel.getWorkPerformedAllSync().find {
            it.wpDescription.trim().equals(name.trim(), ignoreCase = true)
        }
        if (existing != null) return existing

        val newWorkPerformed = WorkPerformed(
            nf.generateRandomIdAsLong(),
            name.trim(),
            false,
            df.getCurrentTimeAsString()
        )
        return try {
            workOrderViewModel.insertWorkPerformed(newWorkPerformed)
            newWorkPerformed
        } catch (e: SQLiteException) {
            Log.d(TAG, e.toString())
            null
        }
    }

    private suspend fun getOrCreateArea(name: String): Areas? {
        if (name.isBlank()) return null
        val existing = workOrderViewModel.getAreasListSync().find {
            it.areaName.trim().equals(name.trim(), ignoreCase = true)
        }
        if (existing != null) return existing

        val newArea = Areas(
            nf.generateRandomIdAsLong(),
            name.trim(),
            false,
            df.getCurrentTimeAsString()
        )
        return try {
            workOrderViewModel.insertArea(newArea)
            newArea
        } catch (e: SQLiteException) {
            Log.e(TAG, "Error inserting area", e)
            null
        }
    }

    private fun updateWorkHistory(
        original: ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWorkPerformedCombined,
        workPerformedId: Long,
        areaId: Long?,
        note: String?
    ) {
        try {
            workOrderViewModel.updateWorkOrderHistoryWorkPerformed(
                WorkOrderHistoryWorkPerformed(
                    original.workOrderHistoryWorkPerformed.workOrderHistoryWorkPerformedId,
                    original.workOrderHistoryWorkPerformed.wowpHistoryId,
                    workPerformedId,
                    areaId,
                    note,
                    original.workOrderHistoryWorkPerformed.wowpSequence,
                    false,
                    df.getCurrentTimeAsString()
                )
            )
        } catch (e: SQLiteException) {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.something_went_wrong))
                .setMessage(getString(R.string.check_to_see_if_this_work_was_already_entered_) + " " + e.toString())
                .setNeutralButton(getString(R.string.ok), null).show()
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun gotoCallingFragment() {
        if (mainViewModel.getCallingFragment()?.contains(FRAG_WORK_ORDER_HISTORY_UPDATE) == true) {
            mainViewModel.apply {
                setWorkPerformedHistoryId(null)
                setWorkPerformedId(null)
                setAreaId(null)
            }
            findNavController().navigate(
                WorkOrderHistoryWorkPerformedUpdateFragmentDirections
                    .actionWorkOrderHistoryWorkPerformedUpdateFragmentToWorkOrderHistoryUpdateFragment()
            )
        }
    }

    override fun onDestroyView() {
        mainScope.cancel()
        super.onDestroyView()
    }
}