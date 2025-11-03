package com.example.trelloclonemaster3.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.model.Notification
import com.example.trelloclonemaster3.utils.FCMConstants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NotificationAdapter(
    private val context: Context,
    private var notifications: ArrayList<Notification>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<Notification>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, notification: Notification)
        fun onLongClick(position: Int, notification: Notification)
    }

    inner class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val llNotificationItem: LinearLayout = view.findViewById(R.id.ll_notification_item)
        private val ivNotificationIcon: ImageView = view.findViewById(R.id.iv_notification_icon)
        private val tvNotificationTitle: TextView = view.findViewById(R.id.tv_notification_title)
        private val tvNotificationBody: TextView = view.findViewById(R.id.tv_notification_body)
        private val tvNotificationProject: TextView =
            view.findViewById(R.id.tv_notification_project)
        private val tvNotificationTime: TextView = view.findViewById(R.id.tv_notification_time)
        private val viewUnreadIndicator: View = view.findViewById(R.id.view_unread_indicator)

        fun bind(notification: Notification) {
            // Set notification content
            tvNotificationTitle.text = notification.title
            tvNotificationBody.text = notification.body
            tvNotificationProject.text = notification.projectName.ifEmpty { "General" }
            tvNotificationTime.text = formatTimeAgo(notification.timestamp)

            // Set icon based on notification type
            setNotificationIcon(notification.type)

            // Show/hide unread indicator and update text style
            if (notification.isRead) {
                viewUnreadIndicator.visibility = View.GONE
                tvNotificationTitle.typeface = Typeface.DEFAULT
                tvNotificationBody.alpha = 0.7f
            } else {
                viewUnreadIndicator.visibility = View.VISIBLE
                tvNotificationTitle.typeface = Typeface.DEFAULT_BOLD
                tvNotificationBody.alpha = 1.0f
            }

            // Set click listeners
            llNotificationItem.setOnClickListener {
                onClickListener?.onClick(adapterPosition, notification)
            }

            llNotificationItem.setOnLongClickListener {
                onClickListener?.onLongClick(adapterPosition, notification)
                true
            }
        }

        private fun setNotificationIcon(type: String) {
            val iconRes = when (type) {
                FCMConstants.NOTIFICATION_TYPE_JOIN_REQUEST -> R.drawable.ic_nav_user
                FCMConstants.NOTIFICATION_TYPE_JOIN_ACCEPTED -> android.R.drawable.ic_dialog_info
                FCMConstants.NOTIFICATION_TYPE_JOIN_REJECTED -> android.R.drawable.ic_dialog_alert
                FCMConstants.NOTIFICATION_TYPE_TASK_ASSIGNED -> android.R.drawable.ic_menu_agenda
                FCMConstants.NOTIFICATION_TYPE_TASK_DUE -> android.R.drawable.ic_lock_idle_alarm
                FCMConstants.NOTIFICATION_TYPE_TASK_COMPLETED -> android.R.drawable.ic_menu_info_details
                else -> android.R.drawable.ic_dialog_info
            }

            ivNotificationIcon.setImageResource(iconRes)

            // Set tint color based on type
            val tintColor = when (type) {
                FCMConstants.NOTIFICATION_TYPE_JOIN_REQUEST -> R.color.colorPrimary
                FCMConstants.NOTIFICATION_TYPE_JOIN_ACCEPTED -> android.R.color.holo_green_dark
                FCMConstants.NOTIFICATION_TYPE_JOIN_REJECTED -> android.R.color.holo_red_dark
                FCMConstants.NOTIFICATION_TYPE_TASK_ASSIGNED -> R.color.colorPrimary
                FCMConstants.NOTIFICATION_TYPE_TASK_DUE -> android.R.color.holo_orange_dark
                FCMConstants.NOTIFICATION_TYPE_TASK_COMPLETED -> android.R.color.holo_green_dark
                else -> R.color.colorPrimary
            }

            ivNotificationIcon.setColorFilter(ContextCompat.getColor(context, tintColor))
        }

        private fun formatTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60 * 1000 -> "Just now"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
                diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
                else -> {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dateFormat.format(Date(timestamp))
                }
            }
        }
    }
}