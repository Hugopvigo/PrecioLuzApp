package com.precioluz.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // Precio hero — 52sp bold
    displayLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 52.sp,
        lineHeight = 50.sp,
        letterSpacing = (-1.82).sp,
    ),
    // Título de sección (Hoy / Mañana)
    displayMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 30.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.9).sp,
    ),
    // Nombre de app
    titleLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 18.sp,
        letterSpacing = (-0.36).sp,
    ),
    // Labels tarjetas
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 12.5.sp,
    ),
    // Valores stats
    headlineSmall = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 19.sp,
        letterSpacing = (-0.38).sp,
    ),
    // Precio en lista de horas
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 14.sp,
    ),
)
