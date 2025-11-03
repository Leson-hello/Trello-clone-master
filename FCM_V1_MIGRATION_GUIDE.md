# Firebase Cloud Messaging API v1 Migration Guide

## Overview

This project has been migrated from the deprecated Firebase Cloud Messaging (FCM) Legacy API to the
new Firebase Cloud Messaging API (V1). The legacy API was deprecated on June 20, 2023, and will be
discontinued on June 21, 2024.

## Key Changes

### 1. Authentication Method

- **Before:** Server Key authentication
- **After:** OAuth 2.0 access tokens using service account credentials

### 2. API Endpoint

- **Before:** `https://fcm.googleapis.com/fcm/send`
- **After:** `https://fcm.googleapis.com/v1/projects/{PROJECT_ID}/messages:send`

### 3. Message Payload Structure

- **Before:** Simple JSON with `to`, `notification`, and `data` fields
- **After:** Structured message with platform-specific configurations

## Setup Instructions

### Step 1: Enable Firebase Cloud Messaging API (V1)

1. Go to your [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Navigate to **Project Settings** > **Cloud Messaging**
4. Ensure **Firebase Cloud Messaging API (V1)** is enabled

### Step 2: Generate Service Account Key

1. In Firebase Console, go to **Project Settings** > **Service Accounts**
2. Click **Generate new private key**
3. Download the JSON file
4. Place the JSON file in `app/src/main/assets/` folder as `service-account-key.json`

### Step 3: Update Project Configuration

1. Update `FCMConstants.kt`:
   ```kotlin
   const val PROJECT_ID = "your-actual-project-id"
   ```

2. Replace `your-actual-project-id` with your Firebase project ID

### Step 4: Update Dependencies

The following dependencies have been added to support FCM v1:

```gradle
// Firebase Messaging
implementation 'com.google.firebase:firebase-messaging-ktx'

// Google Auth Library for OAuth2
implementation 'com.google.auth:google-auth-library-oauth2-http:1.19.0'

// WorkManager for background tasks
implementation "androidx.work:work-runtime-ktx:2.9.0"
```

## New Features

### Enhanced Notifications

The app now supports the following notification types:

1. **Join Request Notifications**
    - Sent to project managers when users request to join projects
    - Uses `join_request` notification type

2. **Join Response Notifications**
    - Sent to users when their join requests are approved/rejected
    - Uses `join_accepted`/`join_rejected` notification types

3. **Task Assignment Notifications**
    - Sent when users are assigned to new tasks
    - Uses `task_assigned` notification type

4. **Due Date Reminder Notifications**
    - Sent when tasks are due soon or overdue
    - Uses `task_due` notification type
    - Includes background worker for periodic checks

5. **Task Completion Notifications**
    - Sent to team members when tasks are completed
    - Uses `task_completed` notification type

### Background Services

- **DueDateReminderWorker**: Periodically checks for due tasks and sends reminders
- **MyFirebaseMessagingService**: Handles incoming FCM messages and displays notifications

## Implementation Details

### FCMv1ApiService Class

This new service class handles all FCM v1 API communications:

- **OAuth2 Authentication**: Automatically generates and refreshes access tokens
- **Cross-platform Support**: Supports both Android and iOS message formats
- **Error Handling**: Comprehensive error handling with fallback mechanisms
- **Multiple Recipients**: Supports sending to individual tokens, multiple tokens, or topics

### Notification Flow

1. **Event Occurs** (e.g., task assignment, join request)
2. **FirestoreClass Method Called** (e.g., `sendTaskAssignmentNotification`)
3. **FCMv1ApiService Generates OAuth Token**
4. **Message Sent via HTTP v1 API**
5. **Fallback In-App Notification Stored** (if FCM fails)
6. **MyFirebaseMessagingService Receives Message**
7. **Local Notification Displayed**

### Security Improvements

- **Short-lived Access Tokens**: OAuth2 tokens expire in ~1 hour
- **Reduced Attack Surface**: Service account keys are less frequently transmitted
- **Secure Storage**: Credentials stored securely in assets folder

## Migration Status

### ✅ Completed

- [x] FCM v1 API service implementation
- [x] OAuth2 authentication setup
- [x] Updated notification system
- [x] Join request/response notifications
- [x] Task assignment notifications
- [x] Due date reminder notifications
- [x] Task completion notifications
- [x] Background worker for due date checks
- [x] Firebase Messaging Service for receiving notifications
- [x] In-app notification fallback system

### 🔄 Legacy Code (Deprecated)

- Legacy FCM constants marked as deprecated
- Old notification methods kept for backward compatibility
- Will be removed in future releases

## Testing

### Test Notification Flow

1. **Join a Project**: Request to join a project and verify manager receives notification
2. **Approve/Reject Request**: Verify user receives approval/rejection notification
3. **Assign Task**: Assign a task to a user and verify notification
4. **Set Due Date**: Set a due date and verify reminder notifications
5. **Complete Task**: Complete a task and verify team members receive notification

### Debug Information

Enable debug logging to troubleshoot issues:

```kotlin
Log.d("FCM v1", "Message sent successfully")
Log.e("FCM v1", "Failed to send message: $error")
```

## Troubleshooting

### Common Issues

1. **"Failed to obtain access token"**
    - Verify service account JSON file is in correct location
    - Check file permissions and format

2. **"FCM message failed: 404"**
    - Verify PROJECT_ID is correct
    - Ensure FCM v1 API is enabled

3. **"Permission denied"**
    - Verify service account has FCM permissions
    - Check Google Cloud Console IAM settings

4. **No notifications received**
    - Check app is in foreground/background
    - Verify notification permissions (Android 13+)
    - Check device FCM token is valid

### Support

For additional support:

1. Check Firebase Console logs
2. Review Android/iOS device logs
3. Verify Google Cloud Console settings
4. Test with Firebase Console test messages

## Resources

- [Firebase FCM v1 API Documentation](https://firebase.google.com/docs/cloud-messaging/migrate-v1)
- [Google OAuth2 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)