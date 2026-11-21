package ms.mattschlenkrich.paycalculator.ui.basiccomponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH

val paddingScale = mutableFloatStateOf(1f)

val LocalMinColumnWidth = staticCompositionLocalOf { DEFAULT_MIN_COLUMN_WIDTH }

val SCREEN_PADDING_HORIZONTAL: Dp
    get() = (8 * paddingScale.floatValue).dp.coerceAtLeast(6.dp)

val SCREEN_PADDING_VERTICAL: Dp
    get() = (6 * paddingScale.floatValue).dp.coerceAtLeast(4.dp)

val ELEMENT_SPACING: Dp
    @Composable
    get() = LocalVerticalSpacing.current

val MAX_CONTENT_WIDTH = 840.dp