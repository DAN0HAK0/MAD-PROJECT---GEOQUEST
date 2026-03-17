package com.dan.mad_project_geoquest.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dan.mad_project_geoquest.R

object GeoQuestNotificationHelper {

    private const val CLUE_CHANNEL_ID = "geoquest_clue_channel"
    private const val FOUND_CHANNEL_ID = "geoquest_found_channel"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val clueChannel = NotificationChannel(
                CLUE_CHANNEL_ID,
                "Cache Clues",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Clue notifications when you are near a cache"
            }

            val foundChannel = NotificationChannel(
                FOUND_CHANNEL_ID,
                "Cache Found",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification when you discover a cache"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(clueChannel)
            manager.createNotificationChannel(foundChannel)
        }
    }

    fun sendClueNotification(context: Context, cacheTitle: String, clue: String, cacheId: Int) {
        val notification = NotificationCompat.Builder(context, CLUE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📍 Cache Nearby: $cacheTitle")
            .setContentText("Clue: $clue")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Clue: $clue"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(cacheId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun sendFoundNotification(context: Context, cacheTitle: String, points: Int, cacheId: Int) {
        val notification = NotificationCompat.Builder(context, FOUND_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🎉 Cache Found: $cacheTitle")
            .setContentText("You discovered this cache and earned $points points!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(cacheId + 1000, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}