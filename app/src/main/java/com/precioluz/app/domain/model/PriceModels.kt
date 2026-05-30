package com.precioluz.app.domain.model

data class PriceHour(
    val hour: Int,
    val priceKwh: Double,
    val priceMwh: Double
)

data class PriceDay(
    val date: String,
    val prices: List<PriceHour>
) {
    val average: Double get() = prices.map { it.priceKwh }.average()
    val min: PriceHour? get() = prices.minByOrNull { it.priceKwh }
    val max: PriceHour? get() = prices.maxByOrNull { it.priceKwh }
}

enum class PriceTier {
    CHEAP,
    AFFORDABLE,
    MEDIUM,
    DEAR
}

fun PriceHour.tier(day: PriceDay): PriceTier {
    val sorted = day.prices.map { it.priceKwh }.sorted()
    val p25 = sorted.percentile(25)
    val p50 = sorted.percentile(50)
    val p75 = sorted.percentile(75)
    return when {
        priceKwh <= p25 -> PriceTier.CHEAP
        priceKwh <= p50 -> PriceTier.AFFORDABLE
        priceKwh <= p75 -> PriceTier.MEDIUM
        else -> PriceTier.DEAR
    }
}

private fun List<Double>.percentile(percentile: Int): Double {
    if (isEmpty()) return 0.0
    val index = (percentile / 100.0) * (size - 1)
    val lower = index.toInt()
    val upper = lower + 1
    if (upper >= size) return get(lower)
    val fraction = index - lower
    return get(lower) + fraction * (get(upper) - get(lower))
}
