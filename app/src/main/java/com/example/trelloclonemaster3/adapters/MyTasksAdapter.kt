package com.example.trelloclonemaster3.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.model.AssignedTask
import com.example.trelloclonemaster3.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

class MyTasksAdapter(
    private val context: Context,
    private var list: ArrayList<AssignedTask>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_my_task,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            // Set task name
            holder.itemView.findViewById<TextView>(R.id.tv_task_name).text = model.taskName

            // Set project name
            holder.itemView.findViewById<TextView>(R.id.tv_project_name).text = model.projectName

            // Set task list name
            holder.itemView.findViewById<TextView>(R.id.tv_task_list_name).text = model.taskListName

            // Set status with appropriate background
            val tvStatus = holder.itemView.findViewById<TextView>(R.id.tv_task_status)
            tvStatus.text = model.status.displayName
            when (model.status) {
                TaskStatus.PENDING -> tvStatus.setBackgroundResource(R.drawable.status_background_pending)
                TaskStatus.IN_PROGRESS -> tvStatus.setBackgroundResource(R.drawable.status_background_in_progress)
                TaskStatus.COMPLETED -> tvStatus.setBackgroundResource(R.drawable.status_background_completed)
            }

            // Set due date if available
            val tvDueDate = holder.itemView.findViewById<TextView>(R.id.tv_due_date)
            if (model.dueDate > 0) {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = Date(model.dueDate)
                tvDueDate.text = sdf.format(date)
                tvDueDate.visibility = View.VISIBLE

                // Highlight overdue tasks
                val currentTime = System.currentTimeMillis()
                if (model.dueDate < currentTime && model.status != TaskStatus.COMPLETED) {
                    tvDueDate.setTextColor(Color.RED)
                } else {
                    tvDueDate.setTextColor(context.resources.getColor(R.color.secondary_text_color))
                }
            } else {
                tvDueDate.visibility = View.GONE
            }

            // Set label color if available
            val viewLabelColor = holder.itemView.findViewById<View>(R.id.view_label_color)
            if (model.labelColor.isNotEmpty()) {
                viewLabelColor.visibility = View.VISIBLE
                viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            } else {
                viewLabelColor.visibility = View.GONE
            }

            // Set click listener
            holder.itemView.setOnClickListener {
                onClickListener?.onClick(model)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(task: AssignedTask)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}