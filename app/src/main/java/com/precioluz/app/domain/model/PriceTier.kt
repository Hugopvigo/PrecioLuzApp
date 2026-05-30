package com.precioluz.app.domain.model

import androidx.compose.ui.graphics.Color

enum class PriceTier {
    GREEN,   // Barato    — 6 horas más baratas
    YELLOW,  // Moderado  — siguiente cuartil
    ORANGE,  // Caro      — siguiente cuartil
    RED;     // Muy caro  — 6 horas más caras

    fun colorLight(): Color = when (this) {
        GREEN  -> Color(0xFF15A34A)
        YELLOW -> Color(0xFFC28A00)
        ORANGE -> Color(0xFFE26A07)
        RED    -> Color(0xFFE11D2E)
    }

    fun colorDark(): Color = when (this) {
        GREEN  -> Color(0xFF30D158)
        YELLOW -> Color(0xFFFFD60A)
        ORANGE -> Color(0xFFFF9F0A)
        RED    -> Color(0xFFFF6961)
    }

    fun label(): String = when (this) {
        GREEN  -> "Barato"
        YELLOW -> "Moderado"
        ORANGE -> "Caro"
        RED    -> "Muy caro"
    }
}
