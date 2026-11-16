package com.precioluz.app.data.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.precioluz.app.domain.model.dailyNotification
import com.precioluz.app.domain.model.tomorrowNotification
import com.precioluz.app.domain.usecase.GetTodayPricesUseCase
import com.precioluz.app.domain.usecase.GetTomorrowPricesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class PriceNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getTodayPrices: GetTodayPricesUseCase,
    private val getTomorrowPrices: GetTomorrowPricesUseCase,
    private val notifier: Notifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        when (inputData.getString(KEY_TYPE)) {
            TYPE_TOMORROW -> {
                val day = getTomorrowPrices().firstOrNull()?.getOrNull()
                val (title, body) = tomorrowNotification(day)
                notifier.show(Notifier.ID_TOMORROW, title, body)
            }
            TYPE_DAILY -> {
                val day = getTodayPrices().firstOrNull()?.getOrNull()
                if (day != null) {
                    val (title, body) = dailyNotification(day)
                    notifier.show(Notifier.ID_DAILY, title, body)
                }
            }
        }
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    companion object {
        const val KEY_TYPE = "type"
        const val TYPE_TOMORROW = "tomorrow"
        const val TYPE_DAILY = "daily"
    }
}
