package com.precioluz.app.data.api

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

private const val PRECIOS_URL = "https://precioluz.hugopvigo.es/api/precios"

@Singleton
class PrecioLuzJsonApi @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
) {
    fun fetch(): PrecioLuzJsonResponse {
        val request = Request.Builder().url(PRECIOS_URL).build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw Exception("Respuesta vacía")
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            return json.decodeFromString(body)
        }
    }
}
