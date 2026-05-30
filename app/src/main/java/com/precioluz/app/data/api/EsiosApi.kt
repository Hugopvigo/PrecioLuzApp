package com.precioluz.app.data.api

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import com.precioluz.app.data.datastore.SettingsDataStore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "EsiosApi"

@Singleton
class EsiosApi @Inject constructor(
    private val client: HttpClient,
    private val settings: SettingsDataStore,
) {
    companion object {
        private const val BASE_URL = "https://apidatos.ree.es/es/datos/mercados/precios-mercados-tiempo-real"
        private val FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    }

    suspend fun getPrices(date: LocalDate): PvpcResponse {
        val apiKey = settings.getApiKeySync()
            ?: throw IllegalStateException("API_KEY_MISSING")

        val start = date.atStartOfDay().format(FMT)
        val end   = date.atTime(23, 59).format(FMT)

        Log.d(TAG, "GET $BASE_URL start=$start end=$end keyLength=${apiKey.length}")

        return try {
            client.get(BASE_URL) {
                header("Authorization", "Bearer $apiKey")
                header("Accept", "application/json")
                parameter("time_trunc", "hour")
                parameter("start_date", start)
                parameter("end_date",   end)
            }.body()
        } catch (e: Exception) {
            Log.e(TAG, "Request failed", e)
            throw e
        }
    }
}
