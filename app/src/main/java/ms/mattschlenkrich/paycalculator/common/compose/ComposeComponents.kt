package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import kotlin.math.max

val paddingScale = mutableFloatStateOf(1f)

val SCREEN_PADDING_HORIZONTAL: Dp
    get() = (6 * paddingScale.floatValue).dp

val SCREEN_PADDING_VERTICAL: Dp
    get() = (6 * paddingScale.floatValue).dp

val ELEMENT_SPACING: Dp
    get() = (18 * paddingScale.floatValue).dp

@Composable
fun calculateGridColumns(minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH): Int {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    return with(density) {
        val widthDp = windowInfo.containerSize.width.toDp()
        max(1, (widthDp.value / minColumnWidth).toInt())
    }
}