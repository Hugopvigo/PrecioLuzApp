package com.precioluz.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.precioluz.app.data.repository.PriceRepository
import com.precioluz.app.domain.model.getTargetDate
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PriceSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: PriceRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val target = getTargetDate()
        val prices = repository.getPrices(target.date)

        return if (prices != null) {
            Result.success()
        } else {
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val MAX_RETRIES = 3
    }
}
