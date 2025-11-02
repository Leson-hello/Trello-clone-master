package com.example.trelloclonemaster3.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {

    // Get all boards
    @Query("SELECT * FROM boards ORDER BY lastUpdated DESC")
    fun getAllBoards(): Flow<List<BoardEntity>>

    // Get all public boards
    @Query("SELECT * FROM boards WHERE isPublic = 1 ORDER BY lastUpdated DESC")
    fun getPublicBoards(): Flow<List<BoardEntity>>

    // Get boards by user
    @Query("SELECT * FROM boards WHERE createdBy = :userId ORDER BY lastUpdated DESC")
    fun getBoardsByUser(userId: String): Flow<List<BoardEntity>>

    // Get board by ID
    @Query("SELECT * FROM boards WHERE id = :boardId")
    suspend fun getBoardById(boardId: String): BoardEntity?

    // Insert or update board
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: BoardEntity)

    // Insert multiple boards
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoards(boards: List<BoardEntity>)

    // Update board
    @Update
    suspend fun updateBoard(board: BoardEntity)

    // Delete board
    @Delete
    suspend fun deleteBoard(board: BoardEntity)

    // Delete board by ID
    @Query("DELETE FROM boards WHERE id = :boardId")
    suspend fun deleteBoardById(boardId: String)

    // Clear all boards
    @Query("DELETE FROM boards")
    suspend fun clearAllBoards()

    // Get count of public boards
    @Query("SELECT COUNT(*) FROM boards WHERE isPublic = 1")
    suspend fun getPublicBoardsCount(): Int

    // Search boards by name
    @Query("SELECT * FROM boards WHERE name LIKE '%' || :searchQuery || '%' ORDER BY lastUpdated DESC")
    fun searchBoards(searchQuery: String): Flow<List<BoardEntity>>

    // Search public boards by name
    @Query("SELECT * FROM boards WHERE isPublic = 1 AND name LIKE '%' || :searchQuery || '%' ORDER BY lastUpdated DESC")
    fun searchPublicBoards(searchQuery: String): Flow<List<BoardEntity>>
}