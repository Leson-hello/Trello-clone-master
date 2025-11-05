package com.example.trelloclonemaster3.firebase

import android.app.Activity
import android.util.Log

import android.widget.Toast

import com.example.trelloclonemaster3.MainActivity
import com.example.trelloclonemaster3.activities.*
import com.example.trelloclonemaster3.model.Board
import com.example.trelloclonemaster3.model.User
import com.example.trelloclonemaster3.model.NotificationData
import com.example.trelloclonemaster3.model.PushNotification
import com.example.trelloclonemaster3.network.ApiClient
import com.example.trelloclonemaster3.network.ApiInterface
import com.example.trelloclonemaster3.utils.Constants
import com.example.trelloclonemaster3.utils.FCMConstants
import com.example.trelloclonemaster3.firebase.FCMv1ApiService
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.trelloclonemaster3.model.ChatRoom
import com.example.trelloclonemaster3.model.ChatMessage
import com.example.trelloclonemaster3.activities.ChatRoomsActivity
import com.example.trelloclonemaster3.activities.ChatActivity

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    // Trong FirestoreClass.kt
    fun registerUser(activity: SignUpActivity, userInfo: User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                // THÀNH CÔNG: Chuyển sang Activity chính và ẩn tiến trình
                activity.userRegisteredSucess()
            }
            .addOnFailureListener { exception -> // SỬA: Lấy exception để có thông báo lỗi

                // THẤT BẠI: Cần ẩn tiến trình và thông báo cho người dùng
                activity.hideCustomProgressDialog() // <--- KHẮC PHỤC LỖI TREO

                val errorMessage = exception.message ?: "Unknown Firestore registration error"

                Log.e("sign Up", "Firestore error: $errorMessage", exception)

                Toast.makeText(
                    activity,
                    "Unable to sign up. Firestore Error: $errorMessage",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun loadUserData(activity: Activity, readBoardList: Boolean = false){
        mFireStore.collection(Constants.USERS).document(getCurrentUserID()).get()
                .addOnSuccessListener { document ->
                    val loggedUser = document.toObject(User::class.java)

                    if(loggedUser != null)
                    when(activity){
                        is SignInActivity -> {
                            activity.signInSucess(loggedUser)
                        }
                        is MainActivity -> {
                            activity.updateNavigationUserDetail(loggedUser,readBoardList)
                        }
                        is MyProfileActivity -> {
                            activity.setUserDataInUi(loggedUser)
                        }
                    }

                }.addOnFailureListener {
                e -> when(activity){
                        is SignInActivity -> {
                            activity.hideCustomProgressDialog()
                        }
                        is MainActivity -> {
                            activity.hideCustomProgressDialog()
                        }
                    }
                }
    }

    fun getCurrentUserID(): String {

        val currentUser = FirebaseAuth.getInstance().currentUser

        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        Log.e("userId",currentUserID)

        return currentUserID
    }

    fun updateUserProfileData(activity: MyProfileActivity,userHashMap: HashMap<String,Any>){
        mFireStore.collection(Constants.USERS).document(getCurrentUserID()).update(userHashMap).addOnSuccessListener {
            Log.e("user profile","UserProfile Updated successfully")
            Toast.makeText(activity,"Profile Updated Successfully",Toast.LENGTH_SHORT).show()
            activity.profileUpdateSuccess()
        }.addOnFailureListener {
            e ->
            activity.hideCustomProgressDialog()
            Log.e("Update","Something Went Wrong")
            Toast.makeText(activity,"Something went wrong please try again later",Toast.LENGTH_SHORT).show()
        }
    }


    fun createBoard(activity: CreatBoardActivity, boardInfo: Board){
        Log.d(Constants.DEBUG_TAG, "Creating new board...")
        Constants.debugBoardInfo(boardInfo, "Creating")

        val boardsCollection = mFireStore.collection(Constants.BOARDS)
        val newBoardDocRef = boardsCollection.document()

        newBoardDocRef.set(boardInfo, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(Constants.DEBUG_TAG, "Board created successfully: ${boardInfo.name}")

                // Get the board document ID and update the board
                val boardId = newBoardDocRef.id
                val updatedBoard = boardInfo.copy(documentId = boardId)

                // Update the board with its document ID
                boardsCollection.document(boardId)
                    .update("documentId", boardId)
                    .addOnSuccessListener {
                        // Create chat room for the board
                        createChatRoomForBoard(activity, updatedBoard)

                        Toast.makeText(activity, "Board created successfully", Toast.LENGTH_SHORT)
                            .show()
                        activity.boardCreatedSuccessfully()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            Constants.DEBUG_TAG,
                            "Failed to update board ID: ${boardInfo.name}",
                            exception
                        )
                        activity.hideCustomProgressDialog()
                        Toast.makeText(
                            activity,
                            "Something went wrong please try again later",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }.addOnFailureListener { exception ->
                Log.e(Constants.DEBUG_TAG, "Failed to create board: ${boardInfo.name}", exception)
                activity.hideCustomProgressDialog()
                Log.e("Update", "Something Went Wrong")
                Toast.makeText(
                    activity,
                    "Something went wrong please try again later",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun getBoardList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS).whereGreaterThan("assignedTo.${getCurrentUserID()}", "")
                .get().addOnSuccessListener {
                    document ->
                    val boardlist: ArrayList<Board> = ArrayList()
                    for (i in document.documents){
                        val board = i.toObject(Board::class.java)!!
                        board.documentId = i.id
                        boardlist.add(board)
                    }
                    activity.populateBoardListInUI(boardlist)
                    activity.hideCustomProgressDialog()
                }.addOnFailureListener {
                    Log.e("Board List","Getting Board list failed")
                    activity.hideCustomProgressDialog()
                }
    }






    fun getPendingJoinRequestsList(activity: MembersActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS).document(board.documentId!!).get()
            .addOnSuccessListener {
                document ->
                val boardData = document.toObject(Board::class.java)!!
                val pendingUserIds = boardData.assignedTo.filter { it.value == Constants.PENDING }.keys

                if (pendingUserIds.isNotEmpty()) {
                    mFireStore.collection(Constants.USERS).whereIn(Constants.ID, pendingUserIds.toList()).get()
                        .addOnSuccessListener {
                            querySnapshot ->
                            val pendingUsersList: ArrayList<User> = ArrayList()
                            for (i in querySnapshot.documents) {
                                val user = i.toObject(User::class.java)!!
                                pendingUsersList.add(user)
                            }
                            activity.populatePendingRequestsList(pendingUsersList)
                        }.addOnFailureListener {
                                e ->
                            Log.e("Pending Requests", "Error getting pending users", e)
                            activity.hideCustomProgressDialog()
                            Toast.makeText(activity, "Failed to get pending requests.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    activity.populatePendingRequestsList(ArrayList())
                    activity.hideCustomProgressDialog()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Pending Requests", "Error getting board for pending requests", e)
                activity.hideCustomProgressDialog()
                Toast.makeText(activity, "Failed to get board details for pending requests.", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateMemberStatus(activity: MembersActivity, board: Board, user: User, status: String, requestingUserName: String) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap["${Constants.ASSIGNED_TO}.${user.id}"] = status

        mFireStore.collection(Constants.BOARDS).document(board.documentId!!)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                if (status == "Member") {
                    activity.memberApprovedSuccess(user)
                    // Send notification to the user that their request has been approved.
                    getUserDetailsForNotification(activity, user.id!!, board.name!!, requestingUserName, true)
                } else { // Rejected
                    activity.memberRejectedSuccess(user)
                    // Send notification to the user that their request has been rejected.
                    getUserDetailsForNotification(activity, user.id!!, board.name!!, requestingUserName, false)
                }
                Log.e("Member Status", "Member status updated successfully for user: ${user.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Member Status", "Error updating member status for user: ${user.id}", e)
                activity.hideCustomProgressDialog()
                Toast.makeText(activity, "Failed to update member status. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    fun getUserDetailsForNotification(activity: MembersActivity, userId: String, boardName: String, managerName: String, isApproved: Boolean) {
        mFireStore.collection(Constants.USERS).document(userId).get()
            .addOnSuccessListener {
                document ->
                val user = document.toObject(User::class.java)!!
                activity.sendNotificationToUser(user.fcmToken!!, boardName, managerName, isApproved)
            }.addOnFailureListener {
                    e ->
                Log.e(" Notification", "Error getting user details for notification", e)
                activity.hideCustomProgressDialog()
                Toast.makeText(activity, "Failed to send notification to user.", Toast.LENGTH_SHORT).show()
            }
    }


    fun getBoardDetails(activity: TaskListActivity,documentId: String){
        mFireStore.collection(Constants.BOARDS).document(documentId)
            .get().addOnSuccessListener {
                    document ->
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }.addOnFailureListener {
                Log.e("Board List","Getting Board list failed")
                activity.hideCustomProgressDialog()
            }
    }

    fun addUpdateTaskList(activity: Activity,board: Board){
        val taskListHashMap = HashMap<String,Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS).document(board.documentId!!).update(taskListHashMap)
                .addOnSuccessListener {
                    Log.e("TaskList","Updated Task List")

                    if(activity is TaskListActivity)
                        activity.addUpdateTaskLIstSuccess()
                    if(activity is CardDetailsActivity)
                        activity.addUpdateTaskListSuccess()
                }.addOnFailureListener {
                    exception ->
                if(activity is TaskListActivity)
                    activity.hideCustomProgressDialog()
                if(activity is CardDetailsActivity)
                    activity.hideCustomProgressDialog()
                    Log.e("TaskList","Task List Update Failed")
                }
    }

    fun getAssignedMembersList(activity: Activity, assignedTo: Set<String>){
        mFireStore.collection(Constants.USERS).whereIn(Constants.ID, assignedTo.toList()).get()
            .addOnSuccessListener {
                document ->
                val usersList: ArrayList<User> = ArrayList()

                for (i in document){
                    val user = i.toObject(User::class.java)
                    usersList.add(user)
                }

                if(activity is MembersActivity)
                    activity.setUpMembersList(usersList)
                if (activity is TaskListActivity)
                    activity.membersDetailList(usersList)

            }.addOnFailureListener {
                    exception ->
                    if(activity is MembersActivity)
                        activity.hideCustomProgressDialog()
                    if(activity is TaskListActivity)
                        activity.hideCustomProgressDialog()
                Log.e("TaskList","failed to get Members list ")
            }
    }
    fun getCurrentUserDetails(activity: BaseActivity) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserID()).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)!!
                when (activity) {
                    is MembersActivity -> activity.onGetCurrentUserSuccess(user)
                    is ChatActivity -> activity.setCurrentUser(user)
                }
            }.addOnFailureListener { e ->
                Log.e("Current User Details", "Error getting current user details", e)
                activity.hideCustomProgressDialog()
                Toast.makeText(activity, "Failed to get current user details.", Toast.LENGTH_SHORT).show()
            }
    }

    fun getMembersDetails(activity: MembersActivity,email: String){
        mFireStore.collection(Constants.USERS).whereEqualTo(Constants.EMAIL,email).get()
                .addOnSuccessListener {
                    document ->
                    if(document.documents.size > 0){
                        val user = document.documents[0].toObject(User::class.java)!!
                        activity.membersDetail(user)
                    }else{
                        activity.hideCustomProgressDialog()
                        activity.showErrorSnackBar("User with this email not found")
                    }
                }.addOnFailureListener {
                    Toast.makeText(activity,"Something went wrong please try again later",Toast.LENGTH_SHORT).show()
                }
    }



    fun assignMemberToBoard(activity: MembersActivity,board: Board,user: User){

        val assignedToHashMap = HashMap<String,Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS).document(board.documentId!!).update(assignedToHashMap)
                .addOnSuccessListener {
                    activity.memberAssignedSuccess(user)
                }.addOnFailureListener {
                    activity.hideCustomProgressDialog()
                    Toast.makeText(activity,"Something went wrong please try again later",Toast.LENGTH_SHORT).show()
                }
    }

    fun getPublicProjects(activity: FindProjectsActivity) {
        Log.d("Public Projects", "Starting to fetch public projects...")

        mFireStore.collection(Constants.BOARDS)
            .whereEqualTo("isPublic", true)
            .get()
            .addOnSuccessListener { document ->
                Log.d(
                    "Public Projects",
                    "Query successful, found ${document.documents.size} documents"
                )

                val projectsList: ArrayList<Board> = ArrayList()
                for (i in document.documents) {
                    try {
                        val board = i.toObject(Board::class.java)!!
                        board.documentId = i.id
                        projectsList.add(board)
                        Log.d("Public Projects", "Added board: ${board.name}")
                    } catch (e: Exception) {
                        Log.e("Public Projects", "Error parsing board document: ${i.id}", e)
                    }
                }

                Log.d("Public Projects", "Successfully loaded ${projectsList.size} public projects")
                activity.populatePublicProjectsList(projectsList)
            }
            .addOnFailureListener { e ->
                Log.e("Public Projects", "Error getting public projects", e)

                // Đảm bảo ẩn progress bar
                activity.hideCustomProgressDialog()

                // Hiển thị thông báo lỗi cho user
                Toast.makeText(
                    activity,
                    "Failed to load public projects: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                // Vẫn gọi populate với list trống để cập nhật UI
                activity.populatePublicProjectsList(ArrayList())
            }
    }

    fun getAssignedTasks(activity: MyTasksActivity) {
        val currentUserId = getCurrentUserID()
        Log.d("Assigned Tasks", "Fetching tasks for user: $currentUserId")

        // Get all boards where the current user is assigned
        mFireStore.collection(Constants.BOARDS)
            .whereGreaterThan("assignedTo.$currentUserId", "")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("Assigned Tasks", "Found ${documents.size()} boards")

                val assignedTasks = ArrayList<com.example.trelloclonemaster3.model.AssignedTask>()

                for (document in documents) {
                    try {
                        val board = document.toObject(Board::class.java)!!
                        board.documentId = document.id

                        // Extract tasks from this board where current user is assigned
                        for (taskList in board.taskList) {
                            for (card in taskList.cards) {
                                // Check if current user is assigned to this card
                                if (card.assignedTo.contains(currentUserId)) {
                                    val assignedTask =
                                        com.example.trelloclonemaster3.model.AssignedTask(
                                            taskId = generateTaskId(
                                                board.documentId!!,
                                                taskList.title!!,
                                                card.name
                                            ),
                                            taskName = card.name,
                                            projectName = board.name ?: "Unknown Project",
                                            projectId = board.documentId!!,
                                            taskListName = taskList.title ?: "Unknown List",
                                            status = card.status,
                                            dueDate = card.dueDate,
                                            labelColor = card.labelColor,
                                            assignedMembers = card.assignedTo,
                                            createdBy = card.createdBy
                                        )
                                    assignedTasks.add(assignedTask)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Assigned Tasks", "Error parsing board: ${document.id}", e)
                    }
                }

                Log.d("Assigned Tasks", "Found ${assignedTasks.size} assigned tasks")
                activity.populateTasksList(assignedTasks)
            }
            .addOnFailureListener { e ->
                Log.e("Assigned Tasks", "Error fetching assigned tasks", e)
                activity.onTasksLoadFailed()
            }
    }

    private fun generateTaskId(boardId: String, taskListTitle: String, cardName: String): String {
        return "${boardId}_${taskListTitle.hashCode()}_${cardName.hashCode()}"
    }

    fun requestToJoinProject(activity: FindProjectsActivity, board: Board, position: Int) {
        val currentUserId = getCurrentUserID()

        Log.d("JOIN_REQUEST_DEBUG", "=== JOIN PROJECT REQUEST DEBUG START ===")
        Log.d("JOIN_REQUEST_DEBUG", "Current User ID: $currentUserId")
        Log.d("JOIN_REQUEST_DEBUG", "Board Name: ${board.name}")
        Log.d("JOIN_REQUEST_DEBUG", "Board ID: ${board.documentId}")
        Log.d("JOIN_REQUEST_DEBUG", "Board Created By: ${board.createdBy}")

        // Add current user to the board's assignedTo with "Pending" status
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap["${Constants.ASSIGNED_TO}.$currentUserId"] = Constants.PENDING

        Log.d("JOIN_REQUEST_DEBUG", "Updating board with pending status...")
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId!!)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                Log.d(
                    "JOIN_REQUEST_DEBUG",
                    "✅ Join request sent successfully for user: $currentUserId to board: ${board.name}"
                )
                activity.onJoinRequestSuccess(position)

                // FIXED: Send notification to project manager about join request
                Log.d("JOIN_REQUEST_DEBUG", "📧 Sending notification to project manager...")
                sendJoinRequestNotificationToManager(
                    activity,
                    board.createdBy!!,
                    board.name!!,
                    currentUserId,
                    board.documentId!!
                )
            }
            .addOnFailureListener { e ->
                Log.e("JOIN_REQUEST_DEBUG", "❌ Error sending join request", e)
                Log.e("JOIN_REQUEST_DEBUG", "=== JOIN PROJECT REQUEST DEBUG END (ERROR) ===")
                activity.onJoinRequestFailure()
            }
    }

    // NEW: Send notification to project manager about join request
    private fun sendJoinRequestNotificationToManager(
        activity: FindProjectsActivity,
        managerId: String,
        boardName: String,
        requestingUserId: String,
        boardId: String
    ) {
        Log.d("JOIN_REQUEST_DEBUG", "=== SENDING NOTIFICATION TO MANAGER DEBUG START ===")
        Log.d("JOIN_REQUEST_DEBUG", "Manager ID: $managerId")
        Log.d("JOIN_REQUEST_DEBUG", "Requesting User ID: $requestingUserId")

        // Get requesting user details first
        Log.d("JOIN_REQUEST_DEBUG", "Getting requesting user details...")
        mFireStore.collection(Constants.USERS).document(requestingUserId).get()
            .addOnSuccessListener { requestingUserDoc ->
                Log.d("JOIN_REQUEST_DEBUG", "✅ Successfully got requesting user document")

                val requestingUser = requestingUserDoc.toObject(User::class.java)!!
                val requestingUserName = requestingUser.name ?: "Unknown User"

                Log.d("JOIN_REQUEST_DEBUG", "Requesting User Name: $requestingUserName")

                // Use new FCM v1 API method
                Log.d("JOIN_REQUEST_DEBUG", "Calling sendJoinRequestNotificationV1...")
                sendJoinRequestNotificationV1(
                    activity,
                    managerId,
                    boardName,
                    requestingUserName,
                    boardId
                )
                Log.d("JOIN_REQUEST_DEBUG", "=== SENDING NOTIFICATION TO MANAGER DEBUG END ===")
            }
            .addOnFailureListener { e ->
                Log.e("JOIN_REQUEST_DEBUG", "❌ Error getting requesting user details", e)
                Log.e(
                    "JOIN_REQUEST_DEBUG",
                    "=== SENDING NOTIFICATION TO MANAGER DEBUG END (ERROR) ==="
                )
            }
    }

    // NEW: Store in-app notification in Firestore
    private fun storeInAppNotification(
        userId: String,
        title: String,
        message: String,
        relatedId: String? = null,
        notificationType: String = "general"
    ) {
        val notification = hashMapOf(
            "title" to title,
            "message" to message,
            "userId" to userId,
            "relatedId" to relatedId,
            "type" to notificationType,
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false
        )

        mFireStore.collection("notifications")
            .add(notification)
            .addOnSuccessListener { documentReference ->
                Log.d("Notification", "In-app notification stored with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Notification", "Failed to store in-app notification", e)
            }
    }

    // NEW: Get notifications for a user (to be used in MainActivity or notification activity)
    fun getUserNotifications(userId: String, callback: (ArrayList<HashMap<String, Any>>) -> Unit) {
        mFireStore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val notifications = ArrayList<HashMap<String, Any>>()
                for (document in documents) {
                    val notification = document.data.toMutableMap()
                    notification["id"] = document.id
                    notifications.add(HashMap(notification))
                }
                callback(notifications)
            }
            .addOnFailureListener { e ->
                Log.e("Notifications", "Error getting user notifications", e)
                callback(ArrayList())
            }
    }

    // NEW: Mark notification as read
    fun markNotificationAsRead(notificationId: String) {
        mFireStore.collection("notifications")
            .document(notificationId)
            .update("isRead", true)
            .addOnSuccessListener {
                Log.d("Notification", "Notification marked as read: $notificationId")
            }
            .addOnFailureListener { e ->
                Log.e("Notification", "Failed to mark notification as read", e)
            }
    }

    // NEW: Update user FCM token
    fun updateUserFCMToken(userId: String, fcmToken: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap["fcmToken"] = fcmToken

        mFireStore.collection(Constants.USERS).document(userId).update(userHashMap)
            .addOnSuccessListener {
                Log.d("FCM Token", "FCM token updated successfully for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("FCM Token", "Failed to update FCM token for user: $userId", e)
            }
    }

    // NEW: Send notification using FCM v1 API for join request acceptance/rejection
    fun sendJoinResponseNotificationV1(
        activity: MembersActivity,
        userId: String,
        boardName: String,
        managerName: String,
        isApproved: Boolean
    ) {
        var title = ""
        var message = ""
        var notificationType = ""
        if (isApproved) {
            title = "Join Request Approved"
            message = "Congratulations! $managerName approved your request to join '$boardName'"
            notificationType = "join_accepted"
        } else {
            title = "Join Request Rejected"
            message = "$managerName rejected your request to join '$boardName'"
            notificationType = "join_rejected"
        }

        mFireStore.collection(Constants.USERS).document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)!!

                if (user.fcmToken?.isNotEmpty() == true) {
                    kotlinx.coroutines.GlobalScope.launch {
                        val fcmService = FCMv1ApiService(activity)
                        val success = fcmService.sendDataMessage(
                            user.fcmToken!!,
                            title,
                            message,
                            mapOf(
                                "boardName" to boardName,
                                "managerName" to managerName
                            ),
                            notificationType
                        )

                        if (success) {
                            Log.d("FCM v1", "Join response notification sent successfully")
                        } else {
                            Log.e("FCM v1", "Failed to send join response notification")
                        }
                    }
                }

                // Always store in-app notification as fallback
                storeInAppNotification(userId, title, message, null, notificationType)
            }
            .addOnFailureListener { e ->
                Log.e("Join Response", "Error getting user details for notification", e)
            }
    }

    // NEW: Send task assignment notification using FCM v1
    fun sendTaskAssignmentNotification(
        context: android.content.Context,
        assignedUserIds: List<String>,
        taskName: String,
        boardName: String,
        assignedBy: String
    ) {
        val title = "New Task Assigned"
        val message = "$assignedBy assigned you a task: '$taskName' in project '$boardName'"
        val data = mapOf(
            "taskName" to taskName,
            "boardName" to boardName,
            "assignedBy" to assignedBy
        )

        for (userId in assignedUserIds) {
            mFireStore.collection(Constants.USERS).document(userId).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)!!

                    if (user.fcmToken?.isNotEmpty() == true) {
                        kotlinx.coroutines.GlobalScope.launch {
                            val fcmService = FCMv1ApiService(context)
                            fcmService.sendDataMessage(
                                user.fcmToken!!,
                                title,
                                message,
                                data,
                                "task_assigned"
                            )
                        }
                    }

                    // Store in-app notification
                    storeInAppNotification(userId, title, message, null, "task_assigned")
                }
        }
    }

    // NEW: Send due date reminder notification
    fun sendDueDateReminderNotification(
        context: android.content.Context,
        assignedUserIds: List<String>,
        taskName: String,
        boardName: String,
        dueDate: Long
    ) {
        val currentTime = System.currentTimeMillis()
        val isOverdue = dueDate < currentTime

        val title = if (isOverdue) "Task Overdue" else "Task Due Soon"
        val message = if (isOverdue) {
            "Task '$taskName' in '$boardName' is now overdue!"
        } else {
            "Task '$taskName' in '$boardName' is due soon"
        }

        val data = mapOf(
            "taskName" to taskName,
            "boardName" to boardName,
            "dueDate" to dueDate.toString(),
            "isOverdue" to isOverdue.toString()
        )

        for (userId in assignedUserIds) {
            mFireStore.collection(Constants.USERS).document(userId).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)!!

                    if (user.fcmToken?.isNotEmpty() == true) {
                        kotlinx.coroutines.GlobalScope.launch {
                            val fcmService = FCMv1ApiService(context)
                            fcmService.sendDataMessage(
                                user.fcmToken!!,
                                title,
                                message,
                                data,
                                "task_due"
                            )
                        }
                    }

                    // Store in-app notification
                    storeInAppNotification(userId, title, message, null, "task_due")
                }
        }
    }

    // NEW: Send task completion notification
    fun sendTaskCompletionNotification(
        context: android.content.Context,
        boardMemberIds: List<String>,
        taskName: String,
        boardName: String,
        completedBy: String
    ) {
        val title = "Task Completed"
        val message = "$completedBy completed task '$taskName' in project '$boardName'"
        val data = mapOf(
            "taskName" to taskName,
            "boardName" to boardName,
            "completedBy" to completedBy
        )

        for (userId in boardMemberIds) {
            // Don't send notification to the person who completed the task
            if (userId != getCurrentUserID()) {
                mFireStore.collection(Constants.USERS).document(userId).get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject(User::class.java)!!

                        if (user.fcmToken?.isNotEmpty() == true) {
                            kotlinx.coroutines.GlobalScope.launch {
                                val fcmService = FCMv1ApiService(context)
                                fcmService.sendDataMessage(
                                    user.fcmToken!!,
                                    title,
                                    message,
                                    data,
                                    "task_completed"
                                )
                            }
                        }

                        // Store in-app notification
                        storeInAppNotification(userId, title, message, null, "task_completed")
                    }
            }
        }
    }

    // NEW: Enhanced join request notification using FCM v1
    fun sendJoinRequestNotificationV1(
        context: android.content.Context,
        managerId: String,
        boardName: String,
        requestingUserName: String,
        boardId: String
    ) {
        Log.d("FCM_DEBUG", "=== JOIN REQUEST NOTIFICATION DEBUG START ===")
        Log.d("FCM_DEBUG", "Manager ID: $managerId")
        Log.d("FCM_DEBUG", "Board Name: $boardName")
        Log.d("FCM_DEBUG", "Requesting User: $requestingUserName")
        Log.d("FCM_DEBUG", "Board ID: $boardId")

        mFireStore.collection(Constants.USERS).document(managerId).get()
            .addOnSuccessListener { document ->
                Log.d("FCM_DEBUG", "✅ Successfully got manager document")

                val manager = document.toObject(User::class.java)!!
                Log.d("FCM_DEBUG", "Manager name: ${manager.name}")
                Log.d("FCM_DEBUG", "Manager FCM token: ${manager.fcmToken?.take(20) ?: "NULL"}...")

                val title = "New Join Request"
                val message = "$requestingUserName wants to join your project '$boardName'"
                val data = mapOf(
                    "boardId" to boardId,
                    "boardName" to boardName,
                    "requestingUserName" to requestingUserName
                )

                Log.d("FCM_DEBUG", "Notification title: $title")
                Log.d("FCM_DEBUG", "Notification message: $message")
                Log.d("FCM_DEBUG", "Notification data: $data")

                if (manager.fcmToken?.isNotEmpty() == true) {
                    Log.d("FCM_DEBUG", "✅ Manager has FCM token, sending notification...")

                    kotlinx.coroutines.GlobalScope.launch {
                        try {
                            val fcmService = FCMv1ApiService(context)
                            val success = fcmService.sendDataMessage(
                                manager.fcmToken!!,
                                title,
                                message,
                                data,
                                "join_request"
                            )

                            if (success) {
                                Log.d(
                                    "FCM_DEBUG",
                                    "✅ Join request notification sent successfully via FCM"
                                )
                            } else {
                                Log.e(
                                    "FCM_DEBUG",
                                    "❌ Failed to send join request notification via FCM"
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("FCM_DEBUG", "❌ Exception while sending FCM notification", e)
                        }
                    }
                } else {
                    Log.w("FCM_DEBUG", "⚠️ Manager FCM token is empty or null")
                }

                // Always store in-app notification
                Log.d("FCM_DEBUG", "📱 Storing in-app notification...")
                storeInAppNotification(managerId, title, message, boardId, "join_request")
                Log.d("FCM_DEBUG", "=== JOIN REQUEST NOTIFICATION DEBUG END ===")
            }
            .addOnFailureListener { e ->
                Log.e("FCM_DEBUG", "❌ Error getting manager details", e)
                Log.e("FCM_DEBUG", "=== JOIN REQUEST NOTIFICATION DEBUG END (ERROR) ===")
            }
    }


    // ====================== CHAT FUNCTIONALITY METHODS ======================

    /**
     * Initialize chat rooms for existing boards that don't have chat rooms
     */
    fun initializeChatRoomsForExistingBoards(activity: Activity) {
        val currentUserId = getCurrentUserID()

        // Get all boards for current user
        mFireStore.collection(Constants.BOARDS)
            .whereGreaterThan("assignedTo.$currentUserId", "")
            .get()
            .addOnSuccessListener { boardDocuments ->
                for (boardDoc in boardDocuments) {
                    val board = boardDoc.toObject(Board::class.java)
                    board.documentId = boardDoc.id

                    // Check if chat room already exists for this board
                    mFireStore.collection(Constants.CHAT_ROOMS)
                        .whereEqualTo("boardId", board.documentId)
                        .get()
                        .addOnSuccessListener { chatRoomDocs ->
                            if (chatRoomDocs.isEmpty) {
                                // No chat room exists, create one
                                createChatRoomForBoard(activity, board)
                                Log.d("ChatInit", "Created chat room for board: ${board.name}")
                            } else {
                                Log.d(
                                    "ChatInit",
                                    "Chat room already exists for board: ${board.name}"
                                )
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                "ChatInit",
                                "Error checking chat room for board: ${board.name}",
                                e
                            )
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatInit", "Error getting boards for chat room initialization", e)
            }
    }

    /**
     * Create a new chat room for a board/project
     */
    fun createChatRoomForBoard(activity: Activity, board: Board) {
        val chatRoom = ChatRoom(
            name = "${board.name} - Team Chat",
            description = "Team chat for ${board.name}",
            type = "group",
            participants = board.assignedTo, // Use the same participants as the board
            boardId = board.documentId!!,
            createdBy = getCurrentUserID(),
            createdAt = System.currentTimeMillis()
        )

        mFireStore.collection(Constants.CHAT_ROOMS)
            .add(chatRoom)
            .addOnSuccessListener { documentReference ->
                Log.d("ChatRoom", "Chat room created successfully: ${documentReference.id}")
                // Update the chat room with its own ID
                val chatRoomId = documentReference.id
                mFireStore.collection(Constants.CHAT_ROOMS)
                    .document(chatRoomId)
                    .update("id", chatRoomId)
                    .addOnSuccessListener {
                        Log.d("ChatRoom", "Chat room ID updated successfully")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ChatRoom", "Error creating chat room", e)
            }
    }

    /**
     * Get all chat rooms for the current user
     */
    fun getChatRooms(activity: ChatRoomsActivity) {
        val currentUserId = getCurrentUserID()

        mFireStore.collection(Constants.CHAT_ROOMS)
            .whereEqualTo("participants.$currentUserId", "Member")
            .get()
            .addOnSuccessListener { documents ->
                val chatRoomsList = ArrayList<ChatRoom>()
                for (document in documents) {
                    try {
                        val chatRoom = document.toObject(ChatRoom::class.java)
                        chatRoom.copy(id = document.id).also { updatedChatRoom ->
                            // Only add active chat rooms
                            if (updatedChatRoom.isActive) {
                                chatRoomsList.add(updatedChatRoom)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ChatRooms", "Error parsing chat room: ${document.id}", e)
                    }
                }

                // Sort by last message time locally (most recent first)
                chatRoomsList.sortByDescending { it.lastMessageTime }

                activity.populateChatRoomsList(chatRoomsList)
            }
            .addOnFailureListener { e ->
                Log.e("ChatRooms", "Error getting chat rooms", e)
                activity.onChatRoomsLoadFailed()
            }
    }

    /**
     * Get messages for a specific chat room
     */
    fun getChatMessages(activity: ChatActivity, chatRoomId: String) {
        mFireStore.collection(Constants.CHAT_ROOMS)
            .document(chatRoomId)
            .collection(Constants.MESSAGES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val messagesList = ArrayList<ChatMessage>()
                for (document in documents) {
                    try {
                        val message = document.toObject(ChatMessage::class.java)
                        messagesList.add(message.copy(id = document.id))
                    } catch (e: Exception) {
                        Log.e("ChatMessages", "Error parsing message: ${document.id}", e)
                    }
                }
                activity.populateMessagesList(messagesList)
            }
            .addOnFailureListener { e ->
                Log.e("ChatMessages", "Error getting messages", e)
                activity.onMessagesLoadFailed()
            }
    }

    /**
     * Send a new chat message
     */
    fun sendChatMessage(activity: ChatActivity, chatRoomId: String, message: ChatMessage) {
        // Add message to messages subcollection
        mFireStore.collection(Constants.CHAT_ROOMS)
            .document(chatRoomId)
            .collection(Constants.MESSAGES)
            .add(message)
            .addOnSuccessListener { documentReference ->
                Log.d("ChatMessage", "Message sent successfully: ${documentReference.id}")

                // Update chat room's last message info
                updateChatRoomLastMessage(chatRoomId, message)

                // Get chat room details to send notifications
                mFireStore.collection(Constants.CHAT_ROOMS)
                    .document(chatRoomId)
                    .get()
                    .addOnSuccessListener { chatRoomDoc ->
                        val chatRoom = chatRoomDoc.toObject(ChatRoom::class.java)
                        if (chatRoom != null) {
                            // Send notifications to all participants except sender
                            sendChatNotification(
                                activity,
                                chatRoom.copy(id = chatRoomId),
                                message,
                                message.senderId
                            )
                        }
                    }

                activity.onMessageSent()
            }
            .addOnFailureListener { e ->
                Log.e("ChatMessage", "Error sending message", e)
                activity.onMessageSendFailed()
            }
    }

    /**
     * Update chat room's last message information
     */
    private fun updateChatRoomLastMessage(chatRoomId: String, message: ChatMessage) {
        val updates = hashMapOf<String, Any>(
            "lastMessage" to message.message,
            "lastMessageTime" to message.timestamp as Any,
            "lastMessageSender" to message.senderName as Any
        )

        mFireStore.collection(Constants.CHAT_ROOMS)
            .document(chatRoomId)
            .update(updates)
            .addOnSuccessListener {
                Log.d("ChatRoom", "Last message updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ChatRoom", "Error updating last message", e)
            }
    }

    /**
     * Mark messages as read for a user
     */
    fun markMessagesAsRead(chatRoomId: String, userId: String) {
        val currentTime = System.currentTimeMillis()

        // Update unread count in chat room
        mFireStore.collection(Constants.CHAT_ROOMS)
            .document(chatRoomId)
            .update("unreadCount.$userId", 0)
            .addOnSuccessListener {
                Log.d("ChatRoom", "Messages marked as read for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("ChatRoom", "Error marking messages as read", e)
            }
    }

    /**
     * Create a direct message chat room between two users
     */
    fun createDirectMessage(
        activity: Activity,
        otherUser: User,
        currentUser: User,
        callback: (String?) -> Unit
    ) {
        // Check if direct message already exists
        val participants = hashMapOf(
            currentUser.id!! to "Member",
            otherUser.id!! to "Member"
        )

        mFireStore.collection(Constants.CHAT_ROOMS)
            .whereEqualTo("type", "direct")
            .whereEqualTo("participants", participants)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Create new direct message chat room
                    val chatRoom = ChatRoom(
                        name = "${currentUser.name} & ${otherUser.name}",
                        description = "Direct message between ${currentUser.name} and ${otherUser.name}",
                        type = "direct",
                        participants = participants,
                        boardId = "", // No associated board for direct messages
                        createdBy = currentUser.id!!,
                        createdAt = System.currentTimeMillis()
                    )

                    mFireStore.collection(Constants.CHAT_ROOMS)
                        .add(chatRoom)
                        .addOnSuccessListener { documentReference ->
                            val chatRoomId = documentReference.id
                            mFireStore.collection(Constants.CHAT_ROOMS)
                                .document(chatRoomId)
                                .update("id", chatRoomId)
                                .addOnSuccessListener {
                                    callback(chatRoomId)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("DirectMessage", "Error creating direct message", e)
                            callback(null)
                        }
                } else {
                    // Return existing chat room ID
                    callback(documents.documents[0].id)
                }
            }
            .addOnFailureListener { e ->
                Log.e("DirectMessage", "Error checking for existing direct message", e)
                callback(null)
            }
    }

    /**
     * Send chat notification to participants
     */
    fun sendChatNotification(
        context: android.content.Context,
        chatRoom: ChatRoom,
        message: ChatMessage,
        excludeUserId: String // Don't send notification to the sender
    ) {
        val title = "New message in ${chatRoom.name}"
        val notificationMessage = "${message.senderName}: ${message.message}"
        val data = mapOf(
            "chatRoomId" to chatRoom.id,
            "chatRoomName" to chatRoom.name,
            "senderName" to message.senderName,
            "messageContent" to message.message
        )

        // Send notification to all participants except the sender
        for ((userId, _) in chatRoom.participants) {
            if (userId != excludeUserId) {
                mFireStore.collection(Constants.USERS).document(userId).get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject(User::class.java)!!

                        if (user.fcmToken?.isNotEmpty() == true) {
                            kotlinx.coroutines.GlobalScope.launch {
                                val fcmService = FCMv1ApiService(context)
                                fcmService.sendDataMessage(
                                    user.fcmToken!!,
                                    title,
                                    notificationMessage,
                                    data,
                                    "chat_message"
                                )
                            }
                        }

                        // Store in-app notification
                        storeInAppNotification(
                            userId,
                            title,
                            notificationMessage,
                            chatRoom.id,
                            "chat_message"
                        )
                    }
            }
        }
    }

}