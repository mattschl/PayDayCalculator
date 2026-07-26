package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

val MAX_CONTENT_WIDTH = 840.dp

@Composable
fun StandardCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(SCREEN_PADDING_HORIZONTAL),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun calculateGridColumns(minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH): Int {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    return with(density) {
        val widthDp = windowInfo.containerSize.width.toDp()
        max(1, (widthDp.value / minColumnWidth).toInt())
    }
}