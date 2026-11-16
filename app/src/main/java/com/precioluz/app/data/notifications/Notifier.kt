package com.precioluz.app.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.precioluz.app.MainActivity
import com.precioluz.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Notifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun show(id: Int, title: String, body: String) {
        val manager = context.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Precios de la luz",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Avisos diarios del PVPC" }
            manager.createNotificationChannel(channel)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            flags,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        manager.notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "precios"
        const val ID_TOMORROW = 1
        const val ID_DAILY = 2
    }
}
