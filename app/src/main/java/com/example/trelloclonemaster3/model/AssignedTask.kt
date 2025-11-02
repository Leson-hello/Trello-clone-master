package com.example.trelloclonemaster3.model

import android.os.Parcel
import android.os.Parcelable

data class AssignedTask(
    val taskId: String = "",
    val taskName: String = "",
    val projectName: String = "",
    val projectId: String = "",
    val taskListName: String = "",
    val status: TaskStatus = TaskStatus.PENDING,
    val dueDate: Long = 0,
    val labelColor: String = "",
    val assignedMembers: ArrayList<String> = ArrayList(),
    val createdBy: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        TaskStatus.valueOf(parcel.readString()!!),
        parcel.readLong(),
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(taskId)
        writeString(taskName)
        writeString(projectName)
        writeString(projectId)
        writeString(taskListName)
        writeString(status.name)
        writeLong(dueDate)
        writeString(labelColor)
        writeStringList(assignedMembers)
        writeString(createdBy)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AssignedTask> {
        override fun createFromParcel(parcel: Parcel): AssignedTask {
            return AssignedTask(parcel)
        }

        override fun newArray(size: Int): Array<AssignedTask?> {
            return arrayOfNulls(size)
        }
    }
}