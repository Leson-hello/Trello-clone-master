package com.example.trelloclonemaster3.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.trelloclonemaster3.model.Board
import com.example.trelloclonemaster3.model.Tasks
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "boards")
@TypeConverters(Converters::class)
data class BoardEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val image: String,
    val createdBy: String,
    val assignedTo: String, // JSON string of HashMap
    val taskList: String,   // JSON string of ArrayList<Tasks>
    val isPublic: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    // Convert to Board model
    fun toBoard(): Board {
        val assignedToMap = Converters.fromStringToHashMap(assignedTo)
        val tasksList = Converters.fromStringToTasksList(taskList)

        return Board(
            name = name,
            image = image,
            createdBy = createdBy,
            assignedTo = assignedToMap,
            documentId = id,
            taskList = tasksList,
            isPublic = isPublic
        )
    }

    companion object {
        // Convert from Board model
        fun fromBoard(board: Board): BoardEntity {
            return BoardEntity(
                id = board.documentId ?: "",
                name = board.name ?: "",
                image = board.image ?: "",
                createdBy = board.createdBy ?: "",
                assignedTo = Converters.fromHashMapToString(board.assignedTo),
                taskList = Converters.fromTasksListToString(board.taskList),
                isPublic = board.isPublic
            )
        }
    }
}

// Type converters for complex data types
class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromHashMapToString(value: HashMap<String, String>): String {
            return Gson().toJson(value)
        }

        @TypeConverter
        @JvmStatic
        fun fromStringToHashMap(value: String): HashMap<String, String> {
            val mapType = object : TypeToken<HashMap<String, String>>() {}.type
            return Gson().fromJson(value, mapType) ?: HashMap()
        }

        @TypeConverter
        @JvmStatic
        fun fromTasksListToString(value: ArrayList<Tasks>): String {
            return Gson().toJson(value)
        }

        @TypeConverter
        @JvmStatic
        fun fromStringToTasksList(value: String): ArrayList<Tasks> {
            val listType = object : TypeToken<ArrayList<Tasks>>() {}.type
            return Gson().fromJson(value, listType) ?: ArrayList()
        }
    }
}