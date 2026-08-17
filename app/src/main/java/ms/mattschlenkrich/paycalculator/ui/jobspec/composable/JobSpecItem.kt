package ms.mattschlenkrich.paycalculator.ui.jobspec.composable

import androidx.compose.runtime.Composable
import ms.mattschlenkrich.paycalculator.common.compose.StandardItem
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec

@Composable
fun JobSpecItem(
    item: JobSpec,
    onClick: (JobSpec) -> Unit
) {
    StandardItem(
        text = item.jsName,
        isDeleted = item.jsIsDeleted,
        onClick = { onClick(item) }
    )
}