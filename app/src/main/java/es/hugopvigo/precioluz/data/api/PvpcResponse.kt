package es.hugopvigo.precioluz.data.api

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
    val title: String,
    @SerialName("last-update") val lastUpdate: String,
    val values: List<PvpcValue>,
)

@Serializable
data class PvpcValue(
    val value: Double,      // €/MWh sin impuestos — convertir a €/kWh y aplicar VAT
    val percentage: Double,
    val datetime: String,   // ISO 8601 con zona horaria
)

@Serializable
data class PvpcIncluded(
    val type: String,
    val id: String,
    val attributes: PvpcAttributes,
)
