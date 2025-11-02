package com.example.trelloclonemaster3.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.model.Board
import com.example.trelloclonemaster3.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView

class JoinableProjectsAdapter(
    private val context: Context,
    private var list: ArrayList<Board>,
    private val currentUserId: String
) : RecyclerView.Adapter<JoinableProjectsAdapter.ViewHolder>() {

    private var onRequestJoinListener: OnRequestJoinListener? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val projectImage: CircleImageView = view.findViewById(R.id.iv_project_image)
        val projectName: TextView = view.findViewById(R.id.tv_project_name)
        val createdBy: TextView = view.findViewById(R.id.tv_created_by)
        val requestButton: Button = view.findViewById(R.id.btn_request_to_join)
        val statusText: TextView = view.findViewById(R.id.tv_project_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_joinable_project,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = list[position]

        // Load project image
        Glide.with(context)
            .load(project.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(holder.projectImage)

        // Set project details
        holder.projectName.text = project.name
        holder.createdBy.text = "Created by: ${project.createdBy}"

        // Determine user status for this project
        val userStatus = project.assignedTo[currentUserId]
        when (userStatus) {
            "Member" -> {
                // User is already a member
                holder.requestButton.visibility = View.GONE
                holder.statusText.visibility = View.VISIBLE
                holder.statusText.text = context.getString(R.string.already_member)
            }

            Constants.PENDING -> {
                // User has a pending request
                holder.requestButton.visibility = View.GONE
                holder.statusText.visibility = View.VISIBLE
                holder.statusText.text = context.getString(R.string.request_sent)
            }

            else -> {
                // User can request to join
                holder.requestButton.visibility = View.VISIBLE
                holder.statusText.visibility = View.GONE
                holder.requestButton.text = context.getString(R.string.request_to_join)

                holder.requestButton.setOnClickListener {
                    onRequestJoinListener?.onRequestJoin(position, project)
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size

    interface OnRequestJoinListener {
        fun onRequestJoin(position: Int, board: Board)
    }

    fun setOnRequestJoinListener(listener: OnRequestJoinListener) {
        this.onRequestJoinListener = listener
    }

    fun updateProjectStatus(position: Int, status: String) {
        if (position in 0 until list.size) {
            list[position].assignedTo[currentUserId] = status
            notifyItemChanged(position)
        }
    }

    fun updateList(newList: ArrayList<Board>) {
        list = newList
        notifyDataSetChanged()
    }
}