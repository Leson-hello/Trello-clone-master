package com.example.trelloclonemaster3.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.activities.TaskListActivity
import com.example.trelloclonemaster3.model.Tasks
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNREACHABLE_CODE")
open class TaskListItemAdapter(private val context: Context, private var list: ArrayList<Tasks>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(
                        R.layout.item_task,
                        parent,
                        false)

        val layoutParams = LinearLayout.LayoutParams((parent.width * 0.7).toInt(),LinearLayout.LayoutParams.WRAP_CONTENT)

        layoutParams.setMargins((15.ToDp().ToPx()),0,(40.ToDp().ToPx()),0)

        view.layoutParams = layoutParams

        return MyViewHolder(view)
    }

    @SuppressLint("CutPasteId")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            if(position == list.size -1){
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.VISIBLE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility = View.GONE
            }else{
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility = View.VISIBLE
            }

            holder.itemView.findViewById<TextView>(R.id.tv_task_list_title).text = model.title
            holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility = View.VISIBLE
            }
            holder.itemView.findViewById<ImageButton>(R.id.ib_close_list_name).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility = View.GONE
            }
            holder.itemView.findViewById<ImageButton>(R.id.ib_done_list_name).setOnClickListener {
                val listName = holder.itemView.findViewById<TextView>(R.id.et_task_list_name).text.toString()

                if(listName.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.createTaskList(listName)
                    }
                }else{
                    Toast.makeText(context,"Please Enter Name For task listt",Toast.LENGTH_SHORT).show()
                }
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_edit_list_name).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.et_edit_task_list_name).text = model.title
                holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility = View.VISIBLE
            }
            holder.itemView.findViewById<ImageButton>(R.id.ib_close_editable_view).setOnClickListener {
                holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility = View.GONE
            }

            // 1. Cập nhật tên Task List
            holder.itemView.findViewById<ImageButton>(R.id.ib_done_edit_list_name).setOnClickListener {
                val listName = holder.itemView.findViewById<TextView>(R.id.et_edit_task_list_name).text.toString()
                if(listName.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.updateTaskList(holder.getAdapterPosition(), listName, model) // Đã sửa
                    }
                }else{
                    Toast.makeText(context,"Please Enter a List Name",Toast.LENGTH_SHORT).show()
                }
            }

            // 2. Xóa Task List
            holder.itemView.findViewById<ImageButton>(R.id.ib_delete_list).setOnClickListener {
                alertDialogForDeleteList(holder.getAdapterPosition(), model.title!!) // Đã sửa
            }

            holder.itemView.findViewById<TextView>(R.id.tv_add_card).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.VISIBLE
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_close_card_name).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility = View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.GONE
            }

            // 3. Thêm Card mới
            holder.itemView.findViewById<ImageButton>(R.id.ib_done_card_name).setOnClickListener {
                val cardName = holder.itemView.findViewById<TextView>(R.id.et_card_name).text.toString()

                if(cardName.isNotEmpty()){
                    if(context is TaskListActivity){
                        context.addCardToArrayList(holder.getAdapterPosition(), cardName) // Đã sửa
                    }
                }else{
                    Toast.makeText(context,"Please Enter a Card Name",Toast.LENGTH_SHORT).show()
                }
            }


            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).layoutManager = LinearLayoutManager(context)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).setHasFixedSize(true)

            val adapter = CardListItemAdapter(context,model.cards)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).adapter = adapter

            // 4. Xem chi tiết Card
            adapter.setOnClickListener(
                object : CardListItemAdapter.OnClickListener{
                    override fun onClick(cardPosition: Int) {
                        if(context is TaskListActivity){
                            context.cardDetails(holder.getAdapterPosition(), cardPosition) // Đã sửa
                        }
                    }
                }
            )

            val dividerItemDecoration = DividerItemDecoration(context,DividerItemDecoration.VERTICAL)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).addItemDecoration(dividerItemDecoration)

            val helper = ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN,0
                ){
                    override fun onMove(recyclerView: RecyclerView, dragged: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                        val draggedPosition = dragged.adapterPosition
                        val targetPosition = target.adapterPosition

                        if (mPositionDraggedFrom == -1) {
                            mPositionDraggedFrom = draggedPosition
                        }
                        mPositionDraggedTo = targetPosition

                        // 5. Thao tác Collections.swap: Cần dùng vị trí hiện tại
                        Collections.swap(list[holder.getAdapterPosition()].cards, draggedPosition, targetPosition) // Đã sửa

                        adapter.notifyItemMoved(draggedPosition, targetPosition)
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        TODO("Not yet implemented")
                    }

                    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                        super.clearView(recyclerView, viewHolder)
                        if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo) {

                            // 6. Cập nhật Cards sau khi kéo thả (Vị trí List)
                            (context as TaskListActivity).updateCardsInTaskList(
                                holder.getAdapterPosition(), // Đã sửa
                                // 7. Cập nhật Cards sau khi kéo thả (Vị trí List)
                                list[holder.getAdapterPosition()].cards // Đã sửa
                            )
                        }

                        // Reset the global variables
                        mPositionDraggedFrom = -1
                        mPositionDraggedTo = -1
                    }

                }
            )
            helper.attachToRecyclerView(holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun Int.ToDp(): Int = (this/ Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.ToPx(): Int = (this* Resources.getSystem().displayMetrics.density).toInt()

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Alert")
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }

        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }
}