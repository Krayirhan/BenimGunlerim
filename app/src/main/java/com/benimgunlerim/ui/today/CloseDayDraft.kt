package com.benimgunlerim.ui.today

data class CloseDayDraft(
    val mood: Int,
    val energy: Int,
    val note: String = "",
    val bestMoment: String = "",
    val challenge: String = "",
    val tomorrowIntention: String = "",
    val carryOverdueTasks: Boolean = false,
)
