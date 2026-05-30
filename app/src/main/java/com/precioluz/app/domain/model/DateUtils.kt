package com.precioluz.app.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class DateTarget(
    val date: String,
    val label: String
)

fun getTargetDate(): DateTarget {
    val madrid = TimeZone.of("Europe/Madrid")
    val now = Clock.System.now()
    val today = now.toLocalDateTime(madrid)
    return if (today.hour >= 20) {
        DateTarget(
            date = today.date.plus(java.time.Period.ofDays(1)).toString(),
            label = "mañana"
        )
    } else {
        DateTarget(
            date = today.date.toString(),
            label = "hoy"
        )
    }
}
