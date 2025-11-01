package com.example.trelloclonemaster3.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.adapters.JoinableProjectAdapter
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.model.JoinableProject
import android.view.View
import android.widget.TextView
import com.example.trelloclonemaster3.utils.Constants
import com.example.trelloclonemaster3.model.NotificationData
import com.example.trelloclonemaster3.model.PushNotification
import com.example.trelloclonemaster3.network.ApiClient
import com.example.trelloclonemaster3.network.ApiInterface
import com.example.trelloclonemaster3.utils.FCMConstants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody
import android.util.Log
import android.widget.Toast
import com.example.trelloclonemaster3.model.User

class JoinProjectActivity : BaseActivity() {

    private lateinit var etSearchProject: EditText
    private lateinit var rvProjectsList: RecyclerView
    private lateinit var tvNoProjectsFound: TextView
    private lateinit var mAdapter: JoinableProjectAdapter
    private var mJoinableProjectsList: ArrayList<JoinableProject> = ArrayList()
    private var mFilteredProjectsList: ArrayList<JoinableProject> = ArrayList()
    private lateinit var mCurrentUserName: String // Added to store current user's name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_project)

        etSearchProject = findViewById(R.id.et_search_project)
        rvProjectsList = findViewById(R.id.rv_projects_list)
        tvNoProjectsFound = findViewById(R.id.tv_no_projects_found)

        setupActionBar()
        showCustomProgressBar()
        FirestoreClass().getPublicBoardList(this)
        FirestoreClass().getCurrentUserDetails(this) // Fetch current user details

        etSearchProject.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProjects(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed
            }
        })
    }

    private fun setupActionBar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_join_project_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "Join Project"
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    fun populatePublicProjectsList(projectsList: ArrayList<JoinableProject>) {
        hideCustomProgressDialog()

        mJoinableProjectsList = projectsList
        mFilteredProjectsList.clear()
        mFilteredProjectsList.addAll(mJoinableProjectsList)

        setupJoinableProjectsRecyclerView()
    }

    private fun setupJoinableProjectsRecyclerView() {
        if (mFilteredProjectsList.size > 0) {
            rvProjectsList.visibility = View.VISIBLE
            tvNoProjectsFound.visibility = View.GONE
            rvProjectsList.layoutManager = LinearLayoutManager(this)
            mAdapter = JoinableProjectAdapter(this, mFilteredProjectsList)
            rvProjectsList.adapter = mAdapter

            mAdapter.setOnClickListener(object : JoinableProjectAdapter.OnClickListener {
                override fun onRequestToJoinClick(project: JoinableProject) {
                    showCustomProgressBar()
                    FirestoreClass().sendJoinRequest(
                        this@JoinProjectActivity,
                        project.documentId,
                        FirestoreClass().getCurrentUserID(),
                        project.createdBy,
                        mCurrentUserName, // Use the fetched current user's name here
                        project.name
                    )
                }
            })
        } else {
            rvProjectsList.visibility = View.GONE
            tvNoProjectsFound.visibility = View.VISIBLE
        }
    }

    private fun filterProjects(query: String) {
        mFilteredProjectsList.clear()
        if (query.isEmpty()) {
            mFilteredProjectsList.addAll(mJoinableProjectsList)
        } else {
            val lowerCaseQuery = query.toLowerCase()
            for (project in mJoinableProjectsList) {
                if (project.name.toLowerCase().contains(lowerCaseQuery)) {
                    mFilteredProjectsList.add(project)
                }
            }
        }
        if (::mAdapter.isInitialized) {
            mAdapter.notifyDataSetChanged()
        }

        if (mFilteredProjectsList.size > 0) {
            rvProjectsList.visibility = View.VISIBLE
            tvNoProjectsFound.visibility = View.GONE
        } else {
            rvProjectsList.visibility = View.GONE
            tvNoProjectsFound.visibility = View.VISIBLE
        }
    }

    fun joinRequestSentSuccess(boardDocumentId: String, currentUserId: String) {
        hideCustomProgressDialog()
        showToast("Join request sent successfully!")

        val indexInJoinable = mJoinableProjectsList.indexOfFirst { it.documentId == boardDocumentId }
        if (indexInJoinable != -1) {
            mJoinableProjectsList[indexInJoinable].status = Constants.PENDING
        }

        val indexInFiltered = mFilteredProjectsList.indexOfFirst { it.documentId == boardDocumentId }
        if (indexInFiltered != -1) {
            mFilteredProjectsList[indexInFiltered].status = Constants.PENDING
        }

        if (::mAdapter.isInitialized) {
            mAdapter.notifyDataSetChanged()
        }
    }

    fun sendNotificationToManager(token: String, requestingUserName: String, boardName: String) {
        val title = "New Join Request for ${boardName}"
        val message = "${requestingUserName} has requested to join your board ${boardName}."

        val data = NotificationData(title, message)
        val pushNotification = PushNotification(data, token)

        val service = ApiClient.getClient(FCMConstants.BASE_URL).create(ApiInterface::class.java)

        service.sendNotification(pushNotification).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.e("FCM Response", "Notification sent successfully to manager.")
                } else {
                    Log.e("FCM Error", "Error sending notification: ${response.code()}")
                    Toast.makeText(this@JoinProjectActivity, "Failed to send notification to manager.", Toast.LENGTH_SHORT).show()
                }
                hideCustomProgressDialog()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("FCM Error", "Network error sending notification", t)
                Toast.makeText(this@JoinProjectActivity, "Network error sending notification.", Toast.LENGTH_SHORT).show()
                hideCustomProgressDialog()
            }
        })
    }

    fun onGetCurrentUserSuccess(user: User) {
        mCurrentUserName = user.name ?: ""
    }
}
