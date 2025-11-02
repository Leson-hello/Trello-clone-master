package com.example.trelloclonemaster3.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.adapters.MyTasksAdapter
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.model.AssignedTask
import com.example.trelloclonemaster3.model.TaskStatus
import com.example.trelloclonemaster3.utils.Constants
import com.google.android.material.tabs.TabLayout

class MyTasksActivity : BaseActivity() {

    private var mAllTasks: ArrayList<AssignedTask> = ArrayList()
    private var mFilteredTasks: ArrayList<AssignedTask> = ArrayList()
    private lateinit var mAdapter: MyTasksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tasks)

        setupActionBar()
        setupRecyclerView()
        setupTabLayout()

        // Load user's tasks
        showCustomProgressBar()
        FirestoreClass().getAssignedTasks(this)
    }

    private fun setupActionBar() {
        val toolbar =
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_my_tasks_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "My Tasks"
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        val rvMyTasks = findViewById<RecyclerView>(R.id.rv_my_tasks)
        rvMyTasks.layoutManager = LinearLayoutManager(this)
        rvMyTasks.setHasFixedSize(true)

        mAdapter = MyTasksAdapter(this, mFilteredTasks)
        rvMyTasks.adapter = mAdapter

        mAdapter.setOnClickListener(object : MyTasksAdapter.OnClickListener {
            override fun onClick(task: AssignedTask) {
                openTaskDetails(task)
            }
        })
    }

    private fun setupTabLayout() {
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout_status_filter)

        tabLayout.addTab(tabLayout.newTab().setText("All"))
        tabLayout.addTab(tabLayout.newTab().setText("Pending"))
        tabLayout.addTab(tabLayout.newTab().setText("In Progress"))
        tabLayout.addTab(tabLayout.newTab().setText("Completed"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    filterTasksByStatus(it.position)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filterTasksByStatus(tabPosition: Int) {
        mFilteredTasks.clear()

        when (tabPosition) {
            0 -> mFilteredTasks.addAll(mAllTasks) // All tasks
            1 -> mFilteredTasks.addAll(mAllTasks.filter { it.status == TaskStatus.PENDING })
            2 -> mFilteredTasks.addAll(mAllTasks.filter { it.status == TaskStatus.IN_PROGRESS })
            3 -> mFilteredTasks.addAll(mAllTasks.filter { it.status == TaskStatus.COMPLETED })
        }

        mAdapter.notifyDataSetChanged()
        updateEmptyView()
    }

    private fun openTaskDetails(task: AssignedTask) {
        // Navigate to TaskListActivity with the specific project and card
        val intent = Intent(this, TaskListActivity::class.java)
        intent.putExtra(Constants.DOCUMENT_ID, task.projectId)
        intent.putExtra("HIGHLIGHT_TASK", task.taskId)
        startActivity(intent)
    }

    fun populateTasksList(tasksList: ArrayList<AssignedTask>) {
        hideCustomProgressDialog()

        mAllTasks = tasksList
        mFilteredTasks.clear()
        mFilteredTasks.addAll(mAllTasks)

        mAdapter.notifyDataSetChanged()
        updateEmptyView()
    }

    private fun updateEmptyView() {
        val tvNoTasks = findViewById<TextView>(R.id.tv_no_tasks)
        val rvMyTasks = findViewById<RecyclerView>(R.id.rv_my_tasks)

        if (mFilteredTasks.isEmpty()) {
            tvNoTasks.visibility = View.VISIBLE
            rvMyTasks.visibility = View.GONE
        } else {
            tvNoTasks.visibility = View.GONE
            rvMyTasks.visibility = View.VISIBLE
        }
    }

    fun onTasksLoadFailed() {
        hideCustomProgressDialog()
        showErrorSnackBar("Failed to load your tasks. Please try again.")
        updateEmptyView()
    }
}