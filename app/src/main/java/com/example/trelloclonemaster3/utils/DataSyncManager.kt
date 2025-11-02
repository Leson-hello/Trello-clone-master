package com.example.trelloclonemaster3.utils

import android.content.Context
import android.util.Log
import com.example.trelloclonemaster3.repository.BoardRepository
import kotlinx.coroutines.*

class DataSyncManager(private val context: Context) {

    private val boardRepository = BoardRepository(context)
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Sync data with retry mechanism
    suspend fun syncPublicBoardsWithRetry(maxRetries: Int = 3): SyncResult {
        return withContext(Dispatchers.IO) {
            var attempt = 0
            var lastException: Exception? = null

            while (attempt < maxRetries) {
                try {
                    Log.d(Constants.DEBUG_TAG, "DataSync: Attempt ${attempt + 1} of $maxRetries")

                    val boards = boardRepository.syncPublicBoardsFromFirestore()

                    return@withContext SyncResult.Success(
                        boards.size,
                        "Successfully synced ${boards.size} boards"
                    )

                } catch (e: Exception) {
                    lastException = e
                    attempt++

                    Log.w(Constants.DEBUG_TAG, "DataSync: Attempt $attempt failed", e)

                    if (attempt < maxRetries) {
                        // Wait before retry (exponential backoff)
                        val delayMs = (1000 * attempt * attempt).toLong()
                        Log.d(Constants.DEBUG_TAG, "DataSync: Waiting ${delayMs}ms before retry")
                        delay(delayMs)
                    }
                }
            }

            // All attempts failed
            Log.e(Constants.DEBUG_TAG, "DataSync: All attempts failed", lastException)

            // Try to get local count as fallback
            val localCount = try {
                boardRepository.getPublicBoardsCount()
            } catch (e: Exception) {
                0
            }

            SyncResult.Failed(
                lastException?.message ?: "Unknown error",
                localCount,
                "Using $localCount local boards"
            )
        }
    }

    // Initialize database with sample data if empty
    suspend fun initializeDatabaseIfEmpty(userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val count = boardRepository.getPublicBoardsCount()

                if (count == 0) {
                    Log.d(Constants.DEBUG_TAG, "DataSync: Database empty, creating sample data")
                    boardRepository.insertSamplePublicBoards(userId)

                    val newCount = boardRepository.getPublicBoardsCount()
                    Log.d(Constants.DEBUG_TAG, "DataSync: Created $newCount sample boards")

                    true // Data was initialized
                } else {
                    Log.d(Constants.DEBUG_TAG, "DataSync: Database already has $count boards")
                    false // No initialization needed
                }
            } catch (e: Exception) {
                Log.e(Constants.DEBUG_TAG, "DataSync: Error initializing database", e)
                false
            }
        }
    }

    // Periodic sync (can be called from background service)
    fun startPeriodicSync(intervalMinutes: Long = 15) {
        syncScope.launch {
            while (true) {
                try {
                    Log.d(Constants.DEBUG_TAG, "DataSync: Starting periodic sync")
                    syncPublicBoardsWithRetry()
                } catch (e: Exception) {
                    Log.e(Constants.DEBUG_TAG, "DataSync: Periodic sync error", e)
                }

                delay(intervalMinutes * 60 * 1000) // Convert minutes to milliseconds
            }
        }
    }

    // Stop all sync operations
    fun stopSync() {
        syncScope.cancel()
        boardRepository.cleanup()
    }

    // Get sync status information
    suspend fun getSyncStatus(): SyncStatus {
        return withContext(Dispatchers.IO) {
            try {
                val localCount = boardRepository.getPublicBoardsCount()

                SyncStatus(
                    localBoardsCount = localCount,
                    lastSyncTime = System.currentTimeMillis(), // Simplified - you might want to store this
                    isOnline = true, // Simplified - you might want to check network status
                    error = null
                )
            } catch (e: Exception) {
                SyncStatus(
                    localBoardsCount = 0,
                    lastSyncTime = 0,
                    isOnline = false,
                    error = e.message
                )
            }
        }
    }
}

// Data classes for sync results
sealed class SyncResult {
    data class Success(val boardsCount: Int, val message: String) : SyncResult()
    data class Failed(val error: String, val localBoardsCount: Int, val fallbackMessage: String) :
        SyncResult()
}

data class SyncStatus(
    val localBoardsCount: Int,
    val lastSyncTime: Long,
    val isOnline: Boolean,
    val error: String?
)