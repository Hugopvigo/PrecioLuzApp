package com.precioluz.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PriceMathTest {

    private fun hours(prices: List<Double>) = prices.mapIndexed { hour, price ->
        HourPrice(hour = hour, price = price, tramo = Tramo.LLANO, tier = PriceTier.GREEN)
    }

    @Test
    fun `cheapestWindow elige las dos horas contiguas mas baratas`() {
        val day = hours(listOf(0.10, 0.20, 0.05, 0.06, 0.30, 0.11) + List(18) { 0.20 })
        assertEquals(2..3, cheapestWindow(day, 0, 2))
        assertEquals(0.055, windowAverage(day, 2..3), 1e-9)
    }

    @Test
    fun `cheapestWindow respeta la hora desde la que se busca`() {
        val day = hours(listOf(0.01, 0.02) + List(22) { 0.20 })
        assertEquals(2..3, cheapestWindow(day, 2, 2))
    }

    @Test
    fun `cheapestWindow devuelve null si no quedan horas suficientes`() {
        val day = hours(List(24) { 0.20 })
        assertNull(cheapestWindow(day, 23, 2))
        assertNull(cheapestWindow(day, 0, 0))
    }

    @Test
    fun `cheapestWindow con una hora devuelve la hora mas barata`() {
        val day = hours(listOf(0.20, 0.09, 0.15) + List(21) { 0.30 })
        assertEquals(1..1, cheapestWindow(day, 0, 1))
    }

    @Test
    fun `versusAverage describe por encima, por debajo y en la media`() {
        assertEquals("50 % por encima de la media de hoy (0,200)", versusAverage(0.30, 0.20))
        assertEquals("50 % por debajo de la media de hoy (0,200)", versusAverage(0.10, 0.20))
        assertEquals("En la media de hoy (0,200)", versusAverage(0.20, 0.20))
    }

    @Test
    fun `savingPercent calcula el ahorro de esperar`() {
        assertEquals(20, savingPercent(0.08, 0.10))
        assertEquals(0, savingPercent(0.10, 0.10))
        assertEquals(0, savingPercent(0.08, 0.0))
    }

    @Test
    fun `formatPrice usa coma decimal`() {
        assertEquals("0,3339", formatPrice(0.3339, 4))
        assertEquals("0,113", formatPrice(0.1129, 3))
    }
}
