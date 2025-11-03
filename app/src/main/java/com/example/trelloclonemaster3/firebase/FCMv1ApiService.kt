package com.example.trelloclonemaster3.firebase

import android.content.Context
import android.util.Log
import com.example.trelloclonemaster3.utils.FCMConstants
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class FCMv1ApiService(private val context: Context) {

    companion object {
        private const val TAG = "FCMv1ApiService"
        private val PROJECT_ID = FCMConstants.PROJECT_ID // Use from FCMConstants
        private const val FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging"
        private val FCM_ENDPOINT = FCMConstants.FCM_V1_ENDPOINT // Use from FCMConstants
    }

    private val client = OkHttpClient()

    /**
     * Send a data message using FCM HTTP v1 API
     * @param token The FCM token of the target device
     * @param title The notification title
     * @param body The notification body
     * @param data Additional data to send with the notification
     * @param notificationType The type of notification (join_request, task_assigned, etc.)
     */
    suspend fun sendDataMessage(
        token: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
        notificationType: String = "general"
    ): Boolean {
        Log.d(TAG, "=== FCM v1 DEBUG START ===")
        Log.d(TAG, "PROJECT_ID: $PROJECT_ID")
        Log.d(TAG, "FCM_ENDPOINT: $FCM_ENDPOINT")
        Log.d(TAG, "Target token: ${token.take(20)}...")
        Log.d(TAG, "Title: $title")
        Log.d(TAG, "Body: $body")
        Log.d(TAG, "Data: $data")
        Log.d(TAG, "NotificationType: $notificationType")

        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting access token...")
                val accessToken = getAccessToken()
                if (accessToken.isEmpty()) {
                    Log.e(TAG, "❌ Failed to obtain access token")
                    return@withContext false
                }
                Log.d(TAG, "✅ Access token obtained: ${accessToken.take(20)}...")

                Log.d(TAG, "Creating message payload...")
                val messageJson = createMessagePayload(token, title, body, data, notificationType)
                Log.d(TAG, "Message payload: $messageJson")

                val mediaType = MediaType.parse("application/json")
                val requestBody = RequestBody.create(mediaType, messageJson.toString())

                val request = Request.Builder()
                    .url(FCM_ENDPOINT)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("Content-Type", "application/json")
                    .build()

                Log.d(TAG, "Sending FCM request...")
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ FCM message sent successfully")
                    Log.d(TAG, "Response: ${response.body()?.string()}")
                    Log.d(TAG, "=== FCM v1 DEBUG END (SUCCESS) ===")
                    return@withContext true
                } else {
                    val responseBody = response.body()?.string()
                    Log.e(TAG, "❌ FCM message failed: ${response.code()} - $responseBody")
                    Log.e(TAG, "=== FCM v1 DEBUG END (FAILED) ===")
                    return@withContext false
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending FCM message", e)
                Log.e(TAG, "=== FCM v1 DEBUG END (ERROR) ===")
                return@withContext false
            }
        }
    }

    /**
     * Send notification to multiple tokens
     */
    suspend fun sendMulticastMessage(
        tokens: List<String>,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
        notificationType: String = "general"
    ): Int {
        var successCount = 0
        for (token in tokens) {
            if (sendDataMessage(token, title, body, data, notificationType)) {
                successCount++
            }
        }
        return successCount
    }

    /**
     * Send notification to a topic
     */
    suspend fun sendTopicMessage(
        topic: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
        notificationType: String = "general"
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken()
                if (accessToken.isEmpty()) {
                    Log.e(TAG, "Failed to obtain access token")
                    return@withContext false
                }

                val messageJson =
                    createTopicMessagePayload(topic, title, body, data, notificationType)
                val mediaType = MediaType.parse("application/json")
                val requestBody = RequestBody.create(mediaType, messageJson.toString())

                val request = Request.Builder()
                    .url(FCM_ENDPOINT)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d(TAG, "FCM topic message sent successfully")
                    return@withContext true
                } else {
                    Log.e(
                        TAG,
                        "FCM topic message failed: ${response.code()} - ${
                            response.body()?.string()
                        }"
                    )
                    return@withContext false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending FCM topic message", e)
                return@withContext false
            }
        }
    }

    /**
     * Get OAuth2 access token using service account credentials
     */
    private suspend fun getAccessToken(): String {
        return withContext(Dispatchers.IO) {
            try {
                // Load service account key from assets
                val serviceAccountStream: InputStream =
                    context.assets.open("service-account-key.json")

                val credentials = GoogleCredentials.fromStream(serviceAccountStream)
                    .createScoped(listOf(FCM_SCOPE))

                credentials.refresh()
                credentials.accessToken?.tokenValue ?: ""
            } catch (e: Exception) {
                Log.e(TAG, "Error getting access token", e)
                // Fallback: try to use application default credentials
                try {
                    val credentials = GoogleCredentials.getApplicationDefault()
                        .createScoped(listOf(FCM_SCOPE))
                    credentials.refresh()
                    credentials.accessToken?.tokenValue ?: ""
                } catch (fallbackException: Exception) {
                    Log.e(TAG, "Fallback access token also failed", fallbackException)
                    ""
                }
            }
        }
    }

    /**
     * Create message payload for device token
     */
    private fun createMessagePayload(
        token: String,
        title: String,
        body: String,
        data: Map<String, String>,
        notificationType: String
    ): JSONObject {
        val messageJson = JSONObject()
        val message = JSONObject()

        // Set target token
        message.put("token", token)

        // Android specific configuration
        val android = JSONObject()
        android.put("priority", "high")

        // Data payload (always delivered to app)
        val dataPayload = JSONObject()
        dataPayload.put("title", title)
        dataPayload.put("body", body)
        dataPayload.put("type", notificationType)

        // Add custom data
        for ((key, value) in data) {
            dataPayload.put(key, value)
        }

        android.put("data", dataPayload)
        message.put("android", android)

        // iOS specific configuration
        val apns = JSONObject()
        val payload = JSONObject()
        val aps = JSONObject()

        aps.put("alert", JSONObject().apply {
            put("title", title)
            put("body", body)
        })
        aps.put("sound", "default")
        aps.put("badge", 1)

        payload.put("aps", aps)

        // Add custom data for iOS
        for ((key, value) in data) {
            payload.put(key, value)
        }
        payload.put("type", notificationType)

        apns.put("payload", payload)
        message.put("apns", apns)

        messageJson.put("message", message)
        return messageJson
    }

    /**
     * Create message payload for topic
     */
    private fun createTopicMessagePayload(
        topic: String,
        title: String,
        body: String,
        data: Map<String, String>,
        notificationType: String
    ): JSONObject {
        val messageJson = JSONObject()
        val message = JSONObject()

        // Set target topic
        message.put("topic", topic)

        // Add notification payload for display
        val notification = JSONObject()
        notification.put("title", title)
        notification.put("body", body)
        message.put("notification", notification)

        // Android specific configuration
        val android = JSONObject()
        android.put("priority", "high")

        // Data payload
        val dataPayload = JSONObject()
        dataPayload.put("type", notificationType)

        // Add custom data
        for ((key, value) in data) {
            dataPayload.put(key, value)
        }

        android.put("data", dataPayload)
        message.put("android", android)

        // iOS specific configuration
        val apns = JSONObject()
        val payload = JSONObject()
        val aps = JSONObject()

        aps.put("sound", "default")
        aps.put("badge", 1)

        payload.put("aps", aps)

        // Add custom data for iOS
        for ((key, value) in data) {
            payload.put(key, value)
        }
        payload.put("type", notificationType)

        apns.put("payload", payload)
        message.put("apns", apns)

        messageJson.put("message", message)
        return messageJson
    }
}