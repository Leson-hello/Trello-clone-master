package com.example.trelloclonemaster3.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val userId: String = "",
    val projectId: String = "",
    val projectName: String = "",
    val senderName: String = "",
    val data: String = "" // JSON string for additional data
)