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
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.Material
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

@Composable
fun MaterialUpdateRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val oldMaterial = mainViewModel.getMaterial() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val materialList by workOrderViewModel.materialsList.observeAsState(emptyList())

    var name by remember { mutableStateOf(oldMaterial.mName) }
    var cost by remember { mutableStateOf(nf.displayDollars(oldMaterial.mCost)) }
    var price by remember { mutableStateOf(nf.displayDollars(oldMaterial.mPrice)) }

    MaterialUpdateScreen(
        name = name,
        onNameChange = { name = it },
        cost = cost,
        onCostChange = { cost = it },
        price = price,
        onPriceChange = { price = it },
        onUpdateClick = {
            if (name.isBlank() || cost.isBlank() || price.isBlank()) {
                return@MaterialUpdateScreen
            }
            if (materialList.any { it.mName == name.trim() && it.materialId != oldMaterial.materialId }) {
                return@MaterialUpdateScreen
            }

            coroutineScope.launch {
                val material = Material(
                    oldMaterial.materialId,
                    name.trim(),
                    nf.getDoubleFromDollars(cost.trim()),
                    nf.getDoubleFromDollars(price.trim()),
                    oldMaterial.mIsDeleted,
                    df.getCurrentUTCTimeAsString()
                )
                workOrderViewModel.updateMaterial(material)
                mainViewModel.setMaterial(material)
                navController.popBackStack()
            }
        },
        onMergeClick = {
            coroutineScope.launch {
                val material = Material(
                    oldMaterial.materialId,
                    name.trim(),
                    nf.getDoubleFromDollars(cost.trim()),
                    nf.getDoubleFromDollars(price.trim()),
                    oldMaterial.mIsDeleted,
                    df.getCurrentUTCTimeAsString()
                )
                workOrderViewModel.updateMaterial(material)
                mainViewModel.setMaterial(material)
                mainViewModel.setMaterialId(oldMaterial.materialId)
                // Defaulting to Master for now, or could show dialog
                mainViewModel.setMaterialIsParent(true)
                navController.navigate(Screen.MaterialMerge.route)
            }
        },
        onCancelClick = { navController.popBackStack() },
        title = stringResource(R.string.update_) + oldMaterial.mName
    )
}