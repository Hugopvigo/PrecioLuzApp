package es.hugopvigo.precioluz.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import es.hugopvigo.precioluz.data.datastore.AppTheme

private val LightColorScheme = lightColorScheme(
    background       = BgLight,
    surface          = GlassBgLight,
    onBackground     = Color(0xFF0C0E16),
    onSurface        = Color(0xFF0C0E16),
    primary          = BrandOrangeEnd,
    secondary        = BrandOrangeStart,
)

private val DarkColorScheme = darkColorScheme(
    background       = BgDark,
    surface          = GlassBgDark,
    onBackground     = Color(0xFFF2F4FB),
    onSurface        = Color(0xFFF2F4FB),
    primary          = BrandOrangeEnd,
    secondary        = BrandOrangeStart,
)

@Composable
fun PrecioLuzTheme(
    appTheme: AppTheme = AppTheme.AUTO,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (appTheme) {
        AppTheme.AUTO  -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK  -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor  = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content,
    )
}
