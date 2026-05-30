package com.precioluz.app.domain.model

import androidx.compose.ui.graphics.Color

enum class PriceTier {
    GREEN,   // Barato    — 6 horas más baratas
    YELLOW,  // Moderado  — siguiente cuartil
    ORANGE,  // Caro      — siguiente cuartil
    RED;     // Muy caro  — 6 horas más caras

    fun colorLight(): Color = when (this) {
        GREEN  -> Color(0xFF15A34A)
        YELLOW -> Color(0xFFB89500)   // amarillo más puro
        ORANGE -> Color(0xFFD95200)   // naranja más cálido
        RED    -> Color(0xFFCC1624)   // rojo más oscuro
    }

    fun colorDark(): Color = when (this) {
        GREEN  -> Color(0xFF30D158)
        YELLOW -> Color(0xFFFFE433)   // amarillo más vivo
        ORANGE -> Color(0xFFFF7020)   // naranja más definido
        RED    -> Color(0xFFFF3B30)   // rojo más oscuro/saturado
    }

    fun label(): String = when (this) {
        GREEN  -> "Barato"
        YELLOW -> "Moderado"
        ORANGE -> "Caro"
        RED    -> "Muy caro"
    }
}
