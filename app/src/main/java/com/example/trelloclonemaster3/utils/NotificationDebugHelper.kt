package com.example.trelloclonemaster3.utils

import android.content.Context
import android.util.Log
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.firebase.FCMv1ApiService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object NotificationDebugHelper {

    private const val TAG = "NotificationDebugHelper"

    /**
     * Test complete notification flow step by step
     */
    fun testCompleteNotificationFlow(context: Context) {
        Log.d(TAG, "🚀 === COMPLETE NOTIFICATION FLOW TEST START ===")

        // Step 1: Check current user
        val currentUserId = FirestoreClass().getCurrentUserID()
        Log.d(TAG, "📱 Step 1: Current User ID: $currentUserId")

        if (currentUserId.isEmpty()) {
            Log.e(TAG, "❌ Test failed: User not logged in")
            return
        }

        // Step 2: Check FCM service availability
        testFCMService(context)

        // Step 3: Test database notification creation
        TestNotificationHelper.createSampleNotifications(context)

        Log.d(TAG, "✅ === COMPLETE NOTIFICATION FLOW TEST END ===")
    }

    /**
     * Test FCM service directly
     */
    private fun testFCMService(context: Context) {
        Log.d(TAG, "🔥 Step 2: Testing FCM Service...")

        GlobalScope.launch {
            try {
                val fcmService = FCMv1ApiService(context)

                // Test with a dummy token (will fail but we can see the logs)
                val result = fcmService.sendDataMessage(
                    "dummy_token_for_testing_12345",
                    "Test Notification",
                    "This is a test notification to check FCM setup",
                    mapOf("test" to "data"),
                    "test"
                )

                Log.d(TAG, "FCM Test Result: $result")

            } catch (e: Exception) {
                Log.e(TAG, "FCM Test Error", e)
            }
        }
    }

    /**
     * Check if all required components are present
     */
    fun checkSystemHealth(context: Context): Boolean {
        Log.d(TAG, "🏥 === SYSTEM HEALTH CHECK START ===")

        var isHealthy = true

        // Check 1: User logged in
        val currentUserId = FirestoreClass().getCurrentUserID()
        if (currentUserId.isEmpty()) {
            Log.e(TAG, "❌ Health Check Failed: User not logged in")
            isHealthy = false
        } else {
            Log.d(TAG, "✅ User logged in: $currentUserId")
        }

        // Check 2: Service account file exists
        try {
            val inputStream = context.assets.open("service-account-key.json")
            inputStream.close()
            Log.d(TAG, "✅ Service account key file exists")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Health Check Failed: Service account key not found", e)
            isHealthy = false
        }

        // Check 3: FCM Constants configured
        if (FCMConstants.PROJECT_ID.isEmpty() || FCMConstants.PROJECT_ID == "your-project-id") {
            Log.e(TAG, "❌ Health Check Failed: PROJECT_ID not configured properly")
            isHealthy = false
        } else {
            Log.d(TAG, "✅ PROJECT_ID configured: ${FCMConstants.PROJECT_ID}")
        }

        Log.d(
            TAG,
            if (isHealthy) "✅ === SYSTEM HEALTH CHECK PASSED ===" else "❌ === SYSTEM HEALTH CHECK FAILED ==="
        )
        return isHealthy
    }

    /**
     * Simulate a join request to test the complete flow
     */
    fun simulateJoinRequest(context: Context, targetUserId: String? = null) {
        Log.d(TAG, "🎭 === SIMULATING JOIN REQUEST ===")

        val currentUserId = FirestoreClass().getCurrentUserID()
        val managerId = targetUserId ?: currentUserId // If no target, send to self

        Log.d(TAG, "Current User (Requester): $currentUserId")
        Log.d(TAG, "Target Manager: $managerId")

        FirestoreClass().sendJoinRequestNotificationV1(
            context,
            managerId,
            "Test Project Board",
            "Test Requester Name",
            "test_board_123"
        )

        Log.d(TAG, "🎭 === JOIN REQUEST SIMULATION SENT ===")
    }
}