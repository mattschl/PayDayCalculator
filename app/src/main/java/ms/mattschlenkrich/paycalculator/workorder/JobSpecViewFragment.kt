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
import ms.mattschlenkrich.paycalculator.common.FRAG_JOB_SPEC_VIEW
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

class JobSpecViewFragment : Fragment() {

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
                val jobSpecList by if (searchQuery.isEmpty()) {
                    workOrderViewModel.getJobSpecsAll().observeAsState(emptyList())
                } else {
                    workOrderViewModel.searchJobSpecs("%$searchQuery%").observeAsState(emptyList())
                }

                JobSpecViewScreen(
                    jobSpecList = jobSpecList,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onJobSpecClick = { jobSpec ->
                        mainViewModel.setCallingFragment(FRAG_JOB_SPEC_VIEW)
                        mainViewModel.setJobSpec(jobSpec)
                        findNavController().navigate(
                            JobSpecViewFragmentDirections.actionJobSpecViewFragmentToJobSpecUpdateFragment()
                        )
                    }
                )
            }
        }
    }
}