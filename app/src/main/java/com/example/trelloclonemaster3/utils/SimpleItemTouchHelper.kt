package com.example.trelloclonemaster3.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.activities.TaskListActivity
import com.example.trelloclonemaster3.adapters.CardListItemAdapter

class SimpleItemTouchHelper(
    private val activity: TaskListActivity,
    private val taskListPosition: Int
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    0
) {

    private var draggedFromPosition = -1
    private var draggedToPosition = -1

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition

        if (draggedFromPosition == -1) {
            draggedFromPosition = fromPosition
        }
        draggedToPosition = toPosition

        // Kéo trong cùng cột
        if (fromPosition != RecyclerView.NO_POSITION && toPosition != RecyclerView.NO_POSITION) {
            val cards = activity.mBoardDetails.taskList[taskListPosition].cards

            if (fromPosition < cards.size && toPosition < cards.size) {
                java.util.Collections.swap(cards, fromPosition, toPosition)
                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
                return true
            }
        }
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Không dùng swipe
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder?.itemView?.apply {
                alpha = 0.7f
                scaleX = 1.1f
                scaleY = 1.1f
                elevation = 8f
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        // Reset visual
        viewHolder.itemView.apply {
            alpha = 1.0f
            scaleX = 1.0f
            scaleY = 1.0f
            elevation = 0f
        }

        // Cập nhật Firestore nếu có thay đổi
        if (draggedFromPosition != -1 && draggedToPosition != -1 &&
            draggedFromPosition != draggedToPosition
        ) {

            activity.updateCardsInTaskList(
                taskListPosition,
                activity.mBoardDetails.taskList[taskListPosition].cards
            )
        }

        // Reset
        draggedFromPosition = -1
        draggedToPosition = -1
    }

    override fun isLongPressDragEnabled(): Boolean = true
    override fun isItemViewSwipeEnabled(): Boolean = false
}