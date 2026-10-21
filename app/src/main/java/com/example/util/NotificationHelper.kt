package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID = "dramaku_updates_channel"
    private const val CHANNEL_NAME = "Pembaruan Episode Dramaku"
    private const val CHANNEL_DESC = "Notifikasi rilis episode terbaru drama pendek dan series favorit"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendEpisodeNotification(
        context: Context,
        dramaId: String,
        dramaTitle: String,
        episodeNumber: Int,
        episodeTitle: String,
        customMessage: String? = null
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_DRAMA_ID", dramaId)
            putExtra("EXTRA_NAVIGATE_EPISODE", episodeNumber)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (dramaId.hashCode() + episodeNumber),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚡ Episode Baru Rilis: $dramaTitle"
        val body = customMessage ?: "Episode $episodeNumber ($episodeTitle) sudah tayang dengan Subtitle Indonesia! Klik untuk menonton."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(0xFFFF2D55.toInt())
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_media_play,
                "Tonton Sekarang",
                pendingIntent
            )
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = (System.currentTimeMillis() % 10000).toInt()
            notificationManager.notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission might not be granted yet, handled gracefully
        }
    }
}
