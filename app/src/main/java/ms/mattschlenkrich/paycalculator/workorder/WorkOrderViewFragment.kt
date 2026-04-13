package ms.mattschlenkrich.paycalculator.workorder

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDERS
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

private const val TAG = FRAG_WORK_ORDERS

class WorkOrderViewFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var employerViewModel: EmployerViewModel

    // Compose states
    private var searchQueryState by mutableStateOf("")

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
                val selectedEmployer = mainViewModel.getEmployer()
                val workOrdersLiveData = if (selectedEmployer != null) {
                    if (searchQueryState.isBlank()) {
                        workOrderViewModel.getWorkOrdersByEmployerId(selectedEmployer.employerId)
                    } else {
                        workOrderViewModel.searchWorkOrders(
                            selectedEmployer.employerId,
                            "%$searchQueryState%"
                        )
                    }
                } else {
                    null
                }
                val workOrders by workOrdersLiveData?.observeAsState(emptyList())
                    ?: androidx.compose.runtime.remember { mutableStateOf(emptyList<WorkOrder>()) }

                WorkOrderViewScreen(
                    employers = employers,
                    selectedEmployer = selectedEmployer,
                    onEmployerSelected = { employer ->
                        mainViewModel.setEmployer(employer)
                    },
                    onAddNewEmployerClick = { gotoEmployerAdd() },
                    searchQuery = searchQueryState,
                    onSearchQueryChange = { searchQueryState = it },
                    onResetSearchClick = { searchQueryState = "" },
                    workOrders = workOrders,
                    onWorkOrderClick = { workOrder -> chooseOptions(workOrder) },
                    onAddNewWorkOrderClick = { addNewWorkOrder(selectedEmployer) }
                )
            }
        }
    }

    private fun chooseOptions(workOrder: WorkOrder) {
        AlertDialog.Builder(requireContext()).setTitle(
            getString(R.string.choose_option_for_wo) + workOrder.woNumber
        ).setMessage(
            getString(R.string.would_you_like_to_open_this_work_order_to_view_or_edit_it)
        ).setPositiveButton(getString(R.string.open)) { _, _ ->
            setWorkOrder(workOrder)
            gotoWorkOrderUpdateFragment()
        }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun setWorkOrder(workOrder: WorkOrder) {
        mainViewModel.apply {
            setWorkOrder(workOrder)
            setWorkOrderNumber(workOrder.woNumber)
        }
    }

    internal fun gotoWorkOrderUpdateFragment() {
        mainViewModel.setCallingFragment(TAG)
        findNavController().navigate(
            WorkOrderViewFragmentDirections.actionWorkOrderViewFragmentToWorkOrderUpdateFragment()
        )
    }

    private fun gotoEmployerAdd() {
        mainViewModel.apply {
            setCallingFragment(TAG)
            setEmployer(null)
        }
        findNavController().navigate(
            WorkOrderViewFragmentDirections.actionWorkOrderViewFragmentToEmployerAddFragment()
        )
    }

    private fun addNewWorkOrder(employer: Employers?) {
        mainViewModel.setEmployer(employer)
        findNavController().navigate(
            WorkOrderViewFragmentDirections.actionWorkOrderViewFragmentToWorkOrderAddFragment()
        )
    }
}