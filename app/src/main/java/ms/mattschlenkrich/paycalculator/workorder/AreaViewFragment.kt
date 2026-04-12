package ms.mattschlenkrich.paycalculator.workorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.FRAG_AREA_VIEW
import ms.mattschlenkrich.paycalculator.common.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.data.Areas
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
                AreaViewScreen(
                    workOrderViewModel = workOrderViewModel,
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

@Composable
fun AreaViewScreen(
    workOrderViewModel: WorkOrderViewModel,
    onAreaClick: (Areas) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val areaList by if (searchQuery.isEmpty()) {
        workOrderViewModel.getAreasList().observeAsState(emptyList())
    } else {
        workOrderViewModel.searchAreas("%$searchQuery%").observeAsState(emptyList())
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Since the search is in the top bar (MainActivity's Toolbar), 
            // and we are migrating ONLY this fragment, we might still rely on 
            // the MainActivity's menu for search for now, OR we add a search bar here.
            // The user requested to upgrade THIS fragment to compose.

            if (areaList.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_areas_in_the_list_to_view),
                    modifier = Modifier
                        .padding(SCREEN_PADDING_HORIZONTAL, SCREEN_PADDING_VERTICAL),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = SCREEN_PADDING_HORIZONTAL,
                        vertical = SCREEN_PADDING_VERTICAL
                    ),
                    horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
                    verticalItemSpacing = ELEMENT_SPACING
                ) {
                    items(areaList) { area ->
                        AreaItem(area = area, onClick = { onAreaClick(area) })
                    }
                }
            }
        }
    }
}

@Composable
fun AreaItem(
    area: Areas,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = if (area.areaIsDeleted) {
                area.areaName + " " + stringResource(R.string._deleted_)
            } else {
                area.areaName
            },
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (area.areaIsDeleted) Color.Red else Color.Black
        )
    }
}