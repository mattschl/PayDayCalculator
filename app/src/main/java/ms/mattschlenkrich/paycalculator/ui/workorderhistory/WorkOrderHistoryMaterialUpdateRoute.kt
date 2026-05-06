package ms.mattschlenkrich.paycalculator.ui.workorderhistory

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
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryMaterialCombined
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable.WorkOrderHistoryMaterialUpdateScreen

@Composable
fun WorkOrderHistoryMaterialUpdateRoute(
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
    val materialSuggestions by workOrderViewModel.materialsList.observeAsState(emptyList())

    var materialHistory by remember {
        mutableStateOf<WorkOrderHistoryMaterialCombined?>(
            null
        )
    }
    LaunchedEffect(materialId) {
        materialHistory = workOrderViewModel.getWorkOrderHistoryMaterialCombined(materialId)
    }

    if (materialHistory == null || historyWithDates == null) return

    var mName by remember { mutableStateOf(materialHistory!!.material.mName) }
    var qty by remember { mutableStateOf(nf.displayNumberFromDouble(materialHistory!!.workOrderHistoryMaterial.wohmQuantity)) }

    WorkOrderHistoryMaterialUpdateScreen(
        info = stringResource(R.string.edit_material_used_for_wo_) +
                " ${historyWithDates!!.workOrder.woNumber} " +
                stringResource(R.string._at_) + " ${historyWithDates!!.workOrder.woAddress}\n" +
                historyWithDates!!.workOrder.woDescription,
        materialName = mName,
        onMaterialNameChange = { mName = it },
        materialSuggestions = materialSuggestions.map { it.mName },
        quantity = qty,
        onQuantityChange = { qty = it },
        originalMaterialLabel = stringResource(R.string.original_material_) + " ${materialHistory!!.material.mName}",
        originalQuantityLabel = stringResource(R.string.original_quantity_) + " ${
            nf.displayNumberFromDouble(
                materialHistory!!.workOrderHistoryMaterial.wohmQuantity
            )
        }",
        onDoneClick = {
            coroutineScope.launch {
                val material = workOrderViewModel.getMaterialSync(mName)
                if (material != null) {
                    workOrderViewModel.updateWorkOrderHistoryMaterial(
                        materialHistory!!.workOrderHistoryMaterial.copy(
                            wohmMaterialId = material.materialId,
                            wohmQuantity = qty.toDoubleOrNull() ?: 0.0,
                            wohmUpdateTime = df.getCurrentUTCTimeAsString()
                        )
                    )
                }
                navController.popBackStack()
            }
        },
        onBackClick = { navController.popBackStack() }
    )
}