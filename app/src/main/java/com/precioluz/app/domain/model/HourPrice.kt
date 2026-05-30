package com.precioluz.app.domain.model

data class HourPrice(
    val hour: Int,          // 0–23
    val price: Double,      // €/kWh con impuestos
    val tramo: Tramo,
    val tier: PriceTier,
    val isMin: Boolean = false,
    val isMax: Boolean = false,
)

enum class Tramo {
    VALLE, LLANO, PUNTA;

    fun label(): String = when (this) {
        VALLE -> "Valle"
        LLANO -> "Llano"
        PUNTA -> "Punta"
    }
}
