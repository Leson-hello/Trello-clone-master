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
import com.example.trelloclonemaster3.model.User // Assuming User model for pending requests

class PendingRequestsAdapter(
    private val context: Context,
    private var list: ArrayList<User>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_pending_request,
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
                .into(holder.itemView.findViewById(R.id.iv_pending_member_image))

            holder.itemView.findViewById<TextView>(R.id.tv_pending_member_name).text = model.name

            holder.itemView.findViewById<Button>(R.id.btn_approve_request).setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onApproveClick(model)
                }
            }

            holder.itemView.findViewById<Button>(R.id.btn_reject_request).setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onRejectClick(model)
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
        fun onApproveClick(user: User)
        fun onRejectClick(user: User)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}