package com.precioluz.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class NotificationMessagesTest {

    private fun day() = DayPrices(
        date = LocalDate.of(2026, 11, 16),
        hours = List(24) { hour ->
            HourPrice(hour, 0.20, Tramo.LLANO, PriceTier.GREEN)
        }.toMutableList().also { hours ->
            hours[2] = hours[2].copy(price = 0.07)
            hours[3] = hours[3].copy(price = 0.08)
            hours[20] = hours[20].copy(price = 0.34)
        },
        avg = 0.20,
        min = HourPrice(2, 0.07, Tramo.VALLE, PriceTier.GREEN),
        max = HourPrice(20, 0.34, Tramo.PUNTA, PriceTier.RED),
        bestWindow = 2..3,
        worstWindow = 20..21,
    )

    @Test
    fun `aviso de manana incluye media y mejor franja`() {
        val (title, body) = tomorrowNotification(day())
        assertEquals("Precios de mañana", title)
        assertEquals(
            "Media 0,200 €/kWh · mejor franja 02:00–04:00 (0,075 €/kWh)",
            body,
        )
    }

    @Test
    fun `aviso de manana sin datos avisa de que faltan`() {
        val (_, body) = tomorrowNotification(null)
        assertEquals(
            "Todavía no están disponibles. Vuelve a abrir la app más tarde.",
            body,
        )
    }

    @Test
    fun `resumen del dia incluye minima media y maxima`() {
        val (title, body) = dailyNotification(day())
        assertEquals("Resumen del día", title)
        assertEquals(
            "Mínima 0,070 a las 02 h · media 0,200 · máxima 0,340 a las 20 h",
            body,
        )
    }
}
