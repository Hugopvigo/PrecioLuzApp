package com.precioluz.app.data.repository

import com.precioluz.app.data.api.EsiosApi
import com.precioluz.app.domain.model.DayPrices
import com.precioluz.app.domain.model.HourPrice
import com.precioluz.app.domain.model.PriceTier
import com.precioluz.app.domain.model.Tramo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrecioLuzRepository @Inject constructor(
    private val api: EsiosApi,
) {
    fun getTodayPrices(): Flow<Result<DayPrices>> = flow {
        emit(runCatching { fetchDay(LocalDate.now()) })
    }

    fun getTomorrowPrices(): Flow<Result<DayPrices?>> = flow {
        emit(runCatching {
            val tomorrow = LocalDate.now().plusDays(1)
            val publishedAt = LocalDate.now().atTime(20, 15)
                .atZone(ZoneId.of("Europe/Madrid"))
            if (ZonedDateTime.now(ZoneId.of("Europe/Madrid")).isBefore(publishedAt)) {
                null  // Todavía no están publicados
            } else {
                fetchDay(tomorrow)
            }
        })
    }

    // ── Fetching + transformación ─────────────────────────────
    private suspend fun fetchDay(date: LocalDate): DayPrices {
        val response  = api.getPrices(date)
        val rawValues = response.included
            .firstOrNull { it.type == "PreciosPVPC" }
            ?.attributes?.values
            ?.takeIf { it.isNotEmpty() }
            ?: response.data.attributes.values

        if (rawValues.isEmpty()) throw Exception("NO_DATA")

        // €/MWh → €/kWh (/ 1000). IVA ya incluido en PVPC desde 2021.
        val prices = rawValues.take(24).mapIndexed { idx, v ->
            idx to (v.value / 1000.0)
        }

        return buildDayPrices(date, prices)
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

        val bestStart = (0..22).minBy { i -> decorated[i].price + decorated[i + 1].price }

        return DayPrices(
            date        = date,
            hours       = decorated,
            avg         = hours.map { it.price }.average(),
            min         = minH,
            max         = maxH,
            bestWindow  = bestStart..bestStart + 1,
            worstWindow = run {
                val w = (0..22).maxBy { i -> decorated[i].price + decorated[i + 1].price }
                w..w + 1
            },
        )
    }

    private fun tramoForHour(hour: Int, date: LocalDate): Tramo {
        val dow = date.dayOfWeek.value  // 1=Lun … 7=Dom
        if (dow >= 6) return Tramo.VALLE
        if ((hour in 10..13) || (hour in 18..21)) return Tramo.PUNTA
        if (hour < 8) return Tramo.VALLE
        return Tramo.LLANO
    }
}
