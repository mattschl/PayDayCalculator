package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

val SCREEN_PADDING_HORIZONTAL = 6.dp
val SCREEN_PADDING_VERTICAL = 4.dp
val ELEMENT_SPACING = 4.dp

@Composable
fun calculateGridColumns(): Int {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    return with(density) {
        if (windowInfo.containerSize.width.toDp() >= 600.dp) 3 else 2
    }
}