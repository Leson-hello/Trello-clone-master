package com.example.trelloclonemaster3.utils

import android.graphics.Canvas
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.activities.TaskListActivity
import com.example.trelloclonemaster3.adapters.CardListItemAdapter

class CrossColumnItemTouchHelper(
    private val activity: TaskListActivity,
    private val taskListPosition: Int
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    0
) {

    private var draggedFromPosition = -1
    private var isInterColumnDrag = false
    private var targetColumn = -1

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

        // Chỉ xử lý kéo trong cùng cột
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
        // Không sử dụng
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> {
                Log.d("DragDrop", "Started dragging from column $taskListPosition")
                viewHolder?.itemView?.apply {
                    alpha = 0.8f
                    scaleX = 1.1f
                    scaleY = 1.1f
                    elevation = 16f
                }
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && isCurrentlyActive) {
            // Detect nếu đang kéo ra ngoài cột hiện tại
            val dragThreshold = 200f // pixels

            if (Math.abs(dX) > dragThreshold) {
                if (!isInterColumnDrag) {
                    isInterColumnDrag = true
                    Log.d("DragDrop", "Inter-column drag detected, dX: $dX")
                }

                // Xác định cột đích đơn giản
                if (dX > 0) {
                    // Kéo sang phải
                    targetColumn = taskListPosition + 1
                } else {
                    // Kéo sang trái  
                    targetColumn = taskListPosition - 1
                }

                // Đảm bảo target column hợp lệ
                val maxColumn = activity.mBoardDetails.taskList.size - 2 // Trừ "Add List"
                if (targetColumn < 0) targetColumn = 0
                if (targetColumn > maxColumn) targetColumn = maxColumn

                Log.d("DragDrop", "Target column: $targetColumn (max: $maxColumn)")
            } else {
                isInterColumnDrag = false
                targetColumn = -1
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        // Reset visual state
        viewHolder.itemView.apply {
            alpha = 1.0f
            scaleX = 1.0f
            scaleY = 1.0f
            elevation = 0f
        }

        Log.d(
            "DragDrop",
            "clearView - isInterColumnDrag: $isInterColumnDrag, targetColumn: $targetColumn, taskListPosition: $taskListPosition"
        )

        // Xử lý di chuyển giữa các cột
        if (isInterColumnDrag && targetColumn != -1 && targetColumn != taskListPosition && draggedFromPosition != -1) {
            Log.d("DragDrop", "Moving card from column $taskListPosition to $targetColumn")

            activity.moveCardBetweenColumns(
                fromColumn = taskListPosition,
                toColumn = targetColumn,
                cardPosition = draggedFromPosition,
                targetPosition = 0 // Thêm vào đầu cột đích
            )
        } else if (draggedFromPosition != -1) {
            // Cập nhật thứ tự trong cùng cột
            Log.d("DragDrop", "Updating cards within same column $taskListPosition")
            activity.updateCardsInTaskList(
                taskListPosition,
                activity.mBoardDetails.taskList[taskListPosition].cards
            )
        }

        // Reset variables
        draggedFromPosition = -1
        isInterColumnDrag = false
        targetColumn = -1
    }

    override fun isLongPressDragEnabled(): Boolean = true
    override fun isItemViewSwipeEnabled(): Boolean = false

    // Tăng threshold để dễ kéo hơn
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f
    override fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f
}