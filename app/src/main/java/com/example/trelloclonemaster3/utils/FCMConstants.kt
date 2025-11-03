package com.example.trelloclonemaster3.utils

object FCMConstants {

    const val BASE_URL: String = "https://fcm.googleapis.com/"

    // Note: You need to replace this with your actual Firebase server key from Firebase Console
    // Go to Firebase Console > Project Settings > Cloud Messaging > Server Key
    const val SERVER_KEY: String = "f5ce6cf9145d6223283e5e7d32d2cccd93eb27db"

    const val CONTENT_TYPE: String = "application/json"

    const val FCM_KEY_TITLE: String = "title"
    const val FCM_KEY_MESSAGE: String = "message"
    const val FCM_KEY_DATA: String = "data"
    const val FCM_KEY_TO: String = "to"
}
