package com.benimgunlerim.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completion_logs",
    indices = [
        Index(value = ["entityType", "entityId", "date"], unique = true),
        Index(value = ["date"]),
        Index(value = ["entityId"]),
    ],
)
data class CompletionLogEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val date: String,
    val completedAt: Long?,
    val status: String,
    val note: String?,
    val value: Float? = null,
    val targetValue: Float? = null,
    val skipReason: String? = null,
)
