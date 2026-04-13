package com.benimgunlerim.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["plannedDate", "completionState"]),
        Index(value = ["plannedDate", "isArchived"]),
    ],
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val note: String?,
    val plannedDate: String,
    val startTime: String?,
    val endTime: String?,
    val category: String?,
    val color: String?,
    val completionState: String,
    val completedAt: Long?,
    val sourceTemplateId: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val priority: Int = 2,
    val reminderTime: String? = null,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false,
    val postponedFromDate: String? = null,
)
