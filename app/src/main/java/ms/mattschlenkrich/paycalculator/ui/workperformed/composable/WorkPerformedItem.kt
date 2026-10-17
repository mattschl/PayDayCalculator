package ms.mattschlenkrich.paycalculator.ui.workperformed.composable

import androidx.compose.runtime.Composable
import ms.mattschlenkrich.paycalculator.common.compose.StandardItem
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed

@Composable
fun WorkPerformedItem(
    item: WorkPerformed,
    onClick: (WorkPerformed) -> Unit,
    index: Int = 0
) {
    StandardItem(
        text = item.wpDescription,
        isDeleted = item.wpIsDeleted,
        onClick = { onClick(item) },
        index = index
    )
}