package com.example.trelloclonemaster3.utils

import android.content.Context
import android.util.Log
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.model.Notification
import com.example.trelloclonemaster3.repository.NotificationRepository

object TestNotificationHelper {

    fun createSampleNotifications(context: Context) {
        val repository = NotificationRepository(context)
        val currentUserId = FirestoreClass().getCurrentUserID()

        if (currentUserId.isEmpty()) return

        Log.d("TestNotificationHelper", "=== CREATING SAMPLE NOTIFICATIONS ===")
        Log.d("TestNotificationHelper", "Current User ID: $currentUserId")

        val sampleNotifications = listOf(
            Notification(
                title = "New Task Assigned",
                body = "You have been assigned to complete the task 'Update UI Design' in project 'Mobile App Development'",
                type = FCMConstants.NOTIFICATION_TYPE_TASK_ASSIGNED,
                userId = currentUserId,
                projectId = "project123",
                projectName = "Mobile App Development",
                senderName = "John Doe",
                timestamp = System.currentTimeMillis() - (1000 * 60 * 30), // 30 minutes ago
                isRead = false
            ),
            Notification(
                title = "Join Request Approved",
                body = "Your request to join project 'Web Development' has been approved by Sarah Smith",
                type = FCMConstants.NOTIFICATION_TYPE_JOIN_ACCEPTED,
                userId = currentUserId,
                projectId = "project456",
                projectName = "Web Development",
                senderName = "Sarah Smith",
                timestamp = System.currentTimeMillis() - (1000 * 60 * 60 * 2), // 2 hours ago
                isRead = false
            ),
            Notification(
                title = "Task Due Soon",
                body = "Your task 'API Integration' is due in 2 hours",
                type = FCMConstants.NOTIFICATION_TYPE_TASK_DUE,
                userId = currentUserId,
                projectId = "project123",
                projectName = "Mobile App Development",
                senderName = "System",
                timestamp = System.currentTimeMillis() - (1000 * 60 * 60 * 6), // 6 hours ago
                isRead = true
            ),
            Notification(
                title = "Task Completed",
                body = "Mike Johnson has completed the task 'Database Setup'",
                type = FCMConstants.NOTIFICATION_TYPE_TASK_COMPLETED,
                userId = currentUserId,
                projectId = "project789",
                projectName = "Backend Development",
                senderName = "Mike Johnson",
                timestamp = System.currentTimeMillis() - (1000 * 60 * 60 * 24), // 1 day ago
                isRead = true
            ),
            Notification(
                title = "New Join Request",
                body = "Alex Wilson wants to join your project 'Mobile App Development'",
                type = FCMConstants.NOTIFICATION_TYPE_JOIN_REQUEST,
                userId = currentUserId,
                projectId = "project123",
                projectName = "Mobile App Development",
                senderName = "Alex Wilson",
                timestamp = System.currentTimeMillis() - (1000 * 60 * 10), // 10 minutes ago
                isRead = false
            )
        )

        sampleNotifications.forEach { notification ->
            Log.d("TestNotificationHelper", "Inserting notification: ${notification.title}")
            repository.insertNotification(notification)
        }

        Log.d(
            "TestNotificationHelper",
            "✅ ${sampleNotifications.size} sample notifications created"
        )
        Log.d("TestNotificationHelper", "=== SAMPLE NOTIFICATIONS COMPLETE ===")
    }

    // NEW: Test FCM notification sending
    fun testFCMNotification(context: Context, targetUserId: String, boardName: String) {
        Log.d("TestNotificationHelper", "=== TESTING FCM NOTIFICATION ===")
        Log.d("TestNotificationHelper", "Target User ID: $targetUserId")
        Log.d("TestNotificationHelper", "Board Name: $boardName")

        FirestoreClass().sendJoinRequestNotificationV1(
            context,
            targetUserId,
            boardName,
            "Test User",
            "test_board_id"
        )
    }
}