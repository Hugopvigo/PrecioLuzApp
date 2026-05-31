package com.precioluz.app.data.repository

import com.precioluz.app.data.api.DayJson
import com.precioluz.app.data.api.PrecioLuzJsonApi
import com.precioluz.app.data.local.PriceDao
import com.precioluz.app.data.local.PriceEntity
import com.precioluz.app.domain.model.DayPrices
import com.precioluz.app.domain.model.HourPrice
import com.precioluz.app.domain.model.PriceTier
import com.precioluz.app.domain.model.Tramo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrecioLuzRepository @Inject constructor(
    private val jsonApi: PrecioLuzJsonApi,
    private val dao: PriceDao,
) {
    fun getTodayPrices(): Flow<Result<DayPrices>> = flow {
        emit(runCatching { getPricesForDate(LocalDate.now()) })
    }

    fun getTomorrowPrices(): Flow<Result<DayPrices?>> = flow {
        emit(runCatching {
            val publishedAt = LocalDate.now().atTime(20, 15)
                .atZone(ZoneId.of("Europe/Madrid"))
            if (ZonedDateTime.now(ZoneId.of("Europe/Madrid")).isBefore(publishedAt)) null
            else getPricesForDate(LocalDate.now().plusDays(1))
        })
    }

    private suspend fun getPricesForDate(date: LocalDate): DayPrices =
        withContext(Dispatchers.IO) {
            val dateStr = date.toString()

            // 1. Cache Room: si hay 24 registros, sin llamada de red
            val cached = dao.getPricesForDate(dateStr)
            if (cached.size == 24) {
                return@withContext buildDayPrices(date, cached.map { it.hour to it.priceKwh })
            }

            // 2. Fetch del servidor (hoy + mañana en una sola llamada)
            val response = jsonApi.fetch()

            // 3. Guardar todos los días recibidos en Room
            listOfNotNull(response.today, response.tomorrow).forEach { saveToRoom(it) }

            // 4. Limpiar fechas anteriores a ayer
            dao.deletePricesOlderThan(LocalDate.now().minusDays(1).toString())

            // 5. Buscar la fecha pedida en la respuesta
            val dayJson = when (dateStr) {
                response.today.date     -> response.today
                response.tomorrow?.date -> response.tomorrow
                else                    -> throw Exception("NO_DATA")
            }
            if (dayJson.prices.size != 24) throw Exception("NO_DATA")

            buildDayPrices(date, dayJson.prices.mapIndexed { idx, p -> idx to p })
        }

    private suspend fun saveToRoom(dayJson: DayJson) {
        if (dayJson.prices.size != 24) return
        dao.insertPrices(dayJson.prices.mapIndexed { hour, price ->
            PriceEntity(date = dayJson.date, hour = hour, priceKwh = price, priceMwh = price * 1000)
        })
    }

    private fun buildDayPrices(date: LocalDate, prices: List<Pair<Int, Double>>): DayPrices {
        val sorted  = prices.sortedBy { it.second }
        val tierMap = sorted.mapIndexed { rank, (hour, _) ->
            hour to PriceTier.entries[minOf(3, rank / 6)]
        }.toMap()

        val hours = prices.map { (hour, price) ->
            HourPrice(
                hour  = hour,
                price = price,
                tramo = tramoForHour(hour, date),
                tier  = tierMap[hour] ?: PriceTier.GREEN,
            )
        }

        val minH = hours.minBy { it.price }
        val maxH = hours.maxBy { it.price }
        val decorated = hours.map { h ->
            h.copy(isMin = h.hour == minH.hour, isMax = h.hour == maxH.hour)
        }
        val bestStart  = (0..22).minBy { i -> decorated[i].price + decorated[i + 1].price }
        val worstStart = (0..22).maxBy { i -> decorated[i].price + decorated[i + 1].price }

        return DayPrices(
            date        = date,
            hours       = decorated,
            avg         = hours.map { it.price }.average(),
            min         = hours[minH.hour],
            max         = hours[maxH.hour],
            bestWindow  = bestStart..bestStart + 1,
            worstWindow = worstStart..worstStart + 1,
        )
    }

    private fun tramoForHour(hour: Int, date: LocalDate): Tramo {
        val dow = date.dayOfWeek.value
        if (dow >= 6) return Tramo.VALLE
        if ((hour in 10..13) || (hour in 18..21)) return Tramo.PUNTA
        if (hour < 8) return Tramo.VALLE
        return Tramo.LLANO
    }
}
