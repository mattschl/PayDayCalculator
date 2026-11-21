package ms.mattschlenkrich.paycalculator.ui.areas.composable

import androidx.compose.runtime.Composable
import ms.mattschlenkrich.paycalculator.data.entity.Areas
import ms.mattschlenkrich.paycalculator.ui.basiccomponents.StandardItem

@Composable
fun AreaItem(
    area: Areas,
    onClick: (Areas) -> Unit,
    index: Int = 0
) {
    StandardItem(
        text = area.areaName,
        isDeleted = area.areaIsDeleted,
        onClick = { onClick(area) },
        index = index
    )
}