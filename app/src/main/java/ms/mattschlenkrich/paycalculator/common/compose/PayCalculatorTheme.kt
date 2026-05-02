package ms.mattschlenkrich.paycalculator.common.compose

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

@Immutable
data class ExtendedDimensions(
    val textFieldMinHeight: Dp = 28.dp,
    val textFieldContentPadding: PaddingValues = PaddingValues(
        start = 8.dp,
        end = 8.dp,
        top = 4.dp,
        bottom = 4.dp
    ),
    val dropdownItemPadding: PaddingValues = PaddingValues(
        start = 8.dp,
        end = 8.dp,
        top = 4.dp,
        bottom = 4.dp
    ),
    val textFieldBorderThickness: Dp = 1.dp
)

val LocalExtendedDimensions = staticCompositionLocalOf { ExtendedDimensions() }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF073763), // deep_blue
    secondary = Color(0xFF00A86B), // green
    tertiary = Color(0xFFBC131F) // red
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF073763), // deep_blue
    secondary = Color(0xFF00A86B), // green
    tertiary = Color(0xFFBC131F), // red
    primaryContainer = Color(0xFF073763),
    onPrimaryContainer = Color.White,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun PayCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontSize: Float = 16f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val typography = Typography(
        bodyLarge = TextStyle(fontSize = fontSize.sp),
        bodyMedium = TextStyle(fontSize = (fontSize * 0.875).sp),
        bodySmall = TextStyle(fontSize = (fontSize * 0.75).sp),
        titleLarge = TextStyle(fontSize = (fontSize * 1.375).sp),
        titleMedium = TextStyle(fontSize = (fontSize * 1.125).sp),
        titleSmall = TextStyle(fontSize = fontSize.sp),
        labelLarge = TextStyle(fontSize = (fontSize * 0.875).sp),
        labelMedium = TextStyle(fontSize = (fontSize * 0.75).sp),
        labelSmall = TextStyle(fontSize = (fontSize * 0.625).sp)
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    val density = LocalDensity.current
    val minHeight = with(density) { (fontSize * 1.1f * 1.1f).sp.toDp() }
    val verticalPadding = with(density) { (fontSize * 0.1f).sp.toDp() }

    val dimensions = ExtendedDimensions(
        textFieldMinHeight = minHeight,
        textFieldContentPadding = PaddingValues(
            start = 8.dp,
            end = 8.dp,
            top = verticalPadding,
            bottom = verticalPadding
        ),
        dropdownItemPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = verticalPadding,
            bottom = verticalPadding
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography
    ) {
        CompositionLocalProvider(
            LocalExtendedDimensions provides dimensions,
            content = content
        )
    }
}