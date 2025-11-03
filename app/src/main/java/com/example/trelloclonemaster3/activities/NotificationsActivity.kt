package com.example.trelloclonemaster3.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.MainActivity
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.adapters.NotificationAdapter
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.model.Notification
import com.example.trelloclonemaster3.repository.NotificationRepository
import com.example.trelloclonemaster3.utils.FCMConstants

class NotificationsActivity : BaseActivity() {

    private lateinit var notificationRepository: NotificationRepository
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var rvNotificationsList: RecyclerView
    private lateinit var llNoNotifications: LinearLayout
    private lateinit var tvUnreadCount: TextView
    private lateinit var tvMarkAllRead: TextView

    private var currentUserId: String = ""
    private var notifications = ArrayList<Notification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        setupActionBar()
        setupViews()
        setupRecyclerView()

        currentUserId = FirestoreClass().getCurrentUserID()

        if (currentUserId.isNotEmpty()) {
            notificationRepository = NotificationRepository(this)
            observeNotifications()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_notifications, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear_all -> {
                showClearAllConfirmationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar() {
        val toolbar =
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_notifications_activity)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)

        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupViews() {
        rvNotificationsList = findViewById(R.id.rv_notifications_list)
        llNoNotifications = findViewById(R.id.ll_no_notifications)
        tvUnreadCount = findViewById(R.id.tv_unread_count)
        tvMarkAllRead = findViewById(R.id.tv_mark_all_read)

        tvMarkAllRead.setOnClickListener {
            markAllAsRead()
        }
    }

    private fun setupRecyclerView() {
        rvNotificationsList.layoutManager = LinearLayoutManager(this)
        notificationAdapter = NotificationAdapter(this, notifications)
        rvNotificationsList.adapter = notificationAdapter

        notificationAdapter.setOnClickListener(object : NotificationAdapter.OnClickListener {
            override fun onClick(position: Int, notification: Notification) {
                handleNotificationClick(notification)
            }

            override fun onLongClick(position: Int, notification: Notification) {
                showNotificationOptionsDialog(notification)
            }
        })
    }

    private fun observeNotifications() {
        // Observe all notifications
        notificationRepository.getAllNotifications(currentUserId)
            .observe(this, Observer { notificationList ->
                notifications.clear()
                notifications.addAll(notificationList)
                notificationAdapter.updateNotifications(notificationList)

                if (notificationList.isEmpty()) {
                    showNoNotificationsView()
                } else {
                    showNotificationsView()
                }
            })

        // Observe unread count
        notificationRepository.getUnreadNotificationCount(currentUserId)
            .observe(this, Observer { count ->
                updateUnreadCount(count)
            })
    }

    private fun updateUnreadCount(count: Int) {
        tvUnreadCount.text = when (count) {
            0 -> "All notifications read"
            1 -> "1 unread notification"
            else -> "$count unread notifications"
        }

        tvMarkAllRead.visibility = if (count > 0) View.VISIBLE else View.GONE
    }

    private fun showNotificationsView() {
        rvNotificationsList.visibility = View.VISIBLE
        llNoNotifications.visibility = View.GONE
    }

    private fun showNoNotificationsView() {
        rvNotificationsList.visibility = View.GONE
        llNoNotifications.visibility = View.VISIBLE
    }

    private fun handleNotificationClick(notification: Notification) {
        // Mark as read if unread
        if (!notification.isRead) {
            notificationRepository.markAsRead(notification.id)
        }

        // Handle different notification types
        when (notification.type) {
            FCMConstants.NOTIFICATION_TYPE_JOIN_REQUEST,
            FCMConstants.NOTIFICATION_TYPE_JOIN_ACCEPTED,
            FCMConstants.NOTIFICATION_TYPE_JOIN_REJECTED -> {
                // Navigate to project or main activity
                if (notification.projectId.isNotEmpty()) {
                    navigateToProject(notification.projectId)
                } else {
                    navigateToMain()
                }
            }

            FCMConstants.NOTIFICATION_TYPE_TASK_ASSIGNED,
            FCMConstants.NOTIFICATION_TYPE_TASK_DUE,
            FCMConstants.NOTIFICATION_TYPE_TASK_COMPLETED -> {
                // Navigate to task or project
                if (notification.projectId.isNotEmpty()) {
                    navigateToProject(notification.projectId)
                } else {
                    navigateToMain()
                }
            }

            else -> {
                // General notification - navigate to main
                navigateToMain()
            }
        }
    }

    private fun navigateToProject(projectId: String) {
        val intent = Intent(this, TaskListActivity::class.java)
        intent.putExtra("DOCUMENT_ID", projectId)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun markAllAsRead() {
        notificationRepository.markAllAsRead(currentUserId)
    }

    private fun showNotificationOptionsDialog(notification: Notification) {
        val options = arrayOf("Mark as read", "Delete notification")

        AlertDialog.Builder(this)
            .setTitle("Notification Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> notificationRepository.markAsRead(notification.id)
                    1 -> notificationRepository.deleteNotification(notification)
                }
            }
            .show()
    }

    private fun showClearAllConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Notifications")
            .setMessage("Are you sure you want to delete all notifications? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                notificationRepository.deleteAllNotifications(currentUserId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}