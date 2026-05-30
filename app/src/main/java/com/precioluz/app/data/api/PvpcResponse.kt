package com.precioluz.app.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// DTOs de la API PVPC de REE / ESIOS
// Endpoint: https://apidatos.ree.es/es/datos/mercados/precios-mercados-tiempo-real
//   ?time_trunc=hour&start_date=YYYY-MM-DDT00:00&end_date=YYYY-MM-DDT23:59

@Serializable
data class PvpcResponse(
    val data: PvpcData,
    val included: List<PvpcIncluded> = emptyList(),
)

@Serializable
data class PvpcData(
    val type: String,
    val id: String,
    val attributes: PvpcAttributes,
)

@Serializable
data class PvpcAttributes(
    val title: String = "",
    @SerialName("last-update") val lastUpdate: String? = null,
    val values: List<PvpcValue> = emptyList(),   // Presente en included[], ausente en data.attributes
)

@Serializable
data class PvpcValue(
    val value: Double,
    val percentage: Double = 0.0,
    val datetime: String,
)

@Serializable
data class PvpcIncluded(
    val type: String,
    val id: String,
    val attributes: PvpcAttributes,
)
