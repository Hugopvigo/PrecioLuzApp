package com.precioluz.app.data.repository

import com.precioluz.app.data.local.PriceDao
import com.precioluz.app.data.local.PriceEntity
import com.precioluz.app.data.network.EsiosApi
import com.precioluz.app.domain.model.PriceDay
import com.precioluz.app.domain.model.PriceHour
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceRepository @Inject constructor(
    private val api: EsiosApi,
    private val dao: PriceDao
) {

    suspend fun getPrices(date: String): PriceDay? {
        val cached = dao.getPricesForDate(date)
        if (cached.isNotEmpty()) {
            return cached.toPriceDay(date)
        }

        return fetchFromApi(date)
    }

    private suspend fun fetchFromApi(date: String): PriceDay? {
        return try {
            val start = "${date}T00:00:00+02:00"
            val end = "${date}T23:59:59+02:00"
            val response = api.getPVPCPrices(startDate = start, endDate = end)

            val peninsula = response.indicator.values
                .filter { it.geoId == GEO_ID_PENINSULA }

            val prices = peninsula.mapNotNull { value ->
                val hour = value.timeInterval?.start?.extractHour()
                    ?: value.datetime.extractHour()
                    ?: return@mapNotNull null

                PriceHour(
                    hour = hour,
                    priceKwh = value.value / 1000.0,
                    priceMwh = value.value
                )
            }.sortedBy { it.hour }

            val entities = prices.map { price ->
                PriceEntity(
                    date = date,
                    hour = price.hour,
                    priceKwh = price.priceKwh,
                    priceMwh = price.priceMwh
                )
            }
            dao.insertPrices(entities)

            PriceDay(date = date, prices = prices)
        } catch (_: Exception) {
            null
        }
    }

    private fun String.extractHour(): Int? {
        return substring(11, 13).toIntOrNull()
    }

    private fun List<PriceEntity>.toPriceDay(date: String): PriceDay {
        return PriceDay(
            date = date,
            prices = map { PriceHour(hour = it.hour, priceKwh = it.priceKwh, priceMwh = it.priceMwh) }
        )
    }

    companion object {
        private const val GEO_ID_PENINSULA = 8741
    }
}
