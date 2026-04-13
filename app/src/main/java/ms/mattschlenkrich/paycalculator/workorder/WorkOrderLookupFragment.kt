package ms.mattschlenkrich.paycalculator.workorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_TIME
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

class WorkOrderLookupFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                val employer = mainViewModel.getEmployer()
                var searchQuery by remember { mutableStateOf("") }
                val workOrders by if (employer != null) {
                    if (searchQuery.isBlank()) {
                        workOrderViewModel.getWorkOrdersByEmployerId(employer.employerId)
                            .observeAsState(emptyList())
                    } else {
                        workOrderViewModel.searchWorkOrders(employer.employerId, "%$searchQuery%")
                            .observeAsState(emptyList())
                    }
                } else {
                    remember { mutableStateOf(emptyList<WorkOrder>()) }
                }

                WorkOrderLookupScreen(
                    employer = employer,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    workOrders = workOrders,
                    onWorkOrderSelected = { workOrder ->
                        chooseThisWorkOrder(workOrder)
                    },
                    onBackClick = { findNavController().navigateUp() }
                )
            }
        }
    }

    private fun chooseThisWorkOrder(workOrder: WorkOrder) {
        setViewModelValues(workOrder)
        gotoCallingFragment()
    }

    private fun setViewModelValues(workOrder: WorkOrder) {
        mainViewModel.apply {
            setWorkOrder(workOrder)
            setWorkOrderNumber(workOrder.woNumber)
            getTempWorkOrderHistoryInfo()?.let { history ->
                setTempWorkOrderHistoryInfo(
                    TempWorkOrderHistoryInfo(
                        history.woHistoryId,
                        workOrder.woNumber,
                        history.woHistoryWorkDate,
                        history.woHistoryRegHours,
                        history.woHistoryOtHours,
                        history.woHistoryDblOtHours,
                        history.woHistoryNote,
                        history.woWorkPerformed,
                        history.woArea,
                        history.woHistoryNote,
                        history.woMaterialQty,
                        history.woMaterial
                    )
                )
            }
        }
    }

    private fun gotoCallingFragment() {
        val callingFragment = mainViewModel.getCallingFragment() ?: return
        when {
            callingFragment.contains(FRAG_WORK_ORDER_HISTORY_UPDATE) -> {
                mainViewModel.removeCallingFragment(FRAG_WORK_ORDER_HISTORY_UPDATE)
                findNavController().navigate(
                    WorkOrderLookupFragmentDirections.actionWorkOrderLookupFragmentToWorkOrderHistoryUpdateFragment()
                )
            }

            callingFragment.contains(FRAG_WORK_ORDER_HISTORY_ADD) -> {
                mainViewModel.removeCallingFragment(FRAG_WORK_ORDER_HISTORY_ADD)
                findNavController().navigate(
                    WorkOrderLookupFragmentDirections.actionWorkOrderLookupFragmentToWorkOrderHistoryAddFragment()
                )
            }

            callingFragment.contains(FRAG_WORK_DATE_TIME) -> {
                mainViewModel.removeCallingFragment(FRAG_WORK_DATE_TIME)
                findNavController().navigate(
                    WorkOrderLookupFragmentDirections.actionWorkOrderLookupFragmentToWorkDateTimes()
                )
            }
        }
    }
}