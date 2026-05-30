package es.hugopvigo.precioluz.domain.usecase

import es.hugopvigo.precioluz.data.repository.PrecioLuzRepository
import es.hugopvigo.precioluz.domain.model.DayPrices
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTomorrowPricesUseCase @Inject constructor(
    private val repository: PrecioLuzRepository,
) {
    // Los precios de mañana se publican ~20:15. Antes devuelve null.
    operator fun invoke(): Flow<Result<DayPrices?>> = repository.getTomorrowPrices()
}
