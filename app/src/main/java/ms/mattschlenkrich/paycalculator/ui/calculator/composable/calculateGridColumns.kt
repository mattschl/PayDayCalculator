package ms.mattschlenkrich.paycalculator.ui.calculator.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import ms.mattschlenkrich.paycalculator.ui.basiccomponents.LocalMinColumnWidth
import kotlin.math.max

@Composable
fun calculateGridColumns(minColumnWidth: Int = LocalMinColumnWidth.current): Int {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    return with(density) {
        val widthDp = windowInfo.containerSize.width.toDp()
        max(1, (widthDp.value / minColumnWidth).toInt())
    }
}