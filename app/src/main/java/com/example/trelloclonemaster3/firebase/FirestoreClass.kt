package com.example.trelloclonemaster3.firebase

import android.app.Activity
import android.util.Log
import android.widget to Toast
import com.example.trelloclonemaster3.MainActivity
import com.example.trelloclonemaster3.activities.*
import com.example.trelloclonemaster3.model.Board
import com.example.trelloclonemaster3.model.User
import com.example.trelloclonemaster3.utils.Constants
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.example.trelloclonemaster3.model.JoinableProject // Import JoinableProject

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
        mFireStore.collection(Constants.BOARDS).document().set(boardInfo, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(activity,"Board created successfully",Toast.LENGTH_SHORT).show()
                    activity.boardCreatedSuccessfully()
                }.addOnFailureListener {
                    exception ->
                    activity.hideCustomProgressDialog()
                    Log.e("Update","Something Went Wrong")
                    Toast.makeText(activity,"Something went wrong please try again later",Toast.LENGTH_SHORT).show()
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

    // New method to fetch public boards
    fun getPublicBoardList(activity: JoinProjectActivity) {
        mFireStore.collection(Constants.BOARDS)
            .whereEqualTo("isPublic", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val publicBoardList: ArrayList<JoinableProject> = ArrayList()
                val currentUserID = getCurrentUserID()

                for (document in querySnapshot.documents) {
                    val board = document.toObject(Board::class.java)!!
                    board.documentId = document.id

                    val status = when {
                        board.assignedTo.containsKey(currentUserID) && board.assignedTo[currentUserID] == "Manager" -> "Joined"
                        board.assignedTo.containsKey(currentUserID) && board.assignedTo[currentUserID] == Constants.PENDING -> "Pending"
                        else -> "NotJoined"
                    }
                    publicBoardList.add(
                        JoinableProject(
                            board.name ?: "",
                            board.image ?: "",
                            board.createdBy ?: "",
                            board.assignedTo.keys.toCollection(ArrayList()),
                            board.documentId ?: "",
                            status
                        )
                    )
                }
                activity.populatePublicProjectsList(publicBoardList)
                activity.hideCustomProgressDialog()
            }
            .addOnFailureListener { e ->
                Log.e("PublicBoardList", "Error getting public boards", e)
                activity.hideCustomProgressDialog()
                Toast.makeText(activity, "Error getting public projects", Toast.LENGTH_SHORT).show()
            }
    }

    fun sendJoinRequest(activity: JoinProjectActivity, boardDocumentId: String, currentUserId: String, boardCreatorId: String, requestingUserName: String, boardName: String) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap["${Constants.ASSIGNED_TO}.${currentUserId}"] = Constants.PENDING

        mFireStore.collection(Constants.BOARDS).document(boardDocumentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                Log.e("Join Request", "Join request sent successfully for board: $boardDocumentId")
                activity.joinRequestSentSuccess(boardDocumentId, currentUserId)
                // Now get the board creator's FCM token and send a notification
                getBoardCreatorDetails(activity, boardCreatorId, requestingUserName, boardName)
            }
            .addOnFailureListener { e ->
                Log.e("Join Request", "Error sending join request to board: $boardDocumentId", e)
                activity.hideCustomProgressDialog()
                Toast.makeText(activity, "Failed to send join request. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    fun getBoardCreatorDetails(activity: JoinProjectActivity, creatorId: String, requestingUserName: String, boardName: String) {
        mFireStore.collection(Constants.USERS).document(creatorId).get()
            .addOnSuccessListener {
                document ->
                val user = document.toObject(User::class.java)!!
                activity.sendNotificationToManager(user.fcmToken!!, requestingUserName, boardName)
            }.addOnFailureListener {
                    e ->
                Log.e("Board Creator Details", "Error getting board creator details", e)
                activity.hideCustomProgressDialog()
                Toast.makeText(activity, "Failed to send notification to board manager.", Toast.LENGTH_SHORT).show()
            }
    }

    fun getCurrentUserDetails(activity: BaseActivity) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserID()).get()
            .addOnSuccessListener {
                document ->
                val user = document.toObject(User::class.java)!!
                when (activity) {
                    is JoinProjectActivity -> activity.onGetCurrentUserSuccess(user)
                    is MembersActivity -> activity.onGetCurrentUserSuccess(user)
                }
            }.addOnFailureListener {
                    e ->
                Log.e("Current User Details", "Error getting current user details", e)
                activity.hideCustomProgressDialog()
                Toast.makeText(activity, "Failed to get current user details.", Toast.LENGTH_SHORT).show()
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
                Log.e("User Details for Notification", "Error getting user details for notification", e)
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
}