package com.example.trelloclonemaster3.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.activities.TaskListActivity
import com.example.trelloclonemaster3.model.Card
import com.example.trelloclonemaster3.model.SelectedMembers
import com.example.trelloclonemaster3.model.TaskStatus

open class CardListItemAdapter(private val context: Context, private var list: ArrayList<Card>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.item_card,
                        parent,
                        false
                )
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.findViewById<TextView>(R.id.tv_card_name).text = model.name

            // Set status display
            val tvCardStatus = holder.itemView.findViewById<TextView>(R.id.tv_card_status)
            tvCardStatus.text = model.status.displayName

            // Set background based on status
            when (model.status) {
                TaskStatus.PENDING -> tvCardStatus.setBackgroundResource(R.drawable.status_background_pending)
                TaskStatus.IN_PROGRESS -> tvCardStatus.setBackgroundResource(R.drawable.status_background_in_progress)
                TaskStatus.COMPLETED -> tvCardStatus.setBackgroundResource(R.drawable.status_background_completed)
            }
        }

        if(model.labelColor.isNotEmpty()){
            holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.VISIBLE
            holder.itemView.findViewById<View>(R.id.view_label_color).setBackgroundColor(Color.parseColor(model.labelColor))
        }else{
            holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.GONE
        }

        if((context as TaskListActivity).mMembersDetailList.size > 0 ){
            val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

            for (i in context.mMembersDetailList.indices){
                for (j in model.assignedTo){
                    if(context.mMembersDetailList[i].id == j){
                        val selectedMember = SelectedMembers(
                            context.mMembersDetailList[i].id!!,
                            context.mMembersDetailList[i].image!!,
                        )
                        selectedMembersList.add(selectedMember)
                    }
                }
            }

            if (selectedMembersList.size > 0){
                if(selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                    holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).visibility = View.GONE
                }else{
                    val rvCardMembers = holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list)
                    rvCardMembers.visibility = View.VISIBLE

                    rvCardMembers.layoutManager = GridLayoutManager(context,4)

                    val adapter = CardsMembersListAdapter(context,selectedMembersList,true)
                    rvCardMembers.adapter = adapter
                    adapter.setOnClickListener(object : CardsMembersListAdapter.OnClickListener{
                        override fun onClick() {
                            if(onClickListener != null){
                                // Đã sửa: Sử dụng bindingAdapterPosition cho vị trí ổn định
                                onClickListener!!.onClick(holder.bindingAdapterPosition)
                            }
                        }
                    })
                }
            }else {
                holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            if(onClickListener != null){
                // Đã sửa: Sử dụng bindingAdapterPosition cho vị trí ổn định
                onClickListener!!.onClick(holder.bindingAdapterPosition)
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
        fun onClick(position: Int)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}