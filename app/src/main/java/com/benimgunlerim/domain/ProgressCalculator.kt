package com.benimgunlerim.domain

object ProgressCalculator {
    fun dailyProgress(
        totalTasks: Int,
        completedTasks: Int,
        totalRoutines: Int,
        completedRoutines: Int,
    ): Float {
        val total = totalTasks + totalRoutines
        if (total <= 0) return 0f
        val completed = (completedTasks + completedRoutines).coerceIn(0, total)
        return completed.toFloat() / total
    }
}
