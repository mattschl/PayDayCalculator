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
import ms.mattschlenkrich.paycalculator.common.FRAG_MATERIAL_VIEW
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

class MaterialViewFragment : Fragment() {

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
                val materialList by if (searchQuery.isEmpty()) {
                    workOrderViewModel.getMaterialsList().observeAsState(emptyList())
                } else {
                    workOrderViewModel.searchMaterials("%$searchQuery%").observeAsState(emptyList())
                }

                MaterialViewScreen(
                    materialList = materialList,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onMaterialClick = { material ->
                        mainViewModel.setCallingFragment(FRAG_MATERIAL_VIEW)
                        mainViewModel.setMaterial(material)
                        findNavController().navigate(
                            MaterialViewFragmentDirections.actionMaterialViewFragmentToMaterialUpdateFragment()
                        )
                    }
                )
            }
        }
    }
}