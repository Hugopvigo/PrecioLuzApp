package com.precioluz.app.domain.model

import java.time.ZoneId
import java.time.ZonedDateTime

data class DateTarget(
    val date: String,
    val label: String
)

fun getTargetDate(): DateTarget {
    val madrid = ZoneId.of("Europe/Madrid")
    val now    = ZonedDateTime.now(madrid)
    return if (now.hour >= 20) {
        DateTarget(
            date  = now.toLocalDate().plusDays(1).toString(),
            label = "mañana"
        )
    } else {
        DateTarget(
            date  = now.toLocalDate().toString(),
            label = "hoy"
        )
    }
}
