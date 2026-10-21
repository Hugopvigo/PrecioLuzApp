package com.precioluz.app.data.api

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

// Se intentan en orden: si un dominio está caído se prueba el siguiente.
// El dominio viejo (hugopvigo.es) está en el parking de OVH desde 2019 y no
// resuelve a ningún servicio: queda fuera de la lista.
private val PRECIOS_URLS = listOf(
    "https://precioluz.hugoperezvigo.es/api/precios",
)

@Singleton
class PrecioLuzJsonApi @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
) {
    fun fetch(): PrecioLuzJsonResponse {
        var lastError: Exception? = null
        for (url in PRECIOS_URLS) {
            try {
                return fetchFrom(url)
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw lastError ?: Exception("Sin URLs configuradas")
    }

    private fun fetchFrom(url: String): PrecioLuzJsonResponse {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw Exception("Respuesta vacía")
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            return json.decodeFromString(body)
        }
    }
}
