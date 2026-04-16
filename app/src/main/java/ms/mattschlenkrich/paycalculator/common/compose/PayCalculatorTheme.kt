package ms.mattschlenkrich.paycalculator.common.compose

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}