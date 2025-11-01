package com.example.trelloclonemaster3.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.model.JoinableProject

class JoinableProjectAdapter(
    private val context: Context,
    private var list: ArrayList<JoinableProject>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_joinable_project,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.itemView.findViewById(R.id.iv_project_image))

            holder.itemView.findViewById<TextView>(R.id.tv_project_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.tv_created_by).text = "Created by: ${model.createdBy}"

            val btnRequestToJoin = holder.itemView.findViewById<Button>(R.id.btn_request_to_join)
            val tvProjectStatus = holder.itemView.findViewById<TextView>(R.id.tv_project_status)

            when (model.status) {
                "NotJoined" -> {
                    btnRequestToJoin.visibility = View.VISIBLE
                    tvProjectStatus.visibility = View.GONE
                    btnRequestToJoin.setOnClickListener { 
                        if (onClickListener != null) {
                            onClickListener!!.onRequestToJoinClick(model)
                        }
                    }
                }
                "Pending" -> {
                    btnRequestToJoin.visibility = View.GONE
                    tvProjectStatus.visibility = View.VISIBLE
                    tvProjectStatus.text = "Pending"
                }
                "Joined" -> {
                    btnRequestToJoin.visibility = View.GONE
                    tvProjectStatus.visibility = View.VISIBLE
                    tvProjectStatus.text = "Joined"
                }
                else -> { // Default case, could be an error or unhandled status
                    btnRequestToJoin.visibility = View.GONE
                    tvProjectStatus.visibility = View.GONE
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
        fun onRequestToJoinClick(project: JoinableProject)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}