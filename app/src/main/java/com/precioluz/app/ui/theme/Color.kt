package com.precioluz.app.ui.theme

import androidx.compose.ui.graphics.Color

val GreenLight = Color(0xFF4CAF50)
val GreenDark = Color(0xFF2E7D32)
val YellowLight = Color(0xFFFFC107)
val YellowDark = Color(0xFFFFA000)
val OrangeLight = Color(0xFFFF9800)
val OrangeDark = Color(0xFFE65100)
val RedLight = Color(0xFFF44336)
val RedDark = Color(0xFFC62828)

val md_theme_light_primary = Color(0xFF1B6B4D)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFA4F5CD)
val md_theme_light_onPrimaryContainer = Color(0xFF002115)
val md_theme_light_secondary = Color(0xFF4E6355)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFD0E8D6)
val md_theme_light_onSecondaryContainer = Color(0xFF0B1F14)
val md_theme_light_background = Color(0xFFFBFDF8)
val md_theme_light_onBackground = Color(0xFF191C1A)
val md_theme_light_surface = Color(0xFFFBFDF8)
val md_theme_light_onSurface = Color(0xFF191C1A)

val md_theme_dark_primary = Color(0xFF88D9B2)
val md_theme_dark_onPrimary = Color(0xFF003824)
val md_theme_dark_primaryContainer = Color(0xFF005237)
val md_theme_dark_onPrimaryContainer = Color(0xFFA4F5CD)
val md_theme_dark_secondary = Color(0xFFB4CCBA)
val md_theme_dark_onSecondary = Color(0xFF203529)
val md_theme_dark_secondaryContainer = Color(0xFF374B3F)
val md_theme_dark_onSecondaryContainer = Color(0xFFD0E8D6)
val md_theme_dark_background = Color(0xFF191C1A)
val md_theme_dark_onBackground = Color(0xFFE1E3DE)
val md_theme_dark_surface = Color(0xFF191C1A)
val md_theme_dark_onSurface = Color(0xFFE1E3DE)

fun tierColor(tier: com.precioluz.app.domain.model.PriceTier, darkTheme: Boolean): Color {
    return when (tier) {
        com.precioluz.app.domain.model.PriceTier.CHEAP -> if (darkTheme) GreenDark else GreenLight
        com.precioluz.app.domain.model.PriceTier.AFFORDABLE -> if (darkTheme) YellowDark else YellowLight
        com.precioluz.app.domain.model.PriceTier.MEDIUM -> if (darkTheme) OrangeDark else OrangeLight
        com.precioluz.app.domain.model.PriceTier.DEAR -> if (darkTheme) RedDark else RedLight
    }
}
