package com.example.trelloclonemaster3.utils

import android.content.Context
import android.view.MenuItem
import androidx.core.view.forEach
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.repository.NotificationRepository
import com.google.android.material.navigation.NavigationView

class NotificationBadgeHelper(
    private val context: Context,
    private val navigationView: NavigationView,
    private val lifecycleOwner: LifecycleOwner
) {

    private lateinit var notificationRepository: NotificationRepository

    fun setupNotificationBadge(userId: String) {
        if (userId.isNotEmpty()) {
            notificationRepository = NotificationRepository(context)

            // Observe unread notification count
            notificationRepository.getUnreadNotificationCount(userId).observe(
                lifecycleOwner,
                Observer { count ->
                    updateNotificationBadge(count)
                }
            )
        }
    }

    private fun updateNotificationBadge(count: Int) {
        val menu = navigationView.menu
        val notificationItem = menu.findItem(R.id.nav_notifications)

        if (count > 0) {
            // You can customize this to show actual badge/number
            // For now, we'll update the title to show count
            notificationItem?.title = "Notifications ($count)"
        } else {
            notificationItem?.title = "Notifications"
        }
    }
}