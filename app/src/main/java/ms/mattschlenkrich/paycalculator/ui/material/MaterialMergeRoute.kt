package ms.mattschlenkrich.paycalculator.ui.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.Material
import ms.mattschlenkrich.paycalculator.data.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel

@Composable
fun MaterialMergeRoute(
    mainViewModel: MainViewModel,
    workOrderViewModel: WorkOrderViewModel,
    navController: NavController
) {
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }
    val coroutineScope = rememberCoroutineScope()

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

    val materialList by workOrderViewModel.materialsList.observeAsState(emptyList())
    val parentMaterial by workOrderViewModel.getMaterial(materialId).observeAsState()
    val childList by workOrderViewModel.getMaterialAndChildList(materialId)
        .observeAsState(emptyList())

    var parentDescription by remember { mutableStateOf("") }
    var childDescription by remember { mutableStateOf("") }
    var selectedChild by remember {
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
                workOrderViewModel.deleteMaterialMerged(
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
                        workOrderViewModel.insertMaterialMerged(
                            MaterialMerged(
                                nf.generateRandomIdAsLong(),
                                materialId,
                                childId,
                                false,
                                df.getCurrentUTCTimeAsString()
                            )
                        )
                    } else if (action == 2) { // Replace and delete
                        workOrderViewModel.updateMaterialMerged(
                            childId,
                            materialId,
                            df.getCurrentUTCTimeAsString()
                        )
                        workOrderViewModel.deleteMaterial(
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
        }
    )
}