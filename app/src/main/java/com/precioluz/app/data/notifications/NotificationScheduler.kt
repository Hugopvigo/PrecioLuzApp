package com.precioluz.app.data.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.precioluz.app.data.datastore.SettingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// Programa los dos avisos diarios y los cancela cuando se apagan en Ajustes.
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsDataStore,
) {
    suspend fun sync() {
        if (settings.notifyTomorrow.first()) {
            enqueue(WORK_TOMORROW, PriceNotificationWorker.TYPE_TOMORROW, 20, 15)
        } else {
            cancel(WORK_TOMORROW)
        }

        if (settings.notifyDaily.first()) {
            enqueue(WORK_DAILY, PriceNotificationWorker.TYPE_DAILY, 8, 0)
        } else {
            cancel(WORK_DAILY)
        }
    }

    private fun enqueue(uniqueName: String, type: String, hour: Int, minute: Int) {
        val request = PeriodicWorkRequestBuilder<PriceNotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayUntil(hour, minute), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(PriceNotificationWorker.KEY_TYPE to type))
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private fun cancel(uniqueName: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName)
    }

    private fun delayUntil(hour: Int, minute: Int): Long {
        val zone = ZoneId.of("Europe/Madrid")
        val now = ZonedDateTime.now(zone)
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis()
    }

    private companion object {
        const val WORK_TOMORROW = "notify_tomorrow_work"
        const val WORK_DAILY = "notify_daily_work"
    }
}
