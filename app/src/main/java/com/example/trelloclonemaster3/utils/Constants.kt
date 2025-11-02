package com.example.trelloclonemaster3.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.util.Log
import com.example.trelloclonemaster3.model.Board

object Constants {

    const val USERS : String = "Users"
    const val BOARDS: String = "Boards"

    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val EMAIL: String = "email"
    const val ASSIGNED_TO: String = "assignedTo"
    const val DOCUMENT_ID: String = "documentId"
    const val TASK_LIST: String = "taskList"
    const val BOARD_DETAILS: String = "boardDetails"
    const val ID = "id"

    const val TASK_LIST_ITEM_POSITION = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION = "card_list_item_position"

    const val BOARDS_MEMBERS_LIST = "boards_members_list"
    const val SELECT: String = "select"
    const val UN_SELECT: String = "unSelect"

    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2

    const val PENDING: String = "Pending"


    fun showImagePicker(activity: Activity){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity ,uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }


    // Debug helper functions
    const val DEBUG_TAG = "TrelloCloneDebug"

    fun logFirestoreQuery(collection: String, query: String) {
        Log.d(DEBUG_TAG, "Firestore Query - Collection: $collection, Query: $query")
    }

    fun logFirestoreResult(collection: String, resultCount: Int) {
        Log.d(DEBUG_TAG, "Firestore Result - Collection: $collection, Results: $resultCount")
    }

    fun logFirestoreError(collection: String, error: String) {
        Log.e(DEBUG_TAG, "Firestore Error - Collection: $collection, Error: $error")
    }

    fun debugBoardInfo(board: Board, prefix: String = "") {
        Log.d(DEBUG_TAG, "$prefix Board Debug Info:")
        Log.d(DEBUG_TAG, "  - Name: ${board.name}")
        Log.d(DEBUG_TAG, "  - IsPublic: ${board.isPublic}")
        Log.d(DEBUG_TAG, "  - CreatedBy: ${board.createdBy}")
        Log.d(DEBUG_TAG, "  - DocumentId: ${board.documentId}")
        Log.d(DEBUG_TAG, "  - AssignedTo: ${board.assignedTo}")
    }

}