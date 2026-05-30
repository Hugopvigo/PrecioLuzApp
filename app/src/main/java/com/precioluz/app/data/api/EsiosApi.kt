package com.precioluz.app.data.api

import android.util.Log
import com.precioluz.app.data.datastore.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "EsiosApi"

@Singleton
class EsiosApi @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
    private val settings: SettingsDataStore,
) {
    companion object {
        private const val BASE_URL =
            "https://apidatos.ree.es/es/datos/mercados/precios-mercados-tiempo-real"
        private val FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    }

    suspend fun getPrices(date: LocalDate): PvpcResponse = withContext(Dispatchers.IO) {
        val apiKey = settings.getApiKeySync()
            ?: throw IllegalStateException("API_KEY_MISSING")

        val start = date.atStartOfDay().format(FMT)
        val end   = date.atTime(23, 59).format(FMT)

        val url = BASE_URL.toHttpUrl().newBuilder()
            .addQueryParameter("time_trunc", "hour")
            .addQueryParameter("geo_ids",    "8741")   // 8741 = Península
            .addQueryParameter("start_date", start)
            .addQueryParameter("end_date",   end)
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "application/json")
            .build()

        Log.d(TAG, "GET $url keyLength=${apiKey.length}")

        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
                ?: throw Exception("Respuesta vacía del servidor")
            if (!response.isSuccessful) {
                Log.e(TAG, "HTTP ${response.code}: ${body.take(200)}")
                throw Exception("HTTP ${response.code}")
            }
            json.decodeFromString<PvpcResponse>(body)
        }
    }
}
