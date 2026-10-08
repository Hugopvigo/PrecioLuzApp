package com.precioluz.app.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regresión: el placeholder todo-ceros de REE ("día no publicado") nunca es un precio válido. */
class IsPlaceholderDayTest {

    @Test
    fun allZero_isPlaceholder() {
        assertTrue(isPlaceholderDay(List(24) { 0.0 }))
    }

    @Test
    fun empty_isNotPlaceholder() {
        assertFalse(isPlaceholderDay(emptyList()))
    }

    @Test
    fun singleZeroHour_isNotPlaceholder() {
        // Una hora suelta a 0 entre precios reales sí ocurre en el mercado.
        assertFalse(isPlaceholderDay(listOf(0.0) + List(23) { 0.05 + it * 0.001 }))
    }

    @Test
    fun realDay_isNotPlaceholder() {
        assertFalse(isPlaceholderDay(List(24) { 0.05 + it * 0.002 }))
    }

    @Test
    fun almostAllZero_isNotPlaceholder() {
        // 23 horas a 0 con una hora real no es placeholder: se conserva.
        assertFalse(isPlaceholderDay(List(23) { 0.0 } + listOf(0.042)))
    }
}
