package ms.mattschlenkrich.paycalculator.ui.material

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
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryMaterialCombined
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.ui.material.composable.MaterialQuantityUpdateScreen

@Composable
fun MaterialQuantityUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val initialHistory = mainViewModel.getWorkOrderHistory()
    val materialId = mainViewModel.getMaterialId()

    if (initialHistory == null || materialId == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val historyWithDates by workOrderViewModel.getWorkOrderHistoriesById(initialHistory.woHistoryId)
        .observeAsState()

    var materialHistory by remember {
        mutableStateOf<WorkOrderHistoryMaterialCombined?>(
            null
        )
    }
    LaunchedEffect(materialId) {
        materialHistory = workOrderViewModel.getWorkOrderHistoryMaterialCombined(materialId)
    }

    if (materialHistory == null || historyWithDates == null) return

    var qty by remember { mutableStateOf(nf.displayNumberFromDouble(materialHistory!!.workOrderHistoryMaterial.wohmQuantity)) }

    MaterialQuantityUpdateScreen(
        details = stringResource(R.string.edit_material_used_for_wo_) +
                " ${historyWithDates!!.workOrder.woNumber} " +
                stringResource(R.string._at_) + " ${historyWithDates!!.workOrder.woAddress}\n" +
                historyWithDates!!.workOrder.woDescription + "\n\n" +
                stringResource(R.string.material) + " ${materialHistory!!.material.mName}",
        quantity = qty,
        onQuantityChange = { qty = it },
        onDoneClick = {
            coroutineScope.launch {
                workOrderViewModel.updateWorkOrderHistoryMaterial(
                    materialHistory!!.workOrderHistoryMaterial.copy(
                        wohmQuantity = qty.toDoubleOrNull() ?: 0.0,
                        wohmUpdateTime = df.getCurrentUTCTimeAsString()
                    )
                )
                navController.popBackStack()
            }
        },
        onBackClick = { navController.popBackStack() }
    )
}