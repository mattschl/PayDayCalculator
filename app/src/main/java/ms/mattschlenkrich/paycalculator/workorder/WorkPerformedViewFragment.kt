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
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_PERFORMED_VIEW
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

class WorkPerformedViewFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workOrderViewModel = mainActivity.workOrderViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                var searchQuery by remember { mutableStateOf("") }
                val workPerformedList by if (searchQuery.isEmpty()) {
                    workOrderViewModel.getWorkPerformedAll().observeAsState(emptyList())
                } else {
                    workOrderViewModel.searchFromWorkPerformed("%$searchQuery%")
                        .observeAsState(emptyList())
                }

                WorkPerformedViewScreen(
                    workPerformedList = workPerformedList,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onWorkPerformedClick = { workPerformed ->
                        mainViewModel.setCallingFragment(FRAG_WORK_PERFORMED_VIEW)
                        mainViewModel.setWorkPerformedId(workPerformed.workPerformedId)
                        findNavController().navigate(
                            WorkPerformedViewFragmentDirections.actionWorkPerformedViewFragmentToWorkPerformedUpdateFragment()
                        )
                    }
                )
            }
        }
    }
}