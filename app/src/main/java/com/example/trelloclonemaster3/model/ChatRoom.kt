package com.example.trelloclonemaster3.model

import android.os.Parcel
import android.os.Parcelable

data class ChatRoom(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val type: String = "group", // "group" for team chat, "direct" for 1-on-1
    val participants: HashMap<String, String> = HashMap(), // userId -> userRole
    val boardId: String = "", // Associated board/project ID
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val lastMessageSender: String = "",
    val unreadCount: HashMap<String, Int> = HashMap(), // userId -> unread count
    val isActive: Boolean = true
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "group",
        parcel.readHashMap(String::class.java.classLoader) as HashMap<String, String>,
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readHashMap(String::class.java.classLoader) as HashMap<String, Int>,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(type)
        parcel.writeMap(participants)
        parcel.writeString(boardId)
        parcel.writeString(createdBy)
        parcel.writeLong(createdAt)
        parcel.writeString(lastMessage)
        parcel.writeLong(lastMessageTime)
        parcel.writeString(lastMessageSender)
        parcel.writeMap(unreadCount)
        parcel.writeByte(if (isActive) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatRoom> {
        override fun createFromParcel(parcel: Parcel): ChatRoom {
            return ChatRoom(parcel)
        }

        override fun newArray(size: Int): Array<ChatRoom?> {
            return arrayOfNulls(size)
        }
    }
}