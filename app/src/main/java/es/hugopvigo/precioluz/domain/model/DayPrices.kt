package es.hugopvigo.precioluz.domain.model

import java.time.LocalDate

data class DayPrices(
    val date: LocalDate,
    val hours: List<HourPrice>,     // siempre 24 elementos, índice = hora
    val avg: Double,
    val min: HourPrice,
    val max: HourPrice,
    val bestWindow: IntRange,       // 2 horas contiguas más baratas
    val worstWindow: IntRange,
)
