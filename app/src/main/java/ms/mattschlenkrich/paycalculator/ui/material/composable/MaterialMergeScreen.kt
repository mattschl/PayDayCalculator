package ms.mattschlenkrich.paycalculator.ui.material.composable

import androidx.compose.runtime.Composable
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.compose.GenericMergeScreen
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.model.MaterialAndChild

@Composable
fun MaterialMergeScreen(
    materialList: List<Material>,
    parentDescription: String,
    onParentDescriptionChange: (String) -> Unit,
    onParentSelected: (Material) -> Unit,
    childList: List<MaterialAndChild>,
    onRemoveChild: (MaterialAndChild) -> Unit,
    childDescription: String,
    onChildDescriptionChange: (String) -> Unit,
    onChildSelected: (Material) -> Unit,
    onMergeAction: (Int) -> Unit,
    onDoneClick: () -> Unit,
    onListItemSelected: (Material) -> Unit,
    minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH
) {
    GenericMergeScreen(
        itemList = materialList,
        parentName = parentDescription,
        onParentNameChange = onParentDescriptionChange,
        onParentSelected = onParentSelected,
        childList = childList,
        onRemoveChild = onRemoveChild,
        childName = childDescription,
        onChildNameChange = onChildDescriptionChange,
        onChildSelected = onChildSelected,
        onMergeAction = onMergeAction,
        onDoneClick = onDoneClick,
        onListItemSelected = onListItemSelected,
        itemToName = { it.mName },
        childToName = { it.materialChild.mName },
        childIsDeleted = { it.materialChild.mIsDeleted },
        masterTitleRes = R.string.master_material,
        parentLabelRes = R.string.parent_material,
        childLabelRes = R.string.child_material,
        minColumnWidth = minColumnWidth
    )
}