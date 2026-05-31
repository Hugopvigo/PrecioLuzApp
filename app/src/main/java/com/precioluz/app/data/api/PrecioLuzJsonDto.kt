package com.precioluz.app.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrecioLuzJsonResponse(
    @SerialName("updated_at") val updatedAt: String,
    val today: DayJson,
    val tomorrow: DayJson? = null,
)

@Serializable
data class DayJson(
    val date: String,
    val prices: List<Double>,   // 24 valores €/kWh, índice = hora
)
