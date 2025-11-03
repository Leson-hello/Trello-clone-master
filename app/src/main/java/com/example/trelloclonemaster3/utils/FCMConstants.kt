package com.example.trelloclonemaster3.utils

object FCMConstants {

    // DEPRECATED: Legacy FCM API endpoint - will be removed June 21, 2024
    @Deprecated("Use FCM HTTP v1 API instead", ReplaceWith("FCMv1ApiService"))
    const val BASE_URL: String = "https://fcm.googleapis.com/"

    // DEPRECATED: Legacy server key - migrate to service account JSON and OAuth2
    @Deprecated(
        "Use service account JSON file with OAuth2 for FCM v1 API",
        ReplaceWith("FCMv1ApiService")
    )
    const val SERVER_KEY: String = "46500a3aa0c4cd5e6ba63323b372439f1520c301"

    // DEPRECATED: Legacy content type
    @Deprecated("Use FCM HTTP v1 API instead")
    const val CONTENT_TYPE: String = "application/json"

    // DEPRECATED: Legacy FCM keys
    @Deprecated("Use FCM HTTP v1 API instead")
    const val FCM_KEY_TITLE: String = "title"

    @Deprecated("Use FCM HTTP v1 API instead")
    const val FCM_KEY_MESSAGE: String = "message"

    @Deprecated("Use FCM HTTP v1 API instead")
    const val FCM_KEY_DATA: String = "data"

    @Deprecated("Use FCM HTTP v1 API instead")
    const val FCM_KEY_TO: String = "to"

    // NEW: FCM v1 API Constants
    const val FCM_V1_SCOPE = "https://www.googleapis.com/auth/firebase.messaging"

    // TODO: Replace with your actual Firebase project ID
    const val PROJECT_ID = "trello-clone-master-39c85"

    // FCM v1 API endpoint
    const val FCM_V1_ENDPOINT = "https://fcm.googleapis.com/v1/projects/$PROJECT_ID/messages:send"

    // Service account file name (should be placed in assets folder)
    const val SERVICE_ACCOUNT_FILE = "service-account-key.json"

    // Notification types for different events
    const val NOTIFICATION_TYPE_JOIN_REQUEST = "join_request"
    const val NOTIFICATION_TYPE_JOIN_ACCEPTED = "join_accepted"
    const val NOTIFICATION_TYPE_JOIN_REJECTED = "join_rejected"
    const val NOTIFICATION_TYPE_TASK_ASSIGNED = "task_assigned"
    const val NOTIFICATION_TYPE_TASK_DUE = "task_due"
    const val NOTIFICATION_TYPE_TASK_COMPLETED = "task_completed"
    const val NOTIFICATION_TYPE_GENERAL = "general"
}
