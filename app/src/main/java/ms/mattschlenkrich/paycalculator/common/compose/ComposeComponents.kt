package ms.mattschlenkrich.paycalculator.common.compose

import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import kotlin.math.max

val SCREEN_PADDING_HORIZONTAL = 6.dp
val SCREEN_PADDING_VERTICAL = 4.dp
val ELEMENT_SPACING = 4.dp

@Composable
fun calculateGridColumns(minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH): Int {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    return with(density) {
        val widthDp = windowInfo.containerSize.width.toDp()
        max(1, (widthDp.value / minColumnWidth).toInt())
    }
}