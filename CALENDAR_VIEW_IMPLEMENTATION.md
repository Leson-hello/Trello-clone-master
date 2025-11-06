# Calendar View Implementation 📅

This document describes the implementation of the Calendar View feature in the Trello Clone Master
application.

## Overview

The Calendar View provides users with a time-centric overview of their tasks and responsibilities,
making it easy to see deadlines and plan work schedules.

## Features Implemented

### Core Components

1. **CalendarActivity** - Main activity that displays the calendar interface
2. **CalendarTasksAdapter** - RecyclerView adapter for displaying tasks on selected dates
3. **FirestoreClass Integration** - Method to fetch tasks with due dates within date ranges
4. **Navigation Integration** - Added to main drawer menu

### UI Components

- **Standard Android CalendarView** - Full calendar grid for date selection
- **Selected Date Display** - Shows the currently selected date in a readable format
- **RecyclerView** - Lists tasks due on the selected date
- **Task Cards** - Individual task items showing:
    - Task name
    - Due time
    - Status (Pending, In Progress, Completed)
    - Assigned member count
    - Color indicator (if task has label color)
    - Overdue indicator (red text for overdue tasks)

### Core Logic

1. **Date Selection**: When user taps on a calendar date, the app filters and displays tasks for
   that specific day
2. **Month Navigation**: Calendar automatically loads tasks for the visible month
3. **Task Filtering**: Only shows tasks that:
    - Have a due date set (dueDate > 0)
    - Are assigned to the current user
    - Fall within the selected date range
4. **Real-time Updates**: Tasks are loaded from Firestore with proper error handling

## File Structure

```
app/src/main/java/com/example/trelloclonemaster3/
├── activities/
│   └── CalendarActivity.kt                    # Main calendar activity
├── adapters/
│   └── CalendarTasksAdapter.kt               # Task list adapter
└── firebase/
    └── FirestoreClass.kt                     # Added getTasksWithDueDatesInRange()

app/src/main/res/
├── layout/
│   ├── activity_calendar_simple.xml         # Calendar activity layout
│   └── item_calendar_task.xml              # Individual task item layout
├── menu/
│   └── activity_main_drawer.xml            # Added calendar menu item
├── values/
│   ├── strings.xml                         # Calendar-related strings
│   └── dimens.xml                          # Calendar-related dimensions
└── AndroidManifest.xml                     # Added CalendarActivity declaration
```

## Usage

### Accessing Calendar View

1. Open the navigation drawer in MainActivity
2. Tap on "Calendar" menu item
3. The CalendarActivity will open showing the current month

### Using Calendar View

1. **Date Selection**: Tap any date on the calendar to view tasks for that day
2. **Task Viewing**: Tasks for the selected date appear below the calendar
3. **Task Details**: Each task shows:
    - Name and due time
    - Current status with color coding
    - Number of assigned members
    - Color indicator based on task label
    - Red highlighting for overdue tasks

### Navigation

- **Back Button**: Returns to MainActivity
- **Calendar Navigation**: Use standard calendar controls to navigate months

## Technical Details

### Data Flow

1. `CalendarActivity.onCreate()` → Initialize UI and load current month
2. `loadTasksForMonth()` → Calculate month date range
3. `FirestoreClass.getTasksWithDueDatesInRange()` → Query Firestore
4. `populateTasksForMonth()` → Update UI with fetched tasks
5. User selects date → `loadTasksForSelectedDate()` filters tasks
6. `updateTasksList()` → Update RecyclerView with filtered tasks

### Firestore Query

```kotlin
mFireStore.collection(Constants.BOARDS)
    .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
    .get()
```

The query fetches all boards where the current user is assigned, then filters tasks with due dates
in the specified range.

### Error Handling

- Network connectivity issues
- Firestore query failures
- Empty result sets
- Invalid date ranges

## Future Enhancements

1. **Visual Indicators**: Add dots or badges to calendar dates that have tasks
2. **Task Creation**: Allow creating tasks directly from calendar
3. **Task Editing**: Navigate to CardDetailsActivity when task is tapped
4. **Month/Week Views**: Add different calendar view modes
5. **Reminders**: Integration with notification system
6. **Search/Filter**: Add filtering by task status, assigned members, etc.

## Dependencies

- Standard Android CalendarView (no external libraries required)
- Firebase Firestore for data storage
- RecyclerView for task lists
- Material Design components for UI

## Testing

To test the Calendar View functionality:

1. Create some tasks with due dates in your Trello boards
2. Assign these tasks to your user account
3. Open the Calendar View from the navigation drawer
4. Select different dates to see tasks filtered by date
5. Verify that overdue tasks appear in red
6. Check that task status colors are displayed correctly

## Limitations

- Uses standard Android CalendarView (basic styling)
- No visual indicators on calendar dates with tasks
- Tasks must be pre-existing with due dates
- Limited to current user's assigned tasks only

This implementation provides a solid foundation for the Calendar View feature with room for future
enhancements.