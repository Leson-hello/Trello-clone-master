package com.example.trelloclonemaster3.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.trelloclonemaster3.database.NotificationDao
import com.example.trelloclonemaster3.database.TrelloDatabase
import com.example.trelloclonemaster3.model.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationRepository(context: Context) {

    private val notificationDao: NotificationDao
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        val database = TrelloDatabase.getDatabase(context)
        notificationDao = database.notificationDao()
    }

    fun getAllNotifications(userId: String): LiveData<List<Notification>> {
        return notificationDao.getAllNotifications(userId)
    }

    fun getUnreadNotifications(userId: String): LiveData<List<Notification>> {
        return notificationDao.getUnreadNotifications(userId)
    }

    fun getUnreadNotificationCount(userId: String): LiveData<Int> {
        return notificationDao.getUnreadNotificationCount(userId)
    }

    fun insertNotification(notification: Notification) {
        coroutineScope.launch {
            notificationDao.insertNotification(notification)
        }
    }

    fun markAsRead(notificationId: String) {
        coroutineScope.launch {
            notificationDao.markAsRead(notificationId)
        }
    }

    fun markAllAsRead(userId: String) {
        coroutineScope.launch {
            notificationDao.markAllAsRead(userId)
        }
    }

    fun deleteNotification(notification: Notification) {
        coroutineScope.launch {
            notificationDao.deleteNotification(notification)
        }
    }

    fun deleteAllNotifications(userId: String) {
        coroutineScope.launch {
            notificationDao.deleteAllNotifications(userId)
        }
    }

    fun deleteOldNotifications(daysOld: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        coroutineScope.launch {
            notificationDao.deleteOldNotifications(cutoffTime)
        }
    }
}