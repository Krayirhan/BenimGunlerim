package com.benimgunlerim.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routines",
    indices = [
        Index(value = ["isArchived", "preferredTime"]),
    ],
)
data class RoutineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val targetDays: String,
    val preferredTime: String?,
    val color: String?,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val targetType: String = "check",
    val targetValue: Int? = null,
    val targetUnit: String? = null,
    val category: String? = null,
    val reminderEnabled: Boolean = false,
    val sortOrder: Int = 0,
    val bestStreak: Int = 0,
)
