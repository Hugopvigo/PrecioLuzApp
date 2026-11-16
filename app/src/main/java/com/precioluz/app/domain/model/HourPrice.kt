package com.precioluz.app.domain.model

import androidx.compose.ui.graphics.Color

data class HourPrice(
    val hour: Int,          // 0–23
    val price: Double,      // €/kWh con impuestos
    val tramo: Tramo,
    val tier: PriceTier,
    val isMin: Boolean = false,
    val isMax: Boolean = false,
)

// Colores de tramo con significado de precio: valle barato (verde), llano medio
// (ámbar) y punta caro (rojo). Mismos tokens que la web, con el valle reforzado
// en oscuro para que no se pierda contra el fondo.
enum class Tramo {
    VALLE, LLANO, PUNTA;

    fun label(): String = when (this) {
        VALLE -> "Valle"
        LLANO -> "Llano"
        PUNTA -> "Punta"
    }

    fun colorLight(): Color = when (this) {
        VALLE -> Color(0xFF05994A)
        LLANO -> Color(0xFFD98600)
        PUNTA -> Color(0xFFD91F2B)
    }

    fun colorDark(): Color = when (this) {
        VALLE -> Color(0xFF1FE27F)
        LLANO -> Color(0xFFFFB81F)
        PUNTA -> Color(0xFFFF4D5A)
    }
}
