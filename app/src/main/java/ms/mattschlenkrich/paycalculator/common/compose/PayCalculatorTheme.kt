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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH

@Immutable
data class ExtendedDimensions(
    val textFieldMinHeight: Dp = 20.dp,
    val textFieldMaxHeight: Dp = 26.dp,
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
    primary = Color(0xFFD0E4FF), // Very light blue
    onPrimary = Color(0xFF00315C), // Very dark blue
    primaryContainer = Color(0xFF004787),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = Color(0xFFA9F7BB), // Very light green
    onSecondary = Color(0xFF00391D), // Very dark green
    secondaryContainer = Color(0xFF00522C),
    onSecondaryContainer = Color(0xFFA9F7BB),
    tertiary = Color(0xFFFFDAD6), // Very light red
    onTertiary = Color(0xFF690005), // Very dark red
    tertiaryContainer = Color(0xFF93000A),
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF3F474D), // Darker grey for surface variants
    onSurfaceVariant = Color(0xFFD1E4FF), // Lighter blue-grey for contrast
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8D9199),
    outlineVariant = Color(0xFF3F474D),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE2E2E6),
    inverseOnSurface = Color(0xFF2F3033),
    inversePrimary = Color(0xFF005FAF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF073763),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001C38),
    secondary = Color(0xFF00A86B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA9F7BB),
    onSecondaryContainer = Color(0xFF00210E),
    tertiary = Color(0xFFBC131F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD6),
    onTertiaryContainer = Color(0xFF410002),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474E),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = Color(0xFFA6C8FF)
)

@Composable
fun PayCalculatorTheme(
    isSystemTheme: Boolean = true,
    isDarkTheme: Boolean = false,
    fontSize: Float = 16f,
    minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH,
    content: @Composable () -> Unit
) {
    val darkTheme = if (isSystemTheme) isSystemInDarkTheme() else isDarkTheme
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val currentDensity = LocalDensity.current
    val customDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale * (fontSize / 16f)
    )

    val typography = Typography(
        displayLarge = TextStyle(fontSize = (fontSize * 3.5625f).sp),
        displayMedium = TextStyle(fontSize = (fontSize * 2.8125f).sp),
        displaySmall = TextStyle(fontSize = (fontSize * 2.25f).sp),
        headlineLarge = TextStyle(fontSize = (fontSize * 2.0f).sp),
        headlineMedium = TextStyle(fontSize = (fontSize * 1.75f).sp),
        headlineSmall = TextStyle(fontSize = (fontSize * 1.5f).sp),
        titleLarge = TextStyle(fontSize = (fontSize * 1.375f).sp),
        titleMedium = TextStyle(fontSize = (fontSize * 1.125f).sp),
        titleSmall = TextStyle(fontSize = fontSize.sp),
        bodyLarge = TextStyle(fontSize = fontSize.sp),
        bodyMedium = TextStyle(fontSize = (fontSize * 0.875f).sp),
        bodySmall = TextStyle(fontSize = (fontSize * 0.75f).sp),
        labelLarge = TextStyle(fontSize = (fontSize * 0.875f).sp),
        labelMedium = TextStyle(fontSize = (fontSize * 0.75f).sp),
        labelSmall = TextStyle(fontSize = (fontSize * 0.6875f).sp)
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    val minHeight = with(customDensity) { (fontSize * 1.1f).sp.toDp() }
    val maxHeight = with(customDensity) { (fontSize * 1.7f).sp.toDp() }
    val verticalPadding = with(customDensity) { (fontSize * 0.05f).sp.toDp() }

    val dimensions = ExtendedDimensions(
        textFieldMinHeight = minHeight,
        textFieldMaxHeight = maxHeight,
        textFieldContentPadding = PaddingValues(
            start = 8.dp,
            end = 8.dp,
            top = verticalPadding,
            bottom = verticalPadding
        ),
        dropdownItemPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 1.dp,
            bottom = 1.dp
        )
    )

    CompositionLocalProvider(
        LocalDensity provides customDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography
        ) {
            CompositionLocalProvider(
                LocalExtendedDimensions provides dimensions,
                LocalMinColumnWidth provides minColumnWidth,
                content = content
            )
        }
    }
}