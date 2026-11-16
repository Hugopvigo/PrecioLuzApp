package com.precioluz.app.domain.model

// Texto de los avisos. Funciones puras para poder probarlas sin Android.
fun tomorrowNotification(day: DayPrices?): Pair<String, String> {
    if (day == null) {
        return "Precios de mañana" to
            "Todavía no están disponibles. Vuelve a abrir la app más tarde."
    }
    val best = day.bestWindow
    val bestAvg = windowAverage(day.hours, best)
    return "Precios de mañana" to
        "Media ${formatPrice(day.avg)} €/kWh · mejor franja ${hour(best.first)}:00–${hour(best.last + 1)}:00 " +
        "(${formatPrice(bestAvg)} €/kWh)"
}

fun dailyNotification(day: DayPrices): Pair<String, String> =
    "Resumen del día" to
        "Mínima ${formatPrice(day.min.price)} a las ${hour(day.min.hour)} h · " +
        "media ${formatPrice(day.avg)} · " +
        "máxima ${formatPrice(day.max.price)} a las ${hour(day.max.hour)} h"

private fun hour(h: Int): String = (h % 24).toString().padStart(2, '0')
