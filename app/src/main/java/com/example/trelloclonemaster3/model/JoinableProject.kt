package com.example.trelloclonemaster3.model

import android.os.Parcel
import android.os.Parcelable

data class JoinableProject(
    val name: String = "",
    val image: String = "",
    val createdBy: String = "",
    val assignedUsers: ArrayList<String> = ArrayList(),
    var documentId: String = "",
    var status: String = "", // "NotJoined", "Pending", "Joined"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(createdBy)
        parcel.writeStringList(assignedUsers)
        parcel.writeString(documentId)
        parcel.writeString(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<JoinableProject> {
        override fun createFromParcel(parcel: Parcel): JoinableProject {
            return JoinableProject(parcel)
        }

        override fun newArray(size: Int): Array<JoinableProject?> {
            return arrayOfNulls(size)
        }
    }
}