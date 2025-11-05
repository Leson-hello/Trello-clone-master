package com.example.trelloclonemaster3.model

import android.os.Parcel
import android.os.Parcelable

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderImage: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: String = "text", // text, image, file
    val isRead: Boolean = false,
    val readBy: HashMap<String, Long> = HashMap() // userId -> timestamp when read
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "text",
        parcel.readByte() != 0.toByte(),
        parcel.readHashMap(String::class.java.classLoader) as HashMap<String, Long>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(senderId)
        parcel.writeString(senderName)
        parcel.writeString(senderImage)
        parcel.writeString(message)
        parcel.writeLong(timestamp)
        parcel.writeString(messageType)
        parcel.writeByte(if (isRead) 1 else 0)
        parcel.writeMap(readBy)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatMessage> {
        override fun createFromParcel(parcel: Parcel): ChatMessage {
            return ChatMessage(parcel)
        }

        override fun newArray(size: Int): Array<ChatMessage?> {
            return arrayOfNulls(size)
        }
    }
}