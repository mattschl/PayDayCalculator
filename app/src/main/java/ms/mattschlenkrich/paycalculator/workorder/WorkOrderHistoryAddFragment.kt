package ms.mattschlenkrich.paycalculator.workorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paycalculator.data.WorkOrder
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

class WorkOrderHistoryAddFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private lateinit var commonFunctions: WorkOrderCommonFunctions

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        commonFunctions = WorkOrderCommonFunctions(mainActivity)

        return ComposeView(requireContext()).apply {
            setContent {
                val workDateObject = commonFunctions.getWorkDateObject()
                val employer = commonFunctions.getEmployer()
                val workOrderList by if (workDateObject != null) {
                    workOrderViewModel.getWorkOrdersByEmployerId(workDateObject.wdEmployerId)
                        .observeAsState(initial = emptyList())
                } else {
                    androidx.compose.runtime.remember {
                        androidx.compose.runtime.mutableStateOf(
                            emptyList<WorkOrder>()
                        )
                    }
                }

                val tempInfo = mainViewModel.getTempWorkOrderHistoryInfo()
                val initialWorkOrderNumber =
                    mainViewModel.getWorkOrderNumber() ?: tempInfo?.woHistoryWorkOrderNumber ?: ""
                val initialRegHours = nf.getNumberFromDouble(tempInfo?.woHistoryRegHours ?: 0.0)
                val initialOtHours = nf.getNumberFromDouble(tempInfo?.woHistoryOtHours ?: 0.0)
                val initialDblOtHours = nf.getNumberFromDouble(tempInfo?.woHistoryDblOtHours ?: 0.0)
                val initialNote = tempInfo?.woHistoryNote ?: ""

                WorkOrderHistoryAddScreen(
                    workOrderList = workOrderList,
                    initialWorkOrderNumber = initialWorkOrderNumber,
                    initialRegHours = initialRegHours,
                    initialOtHours = initialOtHours,
                    initialDblOtHours = initialDblOtHours,
                    initialNote = initialNote,
                    onWorkOrderSearch = { number, reg, ot, dbl, note ->
                        saveTempInfo(number, reg, ot, dbl, note)
                        mainViewModel.addCallingFragment("FRAG_WORK_ORDER_HISTORY_ADD")
                        findNavController().navigate(
                            WorkOrderHistoryAddFragmentDirections.actionWorkOrderHistoryAddFragmentToWorkOrderLookupFragment()
                        )
                    },
                    onWorkOrderAddEdit = { number, reg, ot, dbl, note, isEdit ->
                        saveTempInfo(number, reg, ot, dbl, note)
                        mainViewModel.setCallingFragment("FRAG_WORK_ORDER_HISTORY_ADD")
                        if (isEdit) {
                            mainViewModel.setWorkOrderNumber(number)
                            findNavController().navigate(
                                WorkOrderHistoryAddFragmentDirections.actionWorkOrderHistoryAddFragmentToWorkOrderUpdateFragment()
                            )
                        } else {
                            findNavController().navigate(
                                WorkOrderHistoryAddFragmentDirections.actionWorkOrderHistoryAddFragmentToWorkOrderAddFragment()
                            )
                        }
                    },
                    onDone = { number, reg, ot, dbl, note, chooseNextSteps ->
                        val currentWorkOrder = workOrderList.find { it.woNumber == number }
                        if (currentWorkOrder != null) {
                            val history = WorkOrderHistory(
                                nf.generateRandomIdAsLong(),
                                currentWorkOrder.workOrderId,
                                workDateObject!!.workDateId,
                                reg.toDoubleOrNull() ?: 0.0,
                                ot.toDoubleOrNull() ?: 0.0,
                                dbl.toDoubleOrNull() ?: 0.0,
                                note.ifBlank { null },
                                false,
                                df.getCurrentTimeAsString()
                            )
                            workOrderViewModel.insertWorkOrderHistory(history)
                            mainViewModel.setWorkOrderHistory(history)
                            mainViewModel.setTempWorkOrderHistoryInfo(null)
                            findNavController().navigate(
                                WorkOrderHistoryAddFragmentDirections.actionWorkOrderHistoryAddFragmentToWorkOrderHistoryUpdateFragment()
                            )
                        }
                    },
                    onBack = {
                        findNavController().popBackStack()
                    },
                    displayDate = workDateObject?.let { df.getDisplayDate(it.wdDate) } ?: "",
                    displayEmployer = employer?.employerName ?: ""
                )
            }
        }
    }

    private fun saveTempInfo(number: String, reg: String, ot: String, dbl: String, note: String) {
        mainViewModel.setTempWorkOrderHistoryInfo(
            TempWorkOrderHistoryInfo(
                0,
                number.ifBlank { "000" },
                "",
                reg.toDoubleOrNull() ?: 0.0,
                ot.toDoubleOrNull() ?: 0.0,
                dbl.toDoubleOrNull() ?: 0.0,
                note,
                "", "", "", 0.0, ""
            )
        )
    }

    private fun displayMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}