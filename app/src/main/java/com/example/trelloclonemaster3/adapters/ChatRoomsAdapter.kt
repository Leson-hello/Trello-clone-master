package com.example.trelloclonemaster3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.activities.ChatRoomsActivity
import com.example.trelloclonemaster3.model.ChatRoom
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatRoomsAdapter(
    private val context: ChatRoomsActivity,
    private val list: ArrayList<ChatRoom>
) : RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_chat_room, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        holder.tvChatRoomName.text = model.name
        holder.tvLastMessage.text = if (model.lastMessage.isNotEmpty()) {
            model.lastMessage
        } else {
            "No messages yet"
        }

        // Format timestamp
        if (model.lastMessageTime > 0) {
            holder.tvLastMessageTime.text = formatTime(model.lastMessageTime)
        } else {
            holder.tvLastMessageTime.text = ""
        }

        // Show unread count
        val currentUserId =
            com.example.trelloclonemaster3.firebase.FirestoreClass().getCurrentUserID()
        val unreadCount = model.unreadCount[currentUserId] ?: 0
        if (unreadCount > 0) {
            holder.tvUnreadCount.visibility = View.VISIBLE
            holder.tvUnreadCount.text = if (unreadCount > 99) "99+" else unreadCount.toString()
        } else {
            holder.tvUnreadCount.visibility = View.GONE
        }

        // Show chat type indicator
        if (model.type == "direct") {
            holder.ivChatTypeIndicator.setImageResource(R.drawable.ic_person_24dp)
        } else {
            holder.ivChatTypeIndicator.setImageResource(R.drawable.ic_group_24dp)
        }

        holder.itemView.setOnClickListener {
            context.onChatRoomClick(model)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun formatTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60 * 1000 -> "Just now" // Less than 1 minute
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago" // Less than 1 hour
            diff < 24 * 60 * 60 * 1000 -> { // Less than 1 day
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                formatter.format(Date(timestamp))
            }

            diff < 7 * 24 * 60 * 60 * 1000 -> { // Less than 1 week
                val formatter = SimpleDateFormat("EEE", Locale.getDefault())
                formatter.format(Date(timestamp))
            }

            else -> { // More than 1 week
                val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                formatter.format(Date(timestamp))
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvChatRoomName: TextView = view.findViewById(R.id.tv_chat_room_name)
        val tvLastMessage: TextView = view.findViewById(R.id.tv_last_message)
        val tvLastMessageTime: TextView = view.findViewById(R.id.tv_last_message_time)
        val tvUnreadCount: TextView = view.findViewById(R.id.tv_unread_count)
        val ivChatTypeIndicator: CircleImageView = view.findViewById(R.id.iv_chat_type_indicator)
    }
}