package ms.mattschlenkrich.paycalculator.ui.areas.composable

import androidx.compose.runtime.Composable
import ms.mattschlenkrich.paycalculator.common.compose.StandardItem
import ms.mattschlenkrich.paycalculator.data.entity.Areas

@Composable
fun AreaItem(
    area: Areas,
    onClick: (Areas) -> Unit
) {
    StandardItem(
        text = area.areaName,
        isDeleted = area.areaIsDeleted,
        onClick = { onClick(area) }
    )
}