package com.precioluz.app.domain.usecase

import com.precioluz.app.data.repository.PriceRepository
import com.precioluz.app.domain.model.DateTarget
import com.precioluz.app.domain.model.PriceDay
import com.precioluz.app.domain.model.getTargetDate
import javax.inject.Inject

class GetPricesUseCase @Inject constructor(
    private val repository: PriceRepository
) {
    suspend operator fun invoke(date: String): PriceDay? {
        return repository.getPrices(date)
    }

    suspend fun getToday(): Pair<PriceDay?, DateTarget> {
        val target = getTargetDate()
        val prices = repository.getPrices(target.date)
        return prices to target
    }

    suspend fun getTomorrow(): PriceDay? {
        val target = getTargetDate()
        val tomorrowDate = if (target.label == "mañana") {
            target.date
        } else {
            val today = java.time.LocalDate.parse(target.date)
            today.plusDays(1).toString()
        }
        return repository.getPrices(tomorrowDate)
    }
}
