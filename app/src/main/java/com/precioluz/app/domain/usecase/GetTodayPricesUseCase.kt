package com.precioluz.app.domain.usecase

import com.precioluz.app.data.repository.PrecioLuzRepository
import com.precioluz.app.domain.model.DayPrices
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodayPricesUseCase @Inject constructor(
    private val repository: PrecioLuzRepository,
) {
    operator fun invoke(): Flow<Result<DayPrices>> = repository.getTodayPrices()
}
