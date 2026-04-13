package ms.mattschlenkrich.paycalculator.workorder

import android.app.AlertDialog
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
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_TIME
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

class WorkOrderAddFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var employerViewModel: EmployerViewModel

    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        employerViewModel = mainActivity.employerViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                val employers by employerViewModel.getEmployers().observeAsState(emptyList())
                val fixedEmployer = remember { mainViewModel.getEmployer() }

                var selectedEmployer by remember { mutableStateOf(fixedEmployer) }
                var woNumber by remember {
                    mutableStateOf(
                        mainViewModel.getTempWorkOrderHistoryInfo()?.woHistoryWorkOrderNumber
                            ?: mainViewModel.getWorkOrderNumber()
                            ?: ""
                    )
                }
                var address by remember { mutableStateOf("") }
                var description by remember { mutableStateOf("") }

                val workOrdersByEmployer by workOrderViewModel.getWorkOrdersByEmployerId(
                    selectedEmployer?.employerId ?: -1L
                ).observeAsState(emptyList())

                WorkOrderAddScreen(
                    employers = employers,
                    selectedEmployer = selectedEmployer,
                    onEmployerSelected = { selectedEmployer = it },
                    fixedEmployerName = fixedEmployer?.employerName,
                    woNumber = woNumber,
                    onWoNumberChange = { woNumber = it },
                    address = address,
                    onAddressChange = { address = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    onDoneClick = {
                        val validationResult = validateWorkOrder(
                            woNumber,
                            selectedEmployer,
                            address,
                            description,
                            workOrdersByEmployer
                        )
                        if (validationResult == ANSWER_OK) {
                            saveWorkOrder(
                                woNumber,
                                selectedEmployer!!,
                                address,
                                description,
                                false
                            )
                        } else {
                            displayMessage(getString(R.string.error_) + validationResult)
                        }
                    },
                    onBackClick = { findNavController().popBackStack() }
                )
            }
        }
    }

    private fun validateWorkOrder(
        woNumber: String,
        selectedEmployer: Employers?,
        address: String,
        description: String,
        workOrderList: List<WorkOrder>
    ): String {
        if (selectedEmployer == null) {
            return getString(R.string.no_employers_add_an_employer_through_the_employer_tab)
        }
        if (woNumber.isEmpty()) {
            return getString(R.string.please_enter_a_work_order_number)
        }
        for (workOrder in workOrderList) {
            if (workOrder.woNumber == woNumber) {
                return getString(R.string.this_work_order_has_been_used)
            }
        }
        if (address.isEmpty()) {
            return getString(R.string.please_enter_an_address)
        }
        if (description.isEmpty()) {
            return getString(R.string.please_enter_a_description)
        }
        return ANSWER_OK
    }

    private fun saveWorkOrder(
        woNumber: String,
        employer: Employers,
        address: String,
        description: String,
        gotoNextStep: Boolean
    ) {
        val newWorkOrder = WorkOrder(
            nf.generateRandomIdAsLong(),
            woNumber,
            employer.employerId,
            address.trim(),
            description.trim(),
            false,
            df.getCurrentTimeAsString()
        )
        workOrderViewModel.insertWorkOrder(newWorkOrder)
        mainViewModel.apply {
            setWorkOrder(newWorkOrder)
            setWorkOrderNumber(newWorkOrder.woNumber)
        }

        if (gotoNextStep) {
            chooseToGotoUpdate()
        } else {
            // In the legacy code, long click on FAB sets gotoNextStep to true.
            // But the Screen currently only has onDoneClick.
            // I'll show the dialog by default or just go back depending on requirements.
            // Legacy had saveWorkOrderAndAddJobSpecIfValid() for normal click
            // which calls saveWorkOrderAndAChooseNextSteps(false)
            // which calls gotoCallingFragment().
            // And long click called saveWorkOrderAndAddJobSpecIfValid(true) 
            // which calls chooseToGotoUpdate().

            // For now, I'll follow the normal click behavior: gotoCallingFragment.
            // But maybe I should ask the user if they want to update job specs anyway?
            // The legacy code's normal click did NOT ask.
            gotoCallingFragment()
        }
    }

    private fun chooseToGotoUpdate() {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.choose_the_next_step))
            .setMessage(getString(R.string.would_you_like_to_update_job_specs_for_this_work_order))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> gotoWorkOrderUpdate() }
            .setNegativeButton(getString(R.string.no)) { _, _ -> gotoCallingFragment() }
            .setNeutralButton(getString(R.string.cancel), null).show()
    }

    private fun displayMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun gotoCallingFragment() {
        val frag = mainViewModel.getCallingFragment()
        when {
            frag?.contains(FRAG_WORK_DATE_TIME) == true -> {
                findNavController().navigate(
                    WorkOrderAddFragmentDirections.actionWorkOrderAddFragmentToWorkDateTimes()
                )
            }

            frag?.contains(FRAG_WORK_ORDER_HISTORY_ADD) == true -> {
                findNavController().navigate(
                    WorkOrderAddFragmentDirections.actionWorkOrderAddFragmentToWorkOrderHistoryAddFragment()
                )
            }

            else -> {
                findNavController().popBackStack()
            }
        }
    }

    private fun gotoWorkOrderUpdate() {
        displayMessage(getString(R.string.work_order_has_been_added_automatically_before_adding_work_specs))
        findNavController().navigate(
            WorkOrderAddFragmentDirections.actionWorkOrderAddFragmentToWorkOrderUpdateFragment()
        )
    }
}