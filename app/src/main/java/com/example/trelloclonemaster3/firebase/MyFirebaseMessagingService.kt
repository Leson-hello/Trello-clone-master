package com.example.trelloclonemaster3.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.trelloclonemaster3.MainActivity
import com.example.trelloclonemaster3.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random
import com.example.trelloclonemaster3.model.Notification
import com.example.trelloclonemaster3.repository.NotificationRepository

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "trello_notifications"
        private const val CHANNEL_NAME = "Trello Clone Notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // Send token to your server or save it locally
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "=== FCM MESSAGE RECEIVED DEBUG START ===")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "Message Type: ${remoteMessage.messageType}")

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "✅ Message data payload: ${remoteMessage.data}")

            // Handle data message
            handleDataMessage(remoteMessage.data)

            // Save notification to database
            insertNotificationToDatabase(remoteMessage)
        } else {
            Log.w(TAG, "⚠️ No data payload found")
        }

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "✅ Message Notification Title: ${it.title}")
            Log.d(TAG, "✅ Message Notification Body: ${it.body}")

            // Create and show notification
            showNotification(
                title = it.title ?: "Trello Clone",
                body = it.body ?: "",
                data = remoteMessage.data
            )
        } ?: Log.w(TAG, "⚠️ No notification payload found")

        Log.d(TAG, "=== FCM MESSAGE RECEIVED DEBUG END ===")
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val notificationType = data["type"] ?: "general"
        val title = data["title"] ?: "Trello Clone"
        val body = data["body"] ?: ""

        when (notificationType) {
            "join_request" -> {
                showNotification(
                    title = title,
                    body = body,
                    data = data,
                    iconRes = android.R.drawable.ic_dialog_email,
                    notificationId = Random.nextInt()
                )
            }

            "join_accepted" -> {
                showNotification(
                    title = title,
                    body = body,
                    data = data,
                    iconRes = android.R.drawable.ic_dialog_info,
                    notificationId = Random.nextInt()
                )
            }

            "join_rejected" -> {
                showNotification(
                    title = title,
                    body = body,
                    data = data,
                    iconRes = android.R.drawable.ic_dialog_alert,
                    notificationId = Random.nextInt()
                )
            }

            "task_assigned" -> {
                showNotification(
                    title = title,
                    body = body,
                    data = data,
                    iconRes = android.R.drawable.ic_menu_agenda,
                    notificationId = Random.nextInt()
                )
            }

            "task_due" -> {
                showNotification(
                    title = title,
                    body = body,
                    data = data,
                    iconRes = android.R.drawable.ic_lock_idle_alarm,
                    notificationId = Random.nextInt(),
                    priority = NotificationCompat.PRIORITY_HIGH
                )
            }

            "task_completed" -> {
                showNotification(
                    title = title,
                    body = body,
                    data = data,
                    iconRes = android.R.drawable.ic_menu_info_details,
                    notificationId = Random.nextInt()
                )
            }

            else -> {
                showNotification(
                    title = title,
                    body = body,
                    data = data,
                    notificationId = Random.nextInt()
                )
            }
        }
    }

    private fun insertNotificationToDatabase(remoteMessage: RemoteMessage) {
        Log.d(TAG, "=== SAVING NOTIFICATION TO DB DEBUG START ===")

        val type = remoteMessage.data["type"] ?: "general"
        val title = remoteMessage.data["title"]
            ?: remoteMessage.notification?.title
            ?: "Trello Clone"
        val body = remoteMessage.data["body"]
            ?: remoteMessage.notification?.body
            ?: ""
        val timestamp = System.currentTimeMillis()
        val userId = FirestoreClass().getCurrentUserID()

        Log.d(TAG, "Notification type: $type")
        Log.d(TAG, "Notification title: $title")
        Log.d(TAG, "Notification body: $body")
        Log.d(TAG, "Current user ID: $userId")

        // Convert data map to JSON string
        val dataJson = try {
            org.json.JSONObject(remoteMessage.data as Map<*, *>).toString()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to convert data to JSON", e)
            "{}"
        }

        Log.d(TAG, "Data JSON: $dataJson")

        val notification = Notification(
            title = title,
            body = body,
            type = type,
            userId = userId,
            timestamp = timestamp,
            projectId = remoteMessage.data["projectId"] ?: "",
            projectName = remoteMessage.data["projectName"] ?: "",
            senderName = remoteMessage.data["senderName"] ?: "",
            data = dataJson
        )

        Log.d(TAG, "Created notification object: $notification")

        if (userId.isNotEmpty()) {
            val repository = NotificationRepository(this)
            repository.insertNotification(notification)
            Log.d(TAG, "✅ Notification saved to database")
        } else {
            Log.e(TAG, "❌ Cannot save notification: User ID is empty")
        }

        Log.d(TAG, "=== SAVING NOTIFICATION TO DB DEBUG END ===")
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
        iconRes: Int = android.R.drawable.ic_dialog_info,
        notificationId: Int = Random.nextInt(),
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // Add data to intent for handling notification clicks
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        createNotificationChannel()

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setColor(Color.BLUE)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for Trello Clone notifications"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendTokenToServer(token: String) {
        // Update user's FCM token in Firestore
        val currentUserId = FirestoreClass().getCurrentUserID()
        if (currentUserId.isNotEmpty()) {
            FirestoreClass().updateUserFCMToken(currentUserId, token)
        }
    }
}