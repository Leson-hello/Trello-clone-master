package com.example.trelloclonemaster3.activities

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.adapters.MembersListItemAdapter
import com.example.trelloclonemaster3.adapters.PendingRequestsAdapter
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.model.Board
import com.example.trelloclonemaster3.model.User
import com.example.trelloclonemaster3.utils.Constants
import com.example.trelloclonemaster3.model.NotificationData
import com.example.trelloclonemaster3.model.PushNotification
import com.example.trelloclonemaster3.network.ApiClient
import com.example.trelloclonemaster3.network.ApiInterface
import com.example.trelloclonemaster3.utils.FCMConstants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

class MembersActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private lateinit var mAssignedMembers: ArrayList<User>
    private lateinit var tvPendingRequestsTitle: TextView
    private lateinit var rvPendingRequestsList: RecyclerView
    private lateinit var mPendingRequestsAdapter: PendingRequestsAdapter
    private var mPendingMembersList: ArrayList<User> = ArrayList()
    private lateinit var mCurrentUserName: String // Added to store current user's name

    private var anyChangesMade: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        setupActionBar()

        tvPendingRequestsTitle = findViewById(R.id.tv_pending_requests_title)
        rvPendingRequestsList = findViewById(R.id.rv_pending_requests_list)

        if (intent.hasExtra(Constants.BOARD_DETAILS)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAILS)!!
        }

        showCustomProgressBar()
        FirestoreClass().getAssignedMembersList(this, mBoardDetails.assignedTo.keys)
        FirestoreClass().getPendingJoinRequestsList(this, mBoardDetails)
        FirestoreClass().getCurrentUserDetails(this) // Fetch current user details
    }

    private fun setupActionBar(){
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_members_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "Members"
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUpMembersList(list: ArrayList<User>){
        mAssignedMembers = list.filter { mBoardDetails.assignedTo[it.id] != Constants.PENDING } as ArrayList<User>

        hideCustomProgressDialog()

        val rvMembersList = findViewById<RecyclerView>(R.id.rv_members_list)
        rvMembersList.layoutManager = LinearLayoutManager(this)
        rvMembersList.setHasFixedSize(true)

        val adapter = MembersListItemAdapter(this, mAssignedMembers, mBoardDetails)
        rvMembersList.adapter = adapter
    }

    fun populatePendingRequestsList(list: ArrayList<User>) {
        mPendingMembersList = list
        hideCustomProgressDialog()

        if (mPendingMembersList.size > 0) {
            tvPendingRequestsTitle.visibility = View.VISIBLE
            rvPendingRequestsList.visibility = View.VISIBLE
            rvPendingRequestsList.layoutManager = LinearLayoutManager(this)
            mPendingRequestsAdapter = PendingRequestsAdapter(this, mPendingMembersList)
            rvPendingRequestsList.adapter = mPendingRequestsAdapter

            mPendingRequestsAdapter.setOnClickListener(object : PendingRequestsAdapter.OnClickListener {
                override fun onApproveClick(user: User) {
                    showCustomProgressBar()
                    FirestoreClass().updateMemberStatus(this@MembersActivity, mBoardDetails, user, "Member", mCurrentUserName)
                }

                override fun onRejectClick(user: User) {
                    showCustomProgressBar()
                    FirestoreClass().updateMemberStatus(this@MembersActivity, mBoardDetails, user, "Rejected", mCurrentUserName)
                }
            })
        } else {
            tvPendingRequestsTitle.visibility = View.GONE
            rvPendingRequestsList.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val addMemberMenuItem = menu?.findItem(R.id.action_add_member)
        addMemberMenuItem?.isVisible = mBoardDetails.assignedTo[FirestoreClass().getCurrentUserID()] == "Manager"
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)

        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {
            val email = dialog.findViewById<TextView>(R.id.et_email_search_member).text.toString()

            if(email.isNotEmpty()){
                dialog.dismiss()
                showCustomProgressBar()
                FirestoreClass().getMembersDetails(this,email)
            }else{
                Toast.makeText(this,"Please Enter the email",Toast.LENGTH_SHORT).show()
            }
        }

        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun membersDetail(user: User){
        mBoardDetails.assignedTo[user.id!!] = "Member"
        FirestoreClass().assignMemberToBoard(this,mBoardDetails,user)
    }

    fun memberAssignedSuccess(user: User){
        hideCustomProgressDialog()

        anyChangesMade = true

        mAssignedMembers.add(user)
        setUpMembersList(mAssignedMembers)
    }

    fun memberApprovedSuccess(user: User) {
        hideCustomProgressDialog()
        anyChangesMade = true
        showToast("Member approved successfully!")

        mPendingMembersList.remove(user)
        mPendingRequestsAdapter.notifyDataSetChanged()

        mAssignedMembers.add(user)
        findViewById<RecyclerView>(R.id.rv_members_list).adapter?.notifyDataSetChanged()

        if (mPendingMembersList.isEmpty()) {
            tvPendingRequestsTitle.visibility = View.GONE
            rvPendingRequestsList.visibility = View.GONE
        }
    }

    fun memberRejectedSuccess(user: User) {
        hideCustomProgressDialog()
        anyChangesMade = true
        showToast("Member rejected successfully!")

        mPendingMembersList.remove(user)
        mPendingRequestsAdapter.notifyDataSetChanged()

        if (mPendingMembersList.isEmpty()) {
            tvPendingRequestsTitle.visibility = View.GONE
            rvPendingRequestsList.visibility = View.GONE
        }
    }

    fun sendNotificationToUser(token: String, boardName: String, managerName: String, isApproved: Boolean) {
        val title: String
        val message: String

        if (isApproved) {
            title = "Your request to join ${boardName} has been approved!"
            message = "Your request to join the board ${boardName} has been approved by ${managerName}. You are now a member."
        } else {
            title = "Your request to join ${boardName} has been rejected."
            message = "Your request to join the board ${boardName} has been rejected by ${managerName}."
        }

        val data = NotificationData(title, message)
        val pushNotification = PushNotification(data, token)

        val service = ApiClient.getClient(FCMConstants.BASE_URL).create(ApiInterface::class.java)

        service.sendNotification(pushNotification).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.e("FCM Response", "Notification sent successfully to user.")
                } else {
                    Log.e("FCM Error", "Error sending notification: ${response.code()}")
                    Toast.makeText(this@MembersActivity, "Failed to send notification to user.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("FCM Error", "Network error sending notification", t)
                Toast.makeText(this@MembersActivity, "Network error sending notification.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun onGetCurrentUserSuccess(user: User) {
        mCurrentUserName = user.name ?: ""
    }

    override fun onBackPressed() {
        if(anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }
}
