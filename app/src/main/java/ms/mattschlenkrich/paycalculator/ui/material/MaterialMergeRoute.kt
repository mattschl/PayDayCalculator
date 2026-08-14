package ms.mattschlenkrich.paycalculator.ui.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MaterialViewModel
import ms.mattschlenkrich.paycalculator.ui.material.composable.MaterialMergeScreen
import ms.mattschlenkrich.paycalculator.ui.settings.SettingsViewModel

@Composable
fun MaterialMergeRoute(
    mainViewModel: MainViewModel,
    materialViewModel: MaterialViewModel,
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

    val settings by settingsViewModel.settings.observeAsState()
    val minColumnWidth = settings?.minColumnWidth ?: DEFAULT_MIN_COLUMN_WIDTH

    var materialId = mainViewModel.getMaterialId()
    if (materialId == null) {
        val mat = mainViewModel.getMaterial()
        if (mat != null) {
            materialId = mat.materialId
            mainViewModel.setMaterialId(materialId)
        } else {
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
            return
        }
    }

    val materialList by materialViewModel.getMaterialsList().observeAsState(emptyList())
    val parentMaterial by materialViewModel.getMaterial(materialId).observeAsState()
    val childList by materialViewModel.getMaterialAndChildList(materialId)
        .observeAsState(emptyList())

    var parentDescription by rememberSaveable { mutableStateOf("") }
    var childDescription by rememberSaveable { mutableStateOf("") }
    var selectedChild by rememberSaveable {
        mutableStateOf<Material?>(
            null
        )
    }

    LaunchedEffect(parentMaterial) {
        parentMaterial?.let {
            parentDescription = it.mName
        }
    }

    MaterialMergeScreen(
        materialList = materialList,
        parentDescription = parentDescription,
        onParentDescriptionChange = { parentDescription = it },
        onParentSelected = {
            mainViewModel.setMaterialId(it.materialId)
            parentDescription = it.mName
        },
        childList = childList,
        onRemoveChild = { child ->
            coroutineScope.launch {
                materialViewModel.deleteMaterialMerged(
                    child.materialMerged.materialMergeId,
                    df.getCurrentUTCTimeAsString()
                )
            }
        },
        childDescription = childDescription,
        onChildDescriptionChange = { childDescription = it },
        onChildSelected = {
            selectedChild = it
            childDescription = it.mName
        },
        onMergeAction = { action ->
            val childId = selectedChild?.materialId
            if (childId != null && childId != materialId) {
                coroutineScope.launch {
                    if (action == 1) { // Keep
                        materialViewModel.insertMaterialMerged(
                            MaterialMerged(
                                nf.generateRandomIdAsLong(),
                                materialId,
                                childId,
                                false,
                                df.getCurrentUTCTimeAsString()
                            )
                        )
                    } else if (action == 2) { // Replace and delete
                        materialViewModel.updateMaterialMerged(
                            childId,
                            materialId,
                            df.getCurrentUTCTimeAsString()
                        )
                        materialViewModel.deleteMaterial(
                            childId,
                            df.getCurrentUTCTimeAsString()
                        )
                    }
                    childDescription = ""
                    selectedChild = null
                }
            }
        },
        onDoneClick = {
            navController.popBackStack()
        },
        onListItemSelected = {
            if (mainViewModel.getMaterialIsParent()) {
                mainViewModel.setMaterialId(it.materialId)
                parentDescription = it.mName
            } else {
                selectedChild = it
                childDescription = it.mName
            }
        },
        minColumnWidth = minColumnWidth
    )
}