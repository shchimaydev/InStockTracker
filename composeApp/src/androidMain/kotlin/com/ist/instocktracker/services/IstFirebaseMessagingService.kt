package com.ist.instocktracker.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ist.instocktracker.MainActivity
import com.ist.instocktracker.R

class IstFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Extract data layer
        val data = remoteMessage.data
        val linkItemId = data["linkItemId"]

        val notification = remoteMessage.notification
        val title = notification?.title ?: data["title"] ?: "Status Update"
        val body = notification?.body ?: data["body"] ?: "Item state has changed"

        showNotification(title, body, linkItemId)
    }

    private fun showNotification(title: String, body: String, linkItemId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (linkItemId != null) {
                putExtra("linkItemId", linkItemId)
            }
        }

        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlags)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.img_notification_large)
        val channelId = "link_item_updates_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setLargeIcon(bitmap)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // Create Notification Channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Item Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token is typically handled via the specific 'DeviceTokenProvider' flow
        // during authentication as per the design doc, but you can log or
        // trigger a sync here if needed.
        Log.d("IstFirebaseMessagingService", "New token in FirebaseMessagingService: $token")
    }
}