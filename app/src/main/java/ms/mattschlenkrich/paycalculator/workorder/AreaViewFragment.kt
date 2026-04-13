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
import androidx.navigation.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.common.FRAG_AREA_VIEW
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

private const val TAG = FRAG_AREA_VIEW

class AreaViewFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        workOrderViewModel = mainActivity.workOrderViewModel
        mainViewModel = mainActivity.mainViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                var searchQuery by remember { mutableStateOf("") }
                val areaList by if (searchQuery.isEmpty()) {
                    workOrderViewModel.getAreasList().observeAsState(emptyList())
                } else {
                    workOrderViewModel.searchAreas("%$searchQuery%").observeAsState(emptyList())
                }

                AreaViewScreen(
                    areaList = areaList,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onAreaClick = { area ->
                        gotoAreaUpdate(area.areaId)
                    }
                )
            }
        }
    }

    private fun gotoAreaUpdate(areaId: Long) {
        mainViewModel.apply {
            setCallingFragment(TAG)
            setAreaId(areaId)
        }
        gotoAreaUpdateFragment()
    }

    fun gotoAreaUpdateFragment() {
        view?.findNavController()?.navigate(
            AreaViewFragmentDirections.actionAreaViewFragmentToAreaUpdateFragment()
        )
    }
}