package com.precioluz.app.domain.usecase

import com.precioluz.app.data.repository.PrecioLuzRepository
import com.precioluz.app.domain.model.DayPrices
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTomorrowPricesUseCase @Inject constructor(
    private val repository: PrecioLuzRepository,
) {
    // Los precios de mañana se publican ~20:15. Antes devuelve null.
    operator fun invoke(): Flow<Result<DayPrices?>> = repository.getTomorrowPrices()
}
