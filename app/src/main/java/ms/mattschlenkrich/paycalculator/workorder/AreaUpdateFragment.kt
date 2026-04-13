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
import androidx.navigation.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_AREA_VIEW
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_UPDATE
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_UPDATE
import ms.mattschlenkrich.paycalculator.data.Areas
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

class AreaUpdateFragment : Fragment() {

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
                val areaId = mainViewModel.getAreaId()
                if (areaId == null) {
                    gotoCallingFragment()
                    return@setContent
                }

                val areaList by workOrderViewModel.getAreasList().observeAsState(emptyList())
                val oldArea by workOrderViewModel.getArea(areaId).observeAsState()

                oldArea?.let { area ->
                    var name by remember(area.areaId) { mutableStateOf(area.areaName) }

                    AreaUpdateScreen(
                        name = name,
                        onNameChange = { name = it },
                        title = getString(R.string.update_area_description_for) + area.areaName,
                        onUpdateClick = {
                            validateAndUpdate(name, area, areaList)
                        },
                        onCancelClick = {
                            gotoCallingFragment()
                        }
                    )
                }
            }
        }
    }

    private fun validateAndUpdate(name: String, oldArea: Areas, areaList: List<Areas>) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            displayMessage(getString(R.string.please_enter_a_valid_description_of_the_area))
            return
        }

        if (areaList.any { it.areaName == trimmedName && it.areaId != oldArea.areaId }) {
            displayMessage(getString(R.string.this_area_description_already_exists))
            return
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                workOrderViewModel.updateArea(
                    Areas(
                        oldArea.areaId,
                        trimmedName,
                        false,
                        df.getCurrentTimeAsString()
                    )
                ).join()
            }
            withContext(Dispatchers.Main) {
                gotoCallingFragment()
            }
        }
    }

    private fun displayMessage(answer: String) {
        Toast.makeText(requireContext(), answer, Toast.LENGTH_LONG).show()
    }

    private fun gotoCallingFragment() {
        mainViewModel.setAreaId(null)
        val callingFragment = mainViewModel.getCallingFragment()
        if (callingFragment == null) {
            view?.findNavController()?.navigateUp()
            return
        }

        val navController = view?.findNavController()
        when {
            callingFragment.contains(FRAG_AREA_VIEW) -> {
                navController?.navigate(
                    AreaUpdateFragmentDirections.actionAreaUpdateFragmentToAreaViewFragment()
                )
            }

            callingFragment.contains(FRAG_WORK_ORDER_HISTORY_UPDATE) -> {
                navController?.navigate(
                    AreaUpdateFragmentDirections.actionAreaUpdateFragmentToWorkOrderHistoryUpdateFragment()
                )
            }

            callingFragment.contains(FRAG_WORK_ORDER_UPDATE) -> {
                navController?.navigate(
                    AreaUpdateFragmentDirections.actionAreaUpdateFragmentToWorkOrderUpdateFragment()
                )
            }

            else -> navController?.navigateUp()
        }
    }
}