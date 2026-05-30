package es.hugopvigo.precioluz.domain.usecase

import es.hugopvigo.precioluz.data.repository.PrecioLuzRepository
import es.hugopvigo.precioluz.domain.model.DayPrices
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodayPricesUseCase @Inject constructor(
    private val repository: PrecioLuzRepository,
) {
    operator fun invoke(): Flow<Result<DayPrices>> = repository.getTodayPrices()
}
