package com.example.trelloclonemaster3.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName

data class Board(
    val name: String? = "",
    val image: String? = "",
    val createdBy: String? = "",
    val assignedTo: HashMap<String, String> = HashMap(),
    var documentId: String? = "",
    var taskList: ArrayList<Tasks> = ArrayList(),

    @get:PropertyName("isPublic") // Add this annotation
    val isPublic: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readHashMap(String::class.java.classLoader) as HashMap<String, String>,
        parcel.readString(),
        parcel.createTypedArrayList(Tasks.CREATOR)!!,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(name)
        writeString(image)
        writeString(createdBy)
        writeMap(assignedTo)
        writeString(documentId)
        writeTypedList(taskList)
        writeByte(if (isPublic) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Board> {
        override fun createFromParcel(parcel: Parcel): Board {
            return Board(parcel)
        }

        override fun newArray(size: Int): Array<Board?> {
            return arrayOfNulls(size)
        }
    }
}
