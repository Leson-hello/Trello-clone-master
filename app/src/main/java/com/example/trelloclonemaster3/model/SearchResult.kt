package com.example.trelloclonemaster3.model

import android.os.Parcel
import android.os.Parcelable

enum class SearchResultType {
    PROJECT, TASK, USER
}

data class SearchResult(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val type: SearchResultType = SearchResultType.PROJECT,
    val imageUrl: String = "",
    val boardId: String = "", // For tasks, reference to parent board
    val taskListId: String = "", // For tasks, reference to parent task list
    val projectName: String = "" // For tasks, name of parent project
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        SearchResultType.valueOf(parcel.readString()!!),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(id)
        writeString(title)
        writeString(subtitle)
        writeString(type.name)
        writeString(imageUrl)
        writeString(boardId)
        writeString(taskListId)
        writeString(projectName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SearchResult> {
        override fun createFromParcel(parcel: Parcel): SearchResult {
            return SearchResult(parcel)
        }

        override fun newArray(size: Int): Array<SearchResult?> {
            return arrayOfNulls(size)
        }
    }
}