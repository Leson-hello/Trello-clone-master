# Firebase Firestore Indexes Setup for Chat Feature

## Overview

This document provides guidance on setting up the necessary Firestore indexes for the chat
functionality to work properly.

## Required Indexes

### 1. ChatRooms Collection Indexes

#### Basic Index (Automatic)

Firebase automatically creates single-field indexes. No action needed for:

- `participants.{userId}` (ascending)
- `isActive` (ascending)
- `lastMessageTime` (descending)

#### Composite Index (If needed in future)

If you want to use complex queries with multiple fields, you may need:

**Collection**: `ChatRooms`
**Fields**:

- `participants.{userId}` (Ascending)
- `isActive` (Ascending)
- `lastMessageTime` (Descending)

### 2. Messages Subcollection Indexes

#### Basic Index (Automatic)

Firebase automatically creates indexes for:

- `timestamp` (ascending/descending)
- `senderId` (ascending)

## Current Implementation Notes

### Query Optimization

The current implementation has been optimized to avoid complex composite indexes:

```kotlin
// Simplified query that avoids composite index requirement
mFireStore.collection(Constants.CHAT_ROOMS)
    .whereEqualTo("participants.$currentUserId", "Member")
    .get()
```

Instead of the complex query that would require an index:

```kotlin
// This would require a composite index
mFireStore.collection(Constants.CHAT_ROOMS)
    .whereEqualTo("participants.$currentUserId", "Member")
    .whereEqualTo("isActive", true)
    .orderBy("lastMessageTime", Query.Direction.DESCENDING)
```

### Local Sorting

We perform sorting locally in the application:

```kotlin
// Sort by last message time locally (most recent first)
chatRoomsList.sortByDescending { it.lastMessageTime }
```

## If You Need to Create Indexes

### Method 1: Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `trello-clone-master-39c85`
3. Go to Firestore Database → Indexes
4. Click "Create Index"
5. Set up the fields as needed

### Method 2: Using Firebase CLI

1. Install Firebase CLI: `npm install -g firebase-tools`
2. Login: `firebase login`
3. Initialize: `firebase init firestore`
4. Edit `firestore.indexes.json`:

```json
{
  "indexes": [
    {
      "collectionGroup": "ChatRooms",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "participants.{userId}",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "isActive",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "lastMessageTime",
          "order": "DESCENDING"
        }
      ]
    }
  ]
}
```

5. Deploy: `firebase deploy --only firestore:indexes`

## Error Resolution

### Index Error Message

If you see an error like:

```
FAILED_PRECONDITION: The query requires an index
```

**Solution**: The current implementation should avoid this error. If it persists:

1. Check that you're using the simplified query
2. Ensure local sorting is implemented
3. Create the suggested index if absolutely necessary

### Performance Considerations

- **Current approach**: Good for small to medium datasets
- **Index approach**: Better for large datasets with frequent queries
- **Recommendation**: Start with current approach, add indexes as needed

## Testing

1. Create a new board (auto-creates chat room)
2. Navigate to Chat from main menu
3. Select the chat room
4. Send messages
5. Verify real-time updates work

The chat feature should work without requiring additional indexes with the current implementation.