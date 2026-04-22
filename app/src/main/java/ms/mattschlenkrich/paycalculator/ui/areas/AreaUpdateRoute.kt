package ms.mattschlenkrich.paycalculator.ui.areas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.Areas
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

@Composable
fun AreaUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val areaId = mainViewModel.getAreaId()
    if (areaId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val areaList by workOrderViewModel.getAreasList().observeAsState(emptyList())
    val oldArea by workOrderViewModel.getArea(areaId).observeAsState()

    oldArea?.let { area ->
        var name by remember(area.areaId) { mutableStateOf(area.areaName) }

        AreaUpdateScreen(
            name = name,
            onNameChange = { name = it },
            title = stringResource(R.string.update_area_description_for) + area.areaName,
            onUpdateClick = {
                val trimmedName = name.trim()
                if (trimmedName.isBlank()) {
                    // In a real app, use a proper snackbar or state-based error
                    return@AreaUpdateScreen
                }

                if (areaList.any { it.areaName == trimmedName && it.areaId != area.areaId }) {
                    return@AreaUpdateScreen
                }

                coroutineScope.launch {
                    workOrderViewModel.updateArea(
                        Areas(
                            area.areaId,
                            trimmedName,
                            false,
                            df.getCurrentUTCTimeAsString()
                        )
                    )
                    navController.popBackStack()
                }
            },
            onCancelClick = {
                navController.popBackStack()
            },
            onBackClick = {
                navController.popBackStack()
            }
        )
    }
}