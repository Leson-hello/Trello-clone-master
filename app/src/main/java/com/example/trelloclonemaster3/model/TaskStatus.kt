package com.example.trelloclonemaster3.model

enum class TaskStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    companion object {
        fun fromDisplayName(displayName: String): TaskStatus {
            return values().find { it.displayName == displayName } ?: PENDING
        }
    }
}