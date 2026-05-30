package com.precioluz.app.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EsiosResponse(
    val indicator: EsiosIndicator
)

@Serializable
data class EsiosIndicator(
    val name: String,
    @SerialName("short_name") val shortName: String,
    val id: Int,
    val values: List<EsiosValue>
)

@Serializable
data class EsiosValue(
    val value: Double,
    val datetime: String,
    @SerialName("geo_id") val geoId: Int,
    @SerialName("geo_name") val geoName: String,
    @SerialName("time_interval") val timeInterval: EsiosTimeInterval? = null
)

@Serializable
data class EsiosTimeInterval(
    val start: String
)
