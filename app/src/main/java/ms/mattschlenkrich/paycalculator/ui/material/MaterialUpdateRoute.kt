package ms.mattschlenkrich.paycalculator.ui.material

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MaterialViewModel
import ms.mattschlenkrich.paycalculator.ui.material.composable.MaterialUpdateScreen

@Composable
fun MaterialUpdateRoute(
    mainViewModel: MainViewModel,
    materialViewModel: MaterialViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val oldMaterial = mainViewModel.getMaterial() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val materialList by materialViewModel.getMaterialsList().observeAsState(emptyList())

    var name by rememberSaveable { mutableStateOf(oldMaterial.mName) }
    var cost by rememberSaveable { mutableStateOf(nf.displayDollars(oldMaterial.mCost)) }
    var price by rememberSaveable { mutableStateOf(nf.displayDollars(oldMaterial.mPrice)) }

    var calculatingField by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(mainViewModel.getTransferNum()) {
        val transferNum = mainViewModel.getTransferNum()
        if (transferNum != 0.0) {
            when (calculatingField) {
                "cost" -> {
                    cost = nf.displayDollars(transferNum)
                }

                "price" -> {
                    price = nf.displayDollars(transferNum)
                }
            }
            mainViewModel.setTransferNum(0.0)
            calculatingField = null
        }
    }

    MaterialUpdateScreen(
        name = name,
        onNameChange = { name = it },
        cost = cost,
        onCostChange = { cost = it },
        price = price,
        onPriceChange = { price = it },
        onCostCalcClick = {
            calculatingField = "cost"
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(cost))
            navController.navigate(Screen.Calculator.route)
        },
        onPriceCalcClick = {
            calculatingField = "price"
            mainViewModel.setTransferNum(nf.getDoubleFromDollars(price))
            navController.navigate(Screen.Calculator.route)
        },
        onUpdateClick = {
            val trimmedName = name.trim()
            if (trimmedName.isBlank()) {
                Toast.makeText(
                    context,
                    R.string.msg_material_name_required,
                    Toast.LENGTH_SHORT
                ).show()
                return@MaterialUpdateScreen
            }
            if (materialList.any {
                    it.mName.equals(trimmedName, ignoreCase = true) &&
                            it.materialId != oldMaterial.materialId
                }) {
                Toast.makeText(
                    context,
                    R.string.msg_material_name_exists,
                    Toast.LENGTH_SHORT
                ).show()
                return@MaterialUpdateScreen
            }

            coroutineScope.launch {
                val updatedMaterial = Material(
                    oldMaterial.materialId,
                    trimmedName,
                    nf.getDoubleFromDollars(cost.trim()),
                    nf.getDoubleFromDollars(price.trim()),
                    oldMaterial.mIsDeleted,
                    df.getCurrentUTCTimeAsString()
                )
                materialViewModel.updateMaterial(updatedMaterial)
                mainViewModel.setMaterial(updatedMaterial)
                navController.popBackStack()
            }
        },
        onMergeClick = {
            val trimmedName = name.trim()
            if (trimmedName.isBlank()) {
                Toast.makeText(
                    context,
                    R.string.msg_material_name_required,
                    Toast.LENGTH_SHORT
                ).show()
                return@MaterialUpdateScreen
            }

            coroutineScope.launch {
                val updatedMaterial = Material(
                    oldMaterial.materialId,
                    trimmedName,
                    nf.getDoubleFromDollars(cost.trim()),
                    nf.getDoubleFromDollars(price.trim()),
                    oldMaterial.mIsDeleted,
                    df.getCurrentUTCTimeAsString()
                )
                materialViewModel.updateMaterial(updatedMaterial)
                mainViewModel.setMaterial(updatedMaterial)
                mainViewModel.setMaterialId(oldMaterial.materialId)
                mainViewModel.setMaterialIsParent(true)
                navController.navigate(Screen.MaterialMerge.route)
            }
        },
        onCancelClick = { navController.popBackStack() },
        title = stringResource(R.string.prefix_update) + oldMaterial.mName
    )
}