package com.example.trelloclonemaster3.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.model.Card
import com.example.trelloclonemaster3.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class CalendarTasksAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_calendar_task, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is ViewHolder) {
            val tvTaskName = holder.itemView.findViewById<TextView>(R.id.tv_calendar_task_name)
            val tvTaskDueTime =
                holder.itemView.findViewById<TextView>(R.id.tv_calendar_task_due_time)
            val tvTaskStatus = holder.itemView.findViewById<TextView>(R.id.tv_calendar_task_status)
            val tvTaskAssigned =
                holder.itemView.findViewById<TextView>(R.id.tv_calendar_task_assigned)

            // Set task name
            tvTaskName.text = model.name

            // Set due time
            if (model.dueDate > 0) {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                tvTaskDueTime.text = timeFormat.format(Date(model.dueDate))
                tvTaskDueTime.visibility = View.VISIBLE
            } else {
                tvTaskDueTime.visibility = View.GONE
            }

            // Set task status
            tvTaskStatus.text = model.status.name.replace("_", " ")
            when (model.status) {
                TaskStatus.PENDING -> {
                    tvTaskStatus.setBackgroundResource(R.drawable.status_background_pending)
                    tvTaskStatus.setTextColor(Color.WHITE)
                }

                TaskStatus.IN_PROGRESS -> {
                    tvTaskStatus.setBackgroundResource(R.drawable.status_background_in_progress)
                    tvTaskStatus.setTextColor(Color.WHITE)
                }

                TaskStatus.COMPLETED -> {
                    tvTaskStatus.setBackgroundResource(R.drawable.status_background_completed)
                    tvTaskStatus.setTextColor(Color.WHITE)
                }
            }

            // Set assigned members count
            if (model.assignedTo.isNotEmpty()) {
                tvTaskAssigned.text =
                    context.getString(R.string.assigned_to_count, model.assignedTo.size)
                tvTaskAssigned.visibility = View.VISIBLE
            } else {
                tvTaskAssigned.visibility = View.GONE
            }

            // Set task color indicator if label color exists
            if (model.labelColor.isNotEmpty()) {
                try {
                    val labelColor = Color.parseColor(model.labelColor)
                    val colorIndicator =
                        holder.itemView.findViewById<View>(R.id.view_calendar_task_color)
                    colorIndicator.setBackgroundColor(labelColor)
                    colorIndicator.visibility = View.VISIBLE
                } catch (e: Exception) {
                    holder.itemView.findViewById<View>(R.id.view_calendar_task_color).visibility =
                        View.GONE
                }
            } else {
                holder.itemView.findViewById<View>(R.id.view_calendar_task_color).visibility =
                    View.GONE
            }

            // Check if task is overdue
            if (model.dueDate > 0 && model.dueDate < System.currentTimeMillis() && model.status != TaskStatus.COMPLETED) {
                tvTaskName.setTextColor(Color.RED)
                tvTaskDueTime.setTextColor(Color.RED)
            } else {
                tvTaskName.setTextColor(context.resources.getColor(R.color.primary_text_color))
                tvTaskDueTime.setTextColor(context.resources.getColor(R.color.secondary_text_color))
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
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
        fun onClick(position: Int, model: Card)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}