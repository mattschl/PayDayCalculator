package ms.mattschlenkrich.paycalculator.ui.material

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.MaterialCostHistory
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MaterialViewModel
import ms.mattschlenkrich.paycalculator.ui.material.composable.MaterialUpdateScreen
import java.io.File

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
    val pictures by materialViewModel.getPicturesByMaterialId(oldMaterial.materialId)
        .observeAsState(emptyList())
    val costHistoryList by materialViewModel.getMaterialCostHistoryByMaterial(oldMaterial.materialId)
        .observeAsState(emptyList())

    LaunchedEffect(pictures) {
        val helper = mainViewModel.getOrInitializeDriveService(context) ?: return@LaunchedEffect
        pictures.forEach { pic ->
            val fullInCache = File(context.cacheDir, "pictures/pic_${pic.pictureId}.webp")
            val fullInFiles = File(context.filesDir, "pictures/pic_${pic.pictureId}.webp")
            val existsLocally = (fullInCache.exists() && fullInCache.length() > 0L) ||
                    (fullInFiles.exists() && fullInFiles.length() > 0L)
            if (!existsLocally && !pic.driveFileId.isNullOrBlank()) {
                try {
                    helper.downloadFileById(pic.driveFileId, fullInCache)
                } catch (_: Exception) {
                }
            }
        }
    }

    var name by rememberSaveable { mutableStateOf(oldMaterial.mName) }
    var cost by rememberSaveable { mutableStateOf(nf.displayDollars(oldMaterial.mCost)) }
    var price by rememberSaveable { mutableStateOf(nf.displayDollars(oldMaterial.mPrice)) }

    var calculatingField by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddCostDialog by remember { mutableStateOf(false) }
    var newCostInput by remember { mutableStateOf(cost) }
    var newCostDateInput by remember { mutableStateOf(df.getCurrentDateAsString()) }

    val pendingTransfer = mainViewModel.transferNumState.value
    LaunchedEffect(pendingTransfer) {
        if (pendingTransfer != null) {
            val transferNum = pendingTransfer
            when (calculatingField) {
                "cost" -> {
                    cost = nf.displayDollars(transferNum)
                }

                "price" -> {
                    price = nf.displayDollars(transferNum)
                }
            }
            mainViewModel.clearTransferNum()
            calculatingField = null
        }
    }

    if (showAddCostDialog) {
        AlertDialog(
            onDismissRequest = { showAddCostDialog = false },
            title = { Text(stringResource(R.string.add_cost_history)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        df.showDatePicker(context, newCostDateInput) {
                            newCostDateInput = it
                        }
                    }) {
                        Text(
                            "${stringResource(R.string.purchase_date)}: ${
                                df.getDisplayDate(
                                    newCostDateInput
                                )
                            }"
                        )
                    }
                    DecimalOutlinedTextField(
                        value = newCostInput,
                        onValueChange = { newCostInput = it },
                        label = { Text(stringResource(R.string.cost)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val costVal = nf.getDoubleFromDollars(newCostInput)
                    coroutineScope.launch {
                        val newEntry = MaterialCostHistory(
                            materialExpenseHistoryId = nf.generateRandomIdAsLong(),
                            mehMaterialId = oldMaterial.materialId,
                            mehRawCost = costVal,
                            mehPurchaseDate = newCostDateInput,
                            mehIsDeleted = false,
                            mehUpdateTime = df.getCurrentUTCTimeAsString()
                        )
                        materialViewModel.insertMaterialCostHistory(newEntry)
                        showAddCostDialog = false
                    }
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCostDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
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
                    Toast.LENGTH_LONG
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
                    Toast.LENGTH_LONG
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
                    Toast.LENGTH_LONG
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
        title = stringResource(R.string.prefix_update) + oldMaterial.mName,
        pictures = pictures,
        costHistoryList = costHistoryList,
        onAddCostHistoryClick = {
            newCostInput = cost
            newCostDateInput = df.getCurrentDateAsString()
            showAddCostDialog = true
        },
        onDeleteCostHistoryClick = { historyItem ->
            coroutineScope.launch {
                materialViewModel.updateMaterialCostHistory(
                    historyItem.copy(
                        mehIsDeleted = true,
                        mehUpdateTime = df.getCurrentUTCTimeAsString()
                    )
                )
            }
        }
    )
}