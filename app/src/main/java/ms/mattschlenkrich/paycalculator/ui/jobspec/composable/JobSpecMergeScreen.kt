package ms.mattschlenkrich.paycalculator.ui.jobspec.composable

import androidx.compose.runtime.Composable
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.compose.GenericMergeScreen
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.model.JobSpecAndChild

@Composable
fun JobSpecMergeScreen(
    jobSpecList: List<JobSpec>,
    parentName: String,
    onParentNameChange: (String) -> Unit,
    onParentSelected: (JobSpec) -> Unit,
    childList: List<JobSpecAndChild>,
    onRemoveChild: (JobSpecAndChild) -> Unit,
    childName: String,
    onChildNameChange: (String) -> Unit,
    onChildSelected: (JobSpec) -> Unit,
    onMergeAction: (Int) -> Unit,
    onDoneClick: () -> Unit,
    onListItemSelected: (JobSpec) -> Unit,
    minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH
) {
    GenericMergeScreen(
        itemList = jobSpecList,
        parentName = parentName,
        onParentNameChange = onParentNameChange,
        onParentSelected = onParentSelected,
        childList = childList,
        onRemoveChild = onRemoveChild,
        childName = childName,
        onChildNameChange = onChildNameChange,
        onChildSelected = onChildSelected,
        onMergeAction = onMergeAction,
        onDoneClick = onDoneClick,
        onListItemSelected = onListItemSelected,
        itemToName = { it.jsName },
        childToName = { it.jobSpecChild.jsName },
        childIsDeleted = { it.jobSpecChild.jsIsDeleted },
        masterTitleRes = R.string.master_job_spec,
        parentLabelRes = R.string.parent_job_spec,
        childLabelRes = R.string.child_job_spec,
        minColumnWidth = minColumnWidth
    )
}