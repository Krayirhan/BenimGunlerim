package com.benimgunlerim.ui.progress

data class ProgressDayUi(
    val date: String,
    val dailyScore: Int,
    val completionRate: Float,
    val note: String?,
)
