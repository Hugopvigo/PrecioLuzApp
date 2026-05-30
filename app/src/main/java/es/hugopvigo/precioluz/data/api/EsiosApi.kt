package es.hugopvigo.precioluz.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EsiosApi @Inject constructor(
    private val client: HttpClient,
) {
    companion object {
        private const val BASE_URL = "https://apidatos.ree.es/es/datos/mercados/precios-mercados-tiempo-real"
        private val FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    }

    suspend fun getPrices(date: LocalDate): PvpcResponse {
        val start = date.atStartOfDay().format(FMT)
        val end   = date.atTime(23, 59).format(FMT)
        return client.get(BASE_URL) {
            parameter("time_trunc", "hour")
            parameter("start_date", start)
            parameter("end_date",   end)
        }.body()
    }
}
