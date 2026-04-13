package com.benimgunlerim.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_states")
data class DailyStateEntity(
    @PrimaryKey val date: String,
    val mood: String?,
    val energyLevel: Int?,
    val completionRate: Float,
    val note: String?,
    val reflection: String?,
    val dailyScore: Int,
    val bestMoment: String? = null,
    val challenge: String? = null,
    val tomorrowIntention: String? = null,
    val closedAt: Long? = null,
    val carriedTaskCount: Int = 0,
)
