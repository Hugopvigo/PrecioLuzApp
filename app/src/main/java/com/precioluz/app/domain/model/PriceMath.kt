package com.precioluz.app.domain.model

import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

// Formato europeo determinista, independiente del locale del dispositivo.
fun formatPrice(price: Double, decimals: Int = 3): String =
    String.format(Locale.US, "%.${decimals}f", price).replace('.', ',')

// La franja de `length` horas seguidas más barata que empieza en `from` o después.
// null si ya no quedan tantas horas en el día. Es cheapestWindow() de la web.
fun cheapestWindow(hours: List<HourPrice>, from: Int, length: Int): IntRange? {
    if (length <= 0 || hours.isEmpty()) return null
    var best: IntRange? = null
    var bestAvg = Double.MAX_VALUE
    for (start in from..(hours.size - length)) {
        val end = start + length - 1
        val avg = (start..end).sumOf { hours[it].price } / length
        if (avg < bestAvg) {
            bestAvg = avg
            best = start..end
        }
    }
    return best
}

fun windowAverage(hours: List<HourPrice>, window: IntRange): Double =
    window.sumOf { hours[it].price } / window.count()

// «60 % por encima de la media de hoy (0,209)», como en NowCard de la web.
fun versusAverage(price: Double, avg: Double): String {
    if (avg <= 0.0) return "Media ${formatPrice(avg)}"
    val diff = ((price / avg - 1.0) * 100).roundToInt()
    return when {
        diff == 0 -> "En la media de hoy (${formatPrice(avg)})"
        diff < 0  -> "${abs(diff)} % por debajo de la media de hoy (${formatPrice(avg)})"
        else      -> "${abs(diff)} % por encima de la media de hoy (${formatPrice(avg)})"
    }
}

// Ahorro de esperar a la mejor franja frente a empezar ahora, en %.
fun savingPercent(bestAvg: Double, nowAvg: Double): Int =
    if (nowAvg > 0.0) ((1.0 - bestAvg / nowAvg) * 100).roundToInt() else 0
