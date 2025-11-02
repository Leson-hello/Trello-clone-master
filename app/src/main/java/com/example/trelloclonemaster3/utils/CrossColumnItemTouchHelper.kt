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
    private var totalDragDistanceX = 0f
    private val DRAG_THRESHOLD = 150f

    init {
        Log.d(
            "CrossColumnDrag",
            "=== INIT CrossColumnItemTouchHelper for column $taskListPosition ==="
        )
        Log.d("CrossColumnDrag", "Total columns: ${activity.mBoardDetails.taskList.size}")
        activity.mBoardDetails.taskList.forEachIndexed { index, task ->
            if (index < activity.mBoardDetails.taskList.size - 1) { // Exclude "Add List"
                Log.d(
                    "CrossColumnDrag",
                    "Column $index: '${task.title}' (${task.cards.size} cards)"
                )
            }
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition

        // Chỉ set draggedFromPosition một lần khi bắt đầu drag
        if (draggedFromPosition == -1 && fromPosition != RecyclerView.NO_POSITION) {
            draggedFromPosition = fromPosition
            Log.d(
                "CrossColumnDrag",
                "onMove: FIXED - Initial drag from position $fromPosition in column $taskListPosition"
            )
        }

        // QUAN TRỌNG: Không xử lý swap nếu đang trong inter-column drag
        if (!isInterColumnDrag && fromPosition != RecyclerView.NO_POSITION && toPosition != RecyclerView.NO_POSITION) {
            val cards = activity.mBoardDetails.taskList[taskListPosition].cards

            if (fromPosition < cards.size && toPosition < cards.size) {
                Log.d(
                    "CrossColumnDrag",
                    "onMove: Swapping within column $taskListPosition: $fromPosition <-> $toPosition"
                )
                java.util.Collections.swap(cards, fromPosition, toPosition)
                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
                return true
            }
        } else if (isInterColumnDrag) {
            Log.d(
                "CrossColumnDrag",
                "onMove: FIXED - Skipping swap during inter-column drag, draggedFromPosition preserved: $draggedFromPosition"
            )
            return false // Không cho phép swap trong cùng cột khi đang inter-column drag
        }

        Log.d(
            "CrossColumnDrag",
            "onMove: No swap needed (isInterColumnDrag: $isInterColumnDrag, draggedFromPosition: $draggedFromPosition)"
        )
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Không sử dụng
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> {
                Log.d("CrossColumnDrag", "=== DRAG STARTED ===")
                Log.d(
                    "CrossColumnDrag",
                    "Column: $taskListPosition, Position: ${viewHolder?.adapterPosition}"
                )
                Log.d(
                    "CrossColumnDrag",
                    "Available target columns: ${activity.mBoardDetails.taskList.size - 2}"
                )

                viewHolder?.itemView?.apply {
                    alpha = 0.8f
                    scaleX = 1.1f
                    scaleY = 1.1f
                    elevation = 16f
                }

                // Reset states
                isInterColumnDrag = false
                targetColumn = -1
                totalDragDistanceX = 0f
            }
            ItemTouchHelper.ACTION_STATE_IDLE -> {
                Log.d("CrossColumnDrag", "=== DRAG ENDED ===")
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
            totalDragDistanceX += Math.abs(dX)

            // Log mỗi 50 pixel để không spam quá nhiều
            if (Math.abs(dX).toInt() % 20 == 0 && dX != 0f) {
                Log.d(
                    "CrossColumnDrag",
                    "onChildDraw: dX=$dX, dY=$dY, totalDistance=$totalDragDistanceX, threshold=$DRAG_THRESHOLD"
                )
            }

            // Detect inter-column drag
            if (Math.abs(dX) > DRAG_THRESHOLD) {
                if (!isInterColumnDrag) {
                    isInterColumnDrag = true
                    Log.d("CrossColumnDrag", "*** INTER-COLUMN DRAG DETECTED ***")
                    Log.d("CrossColumnDrag", "dX: $dX, threshold: $DRAG_THRESHOLD")
                }

                // Determine target column based on drag direction
                val newTargetColumn = if (dX > 0) {
                    // Dragging right
                    taskListPosition + 1
                } else {
                    // Dragging left
                    taskListPosition - 1
                }

                // Validate target column
                val maxColumn = activity.mBoardDetails.taskList.size - 2 // Exclude "Add List"
                val validTargetColumn = when {
                    newTargetColumn < 0 -> 0
                    newTargetColumn > maxColumn -> maxColumn
                    else -> newTargetColumn
                }

                if (validTargetColumn != targetColumn) {
                    targetColumn = validTargetColumn
                    Log.d("CrossColumnDrag", "Target column changed to: $targetColumn")
                    Log.d(
                        "CrossColumnDrag",
                        "Target column name: '${activity.mBoardDetails.taskList[targetColumn].title}'"
                    )
                    Log.d("CrossColumnDrag", "Direction: ${if (dX > 0) "RIGHT" else "LEFT"}")
                }
            } else {
                if (isInterColumnDrag) {
                    Log.d("CrossColumnDrag", "Back to intra-column drag (dX: $dX)")
                }
                isInterColumnDrag = false
                targetColumn = -1
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        Log.d("CrossColumnDrag", "=== CLEAR VIEW ===")
        Log.d("CrossColumnDrag", "BEFORE CHECKS:")
        Log.d("CrossColumnDrag", "  isInterColumnDrag: $isInterColumnDrag")
        Log.d("CrossColumnDrag", "  targetColumn: $targetColumn")
        Log.d("CrossColumnDrag", "  taskListPosition: $taskListPosition")
        Log.d("CrossColumnDrag", "  draggedFromPosition: $draggedFromPosition")
        Log.d("CrossColumnDrag", "  viewHolder.adapterPosition: ${viewHolder.adapterPosition}")

        // Reset visual state
        viewHolder.itemView.apply {
            alpha = 1.0f
            scaleX = 1.0f
            scaleY = 1.0f
            elevation = 0f
        }

        // FIXED: Use viewHolder.adapterPosition as fallback if draggedFromPosition is -1
        val actualDraggedPosition = if (draggedFromPosition != -1) {
            draggedFromPosition
        } else {
            viewHolder.adapterPosition
        }

        Log.d("CrossColumnDrag", "USING actualDraggedPosition: $actualDraggedPosition")

        // Handle cross-column move
        if (isInterColumnDrag && targetColumn != -1 && targetColumn != taskListPosition && actualDraggedPosition != -1) {
            Log.d("CrossColumnDrag", "*** EXECUTING CROSS-COLUMN MOVE ***")
            Log.d("CrossColumnDrag", "From column $taskListPosition to column $targetColumn")
            Log.d("CrossColumnDrag", "Card position: $actualDraggedPosition")

            // Validate positions one more time
            if (targetColumn >= 0 && targetColumn < activity.mBoardDetails.taskList.size - 1 &&
                actualDraggedPosition >= 0 && actualDraggedPosition < activity.mBoardDetails.taskList[taskListPosition].cards.size
            ) {

                Log.d(
                    "CrossColumnDrag",
                    "FIXED - Positions validated. Calling moveCardBetweenColumns..."
                )

                activity.moveCardBetweenColumns(
                    fromColumn = taskListPosition,
                    toColumn = targetColumn,
                    cardPosition = actualDraggedPosition,
                    targetPosition = 0 // Add to beginning of target column
                )
            } else {
                Log.e("CrossColumnDrag", "FIXED - Invalid positions for cross-column move!")
                Log.e(
                    "CrossColumnDrag",
                    "targetColumn: $targetColumn (max: ${activity.mBoardDetails.taskList.size - 2})"
                )
                Log.e(
                    "CrossColumnDrag",
                    "actualDraggedPosition: $actualDraggedPosition (max: ${if (taskListPosition < activity.mBoardDetails.taskList.size) activity.mBoardDetails.taskList[taskListPosition].cards.size - 1 else "N/A"})"
                )
            }
        } else if (actualDraggedPosition != -1) {
            Log.d("CrossColumnDrag", "FIXED - Updating cards within same column $taskListPosition")
            activity.updateCardsInTaskList(
                taskListPosition,
                activity.mBoardDetails.taskList[taskListPosition].cards
            )
        } else {
            Log.d("CrossColumnDrag", "No action needed - actualDraggedPosition is -1")
        }

        // Reset all variables
        Log.d("CrossColumnDrag", "RESETTING all variables...")
        draggedFromPosition = -1
        isInterColumnDrag = false
        targetColumn = -1
        totalDragDistanceX = 0f

        Log.d("CrossColumnDrag", "=== CLEAR VIEW COMPLETE ===")
    }

    override fun isLongPressDragEnabled(): Boolean = true
    override fun isItemViewSwipeEnabled(): Boolean = false

    // Make it easier to trigger drag
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.2f
    override fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.2f
}