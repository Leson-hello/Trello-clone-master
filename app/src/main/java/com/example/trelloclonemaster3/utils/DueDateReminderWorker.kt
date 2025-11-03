package com.example.trelloclonemaster3.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.model.Board
import com.example.trelloclonemaster3.model.TaskStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

class DueDateReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "DueDateReminderWorker"
        const val WORK_NAME = "due_date_reminder_work"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting due date reminder check")
            checkDueDatesAndSendReminders()
            Log.d(TAG, "Due date reminder check completed successfully")
            Result.success()
        } catch (exception: Exception) {
            Log.e(TAG, "Error checking due dates", exception)
            Result.failure()
        }
    }

    private suspend fun checkDueDatesAndSendReminders() {
        val firestore = FirebaseFirestore.getInstance()
        val currentUserId = FirestoreClass().getCurrentUserID()

        if (currentUserId.isEmpty()) {
            Log.w(TAG, "No current user ID found")
            return
        }

        try {
            // Get all boards where current user is assigned
            val querySnapshot = firestore.collection(Constants.BOARDS)
                .whereGreaterThan("assignedTo.$currentUserId", "")
                .get()
                .await()

            val currentTime = System.currentTimeMillis()
            val oneDayInMillis = 24 * 60 * 60 * 1000L
            val reminderThreshold = currentTime + oneDayInMillis // 24 hours ahead

            for (document in querySnapshot.documents) {
                try {
                    val board = document.toObject(Board::class.java)!!
                    board.documentId = document.id

                    // Check all tasks in this board
                    for (taskList in board.taskList) {
                        for (card in taskList.cards) {
                            // Only check tasks that are not completed and have due dates
                            if (card.status != TaskStatus.COMPLETED && card.dueDate > 0) {
                                val isAssignedToCurrentUser =
                                    card.assignedTo.contains(currentUserId)
                                val isDueSoon =
                                    card.dueDate <= reminderThreshold && card.dueDate > currentTime
                                val isOverdue = card.dueDate < currentTime

                                if (isAssignedToCurrentUser && (isDueSoon || isOverdue)) {
                                    Log.d(TAG, "Sending due date reminder for task: ${card.name}")

                                    FirestoreClass().sendDueDateReminderNotification(
                                        applicationContext,
                                        listOf(currentUserId),
                                        card.name,
                                        board.name!!,
                                        card.dueDate
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing board: ${document.id}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying boards", e)
            throw e
        }
    }
}