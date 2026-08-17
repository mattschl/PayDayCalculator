package ms.mattschlenkrich.paycalculator.ui.workperformed.composable

import androidx.compose.runtime.Composable
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.compose.GenericMergeScreen
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.model.WorkPerformedAndChild

@Composable
fun WorkPerformedMergeScreen(
    workPerformedList: List<WorkPerformed>,
    parentDescription: String,
    onParentDescriptionChange: (String) -> Unit,
    onParentSelected: (WorkPerformed) -> Unit,
    childList: List<WorkPerformedAndChild>,
    onRemoveChild: (WorkPerformedAndChild) -> Unit,
    childDescription: String,
    onChildDescriptionChange: (String) -> Unit,
    onChildSelected: (WorkPerformed) -> Unit,
    onMergeAction: (Int) -> Unit,
    onDoneClick: () -> Unit,
    onListItemSelected: (WorkPerformed) -> Unit,
    minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH
) {
    GenericMergeScreen(
        itemList = workPerformedList,
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
        itemToName = { it.wpDescription },
        childToName = { it.workPerformedChild.wpDescription },
        childIsDeleted = { it.workPerformedChild.wpIsDeleted },
        masterTitleRes = R.string.master_work_performed_description,
        parentLabelRes = R.string.parent_work_performed,
        childLabelRes = R.string.child_work_performed,
        minColumnWidth = minColumnWidth
    )
}