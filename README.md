# Trello Clone Master

A collaborative project management application inspired by Trello, built with Android and Firebase.

## Features

### Core Functionality

- User authentication (Sign up, Sign in, Password reset)
- User profile management with image upload
- Board/Project creation and management
- Task lists and card management within boards
- Member management for boards
- Real-time collaboration using Firebase Firestore

### New: Project Discovery Feature 🆕

- **Discover Public Projects**: Browse and search through public projects created by other users
- **Search Functionality**: Filter projects by name with real-time search
- **Request to Join**: Send join requests to project managers
- **Status Tracking**: Track your request status (Pending, Approved, Already Member)
- **Public Project Toggle**: Mark your projects as public when creating them

## How to Use the Project Discovery Feature

### For Project Creators:

1. When creating a new board/project, toggle the "Public Project" switch to make it discoverable
2. Public projects will appear in the "Find Projects" section for other users
3. You'll receive notifications when users request to join your public projects
4. Manage join requests through the Members section

### For Users Looking to Join Projects:

1. Open the navigation drawer and select "Find Projects"
2. Browse through available public projects
3. Use the search bar to find specific projects by name
4. Click "Request to Join" for projects you're interested in
5. Your request status will be updated automatically (Pending/Approved)

## Technical Implementation

### New Components Added:

- **FindProjectsActivity**: Main activity for project discovery
- **JoinableProjectsAdapter**: RecyclerView adapter for displaying public projects
- **Enhanced FirestoreClass**: Added methods for fetching public projects and handling join requests
- **Updated UI Components**: New layouts, strings, and navigation items

### Key Files Modified/Added:

- `activities/FindProjectsActivity.kt` - Main discovery activity
- `adapters/JoinableProjectsAdapter.kt` - Adapter for project list
- `firebase/FirestoreClass.kt` - Enhanced with discovery methods
- `layout/activity_find_projects.xml` - Discovery screen layout
- `layout/item_joinable_project.xml` - Individual project item layout
- `values/strings.xml` - Added discovery-related strings
- `menu/activity_main_drawer.xml` - Added "Find Projects" menu item

### Database Structure

The application uses Firebase Firestore with the following key collections:

- `Users` - User profiles and authentication data
- `Boards` - Project/board data with `isPublic` field for discoverability

### Security & Privacy

- Only projects marked as "public" are discoverable
- Join requests use "Pending" status before manager approval
- User data is protected through Firebase security rules

## Setup Instructions

1. Clone the repository
2. Open in Android Studio
3. Set up Firebase project and add `google-services.json`
4. Configure Firebase Authentication and Firestore
5. Build and run the application

## Screenshots

[Add screenshots of the new project discovery feature here]

## Contributing

Feel free to contribute to this project by submitting pull requests or reporting issues.

## License

This project is open source and available under the [MIT License](LICENSE).