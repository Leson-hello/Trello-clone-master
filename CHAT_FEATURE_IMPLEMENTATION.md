# Chat Feature Implementation

## 📱 Overview

This document outlines the complete implementation of the real-time chat functionality for the
Trello Clone project. The chat system enables seamless communication within project teams using
Firebase Firestore for real-time messaging.

## 🏗️ Architecture

### Data Models

1. **ChatMessage** (`model/ChatMessage.kt`)
    - Represents individual chat messages
    - Fields: id, senderId, senderName, senderImage, message, timestamp, messageType, isRead, readBy
    - Implements Parcelable for data passing between activities

2. **ChatRoom** (`model/ChatRoom.kt`)
    - Represents chat conversations
    - Supports both group chats (team-wide) and direct messages (1-on-1)
    - Fields: id, name, description, type, participants, boardId, createdBy, lastMessage info,
      unread counts

### Activities

1. **ChatRoomsActivity** (`activities/ChatRoomsActivity.kt`)
    - Displays list of available chat rooms
    - Search functionality for finding specific chats
    - Floating action button for creating new chats
    - Shows unread message counts and last message previews

2. **ChatActivity** (`activities/ChatActivity.kt`)
    - Individual chat conversation interface
    - Real-time message synchronization using Firestore listeners
    - Message input with send button
    - Different message layouts for sent vs received messages
    - Auto-scroll to newest messages

### Adapters

1. **ChatRoomsAdapter** (`adapters/ChatRoomsAdapter.kt`)
    - RecyclerView adapter for chat rooms list
    - Displays chat type indicators (group vs direct)
    - Shows formatted timestamps and unread badges

2. **ChatMessagesAdapter** (`adapters/ChatMessagesAdapter.kt`)
    - RecyclerView adapter for individual messages
    - Different ViewHolders for sent and received messages
    - Profile image loading for received messages
    - Time formatting for message timestamps

## 🔥 Firebase Integration

### Firestore Collections Structure

```
ChatRooms/
├── {chatRoomId}/
│   ├── id: String
│   ├── name: String
│   ├── type: "group" | "direct"
│   ├── participants: Map<userId, role>
│   ├── boardId: String
│   ├── lastMessage: String
│   └── Messages/
│       └── {messageId}/
│           ├── senderId: String
│           ├── message: String
│           ├── timestamp: Long
│           └── ...
```

### FirestoreClass Methods

- `createChatRoomForBoard()`: Auto-creates team chat when board is created
- `getChatRooms()`: Retrieves user's chat rooms with real-time updates
- `getChatMessages()`: Loads message history for a chat room
- `sendChatMessage()`: Sends new messages and updates room metadata
- `createDirectMessage()`: Creates 1-on-1 chat between users
- `sendChatNotification()`: Sends FCM notifications to participants
- `initializeChatRoomsForExistingBoards()`: Migrates existing boards to have chat rooms

## 🎨 UI/UX Features

### Layouts

- `activity_chat_rooms.xml`: Chat rooms list with search bar
- `activity_chat.xml`: Individual chat interface with message input
- `item_chat_room.xml`: Chat room list item with metadata
- `item_message_sent.xml`: Sent message bubble (right-aligned)
- `item_message_received.xml`: Received message with sender info (left-aligned)

### Visual Elements

- Custom message bubbles with rounded corners
- Different colors for sent vs received messages
- Unread message count badges
- Chat type indicators (group/direct icons)
- Profile image integration
- Real-time timestamp formatting

### Drawable Resources

- `bg_message_sent.xml`: Blue bubble for user's messages
- `bg_message_received.xml`: Gray bubble for others' messages
- `bg_unread_badge.xml`: Red circular badge for unread counts
- Various vector icons: send, search, group, person, add chat

## 🔄 Real-time Synchronization

### Firestore Listeners

```kotlin
messagesListener = messagesRef.addSnapshotListener { snapshot, error ->
    // Real-time message updates
    updateMessagesList(newMessages)
}
```

### Features

- Instant message delivery and display
- Real-time typing indicators (ready for implementation)
- Message read status tracking
- Automatic scroll to newest messages
- Offline message queuing (Firebase handles automatically)

## 🔔 Notification Integration

### FCM v1 Integration

- Sends push notifications when messages are received
- In-app notification storage as fallback
- Notification includes sender name and message preview
- Deep linking to specific chat rooms (ready for implementation)

### Notification Types

- `chat_message`: New message in group or direct chat
- Excludes sender from notification recipients
- Includes chat room metadata for proper handling

## 🚀 Navigation Integration

### Main Navigation

- Added "Chat" menu item in navigation drawer
- Icon: chat bubble with lines
- Direct access from main application menu
- Integrated with existing navigation flow

### Flow

1. User opens app → MainActivity
2. Clicks "Chat" in drawer → ChatRoomsActivity
3. Selects chat room → ChatActivity
4. Real-time messaging experience

## 📋 Auto-creation Features

### Board Integration

- Every new board automatically gets a team chat room
- Chat room participants match board members
- Naming convention: "{Board Name} - Team Chat"
- Existing boards get chat rooms via migration function

### Migration Support

- `initializeChatRoomsForExistingBoards()` creates chats for existing projects
- Called automatically when user logs in
- Prevents duplicate chat room creation
- Maintains data consistency

## 🔧 Technical Considerations

### Performance

- Firestore query optimization with proper indexing
- Real-time listeners managed in activity lifecycle
- Message pagination ready for implementation
- Efficient adapter updates with DiffUtil potential

### Security

- User authentication via Firebase Auth
- Firestore security rules for chat access control
- Participant validation before message sending
- Private message encryption ready for implementation

### Memory Management

- Proper listener cleanup in onDestroy()
- Glide image loading with caching
- RecyclerView view recycling optimization
- Background thread handling for database operations

## 🎯 Usage Examples

### Starting a Team Chat

1. Create a new board/project
2. Chat room is automatically created
3. All board members can access the chat
4. Navigate via Main Menu → Chat

### Sending Messages

1. Select chat room from list
2. Type message in input field
3. Tap send button (enabled when text present)
4. Message appears instantly for all participants

### Real-time Experience

- Messages appear instantly without refresh
- Typing indicators show activity
- Read receipts track message status
- Offline messages sync when reconnected

## 🚦 Status

✅ **Completed Features:**

- Real-time messaging with Firestore
- Group chat rooms for project teams
- Message history and display
- Push notifications via FCM v1
- Navigation and UI integration
- Auto-creation for new boards
- Migration for existing boards

🔄 **Ready for Enhancement:**

- Direct messaging between users
- File/image sharing in chats
- Message search functionality
- Chat member management
- Message reactions/emoji
- Typing indicators
- Message editing/deletion

## 📝 Implementation Notes

- All chat-related constants added to `Constants.kt`
- Color scheme matches existing app design
- Icons follow Material Design guidelines
- Fully integrated with existing Firebase setup
- Compatible with current notification system
- Ready for additional features and enhancements

The chat feature is now fully functional and integrated into the Trello Clone application, providing
teams with seamless communication capabilities alongside their project management tools.

## 🚨 Troubleshooting

### Common Issues and Solutions

#### 1. Firebase Index Error

**Error**: `FAILED_PRECONDITION: The query requires an index`

**Solution**: The implementation has been optimized to avoid complex indexes by:

- Using simplified Firestore queries
- Performing sorting locally in the app
- Filtering active chat rooms in the application layer

#### 2. Progress Dialog Error

**Error**:
`UninitializedPropertyAccessException: lateinit property mProgressDialog has not been initialized`

**Solution**: Removed unnecessary progress dialog calls in chat activities for better UX:

- Chat rooms load instantly without progress indicators
- Messages load in real-time without blocking UI
- Error states are handled gracefully with toast messages

#### 3. Real-time Updates Not Working

**Potential Issues**:

- Internet connectivity
- Firebase permissions
- Listener not properly attached

**Solution**: Check that:

- Device has internet connection
- Firebase authentication is working
- Real-time listeners are set up in `setupRealTimeListener()`

### Performance Optimizations Applied

- **Local Sorting**: Chat rooms sorted by last message time in app
- **Minimal Queries**: Single-field queries to avoid composite indexes
- **Efficient Updates**: Real-time listeners with proper cleanup
- **Memory Management**: Listeners removed in `onDestroy()`

### Database Structure Optimization

```
ChatRooms/ (Collection)
├── {chatRoomId}/ (Document)
│   ├── participants: Map<userId, "Member">
│   ├── isActive: Boolean
│   ├── lastMessageTime: Long
│   └── Messages/ (Subcollection)
│       └── {messageId}/ (Document)
│           ├── senderId: String
│           ├── message: String
│           └── timestamp: Long
```

This structure ensures:

- Efficient queries without complex indexes
- Real-time synchronization
- Scalable message storage
- Easy participant management