package com.example.trelloclonemaster3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.activities.ChatActivity
import com.example.trelloclonemaster3.model.ChatMessage
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatMessagesAdapter(
    private val context: ChatActivity,
    private val list: ArrayList<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view =
                LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = list[position]

        when (holder) {
            is SentMessageViewHolder -> {
                holder.tvMessage.text = message.message
                holder.tvTimestamp.text = formatTime(message.timestamp)
            }

            is ReceivedMessageViewHolder -> {
                holder.tvMessage.text = message.message
                holder.tvTimestamp.text = formatTime(message.timestamp)
                holder.tvSenderName.text = message.senderName

                // Load sender profile image
                if (message.senderImage.isNotEmpty()) {
                    Glide.with(context)
                        .load(message.senderImage)
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(holder.ivSenderImage)
                } else {
                    holder.ivSenderImage.setImageResource(R.drawable.ic_user_place_holder)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun formatTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    class SentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tv_message_sent)
        val tvTimestamp: TextView = view.findViewById(R.id.tv_timestamp_sent)
    }

    class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tv_message_received)
        val tvTimestamp: TextView = view.findViewById(R.id.tv_timestamp_received)
        val tvSenderName: TextView = view.findViewById(R.id.tv_sender_name)
        val ivSenderImage: CircleImageView = view.findViewById(R.id.iv_sender_image)
    }
}